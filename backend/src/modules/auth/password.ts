import bcrypt from 'bcryptjs';

/**
 * Cost 12: roughly 250ms per hash on a small cloud instance. High enough that a leaked database is
 * not worth brute-forcing, low enough that a login does not feel slow.
 *
 * bcryptjs rather than the native `bcrypt` package deliberately -- native builds need a C toolchain
 * and break Windows dev machines and slim deploy images for no benefit at this scale.
 */
const COST = 12;

export function hashPassword(plain: string): Promise<string> {
  return bcrypt.hash(plain, COST);
}

export function verifyPassword(plain: string, hash: string): Promise<boolean> {
  return bcrypt.compare(plain, hash);
}

/**
 * A dummy hash to compare against when the email does not exist.
 *
 * Without this, a login for an unknown email returns in ~1ms while a login for a known email takes
 * ~250ms (the bcrypt compare). That timing difference is a free account-enumeration oracle, and it
 * defeats the whole point of returning an identical error for both cases. Burning the same CPU on
 * the miss path closes it.
 */
const DUMMY_HASH = bcrypt.hashSync('password-that-is-never-correct', COST);

export async function burnTimingBudget(): Promise<void> {
  await bcrypt.compare('password-that-is-never-correct', DUMMY_HASH);
}

/**
 * The server's own password rules. These intentionally duplicate the app's Validators.kt.
 *
 * The client validation exists to give fast feedback; this exists because a client is not a
 * security boundary. Anyone can POST straight to /auth/register with curl.
 */
export function validatePasswordStrength(password: string): string | null {
  if (password.length < 8) return 'Password must be at least 8 characters.';
  if (password.length > 32) return 'Password must be 32 characters or fewer.';
  if (!/[A-Z]/.test(password)) return 'Password must contain an uppercase letter.';
  if (!/[a-z]/.test(password)) return 'Password must contain a lowercase letter.';
  if (!/[0-9]/.test(password)) return 'Password must contain a number.';
  if (!/[^A-Za-z0-9]/.test(password)) return 'Password must contain a special character.';
  return null;
}
