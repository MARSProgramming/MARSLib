// Client-side Mermaid initialization for legacy HTML pages using
// <pre class="mermaid"> blocks injected via dangerouslySetInnerHTML.
import ExecutionEnvironment from '@docusaurus/ExecutionEnvironment';

if (ExecutionEnvironment.canUseDOM) {
  // Wait for the Mermaid CDN script to load, then initialize
  const initMermaid = () => {
    if (typeof (window as any).mermaid !== 'undefined') {
      (window as any).mermaid.initialize({
        startOnLoad: false,
        theme: 'dark',
        themeVariables: {
          primaryColor: '#B32416',
          primaryTextColor: '#e8e8e8',
          lineColor: '#999',
          secondaryColor: '#1a1a1a',
          tertiaryColor: '#141414',
        },
      });
      (window as any).mermaid.run();
    } else {
      // Mermaid CDN hasn't loaded yet, retry after a short delay
      setTimeout(initMermaid, 200);
    }
  };

  // Run on initial load and on every client-side navigation
  if (document.readyState === 'complete') {
    initMermaid();
  } else {
    window.addEventListener('load', initMermaid);
  }

  // Re-run on Docusaurus client-side route changes
  const observer = new MutationObserver(() => {
    const mermaidBlocks = document.querySelectorAll('pre.mermaid:not([data-processed])');
    if (mermaidBlocks.length > 0) {
      initMermaid();
    }
  });

  observer.observe(document.body, { childList: true, subtree: true });
}
