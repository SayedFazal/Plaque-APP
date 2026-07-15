import { AuthTokenType, type User } from '@prisma/client';
import { prisma } from '../../db/prisma.js';
import { sendPasswordResetEmail, sendVerificationEmail } from '../../mail/mailer.js';
import { AppError } from '../../utils/errors.js';
import {
  burnTimingBudget,
  hashPassword,
  validatePasswordStrength,
  verifyPassword,
} from './password.js';
import {
  emailTokenExpiry,
  generateEmailToken,
  generateRefreshToken,
  hashToken,
  refreshTokenExpiry,
  signAccessToken,
} from './tokens.js';

const MAX_FAILED_LOGINS = 5;
const LOCKOUT_MINUTES = 15;
const VERIFICATION_TTL_HOURS = 24;
const RESET_TTL_HOURS = 1;

export interface PublicUser {
  id: string;
  email: string;
  name: string;
  emailVerified: boolean;
}

export interface AuthSession {
  user: PublicUser;
  accessToken: string;
  refreshToken: string;
}

const toPublicUser = (user: User): PublicUser => ({
  id: user.id,
  email: user.email,
  name: user.name,
  emailVerified: user.emailVerified,
});

/** Emails are matched case-insensitively; "Dr.Smith@Clinic.com" and "dr.smith@clinic.com" are one account. */
const normalizeEmail = (email: string) => email.trim().toLowerCase();

/**
 * Registration returns a full session, even though the address is not yet verified.
 *
 * The session is deliberately "logged in but not let in": it is enough to call /auth/me and
 * /auth/resend-verification, and nothing else. Every endpoint that touches clinical data is behind
 * `requireVerified`, which rejects an unverified token outright.
 *
 * The alternative -- withholding tokens until verification -- means the verification screen has no
 * session with which to check "am I verified yet?", and the only ways out are to cache the user's
 * password on the device or to expose an endpoint that answers "does this email exist and is it
 * verified", which is the account-enumeration oracle we spent this whole file avoiding.
 */
export async function register(name: string, rawEmail: string, password: string): Promise<AuthSession> {
  const email = normalizeEmail(rawEmail);

  const weak = validatePasswordStrength(password);
  if (weak) throw AppError.weakPassword(weak);

  const existing = await prisma.user.findUnique({ where: { email } });
  if (existing) throw AppError.emailInUse();

  const user = await prisma.user.create({
    data: { email, name: name.trim(), passwordHash: await hashPassword(password) },
  });

  await issueVerificationEmail(user);
  return issueSession(user);
}

export async function login(rawEmail: string, password: string): Promise<AuthSession> {
  const email = normalizeEmail(rawEmail);
  const user = await prisma.user.findUnique({ where: { email } });

  if (!user) {
    // Spend the same time as a real bcrypt compare, then fail identically. See password.ts.
    await burnTimingBudget();
    throw AppError.invalidCredentials();
  }

  if (user.disabled) throw AppError.accountDisabled();

  if (user.lockedUntil && user.lockedUntil > new Date()) {
    const retryAfterSeconds = Math.ceil((user.lockedUntil.getTime() - Date.now()) / 1000);
    throw AppError.tooManyAttempts(retryAfterSeconds);
  }

  const ok = await verifyPassword(password, user.passwordHash);
  if (!ok) {
    await recordFailedLogin(user);
    throw AppError.invalidCredentials();
  }

  // The lockout counter resets only on a genuinely successful password check.
  if (user.failedLoginCount > 0 || user.lockedUntil) {
    await prisma.user.update({
      where: { id: user.id },
      data: { failedLoginCount: 0, lockedUntil: null },
    });
  }

  // An unverified user is signed in, not rejected. `user.emailVerified` rides along in the response
  // and the client gates on it; `requireVerified` gates every endpoint that matters. Rejecting here
  // instead would leave the verification screen with no session to check itself with.
  return issueSession(user);
}

/**
 * Refresh with rotation: the presented token is revoked and a new one issued.
 *
 * If a revoked token is presented again, that means two parties hold it -- the legitimate device
 * and a thief. We cannot tell which is which, so every session for that user is killed and both are
 * forced to sign in again. Noisy, and strictly better than leaving an attacker with a live session.
 */
export async function refresh(presentedToken: string): Promise<AuthSession> {
  const tokenHash = hashToken(presentedToken);
  const stored = await prisma.refreshToken.findUnique({
    where: { tokenHash },
    include: { user: true },
  });

  if (!stored) throw AppError.sessionExpired();

  if (stored.revokedAt) {
    await prisma.refreshToken.updateMany({
      where: { userId: stored.userId, revokedAt: null },
      data: { revokedAt: new Date() },
    });
    throw AppError.sessionExpired();
  }

  if (stored.expiresAt < new Date()) throw AppError.sessionExpired();
  if (stored.user.disabled) throw AppError.accountDisabled();

  await prisma.refreshToken.update({
    where: { id: stored.id },
    data: { revokedAt: new Date() },
  });

  return issueSession(stored.user);
}

export async function logout(presentedToken: string): Promise<void> {
  // Idempotent: logging out with an already-dead token is a success, not an error. The client's
  // intent (be signed out) is satisfied either way.
  await prisma.refreshToken.updateMany({
    where: { tokenHash: hashToken(presentedToken), revokedAt: null },
    data: { revokedAt: new Date() },
  });
}

export async function verifyEmail(rawToken: string): Promise<void> {
  const record = await prisma.authToken.findUnique({
    where: { tokenHash: hashToken(rawToken) },
  });

  if (!record || record.type !== AuthTokenType.EMAIL_VERIFICATION) throw AppError.invalidToken();
  if (record.usedAt) throw AppError.invalidToken();
  if (record.expiresAt < new Date()) throw AppError.invalidToken();

  await prisma.$transaction([
    prisma.user.update({ where: { id: record.userId }, data: { emailVerified: true } }),
    prisma.authToken.update({ where: { id: record.id }, data: { usedAt: new Date() } }),
  ]);
}

export async function resendVerification(rawEmail: string): Promise<void> {
  const user = await prisma.user.findUnique({ where: { email: normalizeEmail(rawEmail) } });

  // Silent no-op for unknown or already-verified addresses -- the endpoint must not reveal which
  // emails exist, and the caller gets 200 either way.
  if (!user || user.emailVerified) return;

  await issueVerificationEmail(user);
}

/**
 * Always resolves, whether or not the address has an account.
 *
 * The Android app shows "if an account exists for this address, we sent instructions" precisely so
 * that this endpoint can stay silent. Returning 404 for an unknown email would let anyone check
 * which dentists are registered.
 */
export async function forgotPassword(rawEmail: string): Promise<void> {
  const user = await prisma.user.findUnique({ where: { email: normalizeEmail(rawEmail) } });
  if (!user || user.disabled) return;

  const token = generateEmailToken();

  // Invalidate any outstanding reset links first: requesting a new one must kill the old.
  await prisma.$transaction([
    prisma.authToken.updateMany({
      where: { userId: user.id, type: AuthTokenType.PASSWORD_RESET, usedAt: null },
      data: { usedAt: new Date() },
    }),
    prisma.authToken.create({
      data: {
        tokenHash: hashToken(token),
        type: AuthTokenType.PASSWORD_RESET,
        userId: user.id,
        expiresAt: emailTokenExpiry(RESET_TTL_HOURS),
      },
    }),
  ]);

  await sendPasswordResetEmail(user.email, user.name, token);
}

export async function resetPassword(rawToken: string, newPassword: string): Promise<void> {
  const weak = validatePasswordStrength(newPassword);
  if (weak) throw AppError.weakPassword(weak);

  const record = await prisma.authToken.findUnique({
    where: { tokenHash: hashToken(rawToken) },
  });

  if (!record || record.type !== AuthTokenType.PASSWORD_RESET) throw AppError.invalidToken();
  if (record.usedAt) throw AppError.invalidToken();
  if (record.expiresAt < new Date()) throw AppError.invalidToken();

  const passwordHash = await hashPassword(newPassword);

  await prisma.$transaction([
    prisma.user.update({
      where: { id: record.userId },
      data: { passwordHash, failedLoginCount: 0, lockedUntil: null },
    }),
    prisma.authToken.update({ where: { id: record.id }, data: { usedAt: new Date() } }),
    // Changing the password signs out every device. If the reset happened because the account was
    // compromised, leaving the attacker's session alive would defeat the entire exercise.
    prisma.refreshToken.updateMany({
      where: { userId: record.userId, revokedAt: null },
      data: { revokedAt: new Date() },
    }),
  ]);
}

export async function getUser(userId: string): Promise<PublicUser> {
  const user = await prisma.user.findUnique({ where: { id: userId } });
  if (!user) throw AppError.sessionExpired();
  if (user.disabled) throw AppError.accountDisabled();
  return toPublicUser(user);
}

// --- internals ---------------------------------------------------------------

async function issueSession(user: User): Promise<AuthSession> {
  const refreshToken = generateRefreshToken();

  await prisma.refreshToken.create({
    data: {
      tokenHash: hashToken(refreshToken),
      userId: user.id,
      expiresAt: refreshTokenExpiry(),
    },
  });

  return {
    user: toPublicUser(user),
    accessToken: signAccessToken({ sub: user.id, email: user.email }),
    refreshToken,
  };
}

async function issueVerificationEmail(user: User): Promise<void> {
  const token = generateEmailToken();

  await prisma.authToken.create({
    data: {
      tokenHash: hashToken(token),
      type: AuthTokenType.EMAIL_VERIFICATION,
      userId: user.id,
      expiresAt: emailTokenExpiry(VERIFICATION_TTL_HOURS),
    },
  });

  await sendVerificationEmail(user.email, user.name, token);
}

async function recordFailedLogin(user: User): Promise<void> {
  const failedLoginCount = user.failedLoginCount + 1;
  const shouldLock = failedLoginCount >= MAX_FAILED_LOGINS;

  await prisma.user.update({
    where: { id: user.id },
    data: {
      failedLoginCount,
      lockedUntil: shouldLock ? new Date(Date.now() + LOCKOUT_MINUTES * 60 * 1000) : user.lockedUntil,
    },
  });
}
