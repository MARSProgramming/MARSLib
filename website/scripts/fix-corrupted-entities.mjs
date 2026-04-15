/**
 * Second-pass fix: repairs any remaining corrupted HTML closing tags
 * where `</tagname&gt;` was produced instead of `</tagname>`.
 * Also fixes patterns like `<br/&gt;`.
 */
import { readFileSync, writeFileSync, readdirSync, statSync } from 'fs';
import { join, resolve } from 'path';

const docsDir = resolve('src/content/docs');

function walkDir(dir) {
  const files = [];
  for (const entry of readdirSync(dir)) {
    const full = join(dir, entry);
    if (statSync(full).isDirectory()) {
      files.push(...walkDir(full));
    } else if (full.endsWith('.mdx')) {
      files.push(full);
    }
  }
  return files;
}

let fixed = 0;

for (const file of walkDir(docsDir)) {
  let content = readFileSync(file, 'utf-8');
  const original = content;

  // Fix all remaining &gt; that appear right after a tag name or /
  // Pattern: any HTML-like context where &gt; should be >
  // e.g., </span&gt;  ->  </span>
  // e.g., <br/&gt;    ->  <br/>  (handled by mermaid diagrams)
  content = content.replaceAll('&gt;', '>');

  // But we need to RE-escape any bare > that are followed by digits 
  // (which was the original angle-fix intent) — BUT ONLY outside HTML tags
  const lines = content.split('\n');
  let inCodeBlock = false;

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];

    if (line.trim().startsWith('```')) {
      inCodeBlock = !inCodeBlock;
      continue;
    }
    if (inCodeBlock) continue;

    // Skip lines with HTML/JSX tags (span, br, etc)
    if (/<[a-zA-Z\/]/.test(line)) continue;

    // Re-escape bare >digit and <digit patterns in prose
    let newLine = line;
    newLine = newLine.replace(/<(\d)/g, '&lt;$1');
    newLine = newLine.replace(/>(\d)/g, '&gt;$1');
    newLine = newLine.replace(/< (\d)/g, '&lt; $1');
    newLine = newLine.replace(/> (\d)/g, '&gt; $1');

    if (newLine !== line) {
      lines[i] = newLine;
    }
  }

  content = lines.join('\n');

  if (content !== original) {
    writeFileSync(file, content, 'utf-8');
    fixed++;
    console.log(`FIXED: ${file}`);
  }
}

console.log(`\nDone. Fixed ${fixed} files.`);
