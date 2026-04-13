---
sidebar_position: 13
id: fault-resilience
title: "Fault Resilience"
---

import FaultSim from '@site/src/components/FaultSim';

<main class="container" >
  <div >
    <h1>Fault Resilience</h1>
    <p>High-fidelity health monitoring and driver alerting systems.</p>
  </div>

  <p>On Einstein or at the Championship level, a single wire coming loose can lose a match. MARSLib features a military-grade <strong>Two-Layer Fault System</strong> designed to detect failures, protect the hardware, and immediately notify the driver before the match is compromised.</p>

  <h2>1. The Two-Layer Architecture</h2>
  <p>Fault monitoring is split into two distinct responsibilities to ensure zero-latency detection:</p>

  <div class="callout callout-warning">
      <h4 >Layer 1: IO-Level Health</h4>
      <p>This layer lives inside the Hardware Wrapper (e.g., <code>ArmIOKraken</code>). It monitors the lowest level sensor data for "Red Flags":</p>
      <ul>
          <li><strong>CAN Timeouts</strong>: Detecting disconnected motors or <a href="https://v6.docs.ctr-electronics.com/en/stable/docs/canivore/canivore-intro.html" target="_blank">CANivore</a> buses.</li>
          <li><strong>Current Spikes</strong>: Detecting stalled mechanisms or carpet snags.</li>
          <li><strong>Stale Data</strong>: Detecting encoder/IMU disconnects.</li>
      </ul>

      <h4 >Layer 2: User-Facing Alerts</h4>
      <p>This layer propagates errors to the human team. Active faults are configured using persistent <strong><a href="https://docs.wpilib.org/en/stable/docs/software/dashboards/shuffleboard/advanced-usage/sending-data.html#using-the-alert-class" target="_blank">Alert</a></strong> objects and broadcast directly to AdvantageScope or the Driver Station.</p>
  </div>

  <h2>2. The Alert System</h2>
  <p>Instead of relying on easy-to-miss `System.out.println` statements or generic SmartDashboard booleans, MARSLib uses the <strong>Alert</strong> class to trigger specific warnings that light up uniquely defined UI widgets.</p>

  ```java
  // Persistent alert declared in a Subsystem
  private final Alert m_stallAlert = new Alert("Arm Motor Stalled!", AlertType.ERROR);

  @Override
  public void periodic() {
      // Logic to detect a stall using IO inputs
      boolean isStalled = inputs.currentAmps > 40.0 && Math.abs(inputs.velocityRadPerSec) < 0.1;
      m_stallAlert.set(isStalled);
  }
  ```

  <h2>3. Transparent Structural Fallbacks</h2>
  <p>Alerting the driver is step 1. But what if the robot needs to keep moving? Because MARSLib strictly defines the IO Layer using a unified Struct data model, we achieve a massive structural advantage: <strong>Resiliency</strong>.</p>

  <p>If a CAN bus wire snaps in the middle of a match—or a Kraken motor spontaneously reboots—the IO layer can catch the `CANStatus` exception and seamlessly redirect the `Inputs` struct to read from the internal Physics engine fallback. <strong>The Subsystem logic never even knows the hardware failed!</strong> It continues calculating the target state organically using mathematical models while broadcasting the fault alert concurrently.</p>

  <p>Try clicking <strong>SEVER CAN WIRE</strong> below to see the math fallback gracefully handle the outage without zeroing out the reported velocity! Notice that the software continues simulating the mechanism's positional state even when the actual hardware is coasting to a stop.</p>

  <FaultSim />

  <h2>4. Pre-Match Diagnostics</h2>
  <p>MARSLib includes <code>MARSDiagnostics</code>, which standardizes running "Sweep Tests" of all registered mechanisms in the pits. It stresses the arm, drives the chassis 10cm, and spins the intake at 10% power to verify that no cables were loosened during transport.</p>

  <div class="callout callout-info">
      <h4>Safety Shutdowns</h4>
      <p>If a critical error is detected during diagnostic scans (like an over-current spike), MARSLib's high-level fault controllers will automatically block enabling or scale back voltage limits to prevent permanent motor burnout on the field.</p>
  </div>

  <br /><hr /><br />
  <h2>📖 Further Reading & External Resources</h2>
  <ul>
    <li><a href="https://docs.wpilib.org/en/stable/docs/software/roborio-info/roborio-brownouts.html">WPILib: RoboRIO Brownout Protection</a> - Understanding how hardware handles low voltage.</li>
    <li><a href="https://v6.docs.ctr-electronics.com/en/stable/docs/api-reference/api-usage/actuator-limits.html">CTRE: Phoenix 6 Actuator Limits</a> - Implementing hardware-level current and voltage clamping.</li>
  </ul>
  </main>
