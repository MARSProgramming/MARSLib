import React, { useEffect } from 'react';
import Layout from '@theme/Layout';

export default function TutorialSotm() {
  return (
    <Layout title="Tutorial Sotm">
      <div className="legacy-mars">
        <div dangerouslySetInnerHTML={{ __html: `



<main class="container" style="padding-top: 100px; padding-bottom: 80px;">
  <div style="text-align: center; margin-bottom: 40px;">
    <a href="/MARSLib/tutorials" class="back-link">← BACK TO TUTORIALS</a>
    <h1>Elite Shooting (SOTM)</h1>
  </div>

  <p>Standing still to score is a 2010 strategy. In the modern 2026 <strong>REBUILT</strong> era, "Shot-On-The-Move" (SOTM) is a requirement for Einstein-level play. MARSLib includes a physics-based iterative solver that accounts for chassis velocity, time-of-flight, and aerodynamics using high-frequency MegaTag 2.0 odometry.</p>

  <h2>1. The Core Problem</h2>
  <p>If your robot is driving sideways at 4 meters per second and you shoot, the <strong>Fuel Ball</strong> inherits that sideways velocity. To hit the <strong>Hub</strong>, you must aim "behind" where you want it to go, effectively leading the target.</p>

  <div class="callout">
    <h4>Vector Interception</h4>
    <p>MARSLib doesn't just use a simple lookup table. It computes a 3D vector interception where <code>V_shot = V_required - V_chassis</code>. It then solves for the required pitch and RPM to make that resulting vector hit the Hub center.</p>
  </div>

  <h2>2. Tuning the Distance Map</h2>
  <p>The first step is building a sparse lookup table of "Perfect Shots". Park your robot at various distances and find the RPM and Cowl Angle that consistently score Fuel Balls.</p>

  <pre><code class="language-java">public static final InterpolatingDoubleTreeMap RPM_MAP = new InterpolatingDoubleTreeMap();
static {
  RPM_MAP.put(1.0, 2500.0);
  RPM_MAP.put(3.0, 3200.0);
  RPM_MAP.put(5.0, 4500.0);
}</code></pre>

  <h2>3. Predictive SOTM & Latency</h2>
  <p>Because the shot reaches the target at a future time, we must account for where the robot <em>will be</em> when the ball finishes its flight. MARSLib uses a <strong>Recursive Iterative Solver</strong>:</p>
  <ol>
    <li>Guess the time of flight (e.g., 0.5s).</li>
    <li>Calculate where the robot will be in 0.5s.</li>
    <li>Calculate the required shot for that future position.</li>
    <li>Recalculate the time of flight and repeat until convergence (usually 3 iterations).</li>
  </ol>

  <!-- Interactive SOTM Solver Demo -->
  <div class="simulator-container">
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 20px;">
      <div>
        <h3 style="margin:0; color:white;">Interactive SOTM Solver</h3>
        <p style="margin:0; font-size:0.9rem; color:var(--text-secondary);">Visualizing Iterative Convergence & Vector Interception</p>
      </div>
    </div>
    
    <canvas id="sotmCanvas" width="800" height="400" style="width:100%; max-width:800px; aspect-ratio:2/1; height:auto; display:block; margin: 0 auto; background:#0a0a0a; border-radius:8px; border: 1px solid #333; cursor: crosshair;"></canvas>
    
    <div style="display:flex; gap:20px; margin-top:20px; flex-wrap: wrap;">
      <div style="flex:1; min-width:200px;">
        <label style="color:var(--text-secondary); font-size:0.9rem;">Robot Velocity: <span id="velDisplay" style="color:var(--ai-cyan);">4.0 m/s</span></label>
        <input type="range" id="botVelocity" min="0" max="8" value="4" step="0.1" autocomplete="off" style="width:100%;">
      </div>
      <div style="flex:1; min-width:200px;">
        <label style="color:var(--text-secondary); font-size:0.9rem;">Robot Heading: <span id="headDisplay" style="color:var(--ai-cyan);">0&deg;</span></label>
        <input type="range" id="botHeading" min="-180" max="180" value="0" step="1" autocomplete="off" style="width:100%;">
      </div>
      <div style="flex:1; min-width:200px;">
        <label style="color:var(--text-secondary); font-size:0.9rem;">Muzzle Velocity: <span id="shotDisplay" style="color:var(--ai-cyan);">15.0 m/s</span></label>
        <input type="range" id="shotSpeed" min="5" max="30" value="15" step="0.5" autocomplete="off" style="width:100%;">
      </div>
    </div>
    
    <div style="margin-top: 15px; padding: 10px; background: rgba(0,0,0,0.5); border-radius: 6px; font-family: 'JetBrains Mono', monospace; font-size: 0.85rem; color: #aaa;">
      <div id="solverLog">Drag your mouse on the canvas to move the robot.</div>
    </div>
  </div>

  <h2>4. Aerodynamic Compensation</h2>
  <p>Fuel balls have significant air resistance and Magnus lift (if they have spin). In <code>EliteShooterMath.java</code>, you can adjust the <code>DRAG_COEFFICIENT</code> and <code>MAGNUS_CONSTANT</code>.</p>
  <ol>
    <li>Shoot at a distant target while standing still.</li>
    <li>If the ball consistently drops short, increase the <code>DRAG_COEFFICIENT</code>.</li>
    <li>If the ball curves upwards too much, decrease the <code>BACKSPIN_RATIO</code>.</li>
  </ol>
</main>







` }} />
      </div>
    </Layout>
  );
}
