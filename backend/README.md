# PerioCompliance AI — Backend

Node + Express + Prisma + PostgreSQL. Owns authentication now, and will own the Cloudinary and
Gemini API keys later — which is the real reason it exists. Calling Gemini from the Android app
means shipping your API key inside the APK, where anyone can unzip it out.

## Setup

**1. Get a Postgres database.** No local install needed — [Neon](https://neon.tech) has a permanent
free tier with no card. Create a project, copy the connection string.

The *same* URL works from your laptop and from Render. That is deliberate: SQLite locally and
Postgres in production is the classic setup, and the two disagree about case sensitivity, dates and
unique constraints. You find out which at 2am the night before the demo.

**2. Configure.** `.env` already exists with real JWT secrets generated. Paste your database URL in:

```
DATABASE_URL="postgresql://...   ← from Neon
```

**3. Migrate and run.**

```bash
npm run db:migrate    # creates the tables
npm run dev           # http://localhost:3000
```

`GET /health` should return `{"ok":true}`.

## Email

**SMTP is optional.** Leave `SMTP_*` blank and every verification/reset email is printed to your
terminal instead of sent. You copy the link out and paste it in a browser. The whole flow is
testable before you have a mail provider at all.

When you do want real mail — and this is what fixes the spam problem — the simplest option is Gmail:

```
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=you@gmail.com
SMTP_PASS=<16-char App Password>
```

That's an **App Password** (Google Account → Security → 2-Step Verification → App passwords), not
your Google password. Mail then arrives from a real Gmail address with real SPF/DKIM, which is why
it lands in the inbox — unlike Firebase's shared `firebaseapp.com` sender, which every spam filter
has learned to distrust.

## API

| Method | Route | Notes |
|---|---|---|
| POST | `/auth/register` | 201 + user. **No tokens** — registration ends at the verification gate. |
| POST | `/auth/login` | 200 + `{user, accessToken, refreshToken}`. 403 `EMAIL_NOT_VERIFIED` if unconfirmed. |
| POST | `/auth/refresh` | Rotates: the presented token is revoked, a new pair issued. |
| POST | `/auth/logout` | Revokes the refresh token. Idempotent. |
| GET | `/auth/verify-email?token=` | Opened from an email client → renders a page, not JSON. |
| POST | `/auth/resend-verification` | 204 always. |
| POST | `/auth/forgot-password` | 204 **always**, even for an unknown email. |
| POST | `/auth/reset-password` | Also signs out every device. |
| GET | `/auth/me` | Bearer token. |

Errors are always `{ error: { code, message } }`. **The app switches on `code`, never on `message`** —
every code maps 1:1 onto a case of `AuthError` in the Android app. Change the wording freely;
changing a code is a breaking API change.

## The security decisions, and why

**Unknown email and wrong password return the identical error**, with the same status and the same
body. Distinguishing them turns login into an account-enumeration oracle: submit an email with a
junk password, learn from the error whether that dentist has an account. For the same reason
`/auth/forgot-password` returns 204 for an address that was never registered.

**A login for an unknown email still burns ~250ms of bcrypt** against a dummy hash. Without that,
the miss path returns in 1ms and the hit path in 250ms — and the timing difference re-opens the
enumeration hole the identical error message just closed.

**Refresh tokens are opaque random strings, not JWTs.** A JWT refresh token is self-validating,
which sounds convenient and means it cannot be revoked. An opaque token is meaningless without its
database row, so deleting the row kills the session immediately.

**Refresh rotates, and reuse is treated as theft.** Presenting an already-revoked refresh token
means two parties hold it — the real device and an attacker. We cannot tell which is which, so
every session for that user is killed. Noisy, and better than leaving an attacker signed in.

**Nothing token-shaped is stored in the clear** — refresh tokens and email links are SHA-256'd at
rest, so a database leak yields nothing usable. (SHA-256 not bcrypt: these are 256 bits of
randomness, not human-chosen passwords, so there is no dictionary to slow down.)

**Two independent brute-force defences.** Per-account lockout (5 failures → 15 min) stops someone
guessing one dentist's password. Per-IP rate limiting stops someone spraying one common password
across thousands of accounts, which no per-account counter would ever see.

## Deploying to Render

`render.yaml` is a blueprint — Render reads it instead of making you fill in a form, which is where
people forget `APP_BASE_URL` and ship a backend that emails everyone dead `localhost` links.

**1. Push to GitHub.**

```bash
cd C:\APP\backend
git init && git add -A && git commit -m "PerioCompliance backend"
gh repo create periocompliance-backend --private --source=. --push
```

**2. Render → New → Blueprint**, pick the repo. It reads `render.yaml` and asks only for the values
marked `sync: false`:

| Variable | Value |
|---|---|
| `DATABASE_URL` | The **same** Neon URL you use locally. |
| `APP_BASE_URL` | Leave blank on the first deploy — you do not know the URL yet. |
| `SMTP_*` | Optional. Unset ⇒ emails print to the Render logs. |

`JWT_ACCESS_SECRET` and `JWT_REFRESH_SECRET` are generated by Render. They differ from your local
ones, and that is correct: a leaked dev secret must not sign valid production tokens.

**3. Deploy, then set `APP_BASE_URL` to the URL Render gives you** (e.g.
`https://periocompliance-api.onrender.com`) and redeploy. **This is the step everyone skips.** Every
verification and reset link in every email is built from this value; if it still says `localhost`,
your users get links that only work on your laptop.

**4. Check it:** `curl https://your-api.onrender.com/health` → `{"ok":true}`.

### Two things about the free tier

**It sleeps after 15 minutes idle**, and the next request takes ~30–50s to wake it. On stage that
looks like a broken app. Hit `/health` a minute before you present.

**Migrations, not `db push`.** `prisma/migrations/0_init` is baselined against the existing database,
so `npm run db:deploy` is a no-op on your current data and creates the full schema on a fresh one.
Keep using `npm run db:migrate` for schema changes from here — `db push` leaves no history, and the
deploy has nothing to apply.
