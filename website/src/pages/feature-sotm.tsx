import React, { useEffect } from 'react';
import Layout from '@theme/Layout';

export default function FeatureSotm() {
  return (
    <Layout title="Feature Sotm">
      <div className="legacy-mars">
        <div dangerouslySetInnerHTML={{ __html: `



<main class="container" style="padding-top: 100px; padding-bottom: 80px;">
  <div style="text-align: center; margin-bottom: 40px;">
    <a href="/MARSLib/#features" class="back-link">← BACK TO FEATURES</a>
    <h1>Shot-On-The-Move Solver</h1>
  </div>
  
  <div class="card" style="background: var(--bg-card); border: 1px solid var(--border); padding: 30px; border-radius: 12px; margin-top: 30px;">
    <h2 style="font-family: 'Orbitron', sans-serif; color: var(--mars-red-light); margin-bottom: 20px; font-size: 1.8rem; margin-top: 0;">Dynamic Target Interception</h2>
    <p style="margin-bottom: 16px; color: var(--text-secondary); font-size: 1.1rem;">A modern swerve drive loses immense offensive capability if it has to come to a complete stop to line up a shot. We've brought military-grade interception math into FRC to ensure the robot can score a hub shot while pulling rapid maneuvers.</p>
    
    <div id="sim-container" style="width: 100%; height: 350px; background: #050505; border: 1px solid var(--border); border-radius: 12px; margin: 30px 0; position: relative; overflow: hidden; box-shadow: inset 0 0 40px rgba(0,0,0,0.8);">
      <canvas id="sotmCanvas" width="800" height="350"></canvas>
      <div style="position: absolute; top: 15px; right: 15px; font-family: 'Orbitron'; font-size: 0.75rem; color: var(--mars-red-light); letter-spacing: 0.1em; background: rgba(179,36,22,0.1); padding: 4px 12px; border-radius: 12px; border: 1px solid rgba(179,36,22,0.3);">LIVE KINEMATICS</div>
    </div>
    
    
    
    <h2 style="font-family: 'Orbitron', sans-serif; color: var(--mars-red-light); margin-bottom: 20px; font-size: 1.8rem; margin-top: 40px;">The Math Engine</h2>
    <p style="margin-bottom: 16px; color: var(--text-secondary); font-size: 1.1rem;">MARSLib's <code>EliteShooterMath</code> iteratively solves the physics problem of moving targets:</p>
    <ul style="margin-bottom: 20px; padding-left: 20px; color: var(--text-secondary); font-size: 1.1rem;">
      <li style="margin-bottom: 10px;"><strong>Time-of-Flight Iteration:</strong> Because gravity is parabolic, you can't just aim where the goal is now. We iteratively estimate the time the projectile will spend in the air based on a tunable map of flyband speeds, then calculate where the robot <i>will be</i> relative to the goal when the piece hits it.</li>
      <li style="margin-bottom: 10px;"><strong>Swerve Velocity Compensation:</strong> The math calculates the lateral velocity of our chassis during the shot, transforming our chassis speeds to field-centric vectors to apply compensatory angles to the turret and hood.</li>
      <li style="margin-bottom: 10px;"><strong>Magnus Lift Adjustments:</strong> Our flywheels apply high backward spin to game pieces. This alters their parabolic arc in midair due to the Magnus effect. MARSLib compensates for this aerodynamic lift allowing precise shots from long ranges.</li>
    </ul>

    <h2 style="font-family: 'Orbitron', sans-serif; color: var(--mars-red-light); margin-bottom: 20px; font-size: 1.8rem; margin-top: 40px;">Instant Feedback</h2>
    <p style="margin-bottom: 16px; color: var(--text-secondary); font-size: 1.1rem;">Through AdvantageKit, our SOTM solver visualizes the predicted trajectory of the released fuel ball live in 3D, allowing programmers to tune the map accurately against real-world video without guessing.</p>
  </div>
</main>




` }} />
      </div>
    </Layout>
  );
}
