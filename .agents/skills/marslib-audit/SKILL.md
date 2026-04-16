---
name: marslib-audit
description: Helps execute a championship-grade audit of the MARSLib framework. Use when requested to perform a "code audit" or ensure maximum framework reliability for competition.
---

# MARSLib Code Audit Skill

You are a senior reliability engineer for Team MARS 2614. Your task is to relentlessly audit the MARSLib codebase for memory allocations, determinism flaws, and architectural bad practices.

## 1. Zero-Allocation (GC) Enforcement
FRC robot control loops run at 20ms (50Hz) or 4ms (250Hz for Odometry). The JVM Garbage Collector will cause unrecoverable stutter if object allocations occur within `periodic()`.

### Rule A: No `new` keywords in Periodic
Never allow `new Pose2d()`, `new ChassisSpeeds()`, or new Array instantiations to exist inside any `periodic()` block.
**Audit Action**: Identify recurring object creations in the main teleop loops or odometry threads. If found, refactor them into pre-allocated `static final` caches.

### Rule B: No `System.gc()` Calls
It is a dangerous anti-pattern to manually call `System.gc()` inside `Robot.disabledInit()`. Doing so triggers "Stop-The-World" pauses that can desync NetworkTables and CAN bus reporting.
**Audit Action**: If `System.gc()` exists, delete it immediately. Let the generational GC handle paused memory implicitly.

## 2. Advanced Mathematical Safety & Error Prevention

### Rule A: Division By Zero & NaN Propagation
In kinematics and vision scaling algorithms, unhandled edge cases will result in aggressive NaN (Not a Number) propagation to motor outputs, locking out the robot.
**Audit Action**: Scan mathematical utility methods for unprotected division (`/ x`). Require `Math.abs(x) < 1e-6` epsilon checks. Audit `Math.sqrt()` and `Math.acos()/Math.asin()` for values outside of strictly clamped domains `[0, ∞)` and `[-1, 1]` respectively.

### Rule B: Angle Wrapping Errors
Naked modulo operations (`% 360` or `% (2 * Math.PI)`) routinely fail to calculate the shortest path when comparing two angles.
**Audit Action**: Run grep across the repository for `% 360`. Replace any custom heading math with WPILib's `MathUtil.angleModulus()` or `Rotation2d.minus()` to guarantee shortest-distance error bounds inherently [-π, π).

### Rule C: Contextual Domain Integrity & Hardware Limits
A purely mathematical audit isn't enough; the math must respect the physical constraints of an FRC robot. For example, an arm angle being allowed to wrap beyond 360 degrees when the physical mechanism hits a hard-stop at 120 degrees is a critical logic failure.
**Audit Action**: The AI must cross-reference mathematical constraints against the physical mechanism's context. If analyzing an Elevator or Pivot, verify that the `MathUtil.clamp()` boundaries match the hardware limitations rather than arbitrary mathematical maximums.

### Rule D: Unit Safety & Explicit Conversions
Hardcoded magic numbers for conversion factors (like `* 0.0174533`) create unreadable logic and rounding errors.
**Audit Action**: Replace all literal conversion factors natively with WPILib `edu.wpi.first.math.util.Units.degreesToRadians(x)` or the explicit Java Units API (`Meters.of(x)`).

## 3. API Safety & Reliability Design

### Rule A: No Naked `.get()` on Optionals
Crashing Robot code due to a `NoSuchElementException` is grounds for failure.
**Audit Action**: Run grep across the repository for `Optional.get()`. If found in teleop loops, specifically things like `DriverStation.getAlliance().get()`, enforce safe unwrapping (`.orElse()` or `.ifPresent()`).

## 4. Branch Coverage & Unit Testing

### Rule A: Enforce 70%+ Branch Coverage
The Jacoco build report must certify that >70% of mathematical and algorithmic branches are validated by JUnit 5 tests.
**Audit Action**: Look for large mathematical utility classes (like `SwerveSetpointGenerator` or `ControlTheory`) that lack `Test.java` coverage. Generate boundary-condition tests to push their branch coverage.

### Rule B: Build Systems
**Audit Action**: Check `build.gradle` and `gradle.properties`.
1. Ensure the `jacoco.toolVersion` is at least `0.8.12` to prevent Java 21 compilation snags.
2. Ensure `org.gradle.configuration-cache=true` is set in `gradle.properties` to ensure maximum developer build speed.

## 5. Control Theory & Hardware Checks

### Rule A: Integral Windup & Stale Data
PID Controllers relying on accumulation can aggressively over-saturate if trapped behind a mechanical fault (windup). Sensors (like IMU/Vision) can disconnect, generating "stale" readings.
**Audit Action**: Audit PID controllers. Verify they implement `setIZone()` boundaries. Scan IO layers for `inputs.timestamp` latency checks to discard severely delayed data buffers instead of feeding archaic data to `SwerveDrivePoseEstimator`.

### Rule B: Current Limiting & Brownout Protection (Einstein-Tier)
Unrestricted Kraken/Falcon motors will pull 300+ amps, instantly dipping the battery below 6.5V and causing the RoboRIO to reboot mid-match.
**Audit Action**: Verify every `MotorController` instantiation assigns strict `SupplyCurrentLimit` (usually 40A) to prevent battery brownout, and `StatorCurrentLimit` (usually 60-80A) to protect motor coils from smoking during mechanical stalls.

### Rule C: CAN Bus Utilization & Frame Frequencies (Einstein-Tier)
A congested CAN Bus (>80% utilization) will drop critical frames, causing "phantom" PID loops jumping.
**Audit Action**: Search for `BaseStatusSignal.setUpdateFrequency()`. Ensure fast updates (100Hz+) are ONLY given to live control loop inputs (like Swerve encoder positions) while non-critical telemetry (like Motor Temperatures) is aggressively down-sampled to 4Hz or 1Hz.

### Rule D: Hardware Fallback & Blocking Initialization (Einstein-Tier)
A disconnected CANCoder or motor MUST NOT crash the robot or pause the 20ms `Robot.robotInit()` startup lockouts.
**Audit Action**: Check all `Device.waitForUpdate()` or configuration callbacks. Ensure timeouts exist (e.g., `Timeout = 50ms`) so missing hardware graceful fails (logging an error) rather than halting the thread and disabling the robot entirely.

## 6. Code Quality & Cognitive Complexity

### Rule A: The "God Object" & Class Size Caps
Subsystems that exceed 400 lines of code (like Monolithic Swerve Drives) are impossible to maintain and violates the Single Responsibility Principle.
**Audit Action**: Reject any Java class that acts as a localized "God Object." If a class handles IO, filtering, orchestration, and diagnostics simultaneously, it must be shattered into delegated helper classes (e.g., `SwerveDiagnostics`, `SwerveOdometry`).

### Rule B: "Never Nester" & Guard Clauses
Deeply nested logic (e.g., IF statements nested 3+ levels deep) hides bugs and makes mathematical tracking impossible.
**Audit Action**: Audit control loops for deep nesting. Enforce the use of Guard Clauses (early returns) to flatten the method structure and make the primary execution path obvious.

### Rule C: Static Analysis & CI Enforcement
Bad code is automatically detected by Gradle.
**Audit Action**: Run `./gradlew pmdMain checkstyleMain spotlessCheck` as the first line of defense to algorithmically identify magic numbers, unused imports, empty catch blocks, and missing Javadocs before manual inspection begins.

## 7. AI Framework Parity & Knowledge Consistency

### Rule A: Skill Registration & Missing Manifests
If MARSLib code changes but the internal AI `SKILL.md` protocols aren't audited, future AI code generation will fail recursively.
**Audit Action**: Every folder inside `.agents/skills` MUST possess a valid `SKILL.md`. Second, the audit must cross-reference `.agents/skills/marketplace.json` to ensure every populated directory is actively registered. Remove phantom entries pointing to non-existent directories.

### Rule B: Outdated Algorithmic Context
**Audit Action**: Scan through `SKILL.md` rules inside the agent repository and enforce that they reflect the latest architectural choices:
1. Confirm all skills reference `SwerveConfig` injection instead of static `frc.robot.SwerveConstants`.
2. Confirm `marslib-testing` documents the `MARSTestHarness.createSwerveConfig()` / `createPowerConfig()` pattern.
3. Confirm `marslib-swerve` documents `PhoenixOdometryThread.registerModule()` with configurable `odometryHz`.
4. Verify no skill references `frc.robot.*` imports within `com.marslib.*` package code.

## 8. Documentation & Educational Hub

### Rule A: Javadoc Integrity & API Synchronization
If the core classes are updated without documentation, teams cloning MARSLib will misuse them.
**Audit Action**: Every public `com.marslib` algorithm class MUST have valid, WPILib-style class-level and method-level Javadocs. The audit must detect `Missing @param` warnings or completely undocumented static logic paths.

### Rule B: GitHub Pages Site Integrity (github.io)
MARSLib maintains an interactive web documentation hub. A broken link or outdated code snippet destroys the educational value for other drive teams.
**Audit Action**: Audit the `website/src/pages/` and Markdown files for broken local hyperlinks. Verify that code snippets present in the tutorials exactly match the current signature of the Java API.

### Rule C: Middle School Reading Level Compliance
All documentation must be written at a middle school reading level (8th grade equivalent) to ensure accessibility for students of all ages and backgrounds. Complex technical concepts should be explained in simple, clear language.
**Audit Action**: Run readability analysis on all documentation files. Check for:
- Long sentences (>25 words) that should be broken into shorter ones
- Complex technical jargon without plain language explanations
- Passive voice that should be converted to active voice
- Multi-syllable technical terms without pronunciation guides or simple analogies
- Missing "What this means" sections that explain complex concepts simply
- Absence of visual aids (diagrams, examples) for abstract concepts
- Flesch-Kincaid grade level scores above 8.0

**Reading Level Guidelines:**
- Use active voice: "The motor moves the arm" instead of "The arm is moved by the motor"
- Replace jargon with everyday words: "use" instead of "utilize", "find" instead of "ascertain"
- Keep sentences under 15-20 words when possible
- Provide simple analogies: "PID controller is like a thermostat for your robot"
- Add "In other words" sections that rephrase technical concepts
- Include concrete examples before abstract explanations

## 9. Thread Safety & Concurrency

MARSLib runs multiple threads concurrently: the main robot thread (50Hz), `PhoenixOdometryThread` (250Hz), and vision processing threads. Unsynchronized shared mutable state causes invisible data corruption.

### Rule A: Shared Mutable State Protection
Any field written by one thread and read by another MUST be `volatile`, `Atomic*`, or protected by a lock.
**Audit Action**: Enumerate every non-final field in `PhoenixOdometryThread`. Verify that fields read by `SwerveDrive.periodic()` (main thread) are properly protected. Scan for `SyncData` array access patterns — if the odometry thread writes array elements and the main thread reads them, verify happens-before guarantees exist (volatile reference swap, lock, or `System.arraycopy` under lock).

### Rule B: Lock Contention on Hot Paths
Synchronized blocks at 250Hz cause priority inversion and loop overruns.
**Audit Action**: `grep -rn "synchronized" src/main/java/com/marslib/` — verify that NO synchronized block exists on the 250Hz odometry hot path. `LoggedTunableNumber` uses synchronized blocks — verify these are only accessed during `disabledPeriodic()` tuning, never during `teleopPeriodic()`.

### Rule C: Singleton Thread Safety
`PhoenixOdometryThread.getInstance()` uses `synchronized` for lazy init. Verify this is the only access pattern — double-checked locking without volatile is broken in Java.
**Audit Action**: Confirm `getInstance()` is called only during construction (single-threaded robot init), not during periodic execution.

## 10. CAN Bus Error Recovery (StatusCode)

Phoenix 6 API calls (`getConfigurator().apply()`, `setUpdateFrequency()`) return `StatusCode` indicating success or failure. **Ignoring these means silently running with unconfigured motors.**

### Rule A: Configuration Apply Must Be Verified
Every `motor.getConfigurator().apply(config)` call should check the return `StatusCode`.
**Audit Action**: `grep -rn "\.apply(" src/main/java/com/marslib/ --include="*.java"` — verify each result either:
1. Checks `StatusCode.isOK()` and logs failures, OR
2. Uses a retry loop (Phoenix 6 recommends up to 5 retries for CAN contention)

If neither exists, wrap in a utility method:
```java
public static void applyWithRetry(TalonFXConfigurator cfg, TalonFXConfiguration config, int retries) {
    StatusCode status = StatusCode.StatusCodeNotInitialized;
    for (int i = 0; i < retries; i++) {
        status = cfg.apply(config);
        if (status.isOK()) return;
    }
    MARSFaultManager.report("CAN config failed: " + status.getName());
}
```

### Rule B: Signal Frequency Must Be Verified
`BaseStatusSignal.setUpdateFrequency()` also returns StatusCode. If it fails, the signal remains at its default rate (4Hz), meaning our 250Hz odometry thread is actually polling stale 4Hz data.
**Audit Action**: Verify that `setUpdateFrequency()` calls in `SwerveModuleIOTalonFX`, `GyroIOPigeon2`, and mechanism IO layers check the return status or at minimum log failures.

## 11. Command Lifecycle Safety

Commands that never terminate lock out their required subsystem for the entire match.

### Rule A: Every Custom Command Must Terminate
Every `Command` subclass in `com.marslib` must satisfy one of:
1. Has an `isFinished()` method that can return `true`, OR
2. Is a default command (designed to run forever), OR
3. Is always composed with `.withTimeout()` at the usage site

**Audit Action**: `grep -rn "extends Command" src/main/java/com/marslib/` — for each result, verify `isFinished()` exists. For commands without `isFinished()`, verify they are exclusively used as default commands or wrapped with `.withTimeout()`.

### Rule B: Alignment/PID Commands Must Have Convergence Timeouts
PID-based alignment commands (e.g., `MARSAlignmentCommand`) may never converge if the target is unreachable or the mechanism is jammed.
**Audit Action**: Verify that every PID-convergence command is composed with `.withTimeout()` at the call site (typically 2-5 seconds). A command that runs indefinitely because the PID error never reaches tolerance is a match-ender.

## 12. AdvantageKit Replay Contract

AdvantageKit's deterministic replay only works if **every hardware input flows through `@AutoLog` IO layers**. Direct hardware reads bypass replay and cause log divergence.

### Rule A: No Direct Hardware Reads Outside IO Layers
Subsystem `periodic()` methods must NEVER call `Timer.getFPGATimestamp()`, `RobotController.getBatteryVoltage()`, or `DriverStation.*` directly. These must come through IO inputs or utility wrappers.
**Audit Action**: Run the following scan and verify zero results:
```
grep -rn "Timer.getFPGATimestamp\|RobotController.getBatteryVoltage\|DriverStation\." \
  src/main/java/com/marslib/ --include="*.java" \
  | grep -v "IO\.java\|IOSim\.java\|IOReal\.java\|IOTalonFX\.java\|IOPhoton\.java\|IOPigeon2\.java\|AllianceUtil\|IOLimelight\|IOAddressable\|IOQuestNav"
```
Any hits outside IO layers or approved utility wrappers break replay determinism.

### Rule B: All IO Interfaces Must Have `@AutoLog`
Every IO interface in `com.marslib` must have an `@AutoLog` annotation on its `Inputs` inner class.
**Audit Action**: `grep -rn "class.*Inputs" src/main/java/com/marslib/ --include="*IO.java"` — verify each has a preceding `@AutoLog` annotation. Missing `@AutoLog` means the IO data is not captured in logs and cannot be replayed.

## 13. Autonomous Safety Bounds

A misconfigured autonomous path can drive the robot off-field at max speed.

### Rule A: Auto Command Loading Must Have Fallbacks
PathPlanner path loading (in `MARSAuto` or equivalent) is wrapped in try/catch. The catch block MUST provide a safe no-op command (e.g., `Commands.none()`) rather than returning `null` or re-throwing.
**Audit Action**: Inspect every `catch` block in auto-related files. Verify the fallback returns a safe, non-null Command. A `null` auto command will crash the scheduler.

### Rule B: Auto Commands Should Have Safety Timeouts
Autonomous routines should have top-level timeouts to prevent a stuck path from consuming the entire 15-second auto period without scoring.
**Audit Action**: `grep -rn "PathPlannerAuto\|AutoBuilder\|NamedCommands" src/main/java/ --include="*.java"` — verify that auto command compositions include `.withTimeout()` wrappers at the top level or per-path level.

## 14. Dependency Version Auditing

Vendordeps pinned to old versions may silently break when WPILib or vendors push updates.

### Rule A: Vendordep Freshness
**Audit Action**: List all vendordep JSON files in `vendordeps/` and extract their `version` fields. Cross-reference against the latest known stable versions. Flag any vendordep that is more than one minor version behind.
```
cat vendordeps/*.json | grep -E "\"version\"|\"name\""
```

### Rule B: Deprecated API Usage
Vendor APIs change across seasons. Phoenix 6 v24 → v25 renamed several methods.
**Audit Action**: Run the build with `-Xlint:deprecation` flag enabled. Any deprecation warnings in `com.marslib` must be resolved before competition deployment.

### Rule C: WPILib Season Match
The WPILib version in `build.gradle` must match the competition FMS deployment target. Deploying code built against WPILib 2025.3.1 to a RoboRIO running WPILib 2025.2.1 will cause runtime class incompatibility.
**Audit Action**: Verify `wpi.versions.wpilibVersion` in `build.gradle` matches the latest stable WPILib release for the current season.

## 15. Graceful Degradation & Fault Resilience

We test that things *work*, but we must also verify that things *fail safely*.

### Rule A: Hardware Disconnection Must Set `hasHardwareConnected`
Every `*IOReal` and `*IOTalonFX` `updateInputs()` method MUST set `inputs.hasHardwareConnected` based on actual CAN bus communication status.
**Audit Action**: `grep -rn "hasHardwareConnected" src/main/java/com/marslib/` — verify every IO implementation sets this flag. If a motor controller returns stale data (same timestamp as previous tick), the flag should go `false`.

### Rule B: Subsystems Must React to Disconnection
When `hasHardwareConnected` is `false`, the subsystem must:
1. Log a fault via `MARSFaultManager`
2. Command safe outputs (zero voltage, hold position)
3. NOT feed stale sensor data to pose estimation or PID loops

**Audit Action**: Inspect each subsystem's `periodic()` method for disconnection handling. A subsystem that feeds stale gyro data into the pose estimator during a dropout will corrupt the odometry for the entire match.

### Rule C: NaN Propagation Firewall
If a sensor returns `NaN`, it must be caught before entering kinematics or PID. A single `NaN` in `ChassisSpeeds` propagates to all 4 module outputs, commanding `NaN` voltage and disabling the drivetrain.
**Audit Action**: `grep -rn "Double.isNaN\|Double.isFinite\|Double.isInfinite" src/main/java/com/marslib/` — verify NaN guards exist at IO layer boundaries. Critical locations: gyro yaw, vision pose, drive encoder velocity.

## 16. AdvantageScope Layout Completeness

The AdvantageScope layout file (`advantagescope_layout.json`) is the primary competition debugging interface. Missing log keys mean blind spots during critical moments.

### Rule A: Critical Log Keys Must Be Visualized
The layout MUST contain tabs that reference these critical output keys:
- **Odometry**: `SwerveDrive/Pose`, `Odometry/RobotPose`, `Robot/Pose3d`
- **Swerve**: `SwerveDrive/MeasuredStates`, `SwerveDrive/DesiredStates`
- **Vision**: `Vision/ValidPoses/*`, `Vision/CameraFrustums/*`
- **Power**: `PhysicsWorld/ComputedVoltage`, `PhysicsWorld/FrameCurrentDraw_A`
- **System Health**: `System/LoopRunTime_ms`, `System/BatteryVoltage`, `System/CANBusUtilization`, `System/CANivoreUtilization`
- **Teleop**: `Teleop/RawJoystickX`, `Teleop/PostDeadband`, `Teleop/NaNDetected`
- **Faults**: `Alerts/Critical`, `Alerts/Warning`, `Alerts/Info`
- **Tuning**: `TunableNumbers/*` (all mechanism feedforward gains)

**Audit Action**: Parse `advantagescope_layout.json` and extract all `logKey` values. Cross-reference against the grep of `Logger.recordOutput(` across `src/main/java/`. Any critical key that exists in code but is NOT present in any layout tab is a potential debugging blind spot.

### Rule B: Layout File Must Have Version Tag
AdvantageScope requires a `"version"` key at the top level of the layout JSON. Without it, the file silently fails to load.
**Audit Action**: Verify `advantagescope_layout.json` contains `"version": "26.0.0"` (or the appropriate AdvantageScope version). See KI `advantagescope_mcp_missing_version` for known bugs.

### Rule C: Layout Must Reference Correct Field Year
The `"field"` and `"game"` keys in 2D/3D tabs must match the current competition year (e.g., `"FRC:2026 Field"`). An old field reference will render the robot at the wrong coordinates.
**Audit Action**: `grep -i "field\|game" advantagescope_layout.json` — verify all results reference the current season's field.

## 17. Dashboard Configuration Integrity

The team dashboard (`marsteam_dashboard.json`) is used pitside during competition. It must be complete, correct, and match the current codebase.

### Rule A: Dashboard Must Cover Pit-Critical Data
The pitside dashboard MUST include tabs/views for:
1. **3D Field View**: `SwerveDrive/Pose` as robot source (verify driver can see position)
2. **Swerve Diagnostics**: Both `MeasuredStates` and `DesiredStates` (detects dead modules)
3. **Power Monitoring**: Battery voltage and current draw graphs
4. **Driver Inputs**: Raw joystick values (diagnoses stuck buttons or dead axes)
5. **Fault Alerts**: `Alerts/Critical` and `Alerts/Warning` (surface match-impacting faults)

**Audit Action**: Parse `marsteam_dashboard.json` and verify all 5 categories have corresponding tab entries with valid `logKey` references. A dashboard missing fault alerts means the pit crew cannot diagnose hardware failures between matches.

### Rule B: Dashboard Must Not Reference Stale Keys
If a `recordOutput` key is renamed or removed in Java code, the dashboard tab referencing it will show blank/no data.
**Audit Action**: Extract all `logKey` references from `marsteam_dashboard.json`. For each, verify the corresponding `Logger.recordOutput("keyname"` exists in the codebase. Flag any orphaned dashboard keys that no longer have a code source.

### Rule C: Dashboard Version Must Match AdvantageScope
The `"version"` in `marsteam_dashboard.json` must match the installed AdvantageScope version.
**Audit Action**: Verify both `advantagescope_layout.json` and `marsteam_dashboard.json` have the same version string. A mismatch can cause silent load failures.

## 18. Typical Audit Workflow
1. Verify the state of the Build configuration (gradle/jacoco).
2. Execute `./gradlew spotlessCheck` to catch formatting and static analysis flaws.
3. Scan for Zero-Allocation violations (`new` in periodic) and GC triggers (`System.gc()`).
4. Highlight Class Size violations (>400 lines) and "God Objects".
5. Search for Optional misusage (`Optional.get()`) and Naked Modulo expressions (`% 360`).
6. Execute Math Validation checks: unprotected division, unclamped `Math.sqrt()`/`Math.acos()` inputs.
7. Check hardware allocations: Current Limits, CAN Bus frequencies, hardware timeouts.
8. Audit thread safety — verify all shared mutable state in `PhoenixOdometryThread` and `LoggedTunableNumber`.
9. Verify CAN `StatusCode` checking on every `.apply()` and `.setUpdateFrequency()` call.
10. Verify Command lifecycle — `isFinished()` or `.withTimeout()` on every custom command.
11. Enforce AdvantageKit replay contract — no direct hardware reads outside IO layers.
12. Check autonomous safety — fallback commands, timeout wrappers, field boundary clamps.
13. Audit vendordep versions against latest stable releases.
14. Verify graceful degradation — `hasHardwareConnected`, NaN firewalls, fault escalation.
15. **AdvantageScope layout audit** (MANDATORY — do NOT skip):
    a. Locate layout files: `find . -name "*layout*.json" -o -name "*advantagescope*.json"`
    b. Extract all `logKey` values from layout JSON.
    c. Extract all `Logger.recordOutput("` keys from Java source: `grep -rn 'Logger.recordOutput(' src/main/java/ --include="*.java"`
    d. Cross-reference: flag any critical key from Rule 16A that exists in code but NOT in any layout tab.
    e. Verify `"version"` key exists at top level of each layout JSON.
    f. Verify `"field"` / `"game"` keys reference the current season (e.g., `FRC:2026 Field`).
16. **Dashboard config audit** (MANDATORY — do NOT skip):
    a. Locate dashboard files: `find . -name "*dashboard*.json" -o -name "*marsteam*.json"`
    b. Extract all `logKey` references from dashboard JSON.
    c. Cross-reference each key against `Logger.recordOutput` in source code to find orphaned keys.
    d. Verify all 5 pit-critical categories are covered (3D Field, Swerve, Power, Driver Inputs, Faults).
    e. Verify dashboard `"version"` matches layout `"version"`.
    f. If NO layout or dashboard JSON files exist, flag this as a **CRITICAL** defect and generate them.
17. Validate AI Skill parity — `SKILL.md` and `marketplace.json` correctly reference all directories.
18. Verify the documentation site for dead links and stale code snippets.
19. **Reading level audit** — verify all documentation meets middle school reading level standards.
20. Execute `./gradlew test jacocoTestReport` and analyze `.csv` output for untested classes.
21. Provide a summary checklist of detected defects.
22. Systematically remediate defects inline.
