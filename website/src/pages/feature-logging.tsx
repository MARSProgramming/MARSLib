import React, { useEffect } from 'react';
import Layout from '@theme/Layout';

export default function FeatureLogging() {
  return (
    <Layout title="Feature Logging">
      <div className="legacy-mars">
        <div dangerouslySetInnerHTML={{ __html: `



<main class="container" style="padding-top: 100px; padding-bottom: 80px;">
  <div style="text-align: center; margin-bottom: 40px;">
    <a href="/MARSLib/#features" class="back-link">← BACK TO FEATURES</a>
    <h1>AdvantageKit Logging</h1>
  </div>
  
  <div class="card" style="background: var(--bg-card); border: 1px solid var(--border); padding: 30px; border-radius: 12px; margin-top: 30px;">
    <h2 style="font-family: 'Orbitron', sans-serif; color: var(--mars-red-light); margin-bottom: 20px; font-size: 1.8rem; margin-top: 0;">The Value of Determinism</h2>
    <p style="margin-bottom: 16px; color: var(--text-secondary); font-size: 1.1rem;">When an issue happens on the field at a competition, "I couldn't replicate it in the pits" is an unacceptable answer. With AdvantageKit, MARSLib logs all sensor inputs exactly as they enter the code. This means we can plug a thumb drive into the robot, download the log file, and completely "replay" the match step-by-step through our codebase in the pits.</p>
    
    <h2 style="font-family: 'Orbitron', sans-serif; color: var(--mars-red-light); margin-bottom: 20px; font-size: 1.8rem; margin-top: 40px;">The IO Layer Pattern</h2>
    <p style="margin-bottom: 16px; color: var(--text-secondary); font-size: 1.1rem;">We decouple robot logic from hardware calls using an advanced Dependency Injection pattern:</p>
    <ul style="margin-bottom: 20px; padding-left: 20px; color: var(--text-secondary); font-size: 1.1rem;">
      <li style="margin-bottom: 10px;"><strong>Hardware Segregation:</strong> Subsystems never instantiate a \`TalonFX\` or \`SparkMax\` directly. Instead, they accept an \`IO\` interface (e.g., \`SwerveModuleIO\`).</li>
      <li style="margin-bottom: 10px;"><strong>Input Recording:</strong> Every loop, the hardware passes an \`AutoLoggedInputs\` object into the subsystem. This data is recorded to the USB drive.</li>
      <li style="margin-bottom: 10px;"><strong>Replay Mode:</strong> During Replay Mode, the hardware is disconnected. AdvantageKit reads the inputs from the log file and feeds them into the exact same subsystem logic, allowing us to find logic faults deterministically.</li>
    </ul>

    <h2 style="font-family: 'Orbitron', sans-serif; color: var(--mars-red-light); margin-bottom: 20px; font-size: 1.8rem; margin-top: 40px;">AdvantageScope Synergy</h2>
    <p style="margin-bottom: 16px; color: var(--text-secondary); font-size: 1.1rem;">Our AdvantageKit implementation is intricately mapped to AdvantageScope. Every subsystem exports 3D \`Pose3d\` points, allowing us to instantly view 3D renders of our swerve modules, elevator positions, shooter angles, and game piece trajectories synchronized to video footage.</p>
  </div>
</main>




` }} />
      </div>
    </Layout>
  );
}
