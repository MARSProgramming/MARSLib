/**
 * Batch-escapes bare `<` and `>` characters in MDX prose (outside code blocks and JSX tags)
 * that break the MDX JSX parser.
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

let totalFixed = 0;

for (const file of walkDir(docsDir)) {
  const content = readFileSync(file, 'utf-8');
  const lines = content.split('\n');
  let inCodeBlock = false;
  let changed = false;
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    
    // Track code blocks
    if (line.trim().startsWith('```')) {
      inCodeBlock = !inCodeBlock;
      continue;
    }
    
    // Skip lines inside code blocks
    if (inCodeBlock) continue;
    
    // Skip frontmatter
    if (i < 10 && line.trim() === '---') continue;
    
    // Skip lines that are JSX component tags (start with < and have a capital letter or /)
    if (/^\s*<[A-Z\/]/.test(line)) continue;
    
    // Find bare < followed by a digit or space (not part of HTML/JSX tags)
    // Pattern: < followed by digit, or < followed by space then digit
    let newLine = line;
    
    // Replace patterns like "<5ms", "< 10ms", "<50%", "< 100MB"
    newLine = newLine.replace(/(<)(\d)/g, (match, lt, digit) => {
      // Make sure this isn't inside a code span (backticks)
      const before = newLine.substring(0, newLine.indexOf(match));
      const backtickCount = (before.match(/`/g) || []).length;
      if (backtickCount % 2 === 1) return match; // inside inline code
      return `&lt;${digit}`;
    });
    
    // Replace patterns like ">10ms", "> 80%"  
    newLine = newLine.replace(/(>)(\d)/g, (match, gt, digit) => {
      const before = newLine.substring(0, newLine.indexOf(match));
      const backtickCount = (before.match(/`/g) || []).length;
      if (backtickCount % 2 === 1) return match;
      return `&gt;${digit}`;
    });
    
    // Also handle "< 5ms" pattern (with space)
    newLine = newLine.replace(/(<) (\d)/g, (match, lt, digit) => {
      const before = newLine.substring(0, newLine.indexOf(match));
      const backtickCount = (before.match(/`/g) || []).length;
      if (backtickCount % 2 === 1) return match;
      return `&lt; ${digit}`;
    });
    
    newLine = newLine.replace(/(>) (\d)/g, (match, gt, digit) => {
      const before = newLine.substring(0, newLine.indexOf(match));
      const backtickCount = (before.match(/`/g) || []).length;
      if (backtickCount % 2 === 1) return match;
      return `&gt; ${digit}`;
    });
    
    if (newLine !== line) {
      lines[i] = newLine;
      changed = true;
    }
  }
  
  if (changed) {
    writeFileSync(file, lines.join('\n'), 'utf-8');
    totalFixed++;
    console.log(`FIXED: ${file}`);
  }
}

console.log(`\nDone. Fixed ${totalFixed} files.`);
