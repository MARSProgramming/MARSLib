---
name: marslib-documentation
description: Helps architect, format, and style the MARSLib educational hub and Github Pages web documentation. Use when adding tutorials, framework feature pages, or updating terminology.
---

# MARSLib Documentation Skill

You are the lead technical writer and web designer for Team MARS 2614. When modifying the `docs/` folder or authoring tutorials:

## 0. FIRST Core Values Orientation
The MARSLib documentation ecosystem is a platform for **Discovery** and **Innovation**. Whenever authoring new content, you must naturally weave the _FIRST_® Core Values into the narrative:
- **Discovery & Innovation**: Highlight the creative persistence required to solve complex FRC problems (e.g., zero-allocation logic).
- **Impact & Teamwork**: Explain how MARSLib features empower the entire team to have a greater impact on the field.
- **Inclusion & Fun**: Ensure the language is welcoming to all skill levels and celebrates technical successes.

## 0.1 Branding & Naming Compliance
As a registered team, we must respect the official **_FIRST_®** branding guidelines. Whenever you mention the organization or the competition:
- **Always** write it as `_FIRST_®` (italics, all caps, registered trademark).
- Refer to the project's [BRANDING.md](file:///c:/Users/david/dev/robotics/frc/MARSLib/BRANDING.md) for full lockup and logo guidelines.
- Failure to adhere to these standards is a violation of the framework's "Rule 0".

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

## 3. Tutorial Header Numbering & Formatting Restrictions
To achieve the "Cool Number Box" look for tutorial sections, wrap the header number in a `<span class="mars-num">` tag. All numbered headers (`##` or `###`) MUST use this class:
```html
<h2><span class="mars-num">1</span> How to use it</h2>
```
This is a standard across our documentation hub to provide a "Team MARS" premium feel.

**Double Header Avoidance:**
Do NOT append an `<h1>` or `# Title` directly beneath the frontmatter block. Starlight natively pulls the `title` frontmatter configuration and renders it as the page's singular `<h1>` document tag. Manually typing it out creates a duplicated headline block.

## 4. Educational Paradigm
Tutorials should break complex topics down elegantly to foster **Discovery**:
- **Never just paste code.** Provide concise snippets wrapped in `<pre><code>` and explain the *why*, not just the *what*.
- **Use external links.** Always link to official WPILib, AdvantageKit, or PathPlanner documentation at the bottom of the page in a dedicated "External Resources" section to encourage further **Impact**.
- **Use GitHub Alerts.** For Pro Tips, use styled `.note` or `.alert` divs with colored borders to highlight **Innovation**.
- **Active Engagement**: End tutorials with an invitation for **Teamwork** (e.g., "Join us on Discord/Discussions to share your results!").

## 4. The Agentic Skill Architecture
When documenting the framework itself, ensure users understand that MARSLib is co-developed alongside Agentic AI. Refer to `.agents/skills` as the "Agentic Skill Architecture" which enforces FRC best practices programmatically.
## 5. Semantic File Architecture
Do NOT dump files into a generic "core-concepts" or "tutorials" folder. You must strictly file documentation according to its semantic domain:
- **`framework-architecture/`**: For abstract, underlying structural concepts (e.g., `io-layer`, `state-machines`, `zero-allocation`, `fault-resilience`).
- **`subsystems/`**: For concrete, tangible robot features (e.g., `swerve`, `vision`, `elevators`, `control-theory`, `pathfinding`).
- **`getting-started/`**: For installation, vs-code setups, and WPILib migrations.
- **`contributing/`**: For standards, PR tests, and accessibility commitments.
- **`reference/`**: For controller mappings and glossaries.

## 6. Astro "Islands" Migrations & Simulators
When porting or authoring interactive simulators in the Astro Starlight architecture:
- **Canvas Operations**: Always encapsulate DOM manipulations (<canvas>) inside React .tsx components within the `src/components/` directory.
- **Client Hydration**: When injecting React component tags like `<SotmSim client:visible />` into `.mdx` files, you MUST use an Astro client directive (`client:load`, `client:visible`, or `client:idle`) to ensure the interactive logic executes, as Astro ships zero JavaScript by default.
- **Hybrid Deployment**: Rely on Astro's `hybrid` output strictly tied to the `@astrojs/cloudflare` serverless edge adapter. Any Keystatic components like `[...params].ts` will organically handle oauth routes without static bypass hacks.
- **MDX-JSX Compiler Crashes**: ALL Markdown files (`.mdx`) MUST escape `<` characters (e.g. `&lt;`) when used outside of explicit HTML/React tags (e.g., when typing "Less than 60% CPU usage", write `&lt; 60%`). Unescaped less-than operators cause Astro to throw fatal JSX parsing errors.

## 6. Keystatic Navigation Synchronization
Whenever you author new tutorial `.mdx` files inside `src/content/docs/tutorials/*`, do not generate raw files. Use Astro Starlight's sidebar autogeneration by ensuring `sidebar: { order: X }` is populated in the frontmatter, and Keystatic will automatically map it to the Cloudflare UI.

## 7. Keystatic Frontmatter & MDX Schema
Because we use `@keystatic/core` to enforce content architecture, every `.mdx` file MUST strictly adhere to our custom `starlightSchema` frontmatter. Missing or malformed frontmatter will break the CMS and Astro build:

```yaml
---
title: "Your Tutorial Title"
description: "A short multi-line summary of the educational topic."
id: "unique-string-id" # Required
template: "doc" # 'doc' or 'splash'
sidebar:
  order: 1
  label: "Short UI Label"
---
```

Additionally, Keystatic natively injects custom React wrapper components via `keystatic.config.ts`. You MUST NOT use standard markdown blockquotes for rules; instead, utilize these custom components organically in the MDX body (no imports necessary):
- `<RuleSection num="X" title="Title">...</RuleSection>`
- `<CodeComparison>...</CodeComparison>` (for before/after code blocks)
- `<CodeViolation>...</CodeViolation>`
- `<CodeStandard>...</CodeStandard>`
- For interactive React simulations, use the injected components directly: `<ArmKgSim client:visible />`, `<PhysicsSim client:visible />`, `<SwerveSim client:visible />`, etc.

**CRITICAL: Markdoc HTML Parsing Limitations**
When injecting raw HTML tags (like `<span>` or `<div>`) into Keystatic-managed MDX files, Keystatic's Markdoc parser will crash with `Missing component definition` unless the tag is explicitly registered in `keystatic.config.ts` under the `components` block using `mark()` or `inline()`. Always ensure any new HTML tags you introduce to the markdown are whitelisted in the CMS schema first.
