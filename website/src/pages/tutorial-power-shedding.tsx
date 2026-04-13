import React, { useEffect } from 'react';
import Layout from '@theme/Layout';

export default function TutorialPowerShedding() {
  return (
    <Layout title="Tutorial Power Shedding">
      <div className="legacy-mars">
        <div dangerouslySetInnerHTML={{ __html: `



<main class="container" style="padding-top: 100px; padding-bottom: 80px;">
  <div style="text-align: center; margin-bottom: 40px;">
    <a href="/MARSLib/tutorials" class="back-link">← BACK TO TUTORIALS</a>
    <h1>Dynamic Power Shedding</h1>
  </div>

  <p>Modern FRC hardware—specifically Krakens and Falcons—can physically pull more power than the RoboRIO and a 12V battery can safely provide. If your Swerve Drive engages in a massive pushing match while the Intake is stalled on a stuck <strong>Fuel Ball</strong>, the battery voltage will dip below 7V, the RoboRIO will reset, and you will lose the match.</p>

  <h2>1. The Problem with Static Limits</h2>
  <p>Most teams solve this by configuring static breaker current limits. This is a compromise. It neuters your robot's acceleration when the battery is fully charged at 12.8V just to protect it when it drops to 9V.</p>

  <h2>2. MARSPowerManager: Voltage Scaling</h2>
  <p>In MARSLib, we use the <code>MARSPowerManager</code> to implement <strong>Voltage Scaling</strong> and <strong>Power Shedding</strong>. Instead of static stator current bounds, our subsystems dynamically cap their output voltage based on the real-time health of the battery.</p>

  <pre><code class="language-java">// Inside an IO implementation's periodic loop
double currentSystemVoltage = MARSPowerManager.getInstance().getBatteryVoltage();
double maxAllowedVoltage = 12.0;

if (currentSystemVoltage &lt; 9.5) {
    // Battery is sagging heavily, shed power from non-essentials
    maxAllowedVoltage = 3.0; // Throttle intake to prevent brownout
}

motor.setVoltage(MathUtil.clamp(requestedVoltage, -maxAllowedVoltage, maxAllowedVoltage));</code></pre>

  <h2>3. Subsystem Priority Classes</h2>
  <p>Not all subsystems are created equal. When the <code>MARSPowerManager</code> detects a critical system voltage sag (e.g., \`&lt; 9.5V\`), it initiates a lockdown phase. The framework groups mechanisms into three distinct tiers:</p>
  <ul>
      <li><strong>TIER 1 (Critical):</strong> Drivetrain Swerve Modules. These never scale down. Losing traction or evasiveness is unacceptable.</li>
      <li><strong>TIER 2 (High):</strong> Shooter Flywheels & Pivot. Voltage output is slightly scaled to ensure consistent shots, but not eliminated.</li>
      <li><strong>TIER 3 (Sheddable):</strong> Intakes, Feeders, and LEDs. If the voltage drops to 9V, these subsystems are aggressively clamped to a maximum output of 2-3 Volts until the battery recovers.</li>
  </ul>

  <div id="brownout-sim" class="sim-panel" style="padding: 30px;">
    <div style="font-family: 'Orbitron'; font-size: 0.8rem; color: var(--ai-cyan); letter-spacing: 1px; margin-bottom: 20px; text-transform: uppercase;">Real-Time Power Shedding Diagnostics</div>
    
    <div style="display: flex; gap: 30px; align-items: stretch; flex-wrap: wrap;">
      <!-- LOAD TOGGLES -->
      <div style="flex: 1; display: flex; flex-direction: column; gap: 10px;">
        <button id="btnSwerve" style="background:#222; border:1px solid #444; color:#fff; padding:12px; border-radius:6px; cursor:pointer; font-family:'JetBrains Mono'; text-align:left; transition: 0.2s;">[T1] SWERVE DRIVE <span style="float:right;">+160A</span></button>
        <button id="btnShooter" style="background:#222; border:1px solid #444; color:#fff; padding:12px; border-radius:6px; cursor:pointer; font-family:'JetBrains Mono'; text-align:left; transition: 0.2s;">[T2] SHOOTER FLYWHEELS <span style="float:right;">+60A</span></button>
        <button id="btnIntake" style="background:#222; border:1px solid #444; color:#fff; padding:12px; border-radius:6px; cursor:pointer; font-family:'JetBrains Mono'; text-align:left; transition: 0.2s;">[T3] INTAKE MOTORS <span id="intakeLoad" style="float:right;">+40A</span></button>
        <button id="btnComp" style="background:#222; border:1px solid #444; color:#fff; padding:12px; border-radius:6px; cursor:pointer; font-family:'JetBrains Mono'; text-align:left; transition: 0.2s;">[T3] COMPRESSOR <span id="compLoad" style="float:right;">+30A</span></button>
      </div>
      
      <!-- DASHBOARD -->
      <div style="flex: 1; min-width: 250px; background: #0a0a0a; border: 1px solid #333; border-radius: 6px; padding: 20px; position: relative; display: flex; flex-direction: column; justify-content: center; align-items: center;">
        <div id="vTxt" style="font-family: 'JetBrains Mono'; font-size: 3rem; font-weight: 700; color: #4caf50;">12.5V</div>
        <div id="statusTxt" style="font-family: 'Orbitron'; font-size: 1rem; color: #999; margin-top: 10px; letter-spacing: 1px;">SYSTEM NOMINAL</div>
        
        <!-- Battery Bar -->
        <div style="width: 100%; height: 20px; background: #222; border-radius: 10px; margin-top: 20px; overflow: hidden; border: 1px solid #444;">
          <div id="vBar" style="width: 100%; height: 100%; background: #4caf50; transition: width 0.3s, background 0.3s;"></div>
        </div>
        
        <!-- Shed Overlay Warning -->
        <div id="shedWarn" style="position: absolute; top:0; left:0; width:100%; height:100%; background: rgba(179,36,22,0.15); border: 2px solid var(--mars-red); border-radius: 6px; display: none; align-items: center; justify-content: center; pointer-events: none;">
        </div>
      </div>
    </div>
  </div>

  <div class="callout callout-warning">
    <h4>Simulation Parity</h4>
    <p>You can test your power shedding algorithms entirely in Dyn4j. The <code>MARSPhysicsWorld</code> natively simulates voltage sag as the sum of all simulated motor torque output increases, allowing you to trigger brownout scenarios in your JUnit tests and ensure your shedding algorithm functions correctly.</p>
  </div>
</main>







` }} />
      </div>
    </Layout>
  );
}
