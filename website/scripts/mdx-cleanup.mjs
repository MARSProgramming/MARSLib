import fs from 'fs/promises';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

async function* walk(dir) {
    const files = await fs.readdir(dir, { withFileTypes: true });
    for (const file of files) {
        const res = path.resolve(dir, file.name);
        if (file.isDirectory()) {
            yield* walk(res);
        } else {
            if (res.endsWith('.mdx')) yield res;
        }
    }
}

async function processFile(filePath) {
    let content = await fs.readFile(filePath, 'utf8');
    const original = content;

    // 1. Remove import statements (from scratch_refactor.js logic)
    content = content.replace(/^import .* from '.*';\r?\n/gm, '');

    // 2. Bold/Italics/Code
    content = content.replace(/<strong>/g, '**').replace(/<\/strong>/g, '**');
    content = content.replace(/<em>/g, '*').replace(/<\/em>/g, '*');
    content = content.replace(/<b>/g, '**').replace(/<\/b>/g, '**');
    content = content.replace(/<i>/g, '*').replace(/<\/i>/g, '*');
    content = content.replace(/<code>/g, '`').replace(/<\/code>/g, '`');

    // 3. Paragraphs - strip and ensure spacing
    content = content.replace(/<p>/g, '\n').replace(/<\/p>/g, '\n');

    // 4. Headings
    content = content.replace(/<h1>(.*?)<\/h1>/g, '# $1\n');
    content = content.replace(/<h2>(.*?)<\/h2>/g, '## $1\n');
    content = content.replace(/<h3>(.*?)<\/h3>/g, '### $1\n');
    content = content.replace(/<h4>(.*?)<\/h4>/g, '#### $1\n');
    content = content.replace(/<h5>(.*?)<\/h5>/g, '##### $1\n');

    // 5. Lists
    content = content.replace(/<ul>/g, '\n').replace(/<\/ul>/g, '\n');
    content = content.replace(/<ol>/g, '\n').replace(/<\/ol>/g, '\n');
    content = content.replace(/<li>(.*?)[ \t]*<\/li>/g, '- $1\n');

    // 6. Breaks and lines
    content = content.replace(/<br\s*\/?>/g, '\n');
    content = content.replace(/<hr\s*\/?>/g, '---\n');

    // 7. Callouts (from scratch_refactor.js logic)
    content = content.replace(/<div(?: class="[^"]+")?\s*>([\s\S]*?)<\/div>/g, (m, inner) => {
        if (inner.includes('class="callout') || m.includes('class="callout')) {
            let innerText = inner.replace(/<h4[^>]*>([^<]+)<\/h4>/, '**$1**\n\n')
                                 .replace(/<p>([\s\S]*?)<\/p>/, '$1')
                                 .trim();
            let type = 'note';
            if (m.includes('callout-warning')) type = 'caution';
            if (m.includes('callout-info')) type = 'note';
            return `:::${type}\n${innerText}\n:::`;
        }
        return m; // unchanged
    });

    // 8. Image tags mapping
    content = content.replace(/<img(.*?)src="(.*?)"(.*?)alt="(.*?)"(.*?)>/g, '![$4]($2)');
    content = content.replace(/<img(.*?)alt="(.*?)"(.*?)src="(.*?)"(.*?)>/g, '![$2]($4)');
    content = content.replace(/<img src="([^"]+)" alt="([^"]+)"[^\/]*\/>/g, '![$2]($1)');

    // 9. Anchor Tags
    content = content.replace(/<a[^>]*href="([^"]+)"[^>]*>([\s\S]*?)<\/a>/g, '[$2]($1)');

    if (original !== content) {
        await fs.writeFile(filePath, content);
        console.log(`Cleaned: ${filePath}`);
    }
}

(async () => {
    try {
        const docsDir = path.join(__dirname, '../src/content/docs');
        for await (const file of walk(docsDir)) {
            await processFile(file);
        }
        console.log("MDX cleanup complete.");
    } catch (e) {
        console.error(e);
    }
})();
