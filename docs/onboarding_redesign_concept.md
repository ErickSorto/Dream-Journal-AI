# DreamNorth Onboarding Concept

## Goal

Create a 3-page onboarding flow that feels cinematic, premium, and emotionally calming before the auth UI appears. The flow should build desire in this order:

1. Make the app feel beautiful and memorable.
2. Make the product feel magical and useful.
3. Learn what the user wants most while increasing buy-in.

The existing visual identity is already strong:

- [onboarding_long.png](/Users/ericksorto/AndroidStudioProjects/Dream-Journal-AI/composeApp/shared/src/commonMain/composeResources/drawable/onboarding_long.png)
- [dream_onboarding_design.png](/Users/ericksorto/AndroidStudioProjects/Dream-Journal-AI/composeApp/shared/src/commonMain/composeResources/drawable/dream_onboarding_design.png)

The redesign should preserve the current DreamNorth mood:

- moonlit lighthouse
- deep indigo sky
- soft magenta and peach glow
- calm ocean reflections
- gentle wonder, not loud sci-fi energy

## Visual Direction

Use a layered dream-seascape style instead of flat onboarding cards:

- Background: deep navy-to-violet sky with drifting nebula haze
- Accent glow: peach, coral, warm moonlight, dusty rose
- Motion: slow parallax, glow bloom, drifting particles, soft scale pulses
- Texture: subtle grain, fog layers, blurred light halos
- Rhythm: slow and luxurious, never snappy or gamified

## Page 1: Enter The Dream

### Why this page exists

This page is the emotional hook. Its job is to make DreamNorth feel like a place, not just an app.

### User feeling

"This is beautiful. I want to stay here."

### Message

**Headline:** A beautiful place to remember your dreams  
**Body:** Capture the dreams that usually fade by morning, inside a journal designed to feel calm, intimate, and worth returning to.

### Art direction

A tall lighthouse scene at night, but closer and more immersive than the current background. The moon glow should spill into the ocean. Fine particles drift upward like dream dust.

### Motion direction

- slow vertical camera drift
- faint stars twinkling at different depths
- lighthouse glow breathing every 4-5 seconds
- subtle reflection shimmer in the water

### Why it builds excitement

It creates aesthetic trust first. If this page lands, users believe the rest of the experience will feel special too.

## Page 2: Watch Dreams Come Alive

### Why this page exists

This page turns beauty into product desire. It shows that DreamNorth does more than store text: it transforms dreams into insight and imagination.

### User feeling

"This could make my dreams feel vivid and meaningful."

### Message

**Headline:** Turn dream fragments into insight  
**Body:** Save a dream, let DreamNorth help categorize it, surface patterns, and transform it into interpretations, moods, and visuals that feel alive.

### Feature emphasis

- auto-categorization
- interpretation
- mood and vividness reflection
- dream art / dream world generation
- voice capture and transcription

### Art direction

Use a layered composition over the seascape:

- floating glass cards with dream tags like `Lucid`, `Recurring`, `Nightmare`
- soft image frame showing a surreal dream scene
- light trails connecting journal entry to insight cards

### Motion direction

- particles spiral gently toward the center
- glass cards rise with staggered fade/blur
- one orbiting glow line connects content to insights
- hero artwork slowly scales and drifts independently from the background

### Why it builds excitement

The user now sees a before/after story: "my dream goes in, something magical comes back out."

## Page 3: What Pulls You In Most?

### Why this page exists

This page collects marketing signal while also making the experience feel personalized. It should feel like DreamNorth is tuning itself to the user.

### User feeling

"This app understands why I’m here."

### Primary question

**What excites you most about DreamNorth?**

### Suggested answer chips

- Understanding what my dreams mean
- Tracking lucid or recurring dreams
- Saving dreams before they fade
- Turning dreams into beautiful images
- Recording dreams with my voice
- Understanding my mood and patterns

### Optional secondary question

**Which sounds most like you right now?**

- I forget my dreams too fast
- I want deeper meaning from my dreams
- I have intense or recurring dreams
- I love creative dream visuals

### Art direction

The final page should feel like the sky opens up:

- brighter moon halo
- slightly warmer horizon
- floating answer chips with soft rim light
- one larger glow behind the selected choice

### Motion direction

- answer chips float on different timing curves
- selected chip emits a quiet ring pulse
- background haze drifts sideways while stars still drift upward

### Why it builds excitement

Asking the right question turns the user from observer into participant. It also gives product and marketing insight without feeling like a survey wall.

## Recommended Animation Language

These effects match the current theme and are realistic in Compose:

- Ambient particle field with glow blur
- Slow parallax sky layers
- Morphing soft blob or haze field behind content
- Staggered glass-card entrance
- Halo pulse around lighthouse or selected chip
- Shooting-star accent used sparingly as a reward beat

Avoid:

- aggressive bounce
- harsh elastic motion
- neon cyberpunk palettes
- carousel-heavy onboarding UI

## Image Prompt Concepts

### Page 1 prompt

Illustrated cinematic night seascape, solitary lighthouse beneath a glowing moon, deep indigo sky with magenta cloud haze, reflective ocean, warm peach light bloom, dreamy painterly style, elegant atmospheric lighting, soft particles, vertical mobile composition, premium calming onboarding artwork

### Page 2 prompt

Dream journal interface floating over a moonlit sea, translucent glass cards showing lucid dream and recurring dream labels, surreal dream artwork glowing in the center, elegant peach and violet light trails, dreamy premium illustration, soft atmospheric haze, vertical mobile onboarding scene

### Page 3 prompt

Moonlit dream sky opening into a warmer horizon, floating illuminated choice chips, gentle halo light around the selected option, premium painterly fantasy seascape, indigo violet coral palette, soft grain, elegant magical calm onboarding illustration, vertical mobile composition

## Animation Research To Reuse

These references fit the aesthetic and translate well into Jetpack Compose:

- MDN `requestAnimationFrame()` guidance for frame-loop structure and time-based motion: [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window/requestAnimationFrame)
- Codrops ambient canvas techniques for offscreen drawing, glow compositing, and particle arrays: [Ambient Canvas Backgrounds](https://tympanus.net/codrops/2018/12/13/ambient-canvas-backgrounds/)
- Codrops organic layered-shape inspiration for soft topographic/morphing backgrounds: [Gradient Topography Animation](https://tympanus.net/codrops/2018/01/24/gradient-topography-animation/)

## Implementation Notes

If we build this next, keep the auth form as the destination state after the 3-page sequence instead of mixing every message into the current single-screen layout. That will make the onboarding feel intentional instead of crowded.
