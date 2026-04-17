---
name: marslib-mdx-formatting
description: Helps resolve complex MDX, Starlight, and Keystatic formatting issues. Use when debugging hydration errors, component undefined crashes, branding italics in titles, and Keystatic collection visibility.
---

# MARSLib MDX Formatting Skill

When authoring or modifying MARSLib's Astro Starlight documentation hub, there are several strict architectural constraints involving MDX, React components, and the Keystatic CMS.

If you encounter formatting, parsing, or visibility issues, follow these rules:

## 1. Frontmatter Markdown Limitations & Title Overrides
Astro Starlight's frontmatter cannot natively parse Markdown or HTML (e.g., `title: "_FIRST_® Core Values"` will literally render underscores instead of italics, and `title: "<i>FIRST</i>"` will render the tags as text in the `<title>` head).

**The Standard Fix:**
To stylize the main page title (H1) while preserving the clean metadata title, you must manually hide Starlight's default H1 tag via CSS injection in the frontmatter, and then write your own HTML H1 at the top of the body:

```mdx
---
title: "FIRST® Core Values"
description: "Discover our mission."
head:
  - tag: style
    content: "h1#\\_top { display: none !important; }"
---

<h1><i>FIRST</i>® Core Values</h1>

Your content begins here...
```
*(Note: You must escape the underscore in `h1#\_top` inside the YAML string or it may parse incorrectly).*

## 2. Branding Tokens in React Props
The official framework requires strict formatting for **_FIRST_®** (italics, all caps, registered trademark). However, passing markdown or HTML into simple React string props (like `<RuleSection title="_FIRST_®" />`) will break the MDX hydration or syntax tree.

**The Standard Fix:**
Use the designated `[FIRST]` string token. The internal UI components (like `RuleSection`) are programmed to regex-replace this token with the properly stylized HTML element without breaking the AST.

```mdx
<!-- Correct -->
<RuleSection num="01" title="Adhering to [FIRST] Standards">

<!-- Incorrect (will crash MDX or render poorly) -->
<RuleSection num="01" title="Adhering to _FIRST_® Standards">
```

## 3. "Component Not Defined" / Auto-Inject Crashes
If a new React component works in a standard `.tsx` file but throws a `ReferenceError: X is not defined` when used inside an `.mdx` file, it means the component is not being injected into the Markdown context.

**The Standard Fix:**
We utilize an MDX auto-inject plugin. Update `website/astro.config.mjs` to include your new component in the `inject` definition list:
```javascript
// website/astro.config.mjs
const inject = `
  // ... other components
  import { YourNewComponent } from '${components}/YourNewComponent.tsx';
`;
```
After adding it to the inject string, restart the dev server to verify it compiles.

## 4. Keystatic CMS Invisible Pages
If a new folder is created in `website/src/content/docs/` (e.g., `docs/subsystems/`), but the pages inside do not appear in the `http://localhost:4321/keystatic` dashboard, it is because Keystatic's `path` glob natively does not recurse (e.g., `docs/*` only matches the root).

**The Standard Fix:**
You must explicitly declare a new collection for that directory inside `website/keystatic.config.ts`:
1. Add a new explicit collection (e.g., `path: 'website/src/content/docs/subsystems/*'`).
2. Map that collection into the `ui.navigation` array in the config so it appears in the CMS sidebar.
3. Don't forget to define the new React components inside the `starlightSchema.body.components` block if they need to be editable via Keystatic.
