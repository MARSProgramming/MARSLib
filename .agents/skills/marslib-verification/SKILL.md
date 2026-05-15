---
name: marslib-verification
description: Helps write physics-backed JUnit 5 tests and configure dyn4j simulation. Use when writing tests, debugging sim/real disparities, or verifying mechanism behavior.
---

# MARSLib Verification (Testing & Simulation)

**Digital Twin Testing** — real subsystems with simulated IO, NOT mocked objects.

## 1. Architecture

| Component | Purpose |
|---|---|---|
| `*IOSim` classes | Physics-backed simulation |
| `MARSPhysicsWorld` | Shared dyn4j engine |
| `CommandScheduler` | Real WPILib scheduler |
| `SimHooks.stepTiming()` | Advances HAL clock |
| `DriverStationSim` | Simulates DS heartbeat |
| `MARSTestHarness` | Centralized singleton reset |

### Test Execution Loop
```java
for (int i = 0; i < 150; i++) {
    DriverStationSim.notifyNewData();       // Keep DS alive
    SimHooks.stepTiming(0.02);              // Advance 20ms
    CommandScheduler.getInstance().run();    // Process commands
    MARSPhysicsWorld.getInstance().update(0.02); // Step physics
}
```

## 2. Key Rules

| Rule | Why | How |
|---|---|---|
| **Never mock hardware** | Mocks hide physics bugs | Use `*IOSim` implementations |
| **Use MARSTestHarness.reset()** | Prevents cross-test contamination | Call in `@BeforeEach` |
| **Beat DS heartbeat** | Robot disables if not called | Call `notifyNewData()` EVERY loop iteration |
| **Inject config records** | IO layers decoupled from `frc.robot` | Use `MARSTestHarness.createSwerveConfig()` |

### MARSTestHarness.reset() Handles:
1. `HAL.initialize()`
2. `CommandScheduler` (cancel all, unregister subsystems)
3. `MARSPhysicsWorld.resetInstance()`
4. `AprilTagVisionIOSim.resetSimulation()`
5. `MARSFaultManager.clear()`
6. `Alert.resetAll()`
7. `DriverStationSim` (Blue1 alliance, enable, beat heartbeat)

## 3. Selective Testing

When modifying code, run only affected tests:
```bash
./gradlew test --tests *MARSPhysicsWorldTest
./gradlew test --tests *MARSPhysicsWorldTest.testGravity
```

## 4. ProfiledPIDController & Physics

**CRITICAL:** `ProfiledPIDController` relies on FPGA timestamp for $dt$.

- Without `SimHooks.stepTiming()`, $dt = 0.0$ → Trapezoid constraints skipped → raw P output
- **Always step timing** in loops using ProfiledPIDController
- Physics inertia causes realistic overshoot with high P/low D — reduce max velocity or add D
- `isFinished()` may take seconds to settle — validate physical bounds

## 5. Configuration Injection

| IO Layer | Required Config |
|---|---|
| `PowerIOSim` | `MARSTestHarness.createPowerConfig()` |
| `SwerveModuleIOTalonFX` | `MARSTestHarness.createSwerveConfig()` |
| `GyroIOPigeon2` | `MARSTestHarness.createSwerveConfig()` |

**NEVER** reference `frc.robot.*` constants from `com.marslib.*` test classes.

## 6. Test Categories

| Type | Example | Purpose |
|---|---|---|
| Unit | `MARSElevatorTest` | Single-mechanism physics |
| Integration | `RobotLifecycleTest` | Multi-subsystem coordination |
| Diagnostics | `MARSDiagnosticCheckTest` | Pre-match sweep |
| Math | `ShotSetupTest` | Algorithm correctness |
| State Machine | `MARSStateMachineTest` | Transition validation |
| Pipeline | `TeleopDrivePipelineTest` | Input pipeline regression |

## 7. Assert Against Physics

Test what mechanism **actually did**, not what state claims:

```java
// GOOD - assert physical result
assertEquals(targetPosition, elevator.getPositionMeters(), 0.01);

// BAD - assert internal state
assertTrue(elevator.isAtTarget()); // May be wrong due to bugs
```
