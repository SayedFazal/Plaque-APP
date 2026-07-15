---
name: Obsidian Deep
colors:
  surface: '#051424'
  surface-dim: '#051424'
  surface-bright: '#2c3a4c'
  surface-container-lowest: '#010f1f'
  surface-container-low: '#0d1c2d'
  surface-container: '#122131'
  surface-container-high: '#1c2b3c'
  surface-container-highest: '#273647'
  on-surface: '#d4e4fa'
  on-surface-variant: '#c3c6d7'
  inverse-surface: '#d4e4fa'
  inverse-on-surface: '#233143'
  outline: '#8d90a0'
  outline-variant: '#434655'
  surface-tint: '#b4c5ff'
  primary: '#b4c5ff'
  on-primary: '#002a78'
  primary-container: '#2563eb'
  on-primary-container: '#eeefff'
  inverse-primary: '#0053db'
  secondary: '#4edea3'
  on-secondary: '#003824'
  secondary-container: '#00a572'
  on-secondary-container: '#00311f'
  tertiary: '#ffb596'
  on-tertiary: '#581e00'
  tertiary-container: '#bc4800'
  on-tertiary-container: '#ffede6'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#dbe1ff'
  primary-fixed-dim: '#b4c5ff'
  on-primary-fixed: '#00174b'
  on-primary-fixed-variant: '#003ea8'
  secondary-fixed: '#6ffbbe'
  secondary-fixed-dim: '#4edea3'
  on-secondary-fixed: '#002113'
  on-secondary-fixed-variant: '#005236'
  tertiary-fixed: '#ffdbcd'
  tertiary-fixed-dim: '#ffb596'
  on-tertiary-fixed: '#360f00'
  on-tertiary-fixed-variant: '#7d2d00'
  background: '#051424'
  on-background: '#d4e4fa'
  surface-variant: '#273647'
typography:
  headline-xl:
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
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-sm:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Inter
    fontSize: 12px
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
  base: 4px
  gutter: 24px
  margin-mobile: 16px
  margin-desktop: 40px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 32px
---

## Brand & Style
The design system focuses on a high-performance, developer-centric aesthetic that prioritizes clarity and focus within a dark environment. By leveraging deep slates and navys, the interface reduces eye strain while providing a sophisticated backdrop for technical workflows and data-heavy applications. 

The style is **Modern Corporate with a Technical Edge**, utilizing subtle depth and high-contrast accents to guide the user's attention. It evokes a sense of reliability, precision, and state-of-the-art engineering. The visual narrative is built on the contrast between the expansive darkness of the background and the luminous precision of the primary and accent colors.

## Colors
The palette is anchored by **#0F172A** (Slate 950) for the foundation and **#1E293B** (Slate 800) for elevated surfaces. This creates a logical hierarchy of depth without relying on traditional shadows.

- **Primary Blue (#2563EB):** Used for primary actions, active states, and focus indicators. 
- **Emerald Accent (#10B981):** Reserved for success states, secondary highlights, and positive data trends.
- **Neutrals:** Text and icons utilize a scale of slates. High-contrast **#F8FAFC** is used for headings to ensure maximum readability, while **#94A3B8** provides a softer tone for secondary information and metadata.

## Typography
The typography relies exclusively on **Inter**, a typeface designed for screens. The hierarchy is established through weight and color contrast rather than excessive size shifts.

- **Headlines:** Use Bold and Semi-Bold weights with tighter letter spacing to feel compact and "engineered."
- **Body Text:** Uses a 16px base for optimal legibility against dark backgrounds. Line height is kept generous (1.5x) to prevent text crowding.
- **Labels:** Use Medium and Semi-Bold weights to maintain visibility at smaller sizes. The `label-sm` role uses uppercase and increased tracking for clarity in UI controls like tabs or table headers.

## Layout & Spacing
This design system utilizes a **12-column fluid grid** for desktop and a **4-column grid** for mobile. 

- **The 4px Scale:** All spacing increments (padding, margins, gap) must be multiples of 4px.
- **Gutter & Margins:** A consistent 24px gutter maintains air between content blocks. Desktop layouts utilize a 40px outer margin to frame the content, while mobile drops to 16px to maximize screen real estate.
- **Content Blocks:** Use "stack" variables to define vertical relationships. Elements within a component (label + input) use `stack-sm`, while components within a section use `stack-md`.

## Elevation & Depth
In this dark mode environment, depth is communicated through **Tonal Layers** rather than heavy shadows. 

- **Level 0 (Base):** #0F172A. Used for the main background of the application.
- **Level 1 (Surface):** #1E293B. Used for cards, navigation sidebars, and header areas.
- **Level 2 (Overlay):** #334155 (Slate 700). Used for modals, dropdown menus, and tooltips.
- **Borders:** To further define shapes, use low-contrast outlines (1px solid #334155) on surface elements. This replaces the need for shadows, creating a cleaner, "flat-plus" look.

## Shapes
The shape language is consistent and approachable, using **Rounded** corners to soften the technical nature of the dark palette.

- **Standard Elements:** Buttons, inputs, and small cards use a 0.5rem (8px) radius.
- **Large Containers:** Content sections and main application cards use a 1rem (16px) radius.
- **Interactive States:** Focus rings should follow the curvature of the element with a 2px offset, using the primary blue color.

## Components
- **Buttons:**
    - *Primary:* Solid #2563EB with White text. No border.
    - *Secondary:* Ghost style with #334155 border and #F8FAFC text.
    - *Success:* Solid #10B981 with dark text (#0F172A) for maximum legibility.
- **Inputs:** Background #0F172A, border 1px #334155. On focus, the border changes to #2563EB with a subtle 2px outer glow.
- **Cards:** Background #1E293B, 1px border #334155. Do not use shadows; let the tonal difference create the separation.
- **Chips/Badges:** Subtle background (e.g., Primary Blue at 15% opacity) with the solid color used for the text.
- **Lists:** Items separated by a 1px #1E293B border. Hover states should use a subtle highlight of #334155.