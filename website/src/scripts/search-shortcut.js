// Keyboard shortcut for search (Cmd+K / Ctrl+K)
(function() {
  'use strict';

  document.addEventListener('keydown', (e) => {
    // Check for Cmd+K (Mac) or Ctrl+K (Windows/Linux)
    if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
      e.preventDefault();

      // Find and click the search button
      const searchButton = document.querySelector('.search-button');
      if (searchButton && typeof searchButton.click === 'function') {
        searchButton.click();
      }
    }
  });
})();
