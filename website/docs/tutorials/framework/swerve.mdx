---
sidebar_position: 6
id: swerve
title: "High-Frequency Swerve"
---





<main className="container" >
  <div >
    
    <h1>High-Frequency Swerve</h1>
  </div>

  <p>A standard FRC robot executes its control loop every 20ms (50Hz). This is fine for moving an elevator, but it is <strong>unacceptably slow</strong> for Swerve Odometry. If a robot moving at 5 m/s only calculates its position 50 times a second, the math inherently creates a "drift" of several centimeters per second.</p>

  <p>MARSLib solves this by utilizing the <strong>PhoenixOdometryThread</strong>.</p>

  <h2>1. The 250Hz Thread</h2>
  <p>Instead of relying on the main RoboRIO thread, our swerve architecture spins up an isolated background thread that communicates directly with the <strong><a href="https://v6.docs.ctr-electronics.com/en/stable/docs/canivore/canivore-intro.html" target="_blank">CTRE CANivore</a></strong> bus. This thread calculates the robot's physical kinematics at 250Hz.</p>

  <div className="callout callout-warning">
    <h4>Zero-Allocation (GC) Danger</h4>
    <p>Because this thread runs 250 times a second, if you instantiate an object inside this loop, you will instantly crash the RoboRIO due to massive Garbage Collection pressure. This is why our Agentic Skills strictly enforce the <strong>Ephemeral Struct</strong> proxy architecture across the Swerve subsystem.</p>
  </div>

import SwerveSim from '@site/src/components/SwerveSim';

  <SwerveSim />

  <h2>2. MegaTag 2.0 Integration</h2>
  <p><strong><a href="https://limelightvision.io/" target="_blank">Limelight 4's</a></strong> MegaTag 2.0 system relies heavily on the IMU Yaw rates to compensate for camera latency. Because we process the <strong><a href="https://v6.docs.ctr-electronics.com/en/stable/docs/hardware-reference/pigeon2/index.html" target="_blank">Pigeon 2.0</a></strong> data at 250Hz, our gyro-seeding to the Limelight is incredibly precise, allowing for flawless localization even while spinning and traversing the Bump.</p>

  ```java
// Asynchronously injecting vision without locking the thread
public void addVisionMeasurement(Pose2d visionPose, double timestamp, Matrix<N3, N1> stdDevs) {
    poseEstimator.addVisionMeasurement(visionPose, timestamp, stdDevs);
}
```

  <h2>3. 2D Kinematic Traction Control</h2>
  <p>Standard <a href="https://docs.wpilib.org/en/stable/index.html" target="_blank">WPILib</a> implementations apply independent <code>SlewRateLimiters</code> to the X and Y joystick axes. However, if the driver pushes diagonally, the robot attempts to accelerate at <b>1.41x</b> the limit, instantly breaking carpet friction.</p> 
  
  <p>To solve this, MARSLib uses a unified <code>TractionControlLimiter</code> that calculates the magnitude of the requested 2D acceleration vector and strictly caps it below the kinetic slipping envelope of the FRC carpet (approx 1.1g, or 10.78 m/s²). This guarantees the swerve drive never breaks static friction natively, preserving your odometry perfectly!</p>

  <br /><hr /><br />
  <h2>📖 Further Reading & External Resources</h2>
  <ul>
    <li><a href="https://v6.docs.ctr-electronics.com/en/stable/docs/api-reference/api-usage/control-requests.html">CTRE Phoenix 6 Control Requests</a> - Explaining 250Hz frequency CANivore utilization.</li>
    <li><a href="https://pathplanner.dev/">PathPlanner Documentation</a> - Standardized GUI usage for creating 2D autonomous splines.</li>
    <li><a href="https://docs.wpilib.org/en/stable/docs/software/advanced-controls/system-identification/index.html">WPILib System Identification (SysId)</a> - Advanced mathematical breakdowns of Quasistatic friction and Dynamic OLS regression curves.</li>
    <li><a href="https://github.com/Team364/BaseFalconSwerve">Team 364 BaseFalconSwerve</a> - The historic FRC architecture that inspired modern template geometries.</li>
  </ul>
  </main>
