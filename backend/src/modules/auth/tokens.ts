import crypto from 'node:crypto';
import jwt from 'jsonwebtoken';
import { env } from '../../config/env.js';
import { AppError } from '../../utils/errors.js';

export interface AccessTokenPayload {
  sub: string;
  email: string;
}

/**
 * Short-lived (15m default) and stateless -- never stored, never revocable. Revocation happens at
 * the refresh boundary instead: kill the refresh token and the session dies within 15 minutes.
 * Checking a denylist on every request would put a database round trip in front of every API call.
 */
export function signAccessToken(payload: AccessTokenPayload): string {
  return jwt.sign(payload, env.JWT_ACCESS_SECRET, {
    expiresIn: env.ACCESS_TOKEN_TTL,
  } as jwt.SignOptions);
}

export function verifyAccessToken(token: string): AccessTokenPayload {
  try {
    return jwt.verify(token, env.JWT_ACCESS_SECRET) as AccessTokenPayload;
  } catch {
    // Expired and forged are the same to the caller: sign in again.
    throw AppError.sessionExpired();
  }
}

/**
 * Refresh tokens are opaque random strings, not JWTs.
 *
 * A JWT refresh token would carry its own claims and be self-validating, which sounds convenient
 * and means it cannot be revoked. An opaque token is meaningless without the database row, so
 * deleting the row logs the device out immediately.
 */
export function generateRefreshToken(): string {
  return crypto.randomBytes(48).toString('base64url');
}

/** Tokens sent by email. Short, URL-safe, unguessable. */
export function generateEmailToken(): string {
  return crypto.randomBytes(32).toString('base64url');
}

/**
 * Everything token-shaped is stored as a hash, never in the clear.
 *
 * SHA-256 rather than bcrypt here on purpose: these are 256+ bits of cryptographic randomness, not
 * human-chosen passwords, so there is no dictionary to attack and no need for a slow KDF. Using
 * bcrypt would add 250ms to every single API call for zero security gain.
 */
export function hashToken(token: string): string {
  return crypto.createHash('sha256').update(token).digest('hex');
}

export function refreshTokenExpiry(): Date {
  return new Date(Date.now() + env.REFRESH_TOKEN_TTL_DAYS * 24 * 60 * 60 * 1000);
}

export function emailTokenExpiry(hours: number): Date {
  return new Date(Date.now() + hours * 60 * 60 * 1000);
}