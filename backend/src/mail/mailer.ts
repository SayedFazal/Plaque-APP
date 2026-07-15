import nodemailer, { type Transporter } from 'nodemailer';
import { env, hasSmtp } from '../config/env.js';

/**
 * When SMTP is not configured, emails are printed to the console instead of sent.
 *
 * This is not a stub for its own sake -- it means the whole verification and reset flow can be
 * exercised end to end before a mail provider exists. You copy the link out of the terminal. In
 * production, SMTP_* is set and the console transport is never constructed.
 */
let transporter: Transporter | null = null;

if (hasSmtp) {
  transporter = nodemailer.createTransport({
    host: env.SMTP_HOST,
    port: env.SMTP_PORT,
    // 465 is implicit TLS; 587 upgrades via STARTTLS. Getting this backwards is the single most
    // common reason SMTP "just hangs".
    secure: env.SMTP_PORT === 465,
    auth: { user: env.SMTP_USER!, pass: env.SMTP_PASS! },
  });
}

interface Mail {
  to: string;
  subject: string;
  html: string;
  text: string;
}

async function send(mail: Mail): Promise<void> {
  if (!transporter) {
    console.log('\n──────── EMAIL (no SMTP configured, not sent) ────────');
    console.log(`To:      ${mail.to}`);
    console.log(`Subject: ${mail.subject}`);
    console.log(mail.text);
    console.log('─────────────────────────────────────────────────────\n');
    return;
  }

  await transporter.sendMail({ from: env.MAIL_FROM, ...mail });
}

export async function sendVerificationEmail(to: string, name: string, token: string): Promise<void> {
  const link = `${env.APP_BASE_URL}/auth/verify-email?token=${encodeURIComponent(token)}`;

  await send({
    to,
    subject: 'Verify your PerioCompliance AI account',
    text: `Hi ${name},\n\nConfirm your email address to finish setting up your account:\n${link}\n\nThis link expires in 24 hours.\n\nIf you did not create this account, you can ignore this email.`,
    html: `
      <div style="font-family:Inter,Arial,sans-serif;max-width:480px;margin:0 auto;padding:24px;color:#191b23">
        <h1 style="font-size:24px;margin:0 0 8px">Verify your email</h1>
        <p style="color:#434655;line-height:1.5">Hi ${escapeHtml(name)}, confirm your email address to finish setting up your PerioCompliance AI account.</p>
        <p style="margin:24px 0">
          <a href="${link}" style="background:#2563eb;color:#fff;padding:12px 24px;border-radius:8px;text-decoration:none;display:inline-block">Verify email</a>
        </p>
        <p style="color:#737686;font-size:12px;line-height:1.5">This link expires in 24 hours. If you did not create this account, ignore this email.</p>
      </div>`,
  });
}

export async function sendPasswordResetEmail(to: string, name: string, token: string): Promise<void> {
  const link = `${env.APP_BASE_URL}/auth/reset-password?token=${encodeURIComponent(token)}`;

  await send({
    to,
    subject: 'Reset your PerioCompliance AI password',
    text: `Hi ${name},\n\nReset your password here:\n${link}\n\nThis link expires in 1 hour and can only be used once.\n\nIf you did not request this, ignore this email -- your password has not changed.`,
    html: `
      <div style="font-family:Inter,Arial,sans-serif;max-width:480px;margin:0 auto;padding:24px;color:#191b23">
        <h1 style="font-size:24px;margin:0 0 8px">Reset your password</h1>
        <p style="color:#434655;line-height:1.5">Hi ${escapeHtml(name)}, use the button below to choose a new password.</p>
        <p style="margin:24px 0">
          <a href="${link}" style="background:#2563eb;color:#fff;padding:12px 24px;border-radius:8px;text-decoration:none;display:inline-block">Reset password</a>
        </p>
        <p style="color:#737686;font-size:12px;line-height:1.5">This link expires in 1 hour and can only be used once. If you did not request it, ignore this email — your password has not changed.</p>
      </div>`,
  });
}

/** The name comes from user input and lands in an HTML email. Escape it. */
function escapeHtml(value: string): string {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}
