---
sidebar_position: 15
id: sotm
title: "Shooting On The Move"
---


  

    
  

  <p>Stopping the robot to aim is a 2-second penalty on your cycle time. In modern high-level play, the robot must be capable of calculating a solution and firing while traversing the field at 4+ m/s. MARSLib uses a high-fidelity <strong>Iterative Kinematic Solver</strong> to find these solutions in real-time.</p>

  <h2>1. The Moving Target Problem</h2>
  <p>If you aim where the Hub is <em>right now</em>, by the time the ball leaves your shooter and travels 4 meters, the ball will hit the rim and bounce out. You must aim where the Hub <em>will be</em> relative to the ball's flight path.</p>

  <div class="callout callout-warning">
      <h4>Relative Frames of Reference</h4>
      <p>The math requires calculating an "Intercept Point." This depends on your Current Robot Velocity, the Game Piece Exit Velocity, and the physical distance to the goal. MARSLib handles these vector transformations automatically using <strong><a href="https://docs.wpilib.org/en/stable/docs/software/advanced-controls/geometry/index.html" target="_blank">WPILib Geometry</a></strong>.</p>
  

  <h2>2. Iterative Convergence</h2>
  <p>There is no closed-form algebraic solution for shooting on the move because the ball's travel time depends on the exit angle, and the exit angle depends on the robot's future position. MARSLib solves this using a <strong>3-pass Iterative Solver</strong>:</p>
  <ol>
      <li><strong>Pass 1:</strong> Calculate a "Naive" shot to the Hub's current location.</li>
      <li><strong>Pass 2:</strong> Estimate where the robot will be after the Naive shot's flight time.</li>
      <li><strong>Pass 3:</strong> Recalculate the shot to that future location.</li>
  </ol>
  <p>This 3-pass convergence is computationally cheap and brings the shot error down to less than 1cm at 5 m/s.</p>

  <h2>3. Tuning Your Shot Map</h2>
  <p>The solver relies on an <code>InterpolatingTreeMap</code>. You provide the shooter with physical "Known Goods" (e.g., at 2m, I need 4000 RPM and 30&deg; pivot). The <code>MARSShotSetup</code> skill then uses cubic splines to smooth out the values between those points.</p>

  <br /><hr /><br />
  <h2>📖 Further Reading & External Resources</h2>
  <ul>
    <li><a href="https://www.youtube.com/watch?v=yYn9NqJ6kXU">Team 6328: Aiming on the Move</a> - An elite-tier breakdown of the vector math required for dynamic shooting.</li>
    <li><a href="https://docs.wpilib.org/en/stable/docs/software/advanced-controls/geometry/index.html">WPILib: Geometry & Kinematics</a> - Underlying vector and transform math.</li>
    <li><a href="https://www.chiefdelphi.com/t/team-1690-orbit-2024-code-release/464817">1690 Code Release</a> - The definitive implementation of high-accuracy shooter interpolation.</li>
  </ul>
  
