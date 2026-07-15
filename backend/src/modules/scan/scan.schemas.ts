import { z } from 'zod';

/**
 * A calendar day in the user's own timezone, as "YYYY-MM-DD".
 *
 * The client sends its local date rather than letting the server derive one from `now()`: a scan
 * taken at 11pm in Kolkata belongs to that day, not to tomorrow in UTC. The regex fixes the shape;
 * the refine rejects impossible dates ("2026-02-30") that the regex alone would wave through.
 */
const localDate = z
  .string()
  .regex(/^\d{4}-\d{2}-\d{2}$/, 'Expected a YYYY-MM-DD date')
  .refine((value) => {
    const parsed = new Date(`${value}T00:00:00.000Z`);
    return !Number.isNaN(parsed.getTime()) && parsed.toISOString().slice(0, 10) === value;
  }, 'Not a real calendar date');

export const submitScanSchema = z.object({
  localDate,
});

/** GET /scans/summary carries the same local date as a query param, for timezone-correct streaks. */
export const summaryQuerySchema = z.object({
  localDate,
});
