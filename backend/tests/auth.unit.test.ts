import { describe, expect, it } from 'vitest';
import {
  hashPassword,
  validatePasswordStrength,
  verifyPassword,
} from '../src/modules/auth/password.js';
import { generateEmailToken, generateRefreshToken, hashToken } from '../src/modules/auth/tokens.js';

describe('password hashing', () => {
  it('verifies a correct password and rejects a wrong one', async () => {
    const hash = await hashPassword('Str0ng!Pass');

    expect(await verifyPassword('Str0ng!Pass', hash)).toBe(true);
    expect(await verifyPassword('Str0ng!Pas', hash)).toBe(false);
  });

  it('never stores the plaintext', async () => {
    const hash = await hashPassword('Str0ng!Pass');
    expect(hash).not.toContain('Str0ng!Pass');
    expect(hash.startsWith('$2')).toBe(true);
  });

  it('salts: the same password hashes differently every time', async () => {
    const a = await hashPassword('Str0ng!Pass');
    const b = await hashPassword('Str0ng!Pass');

    // Without a per-hash salt, two users with the same password would share a hash and a single
    // rainbow-table hit would crack both.
    expect(a).not.toBe(b);
    expect(await verifyPassword('Str0ng!Pass', a)).toBe(true);
    expect(await verifyPassword('Str0ng!Pass', b)).toBe(true);
  });
});

describe('password strength rules', () => {
  it('accepts a password meeting every rule', () => {
    expect(validatePasswordStrength('Str0ng!Pass')).toBeNull();
  });

  it('rejects each rule violation', () => {
    expect(validatePasswordStrength('Ab1!')).toMatch(/8 characters/);
    expect(validatePasswordStrength('A'.repeat(33) + 'b1!')).toMatch(/32 characters/);
    expect(validatePasswordStrength('lowercase1!')).toMatch(/uppercase/);
    expect(validatePasswordStrength('UPPERCASE1!')).toMatch(/lowercase/);
    expect(validatePasswordStrength('NoDigitsHere!')).toMatch(/number/);
    expect(validatePasswordStrength('NoSpecial123')).toMatch(/special/);
  });

  it('matches the rules the Android client enforces', () => {
    // If these two ever drift, a user passes client validation and is rejected by the server with
    // no field to attach the error to.
    expect(validatePasswordStrength('Abcd123!')).toBeNull();
    expect(validatePasswordStrength('abcd123!')).not.toBeNull();
  });
});

describe('tokens', () => {
  it('generates unguessable, unique tokens', () => {
    const tokens = new Set(Array.from({ length: 100 }, () => generateRefreshToken()));
    expect(tokens.size).toBe(100);

    const token = generateRefreshToken();
    expect(token.length).toBeGreaterThanOrEqual(60);
    expect(token).toMatch(/^[A-Za-z0-9_-]+$/); // base64url: safe in a URL and a header
  });

  it('hashes tokens deterministically, and the hash does not reveal the token', () => {
    const token = generateEmailToken();

    expect(hashToken(token)).toBe(hashToken(token));
    expect(hashToken(token)).not.toBe(token);
    expect(hashToken(token)).toHaveLength(64); // sha256 hex
  });

  it('gives different hashes to different tokens', () => {
    expect(hashToken(generateEmailToken())).not.toBe(hashToken(generateEmailToken()));
  });
});
