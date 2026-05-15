---
name: marslib-cloudflare
description: Helps deploy MARSLib documentation hub (Astro 6 + Starlight + Keystatic) to Cloudflare Pages with SSR workers. Use when debugging 404s, build errors, or worker mounting.
---

# MARSLib Cloudflare Deployment

## 1. Architecture

| Component | File | Purpose |
|---|---|---|
| Astro Config | `website/astro.config.mjs` | SSR mode, Cloudflare adapter, Starlight |
| Wrangler Config | `website/wrangler.toml` | Compatibility date, `nodejs_compat` |
| Routes Manifest | `website/public/_routes.json` | Routes `/keystatic/*` to SSR worker |
| Postbuild Script | `website/scripts/postbuild.mjs` | Reorganizes Astro 6 output for Pages |
| Keystatic Config | `website/keystatic.config.ts` | CMS collections, GitHub storage |
| Keystatic React Island | `website/src/components/KeystaticApp.tsx` | Client-side CMS UI |
| Keystatic Routes | `website/src/pages/keystatic/[...params].astro` | Physical SSR route override |

### Build Pipeline
```
astro build → dist/client/ + dist/server/
                    ↓ (postbuild.mjs)
              dist/client/_worker.js/   ← SSR worker
              dist/client/_routes.json  ← routing rules
```

## 2. Critical Rules (DO NOT VIOLATE)

| Rule | What Happens if Violated | Fix |
|---|---|---|
| **NEVER add `pages_build_output_dir` to `wrangler.toml`** | Crash: `ASSETS is reserved in Pages projects` | Remove property |
| **NEVER use `exclude: ["/*"]` in `_routes.json`** | All routes return 404 (worker never hit) | Use include-only, or `"exclude": []` |
| **NEVER disable postbuild script** | No SSR worker deployed | Must run after `astro build` |
| **NEVER use `mode: 'directory'` in adapter** | Functions not detected by Pages Git integration | Omit `mode` property |
| **NEVER use `keystatic()` integration** | Dynamic routes collide, adapter confused | Use physical route files |
| **Build output directory MUST be `dist/client`** | Worker not found in dashboard | Ensure postbuild places `_worker.js` inside `dist/client/` |

## 3. Adding New SSR Routes

1. Create route in `website/src/pages/` (e.g., `src/pages/admin/[...slug].astro`)
2. Add to `astro.config.mjs`: `routes: { extend: { include: ['/admin', '/admin/*'] } }`
3. Add to `website/public/_routes.json`: `{ "include": ["/admin", "/admin/*"] }`
4. Build locally: `npm run build`
5. Verify route appears in SSR output (not in prerendered static list)

## 4. Common Deployment Fixes

| Symptom | Cause | Fix |
|---|---|---|
| `ASSETS is reserved` | `pages_build_output_dir` in wrangler.toml | Remove property |
| Worker compiles, all routes 404 | `exclude: ["/*"]` in _routes.json | Remove blanket exclude |
| `dist/server/wrangler.json not found` | Stale `.wrangler/deploy/config.json` | Postbuild deletes it |
| `No functions dir found` | Normal — Pages uses `_worker.js`, not `functions/` | Ignore message |
| Keystatic blank/error page | `keystatic()` integration injected | Remove integration, use physical routes |

## 5. Cloudflare KV Database

MARSLib uses `MARSLIB_KV` namespace for student progress tracking.

**To interact:**
```ts
export const POST: APIRoute = async ({ request, locals }) => {
  const kv = locals.runtime.env.MARSLIB_KV;
  await kv.put("team414_progress", JSON.stringify(data));
}
```

**DO NOT** import `@cloudflare/kv-asset-handler` or connect from React components — pipe through Astro API endpoints.

## 6. Key Files

- `website/wrangler.toml` — name, compat date, `nodejs_compat` only
- `website/public/_routes.json` — include-only list for worker routing
- `website/scripts/postbuild.mjs` — runs after astro build via `&&` chain
- `website/package.json` — build script: `"astro build && node scripts/postbuild.mjs"`
- `website/keystatic.config.ts` — CMS with `storage: { kind: 'github' }`
