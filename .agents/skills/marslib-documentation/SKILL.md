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
## 5. Docusaurus React Migrations & Simulators
When porting or authoring interactive simulators in the Docusaurus React architecture:
- **Canvas Operations**: Always encapsulate DOM manipulations (<canvas>) inside React .tsx components within the website/src/components/ directory.
- **Hook Architecture**: Use useRef for mutable animation state (score, loop timers) and useEffect with equestAnimationFrame and cleanup logic to prevent React hydration or unmount loop memory leaks.
- **MDX Formatting Strictness**: When injecting React component tags like <SotmSim /> into .mdx files, you must ensure:
  1. The import statement rests at the parent un-indented block level.
  2. A blank line separates the import and the component tag to prevent the Docusaurus Acorn MDX parser from crashing.
  3. External static paths like /javadoc/index.html MUST use Docusaurus explicit bypass routing (e.g. href: 'pathname:///MARSLib/javadoc/index.html') to avoid React Router Single Page Application (SPA) intercepting it as a soft-nav and generating a 404 page.
