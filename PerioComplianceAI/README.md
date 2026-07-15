# PerioCompliance AI — Android

Phase 0 (foundation) is complete. No features are built yet, by design.

## What exists

| | |
|---|---|
| **Build** | Gradle 8.14, AGP 8.11.1, Kotlin 2.2.20, compileSdk 36, minSdk 26 |
| **UI** | Jetpack Compose, Material 3, Navigation Compose |
| **DI** | Hilt (KSP) — `@HiltAndroidApp` on `PerioComplianceApp`, `@AndroidEntryPoint` on `MainActivity` |
| **Theme** | `ui/theme/` — every token from the exported `DESIGN.md` files |
| **Nav** | `navigation/` — all 15 exported screens have a route; each is a `NotBuiltYet` placeholder |
| **Firebase** | Auth wired (BOM 33.7.0 + `google-services` 4.4.2), `app/google-services.json` in place |

`minSdk 26` is not arbitrary: Inter ships as a variable font and instancing the `wght` axis needs
API 26.

## Build

```bash
./gradlew assembleDebug
```

If Gradle cannot find the SDK, fix `local.properties` — backslashes must be doubled.

## The design tokens

Two design systems were exported, and both are honoured: **PerioCompliance AI** (light) and
**Obsidian Deep** (dark). They are not two coats of paint on one design — Obsidian deliberately
drops shadows in favour of tonal layers, so `Modifier.softShadow()` draws nothing in dark mode.
That is the spec, not a bug.

Read tokens, never literals:

```kotlin
MaterialTheme.colorScheme.primary      // brand colour
MaterialTheme.typography.bodyLarge     // Inter 16/24/400
PerioTheme.colors.success              // Health Green — no M3 role for it
PerioTheme.spacing.cardPadding         // 16dp, from the 8px grid
PerioTheme.shapes.card                 // 16dp radius
Modifier.softShadow(PerioTheme.shapes.card)
```

A handful of colours in `ExtendedColors.kt` are marked `[derived]` — the design brief describes
them in prose ("Warning Amber") but never gives a hex. They are the only judgement calls in the
theme. If you have the real values, change them there and the whole app follows.

There is no Material You / dynamic colour option, deliberately. Wallpaper-tinted brand colours are
exactly the drift the export is meant to prevent.

## Seeing the theme

The app currently opens on `DesignSystemScreen` — every token rendered on one page, so you can hold
it next to the exported PNGs. Flip the phone into dark mode to check Obsidian Deep. Module 1 changes
the start destination to `Routes.SPLASH` and this screen gets deleted.

## Build order

Each module replaces placeholder bodies in `PerioNavHost.kt` and touches nothing else.

1. **Authentication** — see [docs/MODULE_1_AUTH_PROMPT.md](docs/MODULE_1_AUTH_PROMPT.md). Firebase is ready; enable Email/Password in the console first.
2. Patient home dashboard
3. Camera (CameraX)
4. Cloudinary upload
5. Gemini analysis
6. History
7. Charts
8. Notifications
9. Dentist dashboard
10. Deployment

Firebase Auth is wired and the build is green, but no auth *code* exists yet — that is Module 1's
job. The repository is the only place `com.google.firebase` may be imported; a ViewModel that
imports it is a bug.
