// Copy button script for code blocks
(function() {
  'use strict';

  function addCopyButtons() {
    // Find all pre tags with code
    const codeBlocks = document.querySelectorAll('pre > code');

    codeBlocks.forEach((codeBlock) => {
      const pre = codeBlock.parentElement;

      // Skip if already has copy button
      if (pre.querySelector('.copy-button')) return;

      // Get the code content
      const code = codeBlock.textContent || '';

      // Create copy button
      const copyButton = document.createElement('button');
      copyButton.className = 'copy-button';
      copyButton.setAttribute('aria-label', 'Copy to clipboard');
      copyButton.innerHTML = `
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
          <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
        </svg>
      `;

      // Add click handler
      copyButton.addEventListener('click', async () => {
        try {
          await navigator.clipboard.writeText(code);

          // Show success state
          copyButton.classList.add('copied');
          copyButton.innerHTML = `
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="20 6 9 17 4 12"></polyline>
            </svg>
          `;

          setTimeout(() => {
            copyButton.classList.remove('copied');
            copyButton.innerHTML = `
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
              </svg>
            `;
          }, 2000);
        } catch (err) {
          // Show error state
          copyButton.classList.add('error');
          copyButton.setAttribute('aria-label', 'Failed to copy');

          setTimeout(() => {
            copyButton.classList.remove('error');
          }, 2000);
        }
      });

      // Position pre element
      pre.style.position = 'relative';
      pre.appendChild(copyButton);
    });
  }

  // Add buttons when DOM is ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', addCopyButtons);
  } else {
    addCopyButtons();
  }

  // Add buttons after page transitions (for SPA navigation)
  const observer = new MutationObserver(() => {
    addCopyButtons();
  });

  observer.observe(document.body, {
    childList: true,
    subtree: true
  });
})();
