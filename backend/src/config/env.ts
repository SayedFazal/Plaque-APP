import 'dotenv/config';
import { z } from 'zod';

/**
 * Config is validated once, at boot, and the process refuses to start if anything is missing.
 *
 * The alternative -- reading process.env where it is needed -- means a missing JWT secret is
 * discovered by the first user who tries to log in, in production, at which point the server is
 * signing tokens with `undefined`.
 */
const schema = z.object({
  NODE_ENV: z.enum(['development', 'test', 'production']).default('development'),
  PORT: z.coerce.number().default(3000),

  DATABASE_URL: z.string().url(),

  // Separate secrets for the two token types. If they shared one, an access token could be
  // presented as a refresh token and vice versa.
  JWT_ACCESS_SECRET: z.string().min(32, 'JWT_ACCESS_SECRET must be at least 32 characters'),
  JWT_REFRESH_SECRET: z.string().min(32, 'JWT_REFRESH_SECRET must be at least 32 characters'),

  ACCESS_TOKEN_TTL: z.string().default('15m'),
  REFRESH_TOKEN_TTL_DAYS: z.coerce.number().default(30),

  /** Public base URL of this server; used to build the links inside emails. */
  APP_BASE_URL: z.string().url().default('http://localhost:3000'),

  // SMTP is optional. Without it, emails are printed to the console instead of sent -- so the whole
  // flow is testable on day one without waiting on a mail provider.
  SMTP_HOST: z.string().optional(),
  SMTP_PORT: z.coerce.number().optional(),
  SMTP_USER: z.string().optional(),
  SMTP_PASS: z.string().optional(),
  MAIL_FROM: z.string().default('PerioCompliance AI <no-reply@periocompliance.ai>'),
});

const parsed = schema.safeParse(process.env);

if (!parsed.success) {
  console.error('Invalid environment configuration:');
  for (const issue of parsed.error.issues) {
    console.error(`  ${issue.path.join('.')}: ${issue.message}`);
  }
  process.exit(1);
}

export const env = parsed.data;

/** True when SMTP is configured; otherwise the mailer logs to the console. */
export const hasSmtp = Boolean(env.SMTP_HOST && env.SMTP_PORT && env.SMTP_USER && env.SMTP_PASS);
