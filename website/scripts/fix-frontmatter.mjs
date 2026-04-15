/**
 * Batch fixes ALL MDX frontmatter for Starlight schema compliance.
 * Ensures every .mdx file has a top-level `title` field.
 * Moves title from sidebar nesting if needed, or extracts from first H1.
 * Removes invalid Starlight keys: `id`, `template`.
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
  const content = readFileSync(file, 'utf-8');

  if (!content.startsWith('---')) continue;
  const fmEnd = content.indexOf('---', 3);
  if (fmEnd === -1) continue;

  const frontmatter = content.slice(3, fmEnd).trim();
  const body = content.slice(fmEnd + 3);
  const lines = frontmatter.split('\n');

  // Check if title already exists at top level
  const hasTopLevelTitle = lines.some(l => /^title\s*:/.test(l));
  // Check for invalid keys
  const hasInvalidId = lines.some(l => /^id\s*:/.test(l));
  const hasInvalidTemplate = lines.some(l => /^template\s*:/.test(l));

  if (hasTopLevelTitle && !hasInvalidId && !hasInvalidTemplate) continue;

  let title = null;
  let description = null;
  let sidebarOrder = null;
  let sidebarLabel = null;
  const cleanLines = [];
  let inSidebar = false;
  let sidebarIndent = -1;

  for (const line of lines) {
    // Detect sidebar block
    if (/^sidebar\s*:/.test(line)) {
      inSidebar = true;
      sidebarIndent = 0;
      continue;
    }

    if (inSidebar) {
      // Check if still in sidebar (indented)
      const indent = line.match(/^(\s+)/);
      if (indent || line.trim() === '') {
        const trimmed = line.trim();
        if (trimmed === '') continue;

        const titleMatch = trimmed.match(/^title\s*:\s*["']?(.+?)["']?\s*$/);
        const descMatch = trimmed.match(/^description\s*:\s*["']?(.+?)["']?\s*$/);
        const orderMatch = trimmed.match(/^order\s*:\s*(\d+)/);
        const labelMatch = trimmed.match(/^label\s*:\s*["']?(.+?)["']?\s*$/);

        if (titleMatch) title = titleMatch[1];
        else if (descMatch) description = descMatch[1];
        else if (orderMatch) sidebarOrder = orderMatch[1];
        else if (labelMatch) sidebarLabel = labelMatch[1];
        // Skip id, template, badge — not needed at sidebar level
        continue;
      } else {
        inSidebar = false;
      }
    }

    // Skip invalid top-level keys
    if (/^id\s*:/.test(line)) continue;
    if (/^template\s*:/.test(line)) continue;

    // Capture existing top-level title/description if present
    const topTitleMatch = line.match(/^title\s*:\s*["']?(.+?)["']?\s*$/);
    if (topTitleMatch) {
      title = topTitleMatch[1];
      cleanLines.push(line); // keep it
      continue;
    }

    const topDescMatch = line.match(/^description\s*:\s*["']?(.+?)["']?\s*$/);
    if (topDescMatch) {
      description = topDescMatch[1];
      cleanLines.push(line); // keep it
      continue;
    }

    cleanLines.push(line);
  }

  // If no title found, try first H1 in body
  if (!title) {
    const h1Match = body.match(/^#\s+(.+)/m);
    if (h1Match) {
      title = h1Match[1].trim();
    } else {
      console.log(`SKIP (no title): ${file}`);
      continue;
    }
  }

  // Rebuild frontmatter
  const newFmLines = [];

  // Ensure title is always first
  if (!cleanLines.some(l => /^title\s*:/.test(l))) {
    newFmLines.push(`title: "${title}"`);
  }
  
  // Add description if not already present
  if (description && !cleanLines.some(l => /^description\s*:/.test(l))) {
    newFmLines.push(`description: "${description}"`);
  }

  // Add existing clean lines
  for (const line of cleanLines) {
    if (line.trim()) {
      newFmLines.push(line);
    }
  }

  // Add sidebar block if we had order or label
  if (sidebarOrder !== null || sidebarLabel !== null) {
    // Check if sidebar already exists in cleanLines
    if (!newFmLines.some(l => /^sidebar\s*:/.test(l))) {
      newFmLines.push('sidebar:');
      if (sidebarOrder !== null) newFmLines.push(`  order: ${sidebarOrder}`);
      if (sidebarLabel !== null) newFmLines.push(`  label: "${sidebarLabel}"`);
    }
  }

  const newContent = `---\n${newFmLines.join('\n')}\n---${body}`;
  writeFileSync(file, newContent, 'utf-8');
  fixed++;
  console.log(`FIXED: ${file}`);
}

console.log(`\nDone. Fixed ${fixed} files.`);
