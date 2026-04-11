---
name: marslib-autonomous
description: Helps build autonomous code and PathPlanner macros in MARSLib. Use when creating paths, configuring AutoBuilder, or adding named commands.
---

# MARSLib Autonomous Skill

You are an autonomous systems engineer for Team MARS 2614. When building autonomous routines or driver-assist features:

## 1. Architecture

The autonomous subsystem lives in `com.marslib.auto` with 6 core classes:

| Class | Purpose |
|---|---|
| `MARSAuto` | Static factory for PathPlanner and Choreo trajectory commands with pathfinding |
| `GhostManager` | **DEPRECATED & DELETED**. Do NOT attempt to implement macro replays. Elite teams mandate pure PathPlanner determinism. |
| `MARSAlignmentCommand` | ProfiledPID-driven alignment to field targets (Reef, Subwoofer) |
| `ShootOnTheMoveCommand` | Newton-Raphson quadratic projectile solver for shooting while driving |
| `SmartAssistAlign` | Lightweight auto-aim utility for teleop heading correction |
| `MARSDiagnosticCheck` | Pre-match system sweep (see `marslib-diagnostics` skill) |

### PathPlanner Integration
`AutoBuilder` is configured in `RobotContainer` via `SwerveDrive.configurePathPlanner()` — NOT in the SwerveDrive constructor. Named commands are registered before auto selection:
```java
NamedCommands.registerCommand("intake", superstructure.setAbsoluteState(INTAKE_FLOOR));
NamedCommands.registerCommand("score", superstructure.setAbsoluteState(SCORE_HIGH));
drive.configurePathPlanner();
```

## 2. Key Rules

### Rule A: AutoBuilder is a Static Singleton
`AutoBuilder.configure()` MUST only be called once per JVM. The call lives in `SwerveDrive.configurePathPlanner()`, invoked by `RobotContainer`. If you call it in the constructor, test classes that instantiate SwerveDrive will crash on the second instance.

### Rule B: Use 250Hz Odometry for Pathing
PathPlanner must use the `PhoenixOdometryThread`'s high-frequency pose supplier, not the 50Hz periodic loop pose. This gives PathPlanner sub-tick precision for trajectory tracking.


### Rule D: Alignment Wraps Continuous Input
`MARSAlignmentCommand` uses `PIDController` for rotation with `enableContinuousInput(-PI, PI)`. If you create a custom alignment command without this, the robot will spin 350° instead of rotating 10° across the ±π boundary.

## 3. Adding New Autonomous Routines

1. Create the path in PathPlanner GUI and export to `deploy/pathplanner/`.
2. Register any named event commands via `NamedCommands.registerCommand()` in `RobotContainer`.
3. For Choreo trajectories, place `.traj` files in `deploy/choreo/` and use:
   - `MARSAuto.runChoreoTrajectory(name)` — direct playback
   - `MARSAuto.pathfindThenRunChoreoTrajectory(name)` — pathfinds to start first
4. For dynamic teleop routing: `MARSAuto.pathfindObstacleAvoidance(targetPose)`.
5. All constraint parameters (`MAX_VELOCITY_MPS`, `MAX_ACCELERATION_MPS2`) live in `Constants.AutoConstants`.

## 4. Command API

```java
// In RobotContainer autonomous chooser:
autoChooser.addOption("3 Piece", MARSAuto.build3PieceAuto(drive, superstructure));

// Teleop assist bindings:
controller.leftBumper().whileTrue(new MARSAlignmentCommand(drive, targetPose));
controller.rightTrigger().whileTrue(new ShootOnTheMoveCommand(drive, vxSupplier, vySupplier));
```

## 5. Telemetry
- `Auto/ActivePath` — Currently executing PathPlanner path name
- `Auto/TargetPose` — PathPlanner's goal Pose2d
- `Auto/TrajectoryPose` — Ideal position along the trajectory at current time
- `Alignment/AtGoal` — Boolean: alignment PID has converged
- `Alignment/TranslationError` — Meters from target
- `Alignment/RotationError` — Radians from target heading
