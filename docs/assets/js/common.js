/* ===== MARSLib Shared Navigation & Footer Injection ===== */

document.addEventListener('DOMContentLoaded', () => {
  injectHeader();
  injectFooter();
  injectStars();
});

function injectHeader() {
  const headerPlaceholder = document.getElementById('header-placeholder');
  if (!headerPlaceholder) return;

  const editUrl = getEditUrl();
  const nav = document.createElement('nav');
  nav.innerHTML = `
    <div class="container">
      <a href="index.html" class="nav-brand">
        <img src="assets/mars-logo.png" alt="MARS Logo">
        <div class="nav-logo">MARS<span>Lib</span></div>
      </a>
      <ul class="nav-links">
        <li><a href="index.html" class="nav-link-text">Home</a></li>
        <li><a href="index.html#features" class="nav-link-text">Features</a></li>
        <li><a href="tutorials.html" class="nav-link-text">Tutorials</a></li>
        <li><a href="standards.html" class="nav-link-text">Standards</a></li>
        <li class="nav-search">
          <input type="text" id="search-input" class="search-input" placeholder="Search MARSLib...">
          <ul id="search-results" class="search-results"></ul>
        </li>
        <li><a href="${editUrl}" target="_blank" class="nav-edit">Edit</a></li>
        <li><a href="javadoc/index.html" target="_blank" class="nav-cta">API Docs</a></li>
      </ul>
    </div>
  `;
  headerPlaceholder.replaceWith(nav);
  
  // Highlight active page or hash
  highlightActiveLink();
}

function injectFooter() {
  const footerPlaceholder = document.getElementById('footer-placeholder');
  if (!footerPlaceholder) return;

  const editUrl = getEditUrl();
  const footer = document.createElement('footer');
  footer.innerHTML = `
    <div class="container">
      <div style="margin-bottom: 20px;">
        <img src="assets/mars-logo.png" alt="MARS" style="height: 60px; border-radius: 50%; opacity: 0.8;">
      </div>
      <ul class="footer-links">
        <li><a href="javadoc/index.html">API Docs</a></li>
        <li><a href="https://github.com/MARSProgramming/MARSLib">GitHub</a></li>
        <li><a href="${editUrl}">Wiki Edit</a></li>
        <li><a href="https://www.thebluealliance.com/team/2614">TBA Profile</a></li>
        <li><a href="https://marsfirst.org">marsfirst.org</a></li>
        <li><a href="https://www.firsthalloffame.org">Hall of Fame</a></li>
        <li><a href="CONTROLLER_MAPPINGS.html">Controller Mappings</a></li>
      </ul>
      <p>&copy; 2026 Mountaineer Area RoboticS &mdash; FRC Team 2614 &mdash; FIRST Hall of Fame Class of 2017</p>
      <p style="margin-top: 8px; font-size: 0.8rem;">Built with AdvantageKit, Dyn4j, and PathPlanner.</p>
    </div>
  `;
  footerPlaceholder.replaceWith(footer);
}

function getEditUrl() {
  const repoRoot = "https://github.com/MARSProgramming/MARSLib/edit/master/docs/";
  let path = window.location.pathname.split('/').pop();
  if (!path || path === "" || path === "docs") path = "index.html";
  return repoRoot + path;
}

function injectStars() {
  const starsContainer = document.createElement('div');
  starsContainer.className = 'stars';
  document.body.prepend(starsContainer);
}

function highlightActiveLink() {
  const currentPath = window.location.pathname.split('/').pop() || 'index.html';
  const links = document.querySelectorAll('.nav-links a');
  
  links.forEach(link => {
    const href = link.getAttribute('href');
    if (href === currentPath || (currentPath === 'index.html' && href.startsWith('index.html#'))) {
      link.classList.add('active'); // CSS should handle .active if needed
    }
  });
}

/* ===== Site Search Logic ===== */
const searchIndex = [
  { title: "Home", url: "index.html", snippet: "MARSLib software framework features and components" },
  { title: "Features: AdvantageKit", url: "index.html#features", snippet: "Strict dependency injection, replay telemetry, and isolated hardware IO layers." },
  { title: "Features: Dyn4j Simulation", url: "index.html#features", snippet: "High-fidelity 2D physics integration modeling game pieces, mass, and rigid collisions." },
  { title: "Features: SOTM Kinematics", url: "index.html#features", snippet: "Continuous velocity-compensated shoot-on-the-move solver across all ranges." },
  { title: "Core Design Standards", url: "standards.html", snippet: "Zero-allocation physics, mathematical contracts, deterministic memory, and architectural laws." },
  { title: "Tutorial: Environment Setup", url: "tutorial-getting-started.html", snippet: "Installing WPILib, AdvantageScope, and configuring the dev container for MARSLib." },
  { title: "Tutorial: Subsystem IO Layers", url: "tutorial-sotm.html", snippet: "Abstracting hardware logic to support dual real/simulated backend rendering." },
  { title: "Tutorial: PathPlanner & Auto", url: "tutorial-ci.html", snippet: "Automated regression testing, GitHub Actions, Jenkins, and path trajectory configuration." },
  { title: "Tutorial: Zero-Allocation Structs", url: "tutorial-zero-allocation.html", snippet: "Eliminating JVM Garbage Collector spikes from the 20ms hot-path using ephemeral proxy references." }
];

document.addEventListener('DOMContentLoaded', () => {
  const searchInput = document.getElementById('search-input');
  const searchResults = document.getElementById('search-results');
  if (!searchInput || !searchResults) return;

  searchInput.addEventListener('input', (e) => {
    const term = e.target.value.toLowerCase().trim();
    if (term.length < 2) {
      searchResults.classList.remove('active');
      return;
    }
    
    const matches = searchIndex.filter(item => 
      item.title.toLowerCase().includes(term) || item.snippet.toLowerCase().includes(term)
    );
    
    searchResults.innerHTML = '';
    if (matches.length > 0) {
      matches.forEach(m => {
        const li = document.createElement('li');
        li.innerHTML = `<a href="${m.url}">${m.title}<span>${m.snippet}</span></a>`;
        searchResults.appendChild(li);
      });
      searchResults.classList.add('active');
    } else {
      searchResults.innerHTML = '<li><a href="#">No results found<span>Try a different search term</span></a></li>';
      searchResults.classList.add('active');
    }
  });

  document.addEventListener('click', (e) => {
    if (!e.target.closest('.nav-search')) {
      searchResults.classList.remove('active');
    }
  });
});
