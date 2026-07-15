import { prisma } from '../../db/prisma.js';

/**
 * The dashboard's progress numbers, computed from the Scan rows.
 *
 * All of it is derived from the set of distinct `localDate`s the user has scanned — there is no
 * denormalised streak counter to drift out of sync. The client's "today" is passed in so every
 * question ("scanned today?", "what is the current streak?") is answered against the user's own
 * calendar rather than the server's UTC clock.
 */
export interface ScanSummary {
  scannedToday: boolean;
  streakDays: number;
  scansCompleted: number;
  /** Trailing-window adherence as a whole percentage, or null until the first scan exists. */
  complianceScore: number | null;
  /** Most recent scanned day as "YYYY-MM-DD", or null if the user has never scanned. */
  lastScanDate: string | null;
}

const DAY_MS = 24 * 60 * 60 * 1000;
const COMPLIANCE_WINDOW_DAYS = 30;

const parseDay = (key: string): number => new Date(`${key}T00:00:00.000Z`).getTime();
const toKey = (ms: number): string => new Date(ms).toISOString().slice(0, 10);

/**
 * Records today's scan and returns the refreshed summary.
 *
 * Idempotent by construction: the unique (userId, localDate) means a second call on the same day is
 * an upsert no-op, so a double-tap or an offline retry cannot record two scans for one day or nudge
 * the streak. `capturedAt` keeps the real timestamp for later; `localDate` is what everything counts.
 */
export async function recordScan(userId: string, localDate: string): Promise<ScanSummary> {
  await prisma.scan.upsert({
    where: { userId_localDate: { userId, localDate } },
    create: { userId, localDate },
    update: {},
  });

  return getSummary(userId, localDate);
}

export async function getSummary(userId: string, today: string): Promise<ScanSummary> {
  const rows = await prisma.scan.findMany({
    where: { userId },
    select: { localDate: true },
    orderBy: { localDate: 'desc' },
  });

  const scannedDays = new Set(rows.map((r) => r.localDate));
  const scansCompleted = scannedDays.size;

  if (scansCompleted === 0) {
    return {
      scannedToday: false,
      streakDays: 0,
      scansCompleted: 0,
      complianceScore: null,
      lastScanDate: null,
    };
  }

  const todayMs = parseDay(today);
  const scannedToday = scannedDays.has(today);

  return {
    scannedToday,
    streakDays: currentStreak(scannedDays, todayMs, scannedToday),
    scansCompleted,
    complianceScore: adherence(scannedDays, todayMs),
    lastScanDate: rows[0]?.localDate ?? null,
  };
}

/** Recent scans, newest first — the raw material for Module 6's history. */
export async function listScans(userId: string, limit = 60) {
  const rows = await prisma.scan.findMany({
    where: { userId },
    orderBy: { capturedAt: 'desc' },
    take: limit,
    select: { id: true, localDate: true, capturedAt: true },
  });

  return rows.map((r) => ({
    id: r.id,
    localDate: r.localDate,
    capturedAt: r.capturedAt.toISOString(),
  }));
}

/**
 * Records a scan with its captured image.
 *
 * The image is stored in a separate row so the frequently-queried summary/streak logic never
 * loads multi-hundred-KB blobs. This is Module 4; Module 5 reads from ScanImage for AI analysis.
 * Returns the summary with the freshly-recorded scan.
 */
export async function recordScanWithImage(
  userId: string,
  localDate: string,
  imageData: Buffer,
  mimeType: string,
  width?: number,
  height?: number,
): Promise<ScanSummary> {
  const scan = await prisma.scan.upsert({
    where: { userId_localDate: { userId, localDate } },
    create: { userId, localDate },
    update: {},
  });

  // Upsert the image; a retry or re-upload on the same day replaces the old one.
  const imageBytes = new Uint8Array(imageData);
  await prisma.scanImage.upsert({
    where: { scanId: scan.id },
    create: {
      scanId: scan.id,
      data: imageBytes,
      mimeType,
      byteSize: imageData.length,
      width,
      height,
    },
    update: {
      data: imageBytes,
      mimeType,
      byteSize: imageData.length,
      width,
      height,
    },
  });

  return getSummary(userId, localDate);
}

/**
 * Retrieve a scan's image for serving (Module 5 AI analysis, Module 6 history thumbnails).
 */
export async function getImage(userId: string, scanId: string) {
  const image = await prisma.scanImage.findUnique({
    where: { scanId },
    select: { data: true, mimeType: true, byteSize: true },
  });

  if (!image) return null;

  // Verify the scan belongs to this user (prevent cross-user reads).
  const scan = await prisma.scan.findUnique({
    where: { id: scanId },
    select: { userId: true },
  });

  if (!scan || scan.userId !== userId) return null;

  return image;
}

// --- internals ---------------------------------------------------------------

/**
 * Consecutive days ending at today, or at yesterday when today has not been scanned yet.
 *
 * Anchoring at yesterday when today is still blank is deliberate: a five-day streak must not read as
 * zero at 9am simply because the user has not done today's scan — the day is not over. It only
 * breaks once a whole day passes with no scan.
 */
function currentStreak(scannedDays: Set<string>, todayMs: number, scannedToday: boolean): number {
  let cursor = scannedToday ? todayMs : todayMs - DAY_MS;
  // If neither today nor yesterday was scanned, the streak is already broken.
  if (!scannedDays.has(toKey(cursor))) return 0;

  let streak = 0;
  while (scannedDays.has(toKey(cursor))) {
    streak += 1;
    cursor -= DAY_MS;
  }
  return streak;
}

/**
 * Adherence over a window that grows with the account, capped at 30 days.
 *
 * A user on their first day who scanned reads 100%, not 3%: the window is the days they have been
 * enrolled, so the score answers "of the days you could have scanned, how many did you", not "how
 * many of an arbitrary 30". Once enrolled longer than the cap, it becomes a rolling 30-day figure.
 */
function adherence(scannedDays: Set<string>, todayMs: number): number {
  const firstMs = Math.min(...[...scannedDays].map(parseDay));
  const daysEnrolled = Math.floor((todayMs - firstMs) / DAY_MS) + 1;
  const window = Math.min(COMPLIANCE_WINDOW_DAYS, Math.max(1, daysEnrolled));

  let scannedInWindow = 0;
  for (let i = 0; i < window; i += 1) {
    if (scannedDays.has(toKey(todayMs - i * DAY_MS))) scannedInWindow += 1;
  }

  return Math.round((scannedInWindow / window) * 100);
}
