/**
 * Post-build script that patches the Astro-generated wrangler.json
 * so Cloudflare Pages accepts it as a valid worker configuration.
 *
 * Problem: Astro 6's @astrojs/cloudflare adapter generates dist/server/wrangler.json
 * with `"assets": {"binding": "ASSETS"}`. Cloudflare Pages rejects this because:
 *   1. "ASSETS" is a reserved binding name in Pages (Pages auto-provides it)
 *   2. The file lacks `pages_build_output_dir` which Pages requires for validation
 *
 * Without this patch, Cloudflare deploys as static-only (no SSR worker).
 */
import { readFileSync, writeFileSync, existsSync } from 'node:fs';

const configPath = 'dist/server/wrangler.json';

if (!existsSync(configPath)) {
  console.log('[postbuild] No wrangler.json found, skipping patch.');
  process.exit(0);
}

const config = JSON.parse(readFileSync(configPath, 'utf-8'));

// Remove the reserved ASSETS binding — Pages provides env.ASSETS automatically
delete config.assets;

// Add pages_build_output_dir so Cloudflare validates this as a Pages config
// From dist/server/, the static assets live at ../client
config.pages_build_output_dir = '../client';

writeFileSync(configPath, JSON.stringify(config, null, 2));
console.log('[postbuild] Patched wrangler.json: removed ASSETS binding, added pages_build_output_dir');
