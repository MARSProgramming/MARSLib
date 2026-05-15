---
name: marslib-audit
description: Helps execute championship-grade audits of MARSLib. Use when checking zero-allocation, determinism, architectural safety, and compliance.
---

# MARSLib Code Audit Skill

You are a senior reliability engineer auditing MARSLib for memory allocations, determinism flaws, and bad practices.

## 1. Zero-Allocation Enforcement

| Rule | Pattern | Fix |
|---|---|---|
| No `new` in periodic | `grep -rn "new Pose2d\|new ChassisSpeeds" src/main/java/com/marslib/ --include="*IO.java" -A 2 -B 2 \| grep periodic` | Pre-allocate `static final` caches |
| No dynamic arrays | `.toArray(new Type[0])` in hot paths | Statically pre-allocate with locks/atomics |
| No manual GC | `System.gc()` in `disabledInit()` | Delete immediately |
| No raw stack traces | `e.printStackTrace()` | Use `DriverStation.reportError()` |

## 2. Math Safety

| Rule | Pattern | Fix |
|---|---|---|
| Division by zero | Unprotected `/ x` | `Math.abs(x) < 1e-6` epsilon check |
| Angle wrapping | `% 360` or `% (2 * Math.PI)` | `MathUtil.angleModulus()` or `Rotation2d.minus()` |
| Domain violations | `Math.sqrt()` of negative, `Math.asin()` >1 | Clamp to valid domain first |
| Unit safety | Magic conversions like `* 0.0174533` | `Units.degreesToRadians()` or `Meters.of()` |
| NaN bounds | Interpolations without checks | Wrap in `Double.isFinite(guess)` |
| Kinematic reversals | Negative speeds without flip | `speed < 0 ? rotateBy(180deg) : ...` |

## 3. API Safety

| Rule | Pattern | Fix |
|---|---|---|
| No naked `.get()` | `Optional.get()` in teleop loops | `.orElse()` or `.ifPresent()` |
| Branch coverage | Classes lacking tests | Generate boundary-condition tests |

## 4. Control Theory & Hardware

| Rule | Check | Fix |
|---|---|---|
| Integral windup | PID without `setIZone()` | Add zone boundaries |
| Current limits | Missing `SupplyCurrentLimit` (40A) or `StatorCurrentLimit` (60-80A) | Add both limits |
| CAN bus utilization | Non-critical signals at 100Hz+ | Drop to 4-10Hz via `setUpdateFrequency()` |
| Hardware timeouts | `waitForUpdate()` without timeout | Add `Timeout = 50ms` |

## 5. CAN StatusCode Verification

```bash
# Find all .apply() calls without status checks
grep -rn "\.apply(" src/main/java/com/marslib/ --include="*.java"
```

Every `motor.getConfigurator().apply(config)` and `setUpdateFrequency()` must:
1. Check `StatusCode.isOK()` and log failures, OR
2. Use retry loop (up to 5 retries for CAN contention)

## 6. Thread Safety

| Rule | Pattern | Fix |
|---|---|---|
| Shared mutable state | Non-final fields in `PhoenixOdometryThread` | Must be `volatile`, `Atomic*`, or locked |
| Lock contention | `synchronized` on 250Hz path | Verify NO synchronized blocks in odometry thread |
| Singleton caching | `getInstance()` in periodic | Cache in constructor only |

## 7. Command Lifecycle

| Rule | Check | Fix |
|---|---|---|
| Every command terminates | `extends Command` without `isFinished()` | Add `isFinished()` or wrap with `.withTimeout()` |
| PID timeouts | Alignment commands without timeout | Always compose with `.withTimeout(2-5s)` |

## 8. AdvantageKit Replay Contract

```bash
# Verify no direct hardware reads outside IO layers
grep -rn "Timer.getFPGATimestamp\|RobotController.getBatteryVoltage\|DriverStation\." \
  src/main/java/com/marslib/ --include="*.java" \
  | grep -v "IO\.java\|IOSim\.java\|IOTalonFX\.java\|AllianceUtil\|IOLimelight\|IOPigeon2"
```

Every IO interface must have `@AutoLog` on its `Inputs` inner class:
```bash
grep -rn "class.*Inputs" src/main/java/com/marslib/ --include="*IO.java" | grep -B 1 "@AutoLog"
```

## 9. Graceful Degradation

| Rule | Check | Fix |
|---|---|---|
| Hardware disconnection | `hasHardwareConnected` not set in `*IOReal` | Set flag based on CAN status |
| NaN firewalls | `getValueAsDouble()` without `Double.isFinite()` | Add ternary checks at IO boundaries |
| Stale data | Vision/IMU without timestamp checks | Reject if `timestamp` too old |

## 10. Autonomous Safety

| Rule | Check | Fix |
|---|---|---|
| Path load fallbacks | `try/catch` without safe fallback | Return `Commands.none()` not `null` |
| Auto timeouts | No `.withTimeout()` on routines | Add top-level timeout wrapper |

## 11. Documentation Standards

| Rule | Check | Fix |
|---|---|---|
| Javadoc completeness | Public methods without `@param`/`@return` | Add with units |
| Reading level | Flesch-Kincaid > 8.0 | Simplify: active voice, shorter sentences, analogies |

## 12. AdvantageScope Layout Audit

```bash
# Find layout files
find . -name "*layout*.json" -o -name "*advantagescope*.json"

# Required log keys:
# - Swerve/Pose, Odometry/RobotPose, Robot/Pose3d
# - Swerve/MeasuredStates, Swerve/DesiredStates
# - Vision/ValidPoses/*, Vision/CameraFrustums/*
# - System/LoopRunTime_ms, System/BatteryVoltage, System/CANBusUtilization
# - Alerts/Critical, Alerts/Warning, Alerts/Info
# - TunableNumbers/*
```

Verify:
1. `"version"` key exists at top level
2. `"field"`/`"game"` references current season (e.g., `FRC:2026 Field`)
3. All critical keys from code are visualized

## 13. Dashboard Integrity

Pitside dashboard (`marsteam_dashboard.json`) must have:
1. 3D Field View with `Swerve/Pose`
2. Swerve Diagnostics (Measured + Desired states)
3. System Health (loop time, CAN utilization)
4. Fault Alerts (all severity levels)
5. Power Draw (current, voltage)

## 14. AI Skill Parity

Every `.agents/skills` directory must have valid `SKILL.md`.
`marketplace.json` must reference all populated directories.
Skills must reflect latest architecture (SwerveConfig injection, no `frc.robot.*` in `com.marslib.*`).

## 15. Audit Workflow

```bash
# 1. Static analysis
./gradlew spotlessCheck pmdMain checkstyleMain

# 2. Zero-allocation scan
grep -rn "new " src/main/java/com/marslib/ --include="*.java" | grep -i periodic

# 3. Math validation
grep -rn "% 360\|Optional.get()\|e.printStackTrace" src/main/java/com/marslib/

# 4. Coverage
./gradlew test jacocoTestReport

# 5. Report defects and remediate inline
```

## 16. Build Configuration

- `jacoco.toolVersion` ≥ `0.8.12` (Java 21 compatible)
- `org.gradle.configuration-cache = false` (GradleRIO incompatibility — NOT a defect)
- WPILib version must match competition season target
