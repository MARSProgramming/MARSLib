---
sidebar_position: 3
id: command-based
title: "3. Command-Based Paradigm"
---

<main class="container">
  <div>
    <h1>Command-Based Architecture</h1>
  </div>

  <p>If you've built simple robots before, you might have written all your code linearly: "Wait 2 seconds, drive forward, spin the intake, stop." In FRC, <strong>this is lethal.</strong> Blocking the main thread for even a single second violates the FRC networking envelope and crashes the robot.</p>
  <p>To ensure code is evaluated safely every 20ms, WPILib enforces the <strong><a href="https://docs.wpilib.org/en/stable/docs/software/commandbased/index.html" target="_blank">Command-Based Paradigm</a></strong>.</p>

  <h2>1. Subsystems (The Nouns)</h2>
  <div class="callout callout-info">
    <h4>Persistent Wrappers</h4>
    <p>A <a href="https://docs.wpilib.org/en/stable/docs/software/commandbased/subsystems.html" target="_blank">Subsystem</a> represents physical hardware on the robot (e.g., <code>ElevatorSubsystem</code>, <code>ShooterSubsystem</code>). It is instantiated exactly once on boot and persists forever.</p>
  </div>
  <ul>
    <li>Subsystems own the physical motors and CAN IDs.</li>
    <li>They provide methods like <code>setVoltage(12.0)</code> or <code>getDistance()</code>.</li>
    <li>They have a native <code>periodic()</code> method that fires exactly once every 20 milliseconds, usually dedicated to updating telemetry and logs.</li>
  </ul>

  <h2>2. Commands (The Verbs)</h2>
  <p><a href="https://docs.wpilib.org/en/stable/docs/software/commandbased/commands.html" target="_blank">Commands</a> are discrete actions (e.g., <code>FireShooterCommand</code>, <code>ScoreAmpCommand</code>).</p>
  <ul>
    <li>Commands are <strong>ephemeral</strong>. They are instantiated, executed, and then die.</li>
    <li>Every Command must <em>Require</em> a Subsystem. If <code>FireShooterCommand</code> requires the Shooter Subsystem, it locks it. If another routine tries to touch the Shooter, the original Command is forcefully interrupted!</li>
  </ul>

  <h2>3. The RobotContainer (The Glue)</h2>
  <p>You cannot blindly command motors in FRC directly from driver inputs. We use the <strong><a href="https://docs.wpilib.org/en/stable/docs/software/commandbased/structuring-command-based-project.html#robotcontainer" target="_blank">RobotContainer.java</a></strong> class as a strict wiring harness.</p>

  ```java
  // In RobotContainer.java
  joystick.a().onTrue(new FireShooterCommand(shooterSubsystem));
  ```
  <p>When the driver presses the "A" button, the runtime scheduler grabs <code>FireShooterCommand</code>, injects the permanent <code>shooterSubsystem</code> into it, and schedules the execution cleanly alongside the rest of the robot loops.</p>

  <h2>4. The 20ms Control Loop</h2>
  <p>Here is what the RoboRIO is doing 50 times every single second during a match:</p>
  <ol>
    <li><strong>Input Polling:</strong> Read Driver Station Xbox controllers and NetworkTables.</li>
    <li><strong>Subsystem Periodic:</strong> Ask every subsystem to report its encoder/sensor values.</li>
    <li><strong>Command Execution:</strong> For every currently active Command (like the Shooter spinning up), run its <code>execute()</code> block. Check if its <code>isFinished()</code> block is true; if yes, cleanly end it.</li>
    <li><strong>Motor Writing:</strong> Flush all the newly requested voltage calculations over the CAN bus physically to the motors.</li>
  </ol>


  <br /><hr /><br />
  <h2>📖 Further Reading & External Resources</h2>
  <ul>
    <li><a href="https://docs.wpilib.org/en/stable/docs/zero-to-robot/introduction.html">WPILib "Zero to Robot" Guide</a> - The official foundational pipeline for FRC control systems.</li>
    <li><a href="https://www.youtube.com/watch?v=8319J1BEHwM">FRC 0 to Auto Youtube Series</a> - Outstanding video tutorials exploring command-based programming for novices.</li>
    <li><a href="https://docs.wpilib.org/en/stable/docs/software/commandbased/index.html">WPILib Command-Based Documentation</a> - Deep dive into Subsystem and Command scheduling architecture.</li>
  </ul>
  </main>
