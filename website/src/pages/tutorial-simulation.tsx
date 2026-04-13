import React, { useEffect } from 'react';
import Layout from '@theme/Layout';

export default function TutorialSimulation() {
  return (
    <Layout title="Tutorial Simulation">
      <div className="legacy-mars">
        <div dangerouslySetInnerHTML={{ __html: `



<main class="container" style="padding-top: 100px; padding-bottom: 80px;">
  <div style="text-align: center; margin-bottom: 40px;">
    <a href="/MARSLib/tutorials" class="back-link">← BACK TO TUTORIALS</a>
    <h1>Physics-First Development</h1>
  </div>

  <p>Waiting for a physical robot to be built is the #1 cause of losing FRC competitions. MARSLib uses <strong>Dyn4j</strong>, a 2D physics engine, to create a high-fidelity virtual world where you can develop 100% of your code before the metal is even cut.</p>

  <h2>1. The Physics World</h2>
  <p>The <code>MARSPhysicsWorld</code> is a singleton that manages all rigid bodies on the field (leveraging architectural patterns from <a href="https://shenzhen-robotics-alliance.github.io/maple-sim/rebuilt/" target="_blank" style="color: var(--ai-cyan);">MapleSim</a>). It handles collisions, friction, and even battery voltage sag natively alongside standard <a href="https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/robot-simulation/index.html" style="color: var(--ai-cyan);">WPILib simulation classes</a>.</p>

  <pre><code class="language-java">// In simulationPeriodic()
MARSPhysicsWorld.getInstance().update(0.020);</code></pre>

  <h2>2. Spawning Game Pieces</h2>
  <p>To test an intake or the complex <strong>Ladder</strong> kinematics, you need game pieces in the simulation. You can spawn <strong>Fuel Balls</strong> dynamically into the world. They will bounce off walls and react to your robot's bumpers.</p>

  <pre><code class="language-java">Body fuelBall = new Body();
fuelBall.addFixture(Geometry.createCircle(0.12)); // 24cm diameter
fuelBall.setMass(MassType.NORMAL);
fuelBall.translate(2.0, 2.0); // Meters x,y
MARSPhysicsWorld.getInstance().getDyn4jWorld().addBody(fuelBall);</code></pre>

  <!-- Interactive Physics Sandbox Demo -->
  <div class="simulator-container" style="background:#111; border: 1px solid var(--border); border-radius:12px; padding:20px; margin:40px 0;">
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 20px;">
      <div>
        <h3 style="margin:0; color:white;">Interactive Collision Sandbox</h3>
        <p style="margin:0; font-size:0.9rem; color:var(--text-secondary);">Drag the blue robot chassis to collide with the Fuel Balls. Hit them into the glowing goal on the left!</p>
      </div>
      <button id="resetSimBtn" class="btn btn-primary" style="background:var(--ai-cyan); color:#000; border:none; padding:8px 16px; border-radius:6px; cursor:pointer; font-family:'Orbitron'; font-weight:700;">RESET WORLD</button>
    </div>
    
    <canvas id="physicsCanvas" width="800" height="400" style="width:100%; max-width:800px; aspect-ratio:2/1; height:auto; display:block; margin: 0 auto; background:#050505; border-radius:8px; border: 1px solid #333; cursor: grab; box-shadow: inset 0 0 40px rgba(0,0,0,0.8);"></canvas>
    
    <div style="margin-top: 15px; padding: 10px; background: rgba(0,0,0,0.5); border-radius: 6px; font-family: 'JetBrains Mono', monospace; font-size: 0.85rem; color: #aaa;">
      <div id="simLog">Physics Engine Active: 0 collisions.</div>
    </div>
  </div>

  <h2>3. Subsystem IO-Sim</h2>
  <p>As covered in the IO Layer tutorial, your subsystems should use <code>IOSim</code> implementations. These classes connect WPILib simulators (like <code>ElevatorSim</code>) to the physics world. The simulator provides the mathematical state, while Dyn4j provides the collision bounds.</p>

  <div class="callout callout-warning">
    <h4>Realistic Power Modeling</h4>
    <p>MARSLib doesn't just sim motion; it sims electrical draw. If your elevator hits a mechanical limit and stalls, the <code>MARSPowerManager</code> will calculate the voltage drop, which might cause your "virtual" Rio to brown out! This allows you to tune current limits safely in offline code.</p>
  </div>

  <h2>4. Visualizing with AdvantageScope</h2>
  <p>You can't "see" physics math, so we export all body positions to AdvantageScope. By dragging the <code>PhysicsWorld/GamePieces</code> field into the 3D Field view, you can watch your robot interact with Fuel Balls and the Hub in real-time.</p>

  <div class="callout">
    <h4>Student Pro-Tip</h4>
    <p>Run your PathPlanner autonomous paths in simulation with <strong>Efficiency Mode OFF</strong> to test how physical collisions with obstacle walls might knock your robot off path. If your pathing breaks here, it will definitely break on the real field.</p>
  </div>

  <h2>5. Offline Log Replay and Auto-Tuning</h2>
  <p>Because MARSLib strictly abstracts hardware out with the AdvantageKit IO layer, simulation isn't just about the physics engine—it natively supports <strong>Log Replay</strong>. You can pull an active <code>.wpilog</code> file from a real match and feed it straight back into the robot code on your laptop.</p>

  <ul>
      <li><strong>Automatically Figure Out Constants:</strong> Run a match log through the replay tool, and MARSLib's Continuous SysId will passively process the historical velocity and voltage telemetry.</li>
      <li><strong>Iterative Vision Tuning:</strong> Modify your Megatag thresholds in code, then run replay on an old match log to see improvements in AdvantageScope.</li>
      <li><strong>Post-Mortem Debugging:</strong> If the superstructure gets stuck, replay the log and step through the state machine transitions line by line.</li>
  </ul>

  <pre><code class="language-java">// To run Replay offline, pass the wpilog path to the Robot configuration
Robot.setUseReplayMode(true, "C:/logs/einstein_finals_m1.wpilog");</code></pre>
</main>







` }} />
      </div>
    </Layout>
  );
}
