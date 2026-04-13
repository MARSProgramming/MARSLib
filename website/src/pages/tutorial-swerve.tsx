import React, { useEffect } from 'react';
import Layout from '@theme/Layout';

export default function TutorialSwerve() {
  return (
    <Layout title="Tutorial Swerve">
      <div className="legacy-mars">
        <div dangerouslySetInnerHTML={{ __html: `



<main class="container" style="padding-top: 100px; padding-bottom: 80px;">
  <div style="text-align: center; margin-bottom: 40px;">
    <a href="/MARSLib/tutorials" class="back-link">← BACK TO TUTORIALS</a>
    <h1>High-Frequency Swerve</h1>
  </div>

  <p>A standard FRC robot executes its control loop every 20ms (50Hz). This is fine for moving an elevator, but it is <strong>unacceptably slow</strong> for Swerve Odometry. If a robot moving at 5 m/s only calculates its position 50 times a second, the math inherently creates a "drift" of several centimeters per second.</p>

  <p>MARSLib solves this by utilizing the <strong>PhoenixOdometryThread</strong>.</p>

  <h2>1. The 250Hz Thread</h2>
  <p>Instead of relying on the main RoboRIO thread, our swerve architecture spins up an isolated background thread that communicates directly with the CTRE CANivore bus. This thread calculates the robot's physical kinematics at 250Hz.</p>

  <div class="callout callout-warning">
    <h4>Zero-Allocation (GC) Danger</h4>
    <p>Because this thread runs 250 times a second, if you instantiate an object inside this loop, you will instantly crash the RoboRIO due to massive Garbage Collection pressure. This is why our Agentic Skills strictly enforce the <strong>Ephemeral Struct</strong> proxy architecture across the Swerve subsystem.</p>
  </div>

  <div id="swerve-sim" style="background: #050505; border: 1px solid var(--border); border-radius: 12px; margin: 30px 0; display: flex; flex-direction: column; overflow: hidden; box-shadow: inset 0 0 40px rgba(0,0,0,0.8);">
    <div style="display: flex; gap: 20px; padding: 16px 20px; background: rgba(255,255,255,0.03); border-bottom: 1px solid var(--border); align-items: center; justify-content: space-between; flex-wrap: wrap;">
      <div style="display: flex; gap: 24px; flex-wrap: wrap; width: 100%;">
        <label style="color:#e8e8e8; font-family:'JetBrains Mono'; font-size:12px; display: flex; flex-direction: column; gap:4px; flex: 1; min-width: 150px;">
          <span>Vx (Forward/Back) <span id="vx-val" style="color:var(--ai-cyan)">0.0</span> m/s</span>
          <input type="range" id="vx" min="-5" max="5" step="0.1" value="0.0">
        </label>
        <label style="color:#e8e8e8; font-family:'JetBrains Mono'; font-size:12px; display: flex; flex-direction: column; gap:4px; flex: 1; min-width: 150px;">
          <span>Vy (Left/Right) <span id="vy-val" style="color:var(--ai-cyan)">0.0</span> m/s</span>
          <input type="range" id="vy" min="-5" max="5" step="0.1" value="0.0">
        </label>
        <label style="color:#e8e8e8; font-family:'JetBrains Mono'; font-size:12px; display: flex; flex-direction: column; gap:4px; flex: 1; min-width: 150px;">
          <span>Omega (Rotation) <span id="omega-val" style="color:var(--mars-red-light)">0.0</span> rad/s</span>
          <input type="range" id="omega" min="-5" max="5" step="0.1" value="0.0">
        </label>
        <button id="resetSwerve" style="background:#222; color:#fff; border:1px solid #444; padding:8px 16px; border-radius:6px; cursor:pointer; font-family:'Orbitron'; font-weight:700; height: fit-content; align-self: flex-end;">ZERO</button>
      </div>
    </div>
    <div style="display: flex; justify-content: center; align-items: center; padding: 20px;">
      <div style="position:relative;">
        <canvas id="swerveCanvas" width="400" height="400" style="background: #111; border: 1px solid #333; border-radius: 6px; max-width: 100%; height: auto;"></canvas>
        <div style="position:absolute; top:10px; right:10px; font-family:'Orbitron'; font-size:10px; color:var(--mars-red-light); letter-spacing: 1px;">KINEMATICS SOLVER</div>
      </div>
    </div>
  </div>

  <h2>2. MegaTag 2.0 Integration</h2>
  <p>Limelight 4's MegaTag 2.0 system relies heavily on the IMU Yaw rates to compensate for camera latency. Because we process the Pigeon 2.0 data at 250Hz, our gyro-seeding to the Limelight is incredibly precise, allowing for flawless localization even while spinning and traversing the Bump.</p>

  <pre><code class="language-java">// Asynchronously injecting vision without locking the thread
public void addVisionMeasurement(Pose2d visionPose, double timestamp, Matrix<N3, N1> stdDevs) {
    poseEstimator.addVisionMeasurement(visionPose, timestamp, stdDevs);
}</code></pre>

  <h2>3. Heading-Aware Traction Control</h2>
  <p>With high-frequency data, we can implement micro-adjustments in real-time. If the IMU detects that the robot is physically slipping or skidding, MARSLib's traction control algorithm will temporarily limit the torque current applied to the Kraken X60 drive motors to regain grip.</p>
</main>







` }} />
      </div>
    </Layout>
  );
}
