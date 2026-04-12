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
**Audit Action**: Every folder inside `.agents/skills` MUST possess a valid `plugin.json` manifest, otherwise the AI won't load it. Second, the audit must cross-reference `.agents/skills/marketplace.json` to ensure every populated directory is actively injected.

### Rule B: Outdated Algorithmic Context
**Audit Action**: Scan through `SKILL.md` rules inside the agent repository and enforce that they reflect the latest architectural choices (e.g. confirming `marslib-swerve` dictates the use of the new `SwerveOdometry` component instead of allowing monolithic drive generation).

## 8. Documentation & Educational Hub

### Rule A: Javadoc Integrity & API Synchronization
If the core classes are updated without documentation, teams cloning MARSLib will misuse them.
**Audit Action**: Every public `com.marslib` algorithm class MUST have valid, WPILib-style class-level and method-level Javadocs. The audit must detect `Missing @param` warnings or completely undocumented static logic paths.

### Rule B: GitHub Pages Site Integrity (github.io)
MARSLib maintains an interactive web documentation hub. A broken link or outdated code snippet destroys the educational value for other drive teams.
**Audit Action**: Audit the `docs/*.html` and Markdown files for broken local hyperlinks. Verify that Code Snippets present in the tutorials exactly match the current signature of the Java API.

## 9. Typical Audit Workflow
1. Verify the state of the Build configuration (gradle/jacoco).
2. Execute `./gradlew pmdMain checkstyleMain` to catch immediate documentation and static analysis flaws.
3. Scan for Zero-Allocation violations and NetworkTable threading locks.
4. Highlight Class Size violations (>400 lines) and "God Objects".
5. Search for Optional misusage strings and Naked Modulo expressions.
6. Execute Math Validation checks scanning for unprotected division and `Math.sqrt()` inputs.
7. Check hardware allocations for stringent Current Limits and CAN Bus frequency drop-offs.
8. Validate AI Skill parity, ensuring `plugin.json` and `marketplace.json` correctly load all existing directories.
9. Verify the GitHub Pages (github.io) integration for dead links and missing Javadoc implementations.
10. Execute `gradlew test jacocoTestReport` and analyze `.csv` output for untested mathematical helper classes.
11. Provide a summary checklist of detected errors.
12. Systematically remediate them inline.
