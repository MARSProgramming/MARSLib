/**
 * Post-build script that reorganizes Astro 6's output into the layout
 * Cloudflare Pages expects:
 *
 *   dist/              ← static assets (from dist/client/)
 *   dist/_worker.js    ← server worker entry (from dist/server/)
 *
 * Astro 6 outputs dist/client/ and dist/server/, but Cloudflare Pages
 * only auto-detects workers from a _worker.js file at the build output root.
 */
import { cpSync, mkdirSync, rmSync, existsSync, readdirSync, renameSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';

const dist = 'dist';
const clientDir = join(dist, 'client');
const serverDir = join(dist, 'server');
const workerDir = join(dist, '_worker.js');

if (!existsSync(clientDir) || !existsSync(serverDir)) {
  console.log('[postbuild] Expected dist/client and dist/server not found, skipping.');
  process.exit(0);
}

// Step 1: Copy server files into dist/_worker.js/
console.log('[postbuild] Creating _worker.js directory from server output...');
mkdirSync(workerDir, { recursive: true });
cpSync(serverDir, workerDir, { recursive: true });

// Step 2: Create the _worker.js entry that Cloudflare expects
// Cloudflare Pages looks for _worker.js/index.js as the entry point
writeFileSync(join(workerDir, 'index.js'), `export { default } from './entry.mjs';\n`);

// Step 3: Move client assets to dist root (Cloudflare serves static from build output root)
console.log('[postbuild] Moving client assets to dist root...');
for (const entry of readdirSync(clientDir)) {
  const src = join(clientDir, entry);
  const dest = join(dist, entry);
  // Don't overwrite _worker.js or the server/client dirs
  if (entry === '_worker.js' || entry === 'server' || entry === 'client') continue;
  if (existsSync(dest)) rmSync(dest, { recursive: true });
  renameSync(src, dest);
}

// Step 4: Clean up original directories
rmSync(clientDir, { recursive: true });
rmSync(serverDir, { recursive: true });

console.log('[postbuild] Reorganized output:');
console.log('  dist/           ← static assets');
console.log('  dist/_worker.js ← server worker');
console.log('[postbuild] Done!');
