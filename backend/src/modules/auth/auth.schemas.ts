import { z } from 'zod';

/**
 * Request shapes. These mirror the app's Validators.kt on purpose -- the client validates for fast
 * feedback, the server validates because a client is not a security boundary.
 */

export const registerSchema = z.object({
  name: z.string().trim().min(3, 'Name must be at least 3 characters').max(40),
  email: z.string().trim().email('Enter a valid email address').max(254),
  // Complexity is enforced in password.ts so that the same rules apply to /reset-password. Here we
  // only bound the length, to keep a 10MB "password" from ever reaching bcrypt.
  password: z.string().min(8).max(32),
});

export const loginSchema = z.object({
  email: z.string().trim().email().max(254),
  // Deliberately NOT the strength rules: an account created before the rules tightened must still
  // be able to sign in.
  password: z.string().min(1).max(200),
});

export const refreshSchema = z.object({
  refreshToken: z.string().min(1),
});

export const emailOnlySchema = z.object({
  email: z.string().trim().email().max(254),
});

export const resetPasswordSchema = z.object({
  token: z.string().min(1),
  password: z.string().min(8).max(32),
});

export const verifyEmailQuerySchema = z.object({
  token: z.string().min(1),
});
