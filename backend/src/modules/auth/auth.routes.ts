import { Router, type Request, type Response, type NextFunction } from 'express';
import {
  authRateLimit,
  emailRateLimit,
  requireAuth,
  validateBody,
} from '../../middleware/index.js';
import {
  emailOnlySchema,
  loginSchema,
  refreshSchema,
  registerSchema,
  resetPasswordSchema,
  verifyEmailQuerySchema,
} from './auth.schemas.js';
import * as service from './auth.service.js';
import { AppError } from '../../utils/errors.js';

export const authRouter = Router();

/** Wraps an async handler so a rejected promise reaches the error middleware instead of hanging. */
const handle =
  (fn: (req: Request, res: Response) => Promise<void>) =>
  (req: Request, res: Response, next: NextFunction) => {
    fn(req, res).catch(next);
  };

authRouter.post(
  '/register',
  authRateLimit,
  validateBody(registerSchema),
  handle(async (req, res) => {
    const { name, email, password } = req.body;
    // Returns a session, but an unverified one: useless against anything behind `requireVerified`.
    res.status(201).json(await service.register(name, email, password));
  }),
);

authRouter.post(
  '/login',
  authRateLimit,
  validateBody(loginSchema),
  handle(async (req, res) => {
    const { email, password } = req.body;
    res.json(await service.login(email, password));
  }),
);

authRouter.post(
  '/refresh',
  validateBody(refreshSchema),
  handle(async (req, res) => {
    res.json(await service.refresh(req.body.refreshToken));
  }),
);

authRouter.post(
  '/logout',
  validateBody(refreshSchema),
  handle(async (req, res) => {
    await service.logout(req.body.refreshToken);
    res.status(204).send();
  }),
);

/**
 * Opened from an email client, so it renders a page rather than returning JSON. The user is in a
 * browser here, not in the app.
 */
authRouter.get(
  '/verify-email',
  handle(async (req, res) => {
    const parsed = verifyEmailQuerySchema.safeParse(req.query);
    if (!parsed.success) {
      res.status(400).send(page('Invalid link', 'This verification link is malformed.', false));
      return;
    }

    try {
      await service.verifyEmail(parsed.data.token);
      res.send(
        page(
          'Email verified',
          'Your email is confirmed. Return to the PerioCompliance AI app and continue.',
          true,
        ),
      );
    } catch {
      res.status(400).send(
        page('Link expired', 'This link is invalid or has already been used. Request a new one from the app.', false),
      );
    }
  }),
);

authRouter.post(
  '/resend-verification',
  emailRateLimit,
  validateBody(emailOnlySchema),
  handle(async (req, res) => {
    await service.resendVerification(req.body.email);
    // 204 regardless of whether the address exists. See auth.service.
    res.status(204).send();
  }),
);

authRouter.post(
  '/forgot-password',
  emailRateLimit,
  validateBody(emailOnlySchema),
  handle(async (req, res) => {
    await service.forgotPassword(req.body.email);
    res.status(204).send();
  }),
);

/**
 * The page the reset email actually links to.
 *
 * This existed as a POST endpoint only, which meant the link in every reset email 404'd: a user
 * could ask for a reset and never complete one. The password is typed in a browser, not in the app
 * -- the user is here precisely because they cannot get into the app.
 *
 * The form posts to the JSON endpoint below rather than being urlencoded, so there is exactly one
 * code path that changes a password, and it is the one the validation and the rate limiter guard.
 */
authRouter.get('/reset-password', (req, res) => {
  const parsed = verifyEmailQuerySchema.safeParse(req.query);
  if (!parsed.success) {
    res.status(400).send(page('Invalid link', 'This reset link is malformed.', false));
    return;
  }
  // The token is NOT validated here. Telling a visitor "this token is bad" before they have typed
  // anything is a free oracle for probing tokens; the POST below rejects it either way.
  res.send(resetForm(parsed.data.token));
});

authRouter.post(
  '/reset-password',
  authRateLimit,
  validateBody(resetPasswordSchema),
  handle(async (req, res) => {
    await service.resetPassword(req.body.token, req.body.password);
    res.status(204).send();
  }),
);

authRouter.get(
  '/me',
  requireAuth,
  handle(async (req, res) => {
    if (!req.userId) throw AppError.sessionExpired();
    res.json({ user: await service.getUser(req.userId) });
  }),
);

/**
 * "Choose a new password", rendered for the link in a reset email.
 *
 * The rules mirrored in the script below are the same ones enforced by validatePasswordStrength on
 * the server and by Validators.kt in the app. The client copy exists to give instant feedback; the
 * server copy exists because a browser is not a security boundary.
 */
function resetForm(token: string): string {
  // The token goes into a JS string literal. JSON.stringify escapes quotes and angle brackets that
  // would otherwise let a crafted URL break out of the script tag.
  const safeToken = JSON.stringify(token);

  return `<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <title>Reset password — PerioCompliance AI</title>
</head>
<body style="margin:0;background:#faf8ff;font-family:Inter,-apple-system,Arial,sans-serif;color:#191b23">
  <div style="max-width:420px;margin:10vh auto;padding:32px;background:#fff;border:1px solid #c3c6d7;border-radius:16px">
    <h1 style="font-size:24px;margin:0 0 8px">Choose a new password</h1>
    <p style="color:#434655;line-height:1.5;margin:0 0 24px">At least 8 characters, with an uppercase letter, a lowercase letter, a number and a special character.</p>

    <form id="form">
      <input id="password" type="password" placeholder="New password" autocomplete="new-password"
             style="width:100%;box-sizing:border-box;padding:12px;border:1px solid #c3c6d7;border-radius:8px;font-size:16px;margin-bottom:12px">
      <input id="confirm" type="password" placeholder="Confirm new password" autocomplete="new-password"
             style="width:100%;box-sizing:border-box;padding:12px;border:1px solid #c3c6d7;border-radius:8px;font-size:16px;margin-bottom:8px">
      <p id="error" style="color:#ba1a1a;font-size:14px;min-height:20px;margin:0 0 12px"></p>
      <button id="submit" type="submit"
              style="width:100%;padding:14px;background:#2563eb;color:#fff;border:0;border-radius:8px;font-size:16px;cursor:pointer">
        Reset password
      </button>
    </form>

    <div id="done" style="display:none;text-align:center">
      <div style="width:56px;height:56px;border-radius:999px;background:#00a572;margin:0 auto 24px"></div>
      <h2 style="font-size:20px;margin:0 0 8px">Password updated</h2>
      <p style="color:#434655;line-height:1.5;margin:0">You have been signed out on every device. Open the PerioCompliance AI app and sign in with your new password.</p>
    </div>
  </div>

<script>
  const token = ${safeToken};
  const form = document.getElementById('form');
  const error = document.getElementById('error');
  const submit = document.getElementById('submit');

  function validate(password) {
    if (password.length < 8) return 'Password must be at least 8 characters.';
    if (password.length > 32) return 'Password must be 32 characters or fewer.';
    if (!/[A-Z]/.test(password)) return 'Add an uppercase letter.';
    if (!/[a-z]/.test(password)) return 'Add a lowercase letter.';
    if (!/[0-9]/.test(password)) return 'Add a number.';
    if (!/[^A-Za-z0-9]/.test(password)) return 'Add a special character.';
    return null;
  }

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const password = document.getElementById('password').value;
    const confirm = document.getElementById('confirm').value;

    const problem = validate(password) || (password !== confirm ? 'Passwords do not match.' : null);
    if (problem) { error.textContent = problem; return; }

    error.textContent = '';
    submit.disabled = true;
    submit.textContent = 'Resetting…';

    try {
      const response = await fetch('/auth/reset-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token, password }),
      });

      if (response.ok) {
        form.style.display = 'none';
        document.getElementById('done').style.display = 'block';
        return;
      }

      const body = await response.json().catch(() => null);
      // INVALID_TOKEN means the link expired, was already used, or is fake. Say so plainly rather
      // than leaving the user retyping a password that will never be accepted.
      error.textContent = body && body.error && body.error.code === 'INVALID_TOKEN'
        ? 'This link has expired or was already used. Request a new one from the app.'
        : (body && body.error && body.error.message) || 'Something went wrong. Please try again.';
    } catch {
      error.textContent = 'No connection. Check your network and try again.';
    }

    submit.disabled = false;
    submit.textContent = 'Reset password';
  });
</script>
</body>
</html>`;
}

/** Minimal branded confirmation page. Colours are the exported design tokens. */
function page(title: string, body: string, success: boolean): string {
  const accent = success ? '#00a572' : '#ba1a1a';
  return `<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <title>${title} — PerioCompliance AI</title>
</head>
<body style="margin:0;background:#faf8ff;font-family:Inter,-apple-system,Arial,sans-serif;color:#191b23">
  <div style="max-width:420px;margin:15vh auto;padding:32px;background:#fff;border:1px solid #c3c6d7;border-radius:16px;text-align:center">
    <div style="width:56px;height:56px;border-radius:999px;background:${accent};margin:0 auto 24px"></div>
    <h1 style="font-size:24px;margin:0 0 8px">${title}</h1>
    <p style="color:#434655;line-height:1.5;margin:0">${body}</p>
  </div>
</body>
</html>`;
}
