import type { NextFunction, Request, Response } from 'express';
import rateLimit from 'express-rate-limit';
import { ZodError, type ZodSchema } from 'zod';
import { AppError, ErrorCode } from '../utils/errors.js';
import { verifyAccessToken } from '../modules/auth/tokens.js';
import { prisma } from '../db/prisma.js';

declare global {
  // eslint-disable-next-line @typescript-eslint/no-namespace
  namespace Express {
    interface Request {
      userId?: string;
    }
  }
}

/** Validates the body against a schema and replaces it with the parsed (trimmed, typed) result. */
export const validateBody =
  (schema: ZodSchema) => (req: Request, _res: Response, next: NextFunction) => {
    try {
      req.body = schema.parse(req.body);
      next();
    } catch (error) {
      if (error instanceof ZodError) {
        next(
          AppError.validationFailed(
            error.issues.map((i) => ({ field: i.path.join('.'), message: i.message })),
          ),
        );
        return;
      }
      next(error);
    }
  };

/** Bearer-token guard for protected routes. */
export const requireAuth = (req: Request, _res: Response, next: NextFunction) => {
  const header = req.headers.authorization;
  if (!header?.startsWith('Bearer ')) {
    next(AppError.sessionExpired());
    return;
  }

  try {
    req.userId = verifyAccessToken(header.slice('Bearer '.length)).sub;
    next();
  } catch (error) {
    next(error);
  }
};

/**
 * The real verification gate. Chain it after [requireAuth] on every route that touches clinical
 * data: `router.get('/scans', requireAuth, requireVerified, ...)`.
 *
 * An unverified user holds a perfectly valid access token -- that is what lets the app's
 * verification screen call /auth/me and ask "am I verified yet?". This is what stops that token
 * being good for anything else. Without it, registering with an address you do not own would get
 * you into the app.
 *
 * Nothing uses it yet; Module 2 onwards will. It lives here so the next endpoint has no excuse.
 */
export const requireVerified = async (req: Request, _res: Response, next: NextFunction) => {
  try {
    if (!req.userId) throw AppError.sessionExpired();

    const user = await prisma.user.findUnique({
      where: { id: req.userId },
      select: { emailVerified: true },
    });

    if (!user) throw AppError.sessionExpired();
    if (!user.emailVerified) throw AppError.emailNotVerified();

    next();
  } catch (error) {
    next(error);
  }
};

/**
 * Per-IP throttle on the endpoints an attacker would hammer.
 *
 * This is a second, independent layer from the per-account lockout in auth.service. The account
 * lockout stops someone guessing one dentist's password; this stops someone spraying one common
 * password across thousands of accounts, which no per-account counter would ever notice.
 */
export const authRateLimit = rateLimit({
  windowMs: 15 * 60 * 1000,
  limit: 20,
  standardHeaders: 'draft-7',
  legacyHeaders: false,
  handler: (_req, res) => {
    res.status(429).json({
      error: {
        code: ErrorCode.TOO_MANY_ATTEMPTS,
        message: 'Too many attempts. Try again shortly.',
      },
    });
  },
});

/** Tighter still: sending mail costs money and annoys the recipient. */
export const emailRateLimit = rateLimit({
  windowMs: 60 * 60 * 1000,
  limit: 5,
  standardHeaders: 'draft-7',
  legacyHeaders: false,
  handler: (_req, res) => {
    res.status(429).json({
      error: {
        code: ErrorCode.TOO_MANY_ATTEMPTS,
        message: 'Too many emails requested. Try again later.',
      },
    });
  },
});

export const notFound = (_req: Request, res: Response) => {
  res.status(404).json({ error: { code: 'NOT_FOUND', message: 'No such endpoint.' } });
};

/**
 * The only place an error becomes a response.
 *
 * An unrecognised throwable is logged in full and reported as a bare UNKNOWN. Leaking a stack trace
 * or a Prisma message to a client hands an attacker your schema.
 */
export const errorHandler = (
  error: unknown,
  _req: Request,
  res: Response,
  _next: NextFunction,
) => {
  if (error instanceof AppError) {
    res.status(error.status).json({
      error: { code: error.code, message: error.message, details: error.details },
    });
    return;
  }

  console.error('Unhandled error:', error);
  res.status(500).json({
    error: { code: ErrorCode.UNKNOWN, message: 'Something went wrong. Please try again.' },
  });
};
