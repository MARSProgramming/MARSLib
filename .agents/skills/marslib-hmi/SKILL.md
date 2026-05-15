---
name: marslib-hmi
description: Helps configure human-machine interfaces — controller bindings, LED feedback, haptics, and dashboard views. Use when binding buttons, adding rumble, or configuring visual feedback.
---

# MARSLib HMI (Human-Machine Interface)

## 1. Architecture

| Component | Purpose |
|---|---|---|
| `OperatorInterface` | Controller mapping, deadband config |
| `LEDManager` | Priority-based LED pattern selection |
| `LEDIO` | IO interface for LED hardware |
| `LEDIOAddressable` | WPILib AddressableLED (PWM strip, SIM) |
| `LEDIOCANdle` | CTRE CANdle (CAN-based, REAL) |
| `RobotContainer` | Command bindings, auto chooser |
| `TeleopDriveMath` | Joystick→ChassisSpeeds math |

### Dual Controller Layout
- **Port 0** — Drive pilot (translation, rotation, intake, shoot, climb)
- **Port 1** — Copilot (manual feed, scoring, climb controls)

### LED Priority Cascade
```
1. CRITICAL FAULT   → Red flash (MARSFaultManager)
2. LOW VOLTAGE      → Amber pulse (MARSPowerManager warning)
3. GAME PIECE HELD  → Green solid
4. ALLIANCE IDLE    → Blue/Red based on DriverStation
```

## 2. Controller Bindings

### Pilot (Port 0)
| Input | Action |
|---|---|
| Left Stick | Translation (field-relative, slew-limited) |
| Right Stick X | Rotation (gyro-lock when idle) |
| Left Trigger | Intake → STOW on release |
| Right Trigger | Shoot-On-The-Move + SCORE → STOW on release |
| A | Slamtake → STOW on release |
| B | Stationary SCORE → STOW on release |
| Left Bumper | UNJAM → STOW on release |
| Right Bumper | SCORE → STOW on release |
| DPad Right | Deploy intake (no spin) |
| DPad Left | STOWED |
| DPad Up/Down | Climber extend/retract |
| Back + Start | Ghost recording |

### Copilot (Port 1)
| Input | Action |
|---|---|
| Left Trigger | Manual feed |
| Right Trigger | Fixed SCORE → STOW on release |
| Right Bumper | Fixed SCORE → STOW on release |
| Left Bumper | Cowl home |
| DPad Down | Climber reverse |
| X | Emergency swerve stop |

## 3. Key Rules

| Rule | Details |
|---|---|
| **Every command declares requirements** | Command MUST `requires()` every subsystem it touches |
| **Haptic feedback for invisible events** | Rumble for piece collected, alignment locked |
| **LoggedTunableNumber for overrides** | Driver-adjustable values use this for real-time tuning |
| **Dashboard minimalism** | Main tab shows pose, state, possession, faults only |
| **LEDManager accepts MARSPowerManager** | Participates in power shedding during brownout |
| **Hardware in IOReal only** | All CANdle API calls in `LEDIOCANdle`, not LEDManager |
| **LED state logged** | `Logger.recordOutput("LED/CurrentPattern", pattern.name())` |

## 4. Adding New Bindings

1. Define binding in `RobotContainer`: `controller.{button}().onTrue/whileTrue(...)`
2. Ensure command `requires()` every subsystem it actuates
3. For release actions: `.onFalse(superstructure.setAbsoluteState(STOWED))`
4. Add rumble if event is invisible to driver
5. Update button map in this skill's §2 section
6. Test in `RobotContainerTest`

## 5. LED Patterns

### Adding New Patterns
1. Add pattern to `LEDManager` priority logic
2. Define priority level (faults always highest)
3. Test with `LEDIOAddressable` in sim (no CAN required)
4. Log pattern name via AdvantageKit

### Constants
```java
Constants.LEDConstants.CANDLE_ID = 24;
Constants.LEDConstants.CANBUS = "CAN2";
Constants.LEDConstants.LENGTH = 70;
```

### Dependency Injection
```java
// SIM / REPLAY: AddressableLED (software-only)
ledManager = new LEDManager(new LEDIOAddressable(0, length), powerManager);

// REAL: CANdle hardware
ledManager = new LEDManager(new LEDIOCANdle(canId, canbus, length), powerManager);
```

## 6. Telemetry

**Teleop:**
- `Teleop/RawJoystickX/Y/Omega` — Raw inputs
- `Teleop/PostDeadband` — After deadband [x, y, omega]
- `Teleop/FieldRelSpeeds` — Field-relative [vx, vy, omega]
- `Teleop/RobotRelSpeeds` — Robot-relative [vx, vy, omega]
- `Teleop/GyroLockActive` — Heading hold boolean

**LED:**
- `LED/CurrentPattern` — Active pattern name
- `LED/FaultFlashActive` — Fault override boolean
