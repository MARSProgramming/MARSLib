---
name: marslib-systems
description: Helps configure control systems, power management, and threading. Use when tuning PID, setting current limits, managing CAN bus, or writing thread-safe code.
---

# MARSLib Systems (Control, Power, Threading)

## 1. Control Theory

**Feedforward > Feedback** principle:

| Layer | Runs On | Purpose |
|---|---|---|
| Feedforward (kS, kG, kV, kA) | roboRIO | Predicts voltage from physics |
| PID (kP, kI, kD) | Motor controller (1kHz) | Corrects FF error |
| SlewRateLimiter | roboRIO | Constrains driver acceleration |
| Motion Profile | Motor controller | Smooth trajectories |

### Feedforward Classes
- `ElevatorFeedforward` — Linear with gravity
- `ArmFeedforward` — Rotational with gravity
- `SimpleMotorFeedforward` — Flywheels/intakes

All gains stored as `LoggedTunableNumber` for live tuning.

### Rules
| Rule | Details |
|---|---|---|
| **Tune FF before PID** | SysId → set kS/kG/kV → should reach 90% accuracy |
| **Discretize ChassisSpeeds** | `ChassisSpeeds.discretize(speeds, dt)` prevents curve-while-spinning |
| **Clamp driver inputs** | `SlewRateLimiter` prevents wheel slip (typical: 3.0 m/s², 6.0 rad/s²) |
| **LoggedTunableNumber for gains** | Never hardcode `kP = 5.0` |

### Phoenix 6 Control Modes
| Mechanism | Control Mode |
|---|---|
| Intakes/Conveyors | `VoltageOut` or `DutyCycleOut` (open loop) |
| Swerve Wheels/Flywheels | `VelocityVoltage` (closed loop RPM) |
| Swerve Steering | `PositionVoltage` (fast positional) |
| Elevators/Arms | `MotionMagicVoltage` (trap/S-curve profiling) |

## 2. Power Management

### Current Limits
| Limit | Purpose | Typical Value |
|---|---|---|
| `SupplyCurrentLimit` | Prevent battery brownout | 40A |
| `StatorCurrentLimit` | Protect motor coils | 60-80A |

**Unrestricted Kraken/Falcon pulls 300A+ → instant brownout.**

### CAN Bus Utilization
- Target: < 80% utilization
- Fast updates (100Hz+) for live control only (encoder positions)
- Non-critical telemetry (temperatures) at 4-10Hz

```java
BaseStatusSignal.setUpdateFrequency(100, StatusSignal.kCANivore); // Fast
BaseStatusSignal.setUpdateFrequency(4, StatusSignal.kCANivore);  // Slow
```

### Power Shedding
Every mechanism accepts `MARSPowerManager`. In `periodic()`:
```java
double limit = powerManager.calculateLoadSheddedLimit(
    MAX_CURRENT, MIN_CURRENT, NOMINAL_VOLTAGE, CRITICAL_VOLTAGE);
io.setCurrentLimit(limit);
```

## 3. Threading Standards

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
Never call `.getInstance()` in 50Hz periodic loops — cache in constructor:
```java
private final PhoenixOdometryThread odometryThread;

public MySubsystem() {
    this.odometryThread = PhoenixOdometryThread.getInstance(); // Once
}

public void updateInputs() {
    var data = this.odometryThread.getLatestData(); // Direct access
}
```

### Thread Safety
| Pattern | Fix |
|---|---|
| Non-final fields in threads | Must be `volatile`, `Atomic*`, or locked |
| `synchronized` on 250Hz path | Remove — causes lock contention |
| `getInstance()` in periodic | Cache in constructor |

## 4. Constants

All gains live in mechanism's Constants inner class:
- `ElevatorConstants.kS/kG/kV/kA`
- `DriveConstants.SLEW_RATE_TRANSLATION/ROTATION`

## 5. Telemetry

**Control:**
- `{Mechanism}/kS`, `kG`, `kV`, `kA` — Current gains
- `{Mechanism}/TargetPosition` — Setpoint
- `{Mechanism}/PositionError` — Setpoint minus actual

**Power:**
- `Power/TotalCurrentDraw_A` — Total robot current
- `System/BatteryVoltage` — For brownout detection
- `System/CANBusUtilization` — % bandwidth used
