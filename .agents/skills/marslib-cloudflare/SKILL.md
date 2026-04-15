---
name: marslib-cloudflare
description: Helps deploy the MARSLib documentation hub (Astro 6 + Starlight + Keystatic CMS) to Cloudflare Pages with SSR workers. Use when debugging 404s, fixing build errors, modifying _routes.json, or troubleshooting worker mounting.
---

# MARSLib Cloudflare Deployment Skill

You are the infrastructure engineer for Team MARS 2614. When deploying, debugging, or modifying the Cloudflare Pages deployment for the documentation hub at `website/`:

## 1. Architecture

The MARSLib docs site is an Astro 6 SSR application using `@astrojs/cloudflare` deployed to Cloudflare Pages via Git integration.

| Component | File | Purpose |
|---|---|---|
| Astro Config | `website/astro.config.mjs` | SSR mode, Cloudflare adapter, Starlight, React, Markdoc |
| Wrangler Config | `website/wrangler.toml` | Compatibility date, `nodejs_compat` flag |
| Routes Manifest | `website/public/_routes.json` | Routes `/keystatic/*` to the SSR worker |
| Postbuild Script | `website/scripts/postbuild.mjs` | Reorganizes Astro 6 output for Pages compatibility |
| Keystatic Config | `website/keystatic.config.ts` | CMS collections, GitHub storage mode, branding |
| Keystatic React Island | `website/src/components/KeystaticApp.tsx` | Client-side React component for Keystatic UI |
| Keystatic Route | `website/src/pages/keystatic/[...params].astro` | Physical SSR route override |
| Keystatic API | `website/src/pages/api/keystatic/[...params].ts` | Physical API route override |

### Build Pipeline

```
astro build → dist/client/ + dist/server/
                    ↓ (postbuild.mjs)
              dist/client/_worker.js/   ← SSR worker
              dist/client/index.html    ← static assets
              dist/client/_routes.json  ← routing rules
```

Cloudflare Pages reads `dist/client/` as the build output directory, auto-detects `_worker.js/` as the SSR worker, and reads `_routes.json` for routing.

## 2. Key Rules

### Rule A: NEVER Add `pages_build_output_dir` to `wrangler.toml`
Astro 6's `@cloudflare/vite-plugin` treats any project with `pages_build_output_dir` as a Pages project and validates the auto-generated `.prerender/wrangler.json`. That file contains `"assets": {"binding": "ASSETS"}` which is a **reserved name in Pages projects**, crashing the build with exit code 1. If you violate this, the build will hard-crash with `The name 'ASSETS' is reserved in Pages projects`.

### Rule B: NEVER Use `exclude: ["/*"]` in `_routes.json`
Cloudflare docs state: **"exclude always takes priority over include"** regardless of pattern specificity. A blanket `"exclude": ["/*"]` silently blocks ALL traffic from reaching the SSR worker, even if the URLs match include patterns. If you violate this, the worker compiles and deploys but EVERY route returns a static 404.

### Rule C: The Postbuild Script is Load-Bearing
Astro 6 outputs `dist/server/` + `dist/client/`, but Cloudflare Pages only auto-detects workers from a `_worker.js/` directory inside the build output root. The postbuild script (`scripts/postbuild.mjs`) performs three critical operations:
1. **Copies** `dist/server/*` into `dist/client/_worker.js/`
2. **Creates** `dist/client/_worker.js/index.js` as the entry point
3. **Deletes** `.wrangler/deploy/config.json` (stale redirect to the now-moved `dist/server/`)

If you remove or disable the postbuild script, Cloudflare will deploy as static-only with no SSR worker.

### Rule D: NEVER Use `mode: 'directory'` in the Adapter
The `mode: 'directory'` option in `@astrojs/cloudflare` generates a `functions/` directory structure that Cloudflare Pages' Git integration does not reliably detect. Omit the `mode` property entirely — the default behavior generates `dist/server/entry.mjs` which the postbuild script handles.

### Rule E: NEVER Use the `keystatic()` Astro Integration
The native `keystatic()` integration from `@keystatic/astro` injects dynamic routes that collide with our physical route overrides and confuse the Cloudflare adapter's route generation. Instead, use physical route files at `src/pages/keystatic/[...params].astro` and `src/pages/api/keystatic/[...params].ts`.

### Rule F: The Cloudflare Dashboard Build Output Directory MUST Be `dist/client`
The Cloudflare Pages project settings have the Build Output Directory set to `dist/client`. The postbuild script places the `_worker.js/` directory INSIDE `dist/client/`. If you change the dashboard setting, update the postbuild script to match.

## 3. Adding New SSR Routes

To add a new server-rendered route:

1. Create the route file in `website/src/pages/` (e.g., `src/pages/admin/[...slug].astro`).
2. Add the route pattern to `astro.config.mjs` under `adapter.routes.extend.include`:
   ```js
   routes: { extend: { include: ['/admin', '/admin/*'] } }
   ```
3. Add the same patterns to `website/public/_routes.json` include array:
   ```json
   { "include": ["/admin", "/admin/*"] }
   ```
4. Verify the `_routes.json` has `"exclude": []` — never add blanket excludes.
5. Build locally with `npm run build` and verify the route appears in the prerender output (SSR routes will NOT be listed in the prerendered static routes section).

## 5. Common Deployment Fixes

| Symptom | Cause | Fix |
|---|---|---|
| `ASSETS is reserved in Pages` crash | `pages_build_output_dir` in `wrangler.toml` | Remove the property (Rule A) |
| Worker compiles but all routes 404 | `exclude: ["/*"]` in `_routes.json` | Remove blanket exclude (Rule B) |
| `dist/server/wrangler.json does not exist` | Stale `.wrangler/deploy/config.json` | Postbuild script deletes it (Rule C) |
| `Output directory not found` | Postbuild moved `dist/client/` | Ensure postbuild places `_worker.js` INSIDE `dist/client/` (Rule F) |
| `No functions dir found. Skipping.` | Normal — Pages uses `_worker.js`, not `functions/` | Not an error, ignore this message |
| Keystatic returns blank/error page | `keystatic()` integration injected | Remove integration, use physical routes (Rule E) |
| `wrangler.toml is not valid` warning | Missing `pages_build_output_dir` | Intentional — see Rule A. Cloudflare falls back to dashboard settings |

## 6. Cloudflare KV Database Persistence
For student progress tracking, MARSLib uses a Cloudflare KV namespace called \`MARSLIB_KV\`.
Because Astro is built in \`output: 'server'\` mode, your \`src/pages/api/...\` TypeScript endpoints can natively execute inside the edge worker and access this database.

**To interact with KV:**
```ts
export const POST: APIRoute = async ({ request, locals }) => {
  const kv = locals.runtime.env.MARSLIB_KV;
  // KV is now fully accessible!
  await kv.put("team414_progress", JSON.stringify(data));
}
```
**Important:** Do NOT attempt to import \`@cloudflare/kv-asset-handler\` or connect from normal React components; you MUST pipe fetches through your own Astro API endpoints.

## 7. Key Files Reference

- `website/wrangler.toml` — Minimal: name, compat date, `nodejs_compat` only
- `website/public/_routes.json` — Include-only list for worker routing
- `website/scripts/postbuild.mjs` — Must run after `astro build` via `&&` chain
- `website/package.json` — Build script: `"astro build && node scripts/postbuild.mjs"`
- `website/keystatic.config.ts` — CMS config with `storage: { kind: 'github' }`
- `website/src/components/KeystaticApp.tsx` — React island for CMS UI
