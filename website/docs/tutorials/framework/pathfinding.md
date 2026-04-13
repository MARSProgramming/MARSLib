---
sidebar_position: 12
id: pathfinding
title: "Pathfinding & Autonomous"
---

import AutoSim from '@site/src/components/AutoSim';


  
    
  

  <p>To win on Einstein, a robot cannot simply drive straight lines from point A to point B in the autonomous period. The field is littered with obstacles, standard trajectories lack fluidity, and sudden game piece collisions require dynamic recalculations. MARSLib integrates deeply with <strong>PathPlanner</strong> to bring seamless, holistic pathfinding to the FRC field.</p>

  <h2>1. Catmull-Rom Splines</h2>
  <p>Instead of hardcoding angles and trapezoidal movement profiles manually, we utilize Catmull-Rom splines. Splining allows the robot's Holonomic Drive Controller to continuously traverse a curved trajectory without having to decelerate at individual waypoints.</p>

  <p>Try dragging the waypoints (nodes) on the field simulator below to watch how the trajectory natively curves to intercept them smoothly. Press <strong>FOLLOW SPLINE</strong> to witness the holonomic controller drive the trajectory:</p>

  <AutoSim />

  <h2>2. A* NavGrid Traversal</h2>
  <p>During the tele-op period, the driver may need to command the robot to auto-align or traverse from the Loading Zone directly to the Hub. MARSLib uses a NavGrid A* solver running on the Driver Station (or Coprocessor) to intelligently weave a safe path around the Stage/Ladder obstacles.</p>

  ```java
  // Dynamically request a path to a scoring node safely avoiding the Stage
  Command autoScoreCmd = AutoBuilder.pathfindToPose(
      new Pose2d(14.5, 5.5, Rotation2d.fromDegrees(180)),
      new PathConstraints(3.0, 4.0, Units.degreesToRadians(540), Units.degreesToRadians(720)),
      0.0,
      0.0
  );
  ```

  <div class="callout callout-warning">
    <h4>Avoid On-RIO A* Calculation</h4>
    <p>Calculating an entire field NavGrid requires traversing a massive 2D array graph. Executing this on the core RoboRIO thread can cause devastating loop overruns (exceeding 20ms). Always offload advanced Pathfinding math to the <strong>Driver Station Laptop</strong> or a dedicated coprocessor.</p>
  

  <h2>3. Fusing Vision with Odometry</h2>
  <p>Path execution relies 100% on where the robot *thinks* it is on the field. Without AprilTags, the wheel encoders slowly drift over time. This implies that your Autonomous Splines will gradually shift away from their hardcoded positions as the match progresses.</p>

  <p>Path execution relies 100% on where the robot *thinks* it is on the field. Without <a href="https://docs.wpilib.org/en/stable/docs/software/vision-processing/apriltag/index.html">AprilTags</a>, the wheel encoders slowly drift over time. This implies that your Autonomous Splines will gradually shift away from their hardcoded positions as the match progresses.</p>

  <p>To guarantee mm-perfect precision, we feed the <a href="https://limelightvision.io/">Limelight 4's</a> MegaTag 2.0 poses directly into the <code>SwerveDrivePoseEstimator</code>. This organically corrects the internal map as the robot drives, ensuring PathPlanner always knows the true distance remaining to the next Spline knot!</p>


  <br /><hr /><br />
  <h2>📖 Further Reading & External Resources</h2>
  <ul>
    <li><a href="https://pathplanner.dev/">PathPlanner Documentation</a> - Standardized GUI for creating autonomous paths.</li>
    <li><a href="https://docs.wpilib.org/en/stable/docs/software/pathplanning/trajectory-tutorial/index.html">WPILib Trajectory Tutorial</a> - Understanding the math behind spline-based pathing.</li>
    <li><a href="https://v6.docs.ctr-electronics.com/en/stable/docs/api-reference/api-usage/control-requests.html">CTRE Phoenix 6 Control Requests</a> - Explaining 250Hz frequency CANivore utilization.</li>
    <li><a href="https://docs.wpilib.org/en/stable/docs/software/advanced-controls/system-identification/index.html">WPILib System Identification (SysId)</a> - Advanced mathematical breakdowns of Quasistatic friction and Dynamic OLS regression curves.</li>
    <li><a href="https://github.com/Team364/BaseFalconSwerve">Team 364 BaseFalconSwerve</a> - The historic FRC architecture that inspired modern template geometries.</li>
  </ul>
  
