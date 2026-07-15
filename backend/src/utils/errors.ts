/**
 * The error contract between this server and the Android app.
 *
 * Every value in ErrorCode maps 1:1 onto a case of the app's AuthError sealed interface
 * (domain/model/AuthError.kt). The app switches on `code`; it never parses `message`. That means
 * the wording below can be changed freely, and the codes cannot -- changing one is a breaking API
 * change that will silently fall through to "unknown error" on every installed client.
 */
export const ErrorCode = {
  INVALID_CREDENTIALS: 'INVALID_CREDENTIALS',
  EMAIL_IN_USE: 'EMAIL_IN_USE',
  ACCOUNT_DISABLED: 'ACCOUNT_DISABLED',
  TOO_MANY_ATTEMPTS: 'TOO_MANY_ATTEMPTS',
  WEAK_PASSWORD: 'WEAK_PASSWORD',
  INVALID_EMAIL: 'INVALID_EMAIL',
  EMAIL_NOT_VERIFIED: 'EMAIL_NOT_VERIFIED',
  SESSION_EXPIRED: 'SESSION_EXPIRED',
  INVALID_TOKEN: 'INVALID_TOKEN',
  VALIDATION_FAILED: 'VALIDATION_FAILED',
  UNKNOWN: 'UNKNOWN',
} as const;

export type ErrorCode = (typeof ErrorCode)[keyof typeof ErrorCode];

export class AppError extends Error {
  constructor(
    readonly code: ErrorCode,
    readonly status: number,
    message: string,
    readonly details?: unknown,
  ) {
    super(message);
    this.name = 'AppError';
  }

  /**
   * Wrong password, unknown email and malformed credential all collapse into this one error, with
   * an identical response body and status.
   *
   * Distinguishing them turns the login endpoint into an account-enumeration oracle: an attacker
   * submits an email with a junk password and learns from the error whether that dentist has an
   * account here. The same reasoning drives forgot-password always returning 200.
   */
  static invalidCredentials() {
    return new AppError(ErrorCode.INVALID_CREDENTIALS, 401, 'Incorrect email or password.');
  }

  static emailInUse() {
    return new AppError(ErrorCode.EMAIL_IN_USE, 409, 'An account already exists for this email.');
  }

  static accountDisabled() {
    return new AppError(ErrorCode.ACCOUNT_DISABLED, 403, 'This account has been disabled.');
  }

  static tooManyAttempts(retryAfterSeconds?: number) {
    return new AppError(
      ErrorCode.TOO_MANY_ATTEMPTS,
      429,
      'Too many attempts. Try again shortly.',
      retryAfterSeconds ? { retryAfterSeconds } : undefined,
    );
  }

  static emailNotVerified() {
    return new AppError(ErrorCode.EMAIL_NOT_VERIFIED, 403, 'Verify your email address to continue.');
  }

  static sessionExpired() {
    return new AppError(ErrorCode.SESSION_EXPIRED, 401, 'Your session expired. Sign in again.');
  }

  static invalidToken() {
    return new AppError(ErrorCode.INVALID_TOKEN, 400, 'This link is invalid or has expired.');
  }

  static weakPassword(message = 'That password is too weak.') {
    return new AppError(ErrorCode.WEAK_PASSWORD, 400, message);
  }

  static validationFailed(details: unknown) {
    return new AppError(ErrorCode.VALIDATION_FAILED, 400, 'Some fields are invalid.', details);
  }
}
