const searchIndex = [
  { title: "Odometry & Vision Fusion", url: "tutorial-vision.html", keywords: "megatag vision apriltag odometry pose estimation swervedriveposeestimator" },
  { title: "Swerve Kinematics", url: "tutorial-swerve.html", keywords: "swerve drive kinematics chassis speeds rotation module steering heading" },
  { title: "Zero-Allocation Concepts", url: "tutorial-zero-allocation.html", keywords: "garbage collection allocation gc memory realtime struct latency determinism" },
  { title: "Power Shedding & Faults", url: "tutorial-power-shedding.html", keywords: "fault alert brownout voltage current stator supply limit shed drops" },
  { title: "SysId Tuning", url: "tutorial-sysid.html", keywords: "sysid tuning quasistatic dynamic identification PID feedforward kv ka ks rx" },
  { title: "Shoot-On-The-Move (SOTM)", url: "tutorial-sotm.html", keywords: "sotm shoot on the move kinematics interpolation aiming turret latency compensation solver" },
  { title: "Simulation & Physics", url: "tutorial-simulation.html", keywords: "dyn4j physics simulation bump collisions hitboxes virtual inertia field" },
  { title: "State Machines", url: "tutorial-state-machines.html", keywords: "state machine superstructure fsm transition requirements actions coordination" },
  { title: "Telemetry & Logging", url: "tutorial-telemetry.html", keywords: "advantagekit logging telemetry scope json replay real sim nt4 coprocessor" },
  { title: "Testing (JUnit)", url: "tutorial-testing.html", keywords: "junit tests coverage branch mock assertions exception throw bounds test" }
];

document.addEventListener('DOMContentLoaded', () => {
  const searchInput = document.getElementById('search-input');
  const searchResults = document.getElementById('search-results');

  if (!searchInput || !searchResults) return;

  searchInput.addEventListener('input', (e) => {
    const query = e.target.value.toLowerCase().trim();
    searchResults.innerHTML = '';
    
    if (query.length < 2) {
      searchResults.style.display = 'none';
      return;
    }

    const matches = searchIndex.filter(item => 
      item.title.toLowerCase().includes(query) || 
      item.keywords.includes(query)
    );

    if (matches.length > 0) {
      matches.forEach(match => {
        const li = document.createElement('li');
        const a = document.createElement('a');
        a.href = match.url;
        a.textContent = match.title;
        li.appendChild(a);
        searchResults.appendChild(li);
      });
      searchResults.style.display = 'block';
    } else {
      searchResults.innerHTML = '<li><span style="color:var(--text-muted); padding: 8px 16px;">No results found...</span></li>';
      searchResults.style.display = 'block';
    }
  });

  // Close results when clicking outside
  document.addEventListener('click', (e) => {
    if (!e.target.closest('.search-container')) {
      searchResults.style.display = 'none';
    }
  });
});
