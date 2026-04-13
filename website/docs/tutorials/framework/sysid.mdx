---
sidebar_position: 14
id: sysid
title: "Continuous SysId"
---

<main className="container" >
  <div >
    
    <h1>Continuous SysId</h1>
  </div>

  <p>Static System Identification (running specific "quasistatic" tests in the pits) is the industry standard. However, mechanism friction changes as the robot's gears wear down or get dirty throughout a 3-day competition. MARSLib introduces <strong>Continuous SysId</strong>, which passively identifies your <code>kV</code> and <code>kA</code> constants during real matches.</p>

  <h2>1. The Logging Advantage</h2>
  <p>Because every motor voltage and velocity is logged at high frequency through <strong><a href="https://github.com/Mechanical-Advantage/AdvantageKit" target="_blank">AdvantageKit</a></strong>, we don't need dedicated tests. The <code>MARSSysIdManager</code> runs an <strong>Ordinary Least Squares (OLS)</strong> regression against the log data every time the robot is disabled.</p>

  <h2>2. Implementation</h2>
  <p>To enable passive characterization, you must register your subsystem's IO with the manager.</p>

  ```java
// Inside RobotContainer.java
SysIdManager.getInstance().registerMechanism(
    "Shooter", 
    flywheel::getVoltage, 
    flywheel::getVelocityRadPerSec
);
```

  <h2>3. Viewing the Results</h2>
  <p>The calculated <code>kV</code> (Volts per Unit/s) and <code>kA</code> (Volts per Unit/s&sup2;) are pushed to NetworkTables. If the passion-identified kV differs from your hardcoded constant by more than 15%, a <strong>Fault Alert</strong> will trigger, notifying the pit crew that a mechanism is experiencing unexpected friction.</p>

  <br /><hr /><br />
  <h2>📖 Further Reading & External Resources</h2>
  <ul>
    <li><a href="https://docs.wpilib.org/en/stable/docs/software/advanced-controls/system-identification/index.html">WPILib: System Identification (SysId)</a> - Characterize your robot with automated experiments.</li>
    <li><a href="https://v6.docs.ctr-electronics.com/en/stable/docs/api-reference/api-usage/control-requests.html">CTRE Phoenix 6: Signal Logging</a> - Best practices for high-speed telemetry in characterization.</li>
  </ul>
  </main>
