const fs = require('fs');
const path = require('path');

const targetDir = path.join(__dirname, 'src', 'pages', 'keystatic');
const targetFile = path.join(targetDir, '[...params].astro');
const targetApiDir = path.join(__dirname, 'src', 'pages', 'api', 'keystatic');
const targetApiFile = path.join(targetApiDir, '[...params].ts');

try {
  if (!fs.existsSync(targetDir)) {
    fs.mkdirSync(targetDir, { recursive: true });
  }
  if (!fs.existsSync(targetApiDir)) {
    fs.mkdirSync(targetApiDir, { recursive: true });
  }

  const astroContent = `---
import { makePage } from '@keystatic/astro/ui';
import keystaticConfig from '../../../keystatic.config';

export const all = makePage(keystaticConfig);
---`;

  const apiContent = `import { makeAPIRoute } from '@keystatic/astro/api';
import keystaticConfig from '../../../../keystatic.config';

export const all = makeAPIRoute(keystaticConfig);`;

  fs.writeFileSync(targetFile, astroContent);
  fs.writeFileSync(targetApiFile, apiContent);
  console.log('Successfully injected Keystatic dashboard for local development mode.');
} catch (e) {
  console.error('Failed to inject Keystatic dashboard: ', e);
}
