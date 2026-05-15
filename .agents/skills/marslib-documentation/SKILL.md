---
name: marslib-documentation
description: Helps architect and style the MARSLib educational hub (Astro/Starlight/Keystatic). Use when writing tutorials, formatting MDX, configuring CMS, or managing web accessibility.
---

# MARSLib Documentation Skill

You are the lead technical writer for Team MARS 2614. When modifying `docs/` or `website/`:

## 1. Branding & Core Values

- **_FIRST_®**: Always write as `_FIRST_®` (italics, all caps, trademark). In JSX props that don't parse Markdown, use `<i>FIRST</i>®`.
- **Core Values**: Weave Discovery, Innovation, Impact, Teamwork, Inclusion, and Fun into narratives naturally.
- **2026 Terminology**: Fuel Ball (not Note/Ring), The Hub (not Speaker), The Ladder (not Stage).

## 2. MDX & Keystatic Constraints

### Frontmatter Title Styling
Starlight's frontmatter cannot parse Markdown/HTML in titles. To stylize:
```mdx
---
title: "FIRST® Core Values"
head:
  - tag: style
    content: "h1#\_top { display: none !important; }"
---

<h1><i>FIRST</i>® Core Values</h1>
```

### Component Injection Failures
If a React component throws `ReferenceError: X is not defined` in MDX:
- Add to `astro.config.mjs` inject string
- Restart dev server

### Keystatic Invisible Pages
If a new docs folder doesn't appear in CMS:
- Add explicit collection in `keystatic.config.ts`
- Map to `ui.navigation` array

### Branding in React Props
Use `[FIRST]` token instead of `_FIRST_®` in component props (e.g., `<RuleSection title="[FIRST] Standards">`). The UI component regex-replaces this.

### Unescaped Less-Than Operators
ALL `.mdx` files MUST escape `<` as `&lt;` outside HTML/React tags. Unescaped `<` causes fatal JSX parsing errors.

## 3. File Architecture

Place content semantically:
- `framework-architecture/` — Abstractions (IO layers, state machines)
- `subsystems/` — Concrete features (swerve, vision, elevators)
- `getting-started/` — Installation, setup, migrations
- `contributing/` — Standards, PR tests, accessibility
- `reference/` — Controller mappings, glossaries

## 4. Starlight Directives & Hydration

- Use `client:load`, `client:visible`, or `client:idle` for React components in MDX
- Astro ships zero JS by default — interactive components need directives

## 5. Keystatic Frontmatter Schema

Every `.mdx` MUST have:
```yaml
---
title: "Tutorial Title"
description: "Multi-line summary."
id: "unique-string-id"
template: "doc"  # 'doc' or 'splash'
sidebar:
  order: 1
  label: "UI Label"
---
```

### Custom React Components (No Imports Needed)
- `<RuleSection num="X" title="Title">...</RuleSection>`
- `<CodeComparison>...</CodeComparison>`
- `<CodeViolation>...</CodeViolation>`
- `<CodeStandard>...</CodeStandard>`
- Interactive sims: `<ArmKgSim client:visible />`, `<PhysicsSim client:visible />`, etc.

### Markdoc HTML Tags
New HTML tags in MDX MUST be registered in `keystatic.config.ts` under `components` using `mark()` or `inline()`, or Keystatic's parser crashes.

## 6. Accessibility (WCAG 2.1 AA)

- **Semantic HTML**: Prefer `<button>`, `<dialog>`, `<nav>` over `<div>` with ARIA
- **Keyboard**: All interactive elements reachable via Tab; visible `:focus-visible` states
- **ARIA**: `aria-hidden="true"` on decorative icons; descriptive labels for functional icons
- **Contrast**: Minimum 4.5:1 for text, 3:1 for large text; use cyan (`#00f2ff`) on dark, not red (`#ef4435`)
- **Forms**: Every `<input>`/`<select>` MUST have associated `<label>` via `for`/`id`
- **Canvas**: Use `<canvas role="img" aria-label="Interactive simulation of...">`

### Testing
- CI runs Pa11y via `.github/workflows/a11y.yml` (requires `--no-sandbox`)
- Validate with WebAIM WAVE
- Ensure "Skip to content" link has valid anchor target

## 7. Dark Mode & Theming

Starlight uses `data-theme` attribute. Custom CSS must use:
- `:root[data-theme='dark']` for global custom properties
- `var(--sl-color-text)` or transparent fallbacks (no hardcoded black/white)

## 8. Tutorial Header Numbering

Wrap section numbers in `<span class="mars-num">`:
```html
<h2><span class="mars-num">1</span> How to use it</h2>
```

**DO NOT** append `<h1>` below frontmatter — Starlight auto-generates it from `title`, causing duplicate headlines.

## 9. Educational Style

- **Never just paste code** — explain the *why*
- Use external links to WPILib/AdvantageKit docs
- Use GitHub Alerts (`.note`/`.alert` divs) for Pro Tips
- End with **Teamwork** invitations (Discord/Discussions)
- Use `<pre><code>` for snippets, not bare code blocks

## 10. Agentic Skill Architecture

Document `.agents/skills/` as the "Agentic Skill Architecture" that enforces FRC best practices programmatically.
