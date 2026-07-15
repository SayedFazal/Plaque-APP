# Module 1 — Authentication

Paste the block below into Antigravity. Nothing above this line is part of the prompt.

The important difference from the original draft: it does not say *"don't change the colours."* A
code generator cannot obey that. It says *"read colours from `MaterialTheme.colorScheme`"* — which
is a thing that either happens or fails to compile. Constraints beat requests.

---

```text
This is an existing Android project. Open it, do not create a new one.

The design system is ALREADY BUILT and is the single source of truth. Your job is functionality only.

HARD RULES — these are not style preferences, they are build constraints:

1. Never write a hex colour. Not one. Read every colour from:
     MaterialTheme.colorScheme.<role>     (primary, surface, error, onSurfaceVariant, ...)
     PerioTheme.colors.<token>            (success, warning, chartTrend, glassSurface, ...)
2. Never write a raw sp value for text. Use MaterialTheme.typography.<style>.
3. Never write a raw dp value for spacing. Use PerioTheme.spacing.<token>
     (xs sm md lg xl / screenMargin, cardPadding, sectionGap, itemGap, minTouchTarget).
4. Never write a raw corner radius. Use PerioTheme.shapes.<token>
     (button, input, card, modal, sheet, chip) or MaterialTheme.shapes.
5. Card shadows come from Modifier.softShadow(PerioTheme.shapes.card). Do not call Modifier.shadow
   directly — it is wrong in dark mode, where the design uses tonal layers instead.
6. Do not add a dynamicColor / Material You option. The brand colours are fixed.
7. Do not edit anything in ui/theme/. It is already correct.

These files exist. Read them before you write anything:
    app/src/main/java/com/periocompliance/ai/ui/theme/Theme.kt
    app/src/main/java/com/periocompliance/ai/ui/theme/Color.kt
    app/src/main/java/com/periocompliance/ai/ui/theme/Type.kt
    app/src/main/java/com/periocompliance/ai/ui/theme/Spacing.kt
    app/src/main/java/com/periocompliance/ai/ui/theme/Shape.kt
    app/src/main/java/com/periocompliance/ai/ui/theme/Elevation.kt
    app/src/main/java/com/periocompliance/ai/navigation/PerioDestinations.kt
    app/src/main/java/com/periocompliance/ai/navigation/PerioNavHost.kt

The visual reference for each screen is the exported PNG:
    ../stitch_periocompliance_ai_dental_app/login/screen.png
    ../stitch_periocompliance_ai_dental_app/register/screen.png
    ../stitch_periocompliance_ai_dental_app/forgot_password/screen.png
    ../stitch_periocompliance_ai_dental_app/splash_screen/screen.png
    ../stitch_periocompliance_ai_dental_app/onboarding_track/screen.png
    ../stitch_periocompliance_ai_dental_app/onboarding_ai_analysis/screen.png
    ../stitch_periocompliance_ai_dental_app/onboarding_compliance/screen.png
Match the layout in the PNG. Get the colours, type, spacing and radii from the theme, not the PNG.

BUILD ONLY MODULE 1: AUTHENTICATION.

Stack (already on the classpath, do not add alternatives):
  Kotlin, Jetpack Compose, Material 3, MVVM, Hilt, Navigation Compose, StateFlow, Coroutines.

Firebase Auth is ALREADY on the classpath and app/google-services.json is ALREADY in place, for
project "periocomplianceai", package com.periocompliance.ai. The build is green. Do not add,
change or re-version any Gradle dependency. If you think you need a new library, say so and stop.

Scope:
  Splash (decides: logged in -> home, else -> onboarding/login)
  Onboarding x3 (first launch only, persisted in DataStore)
  Register, Login, Forgot Password, Email Verification
  Logout, Remember Me, session persistence across process death

Registration fields: Full Name, Email, Password, Confirm Password.

Validation — real-time, per field, error text below the field, submit disabled until the form is valid:
  Name      3..40 characters
  Email     RFC 5322 shape
  Password  8..32, must contain uppercase, lowercase, digit, special character
            strength meter: Weak / Medium / Strong
  Confirm   must equal Password
  Show/hide password toggle on both password fields.

Every screen must model state explicitly. One sealed UiState per screen, exposed as StateFlow from
the ViewModel. Handle, with a distinct visible state for each:
  Idle, Loading, Success, and these failures —
  network unavailable, email already registered, wrong password, account disabled,
  too many attempts, email not verified, unknown error.
Never show a raw exception message to the user. Map Firebase error codes to strings in strings.xml.

Email verification gates the app: an unverified user cannot reach the main graph. Offer "resend
email" with a 60-second cooldown.

Architecture — one package per layer, no shortcuts:
  data/     AuthRepositoryImpl, FirebaseAuthDataSource, TokenStore (DataStore + EncryptedSharedPreferences)
  domain/   AuthRepository (interface), User model, use cases:
            SignInUseCase, RegisterUseCase, SendPasswordResetUseCase,
            SendVerificationEmailUseCase, ObserveAuthStateUseCase, SignOutUseCase
  ui/auth/  one Screen + one ViewModel per destination
  di/       AuthModule (@Module @InstallIn(SingletonComponent::class))

The ViewModel must not import anything from com.google.firebase. The repository is the only place
Firebase exists.

Session: Firebase already persists the session and refreshes the ID token. Do NOT hand-roll a JWT
cache. TokenStore is only for the "remember me" flag and the onboarding-seen flag.

Wire the screens into the EXISTING routes in PerioDestinations.kt. Replace the NotBuiltYet()
placeholder bodies in PerioNavHost.kt for the Module 1 routes only. Change the NavHost start
destination from Routes.DESIGN_SYSTEM to Routes.SPLASH. Leave every other placeholder alone.

Write unit tests for the validators and for each ViewModel's state transitions.

When you are done, run:  ./gradlew assembleDebug
It must succeed. Fix what you broke until it does.

Then STOP. Do not start Module 2. Do not touch the dashboard, camera, AI, history or charts.
```

---

## Before you paste this

`google-services.json` is in place and the build compiles with Firebase Auth. One thing that is
**not** verifiable from here: Email/Password must be enabled in the Firebase console under
Authentication → Sign-in method. Creating a project does not enable it. If it is off, registration
fails at runtime with `CONFIGURATION_NOT_FOUND` and it will look like a code bug for an hour.

## How you know Module 1 is actually done

Not "it compiles." These:

- Register a new account → verification email arrives → app refuses to proceed until you click it.
- Kill the app from recents, reopen → still logged in, straight to home.
- Turn on airplane mode, try to log in → a readable "no connection" state, not a crash and not a
  Firebase stack trace.
- Type a wrong password 6 times → the "too many attempts" state renders.
- Log out → back stack is gone; the back button does not return you to the dashboard.
- Flip the phone to dark mode on every auth screen → still legible, still on-brand.

That last one is the one everybody forgets, and it is the one a judge will do by accident.
