---
name: marslib-testing
description: Helps write JUnit 5 tests for MARSLib with physics-backed simulation. Use when writing tests for WPILib commands, subsystems, or autonomous behaviors.
---

# MARSLib Integrated Testing

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
| **Beat DS heartbeat** | Robot disables if not called every ~0.5s | Call `notifyNewData()` EVERY loop iteration |
| **Inject config records** | IO layers are decoupled from `frc.robot` | Use `MARSTestHarness.createSwerveConfig()` |

### MARSTestHarness.reset() Handles:
1. `HAL.initialize()`
2. `CommandScheduler` (cancel all, unregister subsystems)
3. `MARSPhysicsWorld.resetInstance()`
4. `AprilTagVisionIOSim.resetSimulation()`
5. `MARSFaultManager.clear()`
6. `Alert.resetAll()`
7. `DriverStationSim` (Blue1 alliance, enable, beat heartbeat)

### Test Pattern
```java
@BeforeEach
public void setUp() {
    MARSTestHarness.reset();
    // construct subsystems with *IOSim
}

@AfterEach
public void tearDown() {
    MARSTestHarness.cleanup();
}
```

## 3. Selective Testing (CRITICAL)

When modifying code, **NEVER** run full test suite blindly. Run only affected tests:

```bash
# Run specific test class
./gradlew test --tests *MARSPhysicsWorldTest

# Run specific test method
./gradlew test --tests *MARSPhysicsWorldTest.testGravity
```

## 4. Configuration Injection

All IO layers require config records:

| IO Layer | Required Config |
|---|---|
| `PowerIOSim` | `MARSTestHarness.createPowerConfig()` |
| `SwerveModuleIOTalonFX` | `MARSTestHarness.createSwerveConfig()` |
| `GyroIOPigeon2` | `MARSTestHarness.createSwerveConfig()` |

**NEVER** reference `frc.robot.*` constants from `com.marslib.*` test classes.

## 5. ProfiledPIDController & Physics

**CRITICAL:** `ProfiledPIDController` relies on FPGA timestamp for $dt$.

- Without `SimHooks.stepTiming()`, $dt = 0.0$ → Trapezoid constraints skipped → raw P output
- **Always step timing** in loops using ProfiledPIDController
- Physics inertia causes realistic overshoot with high P/low D — reduce max velocity or add D gain
- `isFinished()` may take seconds to settle under physics — validate physical bounds instead of absolute convergence

## 6. Test Categories

| Type | Example | What It Catches |
|---|---|---|
| Unit | `MARSElevatorTest` | Single-mechanism physics |
| Integration | `RobotLifecycleTest` | Multi-subsystem coordination |
| Diagnostics | `MARSDiagnosticCheckTest` | Pre-match sweep validation |
| Math | `KinematicAimingTest`, `ShotSetupTest` | Algorithm correctness |
| State Machine | `MARSStateMachineTest` | Transition validation |
| Pipeline | `TeleopDrivePipelineTest` | Joystick→ChassisSpeeds math |

## 7. Reference Implementations

- `MARSSuperstructureTest` — Physics-backed state transitions
- `RobotLifecycleTest` — Full auto→teleop→score→stow lifecycle
- `MARSStateMachineTest` — FSM transition validation
- `MARSAlignmentCommandTest` — PID convergence under physics
- `TeleopDrivePipelineTest` — Input pipeline regression

## 8. Testing Commands

```java
// Test command termination
assertTrue(command.isFinished());

// Test state machine transitions
assertEquals(SuperstructureState.SCORE, superstructure.getCurrentState());
assertEquals(1, superstructure.getStateMachine().getTotalTransitionCount());
```

## 9. Assert Against Physics, Not State

Test what the mechanism **actually did**, not what command state claims:

```java
// GOOD - assert physical result
assertEquals(targetPosition, elevator.getPositionMeters(), 0.01);

// BAD - assert internal state
assertTrue(elevator.isAtTarget()); // May be wrong due to bugs
```
