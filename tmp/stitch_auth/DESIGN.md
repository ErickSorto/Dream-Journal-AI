# Design System Strategy: The Celestial Editorial

## 1. Overview & Creative North Star
**Creative North Star: The Ethereal Archive**

This design system is not a utility; it is a sanctuary. We are moving away from the "app-as-a-tool" aesthetic and toward a "high-end digital journal" experience. The goal is to evoke the quiet, expansive feeling of a night sky. We achieve this through **The Ethereal Archive** philosophy: a blend of rigid organizational clarity (the Archive) and soft, glowing whimsy (the Ethereal).

To break the "template" look, we utilize intentional asymmetry—such as staggered card layouts and overlapping decorative elements (moons, stars, and lighthouses)—that break out of their containers. This system prioritizes depth and atmosphere over flat structural lines, using light and shadow to guide the eye through the dreamscape.

---

## 2. Colors & Surface Philosophy
The palette is rooted in deep cosmic indigos and shifting violets, punctuated by a warm, "golden-hour" primary yellow.

### The "No-Line" Rule
**Explicit Instruction:** Designers are prohibited from using 1px solid borders to define sections or containers. The interface must feel boundless. Structure is created through:
- **Tonal Shifts:** Placing a `surface-container-low` (#111127) card against a `surface` (#0c0c1f) background.
- **Glassmorphism:** Using semi-transparent surfaces with a `backdrop-blur` of 12px–20px to create a physical sense of "frosted glass."

### Surface Hierarchy & Nesting
Treat the UI as a series of nested layers. 
- **Base Layer:** `surface` (#0c0c1f).
- **Secondary Containers:** `surface-container` (#17172f) for main content blocks.
- **Elevated Content:** `surface-container-highest` (#23233f) for active inputs or featured dream entries.

### Signature Textures
Main CTAs must never be flat. Use a **Signature Gradient** transitioning from `primary` (#fdd34d) to `primary-container` (#c19c13) at a 135-degree angle. This mimics the glow of a lantern or a star, providing a professional "soul" that flat hex codes lack.

---

## 3. Typography
We use a high-contrast pairing to balance the whimsical nature of dreams with the organized nature of a journal.

*   **Display & Headline (Noto Serif):** Our editorial voice. The serif choice provides authority and a "classic book" feel. Use `display-lg` (3.5rem) for hero moments and `headline-md` (1.75rem) for dream titles.
*   **Body & Labels (Manrope):** Our functional voice. A modern, clean sans-serif that ensures legibility within the "glowing" environment. Manrope’s geometric nature keeps the app feeling "organized" despite the whimsical backdrop.

**Hierarchy Note:** Always lead with Serif for storytelling and Sans-Serif for data/input. This distinction clearly separates the *experience* from the *utility*.

---

## 4. Elevation & Depth
Depth is achieved through **Tonal Layering** rather than traditional drop shadows.

*   **The Layering Principle:** Place a `surface-container-lowest` (#000000) field inside a `surface-container` (#17172f) to create a "recessed" look for inputs.
*   **Ambient Shadows:** For floating elements like a "New Entry" button, use a shadow color tinted with `on-surface` (#e5e3ff) at 6% opacity. Blur radius must be large (24px–32px) to simulate the soft diffusion of starlight.
*   **The "Ghost Border" Fallback:** If accessibility requires a border, use the `outline-variant` (#46465c) at **15% opacity**. It should be felt, not seen.
*   **Glassmorphism:** Use `secondary-container` (#503d73) with 40% opacity and a heavy backdrop blur for "floating" cards. This allows the background nebula to bleed through, softening the edges of the interface.

---

## 5. Components

### Buttons
*   **Primary:** Gradient of `primary` to `primary-container`. `Roundedness-xl` (1.5rem). High-contrast `on-primary` (#5c4900) text. Subtle shadow of `primary` at 10% opacity.
*   **Secondary (Glass):** Semi-transparent `surface-bright` (#292948) with a 1px "Ghost Border" at 20% opacity.
*   **Tertiary:** `on-surface` text with no container; used for "Cancel" or "Skip."

### Input Fields
*   **Style:** `surface-container-highest` background with `roundedness-md`. 
*   **States:** On focus, the border (outline) should transition to `primary-dim` (#eec540) with a 2px "glow" (outer shadow).
*   **Icons:** Use "glowing" icons—lines are `primary` with a 2px blur duplicate layer underneath to simulate light emission.

### Cards (Dream Entries)
*   **Forbidden:** Divider lines. 
*   **Required:** Use `spacing-6` (2rem) for vertical separation. Cards should use `surface-container-low` with a very subtle gradient (top-left to bottom-right) from `surface-variant` to `surface-container-low`.

### The Progress Nebula (Custom Component)
*   Instead of a flat progress bar, use a gradient-filled track (`secondary-container`) with a glowing "star" (`primary`) as the indicator head.

---

## 6. Do's and Don'ts

### Do
*   **Do** use asymmetrical layouts. Let a moon icon overlap a card boundary by `spacing-2.5`.
*   **Do** use `notoSerif` for any text that is meant to be read as a "story."
*   **Do** use Glassmorphism for overlays to maintain a sense of depth and cosmic "fog."
*   **Do** apply `roundedness-xl` to main containers to keep the UI feeling soft and approachable.

### Don't
*   **Don't** use pure black (#000000) for backgrounds; use `surface` (#0c0c1f) to keep the "Night Sky" tone.
*   **Don't** use 1px solid white borders. They break the immersion.
*   **Don't** crowd the interface. Use the `spacing-8` and `spacing-10` tokens to give elements room to "breathe" in the void.
*   **Don't** use high-saturation reds for errors. Use the `error` token (#ff6e84) which is softened to fit the violet palette.