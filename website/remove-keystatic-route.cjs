const fs = require('fs');
const path = require('path');

const targetDir = path.join(__dirname, 'src', 'pages', 'keystatic');

try {
  if (fs.existsSync(targetDir)) {
    fs.rmSync(targetDir, { recursive: true, force: true });
    console.log('Successfully detached Keystatic dashboard for static production build.');
  }
} catch (e) {
  console.error('Failed to detach Keystatic dashboard: ', e);
}
