---
sidebar:
  order: 10
id: vision
title: "Vision Fusion & Filtration"
---

  <p>In the FRC 2026: <strong>REBUILT</strong> game, absolute field localization is critical. MARSLib's <code>MARSVision</code> subsystem combines data from multiple cameras and strictly filters out "hallucinations" using techniques pioneered by elite teams. We utilize <strong><a href="https://docs.wpilib.org/en/stable/docs/software/vision-processing/apriltag/apriltag-intro.html" target="_blank">AprilTags</a></strong> as the primary landmarks for global pose estimation.</p>

  <h2>1. Strict Rejection Filters</h2>
  <p>Vision poses are frequently wrong during high-speed gameplay. Before a measurement reached the Pose Estimator, it must survive five strict boundary checks:</p>
  <ul >
    <li ><strong>Z-Height Hallucinations:</strong> If the pose estimates the robot is flying (Z > 0.5m), it's rejected.</li>
    <li ><strong>Out of Bounds:</strong> If the pose is tracked outside the physical field, it's rejected.</li>
    <li ><strong>Motion Blur:</strong> If the gyro reports > 120&deg;/s yaw rate, vision is entirely blocked.</li>
    <li ><strong>Beaching (Pitch/Roll):</strong> If you ride over an obstacle and tilt > 15&deg;, the pose is rejected.</li>
    <li ><strong>Ambiguity:</strong> Low quality single-tag solutions are discarded.</li>
  </ul>

  {/* Interactive Vision Simulation Demo */}
import VisionSim from '../../../components/VisionSim';

<VisionSim />


  <h2>2. Quadratic StdDev Scaling</h2>
  <p>Vision poses should mathematically never be trusted equally. A pose from 6 meters away is tiny on the camera sensorâ€”1 pixel of error dramatically shifts the calculated location.</p>

  <p>MARSLib enforces <strong>Quadratic StdDev Scaling</strong>. Trust in vision decays <em>exponentially</em> at long range, stopping the robot from making violent odometry correction jumps based on far-away tags.</p>

  <div class="callout">
    <h4>MegaTag / Multi-Tag Boost</h4>
    <p>When multiple AprilTags are visible, geometric ambiguity essentially drops to zero. Our code automatically scales down standard deviations by <code>x0.1</code>, dramatically tightening the Pose Estimator's trust.</p>
  </div>

  <h2>3. Simulating Imperfection</h2>
  <p>To ensure tuning works globally, the <code>AprilTagVisionIOSim</code> in MARSLib purposefully injects:</p>
  <ul >
    <li ><strong>Gaussian Noise:</strong> StdDev matched jitter scaled by distance.</li>
    <li ><strong>Dropped Frames:</strong> A 5% chance every frame that no pose is returned.</li>
    <li ><strong>Latency:</strong> Simulates a 10ms-30ms phase delay offset.</li>
  </ul>

  <br /><hr /><br />
  <h2>📖 Further Reading & External Resources</h2>
  <ul>
    <li><a href="https://www.youtube.com/watch?v=_eP941oXGow">Limelight MegaTag 2.0 Breakdown</a> - Understanding how FRC 1690 style IMU yaw-seeding rejects rapid AprilTag noise variations.</li>
    <li><a href="https://docs.photonvision.org/en/latest/">PhotonVision Hardware Guide</a> - Best practices for illuminating targets globally.</li>
  </ul>

