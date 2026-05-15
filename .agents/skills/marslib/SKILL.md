---
name: marslib
description: Helps write FRC robot code using the MARSLib Advanced Simulation and Abstraction framework. Use when creating new subsystems, writing hardware IO layers, configuring Dyn4j simulation physics, or setting up fault management.
---

# MARSLib Framework Skill

You are an expert FRC Software Engineer for Team MARS 2614. This is the **root skill** defining core architectural rules that apply to ALL MARSLib code.

## 0. Skill Maintenance

**CRITICAL:** Whenever you make architectural or significant code changes to the library, you **MUST** identify and update the relevant `SKILL.md` files to reflect the new truth.

## 1. IO Abstraction (AdvantageKit Rule)

Every subsystem MUST abstract hardware behind an IO interface. Generate exactly four files:
1. **`[Name]IO.java`** — Interface with `@AutoLog` inner class `[Name]IOInputs` (struct MUST end with `Inputs`)
2. **`[Name]IOSim.java`** — Physics sim using `MARSPhysicsWorld` and dyn4j
3. **`[Name]IOTalonFX.java`** — Real hardware via Phoenix 6
4. **`[Name].java`** — Subsystem accepting `[Name]IO` via dependency injection

In `periodic()`: call `io.updateInputs(inputs)` then `Logger.processInputs("[Name]", inputs)`.

## 2. Core Standards (Apply to ALL Code)

### Unit Nomenclature
- Physical units MUST be explicit: `double velocityMetersPerSecond`, `double wheelRadiusInches`, `double delaySeconds`
- Use `edu.wpi.first.units` for public interfaces; suffix internal doubles with SI units
- **BAD:** `double velocity;`, `int x;` — **GOOD:** `double velocityMetersPerSecond;`, `int xIndex;`

### No Hungarian Notation
- **BAD:** `double m_velocity;`, `final int k_maxSpeed;`, `boolean bIsActive;`
- **GOOD:** `double velocity;`, `final int MAX_SPEED;`, `boolean isActive;`

### Never Nester
Use guard clauses and early returns:
```java
// BAD - nested
if (item != null) {
    if (item.isValid()) {
        item.process();
    }
}
// GOOD - guard clauses
if (item == null) return;
if (!item.isValid()) return;
item.process();
```

### Math References
Include comment blocks referencing underlying physics (Wiki, whitepaper, textbook) when implementing equations.

### Zero-Allocation Hot Paths
Never use `new` in `periodic()` or 250Hz loops. Pre-allocate `static final` caches. Mutable proxies must be completely overwritten each tick, never `+=` or `*=`.

### AdvantageKit Logging
Never `System.out.println()`. Always use `Logger.recordOutput("Category/Subsystem", value)` for deterministic replay.

### @AutoLog Boundary
- Struct handling data MUST end with `Inputs` (e.g., `ElevatorIOInputs`)
- Hardware implementation files (`*IOTalonFX.java`) do NOT need `@AutoLog`
- Never put fake `// @AutoLog` comments to suppress warnings

## 3. Core Constraints

| Rule | Details | Skill |
|---|---|---|
| No SmartDashboard for telemetry | Use `Logger.recordOutput()` and `LoggedTunableNumber`. `SmartDashboard.putData()` only for interactive widgets. | `marslib-telemetry` |
| No Mockito in tests | Use `*IOSim` with dyn4j physics instead | `marslib-testing` |
| Phoenix 6 only | No Phoenix 5 APIs (`WPI_TalonFX`, `TalonFXControlMode`) | `marslib-power` |
| Dual current limits | Always set both Stator and Supply limits on TalonFX | `marslib-power` |
| CAN bus < 80% | Drop non-critical signal frequencies to 4-10Hz | `marslib-power` |
| Collision routing | Multi-mechanism moves go through `MARSSuperstructure` | `marslib-superstructure` |
| Faults through manager | All hardware errors go via `MARSFaultManager` | `marslib-diagnostics` |

## 4. Threading Standards

When creating background loops (`PhoenixOdometryThread`, coprocessor listeners):

### No Infinite Loops
Always check interruption:
```java
while (!Thread.currentThread().isInterrupted()) {
    // ... logic
}
```

### No Raw Thread Sleeps
Use `TimeUnit` with proper interrupt handling:
```java
try {
    TimeUnit.MILLISECONDS.sleep(20);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt(); // Restore status
    break; // Exit gracefully
}
```

### Cache Thread References
Never call `.getInstance()` in 50Hz periodic loops. Cache in constructor:
```java
private final PhoenixOdometryThread odometryThread;

public MySubsystem() {
    this.odometryThread = PhoenixOdometryThread.getInstance(); // Once
}

public void updateInputs() {
    var data = this.odometryThread.getLatestData(); // Direct access
}
```

## 5. Domain Skills Index

| Domain | Skill | Covers |
|---|---|---|
| Drivetrain | `marslib-swerve` | SwerveDrive, odometry, PathPlanner |
| Mechanisms | `marslib-mechanisms` | Elevator, arm, intake, shooter IO |
| Superstructure | `marslib-superstructure` | Collision safety, state coordination |
| State Machines | `marslib-statemachine` | Generic FSM framework |
| Autonomous | `marslib-autonomous` | PathPlanner, Choreo, alignment |
| Shot Setup | `marslib-shotsetup` | EliteShooterMath SOTM solver |
| Vision | `marslib-vision` | AprilTag fusion, VIO SLAM |
| Simulation | `marslib-simulation` | dyn4j physics, field boundaries |
| Controls | `marslib-control-theory` | PID, feedforward, SysId |
| Power | `marslib-power` | Current limits, CAN bus, brownout |
| Telemetry | `marslib-telemetry` | AdvantageKit logging, replay |
| Diagnostics | `marslib-diagnostics` | Faults, alerts, pre-match checks |
| Operator | `marslib-operator` | Controller bindings, haptics, LEDs |
| Math | `marslib-math` | Interpolation, filtering, transforms |
| Network | `marslib-network` | NT4, coprocessor streams |
| Testing | `marslib-testing` | JUnit 5, singleton resets, physics |
| Mining | `marslib-mining` | Elite team code ingestion (FRC + maple-sim) |
| CI/CD | `marslib-ci` | Gradle, Spotless, GitHub Actions |
| Documentation | `marslib-documentation` | Astro/Starlight/Keystatic, accessibility |

## 6. File Limits

Restrict logic classes to ~600 lines max. Refactor large loops into helper libraries.
