import React, { useEffect } from 'react';
import Layout from '@theme/Layout';

export default function FeatureSimulation() {
  return (
    <Layout title="Feature Simulation">
      <div className="legacy-mars">
        <div dangerouslySetInnerHTML={{ __html: `



<main class="container" style="padding-top: 100px; padding-bottom: 80px;">
  <div style="text-align: center; margin-bottom: 40px;">
    <a href="/MARSLib/#features" class="back-link">← BACK TO FEATURES</a>
    <h1>Dyn4j Physics Simulation</h1>
  </div>
  
  <div class="card" style="background: var(--bg-card); border: 1px solid var(--border); padding: 30px; border-radius: 12px; margin-top: 30px;">
    <h2 style="font-family: 'Orbitron', sans-serif; color: var(--mars-red-light); margin-bottom: 20px; font-size: 1.8rem; margin-top: 0;">Why Simulate with Dyn4j?</h2>
    <p style="margin-bottom: 16px; color: var(--text-secondary); font-size: 1.1rem;">Standard WPILib simulation uses 1D kinematics, which are great for simple mechanisms, but fail to capture the chaotic reality of an FRC field. Hardware development time is precious—having a high-fidelity virtual robot allows the software team to develop and tune complete autonomous routines before the mechanical team has even finished the drivetrain.</p>
    
    <h2 style="font-family: 'Orbitron', sans-serif; color: var(--mars-red-light); margin-bottom: 20px; font-size: 1.8rem; margin-top: 40px;">MARSLib's Integration</h2>
    <p style="margin-bottom: 16px; color: var(--text-secondary); font-size: 1.1rem;">MARSLib deeply natively integrates the <code>dyn4j</code> 2D physics engine. The \`IO\` layer pattern abstracts the physical robot hardware into simulated counterparts:</p>
    <ul style="margin-bottom: 20px; padding-left: 20px; color: var(--text-secondary); font-size: 1.1rem;">
      <li style="margin-bottom: 10px;"><strong>Rigid Bodies & Collisions:</strong> The robot bumpers, game pieces (Fuel Balls, etc.), and field elements (like the Hub or Ladder) are constructed as rigid and soft bodies. They bounce, spin, and collide realistically.</li>
      <li style="margin-bottom: 10px;"><strong>Intake Mechanics:</strong> Our simulated intakes apply actual physics forces to game piece geometry to pull them inward. If you miss your alignment, the game piece bounces away—just like real life.</li>
      <li style="margin-bottom: 10px;"><strong>Center of Gravity Modeling:</strong> The robot's mass and angular inertia are modeled. If you accelerate a heavy elevator to the top and hit the brakes, the simulated robot will tip and its odometry will reflect the disturbance.</li>
    </ul>

    <h2 style="font-family: 'Orbitron', sans-serif; color: var(--mars-red-light); margin-bottom: 20px; font-size: 1.8rem; margin-top: 40px;">Virtual Testing & AdvantageScope</h2>
    <p style="margin-bottom: 16px; color: var(--text-secondary); font-size: 1.1rem;">By simulating in dyn4j, we instantly get 3D rendered poses for AdvantageScope, making debugging autonomous paths, vision pose fusion, and collision avoidance incredibly intuitive and fast. Core parts of our simulation architectural logic are derived from the excellent work done by the <a href="https://shenzhen-robotics-alliance.github.io/maple-sim/rebuilt/" target="_blank" style="color: var(--mars-red-light);">MapleSim</a> project.</p>
  </div>
</main>




` }} />
      </div>
    </Layout>
  );
}
