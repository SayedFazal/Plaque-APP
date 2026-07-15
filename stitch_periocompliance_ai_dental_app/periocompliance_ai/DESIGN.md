---
name: PerioCompliance AI
colors:
  surface: '#faf8ff'
  surface-dim: '#d9d9e5'
  surface-bright: '#faf8ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f3f3fe'
  surface-container: '#ededf9'
  surface-container-high: '#e7e7f3'
  surface-container-highest: '#e1e2ed'
  on-surface: '#191b23'
  on-surface-variant: '#434655'
  inverse-surface: '#2e3039'
  inverse-on-surface: '#f0f0fb'
  outline: '#737686'
  outline-variant: '#c3c6d7'
  surface-tint: '#0053db'
  primary: '#004ac6'
  on-primary: '#ffffff'
  primary-container: '#2563eb'
  on-primary-container: '#eeefff'
  inverse-primary: '#b4c5ff'
  secondary: '#565e74'
  on-secondary: '#ffffff'
  secondary-container: '#dae2fd'
  on-secondary-container: '#5c647a'
  tertiary: '#943700'
  on-tertiary: '#ffffff'
  tertiary-container: '#bc4800'
  on-tertiary-container: '#ffede6'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dbe1ff'
  primary-fixed-dim: '#b4c5ff'
  on-primary-fixed: '#00174b'
  on-primary-fixed-variant: '#003ea8'
  secondary-fixed: '#dae2fd'
  secondary-fixed-dim: '#bec6e0'
  on-secondary-fixed: '#131b2e'
  on-secondary-fixed-variant: '#3f465c'
  tertiary-fixed: '#ffdbcd'
  tertiary-fixed-dim: '#ffb596'
  on-tertiary-fixed: '#360f00'
  on-tertiary-fixed-variant: '#7d2d00'
  background: '#faf8ff'
  on-background: '#191b23'
  surface-variant: '#e1e2ed'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  title-md:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 16px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  2xl: 48px
  3xl: 64px
---

## Brand & Style
The design system is engineered for the high-stakes intersection of healthcare and artificial intelligence. It prioritizes clarity, precision, and a sense of "calm authority." The aesthetic is a fusion of **Apple-level minimalism**—characterized by generous whitespace and subtle transitions—and the structural logic of **Material Design 3**. 

The target audience includes dental professionals and healthcare administrators who require high data density without cognitive overload. The UI should feel like a premium tool: unobtrusive, exceptionally organized (Notion-esque), and inherently trustworthy. The emotional response should be one of "effortless compliance" and "clinical excellence."

## Colors
The palette is rooted in a professional "Clinical Blue" (#2563EB) that signals reliability and intelligence. 

- **Primary:** Used for key actions, active states, and brand recognition.
- **Secondary:** Reserved for deep text hierarchy, navigation backgrounds, and high-contrast elements to ensure accessibility.
- **Accent:** A soft "Health Green" used exclusively for positive status indicators, successful AI compliance checks, and growth metrics.
- **Background & Surface:** The foundation is a cool-tinted grey (#F8FAFC) to reduce eye strain, while pure white (#FFFFFF) is used for elevated cards and content containers.
- **Accessibility:** All text-on-color combinations must pass WCAG AA standards, particularly the primary blue against white surfaces.

## Typography
This design system utilizes **Inter** exclusively to achieve a systematic, utilitarian, and highly readable interface. 

The type scale is designed for complex data environments. Headlines use a tighter letter-spacing and heavier weights to anchor sections, while body text maintains standard spacing for maximum legibility in patient notes and AI reports. Small labels utilize a medium or semi-bold weight to ensure they remain crisp on low-resolution medical monitors.

## Layout & Spacing
The layout follows a strict **8px grid system**. All margins, paddings, and component heights must be multiples of 8 (with 4px used for internal atomic spacing).

- **Desktop:** A 12-column fixed-width grid (1440px max) is used for dashboard views to maintain content density.
- **Mobile:** Elements reflow to a single column with 16px side margins.
- **Rhythm:** Use "md" (16px) for internal card padding and "lg" (24px) for spacing between major sections. This creates the "breathable" feel characteristic of high-end software.

## Elevation & Depth
Depth is created through a combination of **Tonal Layering** and **Ambient Shadows**.

1.  **Base Layer:** Background (#F8FAFC) sits at the lowest level.
2.  **Surface Layer:** White (#FFFFFF) cards use a "Soft Shadow" (0px 4px 12px rgba(15, 23, 42, 0.05)).
3.  **Overlay Layer:** Modals and dropdowns utilize **Glassmorphism**. Surfaces feature a `backdrop-filter: blur(12px)` with a 80% opaque white background and a 1px subtle border (#E2E8F0).
4.  **Interaction:** Buttons lift slightly on hover, increasing the shadow spread, mimicking a physical tactile response.

## Shapes
The shape language is modern and approachable. A consistent **16px border radius** is applied to primary containers and cards, creating a friendly yet professional silhouette.

- **Buttons & Inputs:** Use the 8px (small) radius for a more precise, tool-like appearance.
- **Cards & Modals:** Use the 16px (base) radius.
- **Chips/Badges:** Use a fully rounded "pill" shape to distinguish them from interactive buttons.

## Components

- **Buttons:** Inspired by Material 3. Primary buttons use a solid blue background with white text. Secondary buttons use a subtle grey-tinted ghost style. Large touch targets (min 44px) are mandatory.
- **Cards:** White surfaces with 16px radius and soft ambient shadows. Cards should have a 1px border (#E2E8F0) to maintain definition on white backgrounds.
- **Input Fields:** Outlined style with 8px radius. Active state uses a 2px primary blue border with a soft glow.
- **Charts:** Use a custom healthcare-centric palette for data viz: Primary Blue for trends, Accent Green for "Healthy," and Warning Amber for "At-Risk." Lines should be smoothed (monotone cubic) for a modern, high-end look.
- **AI Indicators:** Use a subtle gradient (Primary to Accent) and a specific "Sparkle" icon to denote AI-generated compliance suggestions.
- **Glass Overlays:** Tooltips and floating action menus should use the backdrop-blur effect to maintain context of the underlying medical data.