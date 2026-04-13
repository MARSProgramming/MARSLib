---
name: marslib-documentation
description: Helps architect, format, and style the MARSLib educational hub and Github Pages web documentation. Use when adding tutorials, framework feature pages, or updating terminology.
---

# MARSLib Documentation Skill

You are the lead technical writer and web designer for Team MARS 2614. When modifying the `docs/` folder or authoring tutorials:

## 1. 2026 "Fuel Frenzy" Terminology
All tutorials, code comments (when referring to the game strategy), and documentation must strictly use the official 2026 terminology:
- **Game Piece:** Fuel Ball (NOT Note, Ring, or Cube).
- **Scoring Target:** The Hub (NOT Speaker, Amp, or Goal).
- **Endgame Structure:** The Ladder (NOT Stage, Chain, or Climb).

## 2. Premium Dark-Mode Aesthetics
MARSLib documentation isn't just text; it's a "Recruit Training" platform that should WOW the reader. All HTML must adhere to the design system:
- **Background:** `--bg-primary: #0a0a0a`
- **Cards/Containers:** `--bg-card: #1a1a1a`
- **Text:** `--text-primary: #e8e8e8`, `--text-secondary: #999999`
- **Accents:** `--mars-red: #B32416`, `--mars-red-light: #d42e1e`
- **Typography:** Orbitron for Headers (H1, H2, H3) and UI elements. Ubuntu for body text.

## 3. Educational Paradigm
Tutorials should break complex topics down elegantly:
- **Never just paste code.** Provide concise snippets wrapped in `<pre><code>` and explain the *why*, not just the *what*.
- **Use external links.** Always link to official WPILib, AdvantageKit, or PathPlanner documentation at the bottom of the page in a dedicated "External Resources" section.
- **Use GitHub Alerts.** For Pro Tips, use styled `.note` or `.alert` divs with colored borders.

## 4. The Agentic Skill Architecture
When documenting the framework itself, ensure users understand that MARSLib is co-developed alongside Agentic AI. Refer to `.agents/skills` as the "Agentic Skill Architecture" which enforces FRC best practices programmatically.
## 5. Astro "Islands" Migrations & Simulators
When porting or authoring interactive simulators in the Astro Starlight architecture:
- **Canvas Operations**: Always encapsulate DOM manipulations (<canvas>) inside React .tsx components within the `src/components/` directory.
- **Client Hydration**: When injecting React component tags like `<SotmSim client:visible />` into `.mdx` files, you MUST use an Astro client directive (`client:load`, `client:visible`, or `client:idle`) to ensure the interactive logic executes, as Astro ships zero JavaScript by default.
- **Hybrid Deployment**: Rely on Astro's `hybrid` output strictly tied to the `@astrojs/cloudflare` serverless edge adapter. Any Keystatic components like `[...params].ts` will organically handle oauth routes without static bypass hacks.

## 6. Keystatic Navigation Synchronization
Whenever you author new tutorial `.mdx` files inside `src/content/docs/tutorials/*`, do not generate raw files. Use Astro Starlight's sidebar autogeneration by ensuring `sidebar: { order: X }` is populated in the frontmatter, and Keystatic will automatically map it to the Cloudflare UI.
