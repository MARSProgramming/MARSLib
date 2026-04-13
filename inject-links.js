const fs = require('fs');
const path = require('path');

const tutorialsDir = path.join(__dirname, 'website', 'docs', 'tutorials');

const resources = {
  // Cluster 1: Zero-to-Hero & Setup
  cluster1: `
  <hr style={{ margin: '40px 0' }} />
  <h2>📖 Further Reading & External Resources</h2>
  <ul>
    <li><a href="https://docs.wpilib.org/en/stable/docs/zero-to-robot/introduction.html">WPILib "Zero to Robot" Guide</a> - The official foundational pipeline for FRC control systems.</li>
    <li><a href="https://www.youtube.com/watch?v=8319J1BEHwM">FRC 0 to Auto Youtube Series</a> - Outstanding video tutorials exploring command-based programming for novices.</li>
    <li><a href="https://docs.wpilib.org/en/stable/docs/software/commandbased/index.html">WPILib Command-Based Documentation</a> - Deep dive into Subsystem and Command scheduling architecture.</li>
  </ul>
  `,
  // Cluster 2: Telemetry, CI, IO
  cluster2: `
  <hr style={{ margin: '40px 0' }} />
  <h2>📖 Further Reading & External Resources</h2>
  <ul>
    <li><a href="https://www.youtube.com/watch?v=yYn9NqJ6kXU">FRC Log Replay and Simulation</a> - Mechanical Advantage's definitive 2024 Championship presentation on the why behind the IO-Layer abstraction.</li>
    <li><a href="https://github.com/Mechanical-Advantage/AdvantageKit">AdvantageKit Architecture</a> - The core repository governing deterministic replay loops on the RIO.</li>
    <li><a href="https://github.com/Mechanical-Advantage/AdvantageScope">AdvantageScope Documentation</a> - Visualizing 3D logs natively in real-time.</li>
  </ul>
  `,
  // Cluster 3: Swerve, PID, System ID
  cluster3: `
  <hr style={{ margin: '40px 0' }} />
  <h2>📖 Further Reading & External Resources</h2>
  <ul>
    <li><a href="https://v6.docs.ctr-electronics.com/en/stable/docs/api-reference/api-usage/control-requests.html">CTRE Phoenix 6 Control Requests</a> - Explaining 250Hz frequency CANivore utilization.</li>
    <li><a href="https://pathplanner.dev/">PathPlanner Documentation</a> - Standardized GUI usage for creating 2D autonomous splines.</li>
    <li><a href="https://docs.wpilib.org/en/stable/docs/software/advanced-controls/system-identification/index.html">WPILib System Identification (SysId)</a> - Advanced mathematical breakdowns of Quasistatic friction and Dynamic OLS regression curves.</li>
    <li><a href="https://github.com/Team364/BaseFalconSwerve">Team 364 BaseFalconSwerve</a> - The historic FRC architecture that inspired modern template geometries.</li>
  </ul>
  `,
  // Cluster 4: Vision & Faults
  cluster4: `
  <hr style={{ margin: '40px 0' }} />
  <h2>📖 Further Reading & External Resources</h2>
  <ul>
    <li><a href="https://www.youtube.com/watch?v=_eP941oXGow">Limelight MegaTag 2.0 Breakdown</a> - Understanding how FRC 1690 style IMU yaw-seeding rejects rapid AprilTag noise variations.</li>
    <li><a href="https://docs.photonvision.org/en/latest/">PhotonVision Hardware Guide</a> - Best practices for illuminating targets globally.</li>
  </ul>
  `
};

const mappings = {
  'setup/getting-started.mdx': resources.cluster1,
  'zero-to-hero/01-frc-landscape.mdx': resources.cluster1,
  'zero-to-hero/02-hardware-basics.mdx': resources.cluster1,
  'zero-to-hero/03-command-based.mdx': resources.cluster1,
  'setup/telemetry.mdx': resources.cluster2,
  'framework/io-layer.mdx': resources.cluster2,
  'elite/uploader.mdx': resources.cluster2,
  'testing.mdx': resources.cluster2,
  'elite/zero-allocation.mdx': resources.cluster2,
  'framework/zero-allocation.mdx': resources.cluster2,
  'framework/swerve.mdx': resources.cluster3,
  'framework/pathfinding.mdx': resources.cluster3,
  'framework/control-theory.mdx': resources.cluster3,
  'framework/sysid.mdx': resources.cluster3,
  'framework/sotm.mdx': resources.cluster3,
  'zero-to-hero/04-control-basics.mdx': resources.cluster3,
  'framework/vision.mdx': resources.cluster4,
  'framework/fault-resilience.mdx': resources.cluster4,
  'framework/architecture.mdx': resources.cluster4
};

function processFiles(dir) {
  const files = fs.readdirSync(dir);
  
  for (const file of files) {
    const fullPath = path.join(dir, file);
    if (fs.statSync(fullPath).isDirectory()) {
      processFiles(fullPath);
    } else if (file.endsWith('.mdx')) {
      const relPath = path.relative(tutorialsDir, fullPath).replace(/\\/g, '/');
      let content = fs.readFileSync(fullPath, 'utf8');
      
      // If we already added a Further Reading block, skip to prevent duplicates
      if (content.includes('<h2>📖 Further Reading')) {
        console.log("Skipping " + relPath + " - Block already exists.");
        continue;
      }

      let appendBlock = '';
      if (mappings[relPath]) {
        appendBlock = mappings[relPath];
      } else {
        // Default to WPILib docs if unmapped
        appendBlock = `
  <hr style={{ margin: '40px 0' }} />
  <h2>📖 Further Reading & External Resources</h2>
  <ul>
    <li><a href="https://docs.wpilib.org/en/stable/">WPILib Official Documentation</a> - The definitive baseline resource.</li>
  </ul>
  `;
      }

      // Inject right before </main>
      if (content.includes('</main>')) {
        content = content.replace('</main>', appendBlock + '</main>');
        fs.writeFileSync(fullPath, content, 'utf8');
        console.log("Injected block into " + relPath);
      } else {
        console.log("Warning: No </main> tag found in " + relPath + ", skipping injection.");
      }
    }
  }
}

console.log("Starting Link Injection...");
processFiles(tutorialsDir);
console.log("Done.");
