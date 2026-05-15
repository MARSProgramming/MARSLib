---
name: marslib-data
description: Helps manage data flows — NetworkTables 4 coprocessor streams, AdvantageKit logging, and telemetry. Use when integrating coprocessors, reading NT4, or adding log keys.
---

# MARSLib Data & Telemetry

## 1. NetworkTables 4 (Coprocessor)

| Component | Purpose |
|---|---|---|
| `DoubleArraySubscriber` | Numeric arrays from coprocessors (ML bounding boxes) |
| `StructSubscriber` | WPILib struct data (Pose2d, etc.) |
| `@StructSerializable` | Binary serialization for bandwidth efficiency |
| IO `updateInputs()` | Reads NT4 inside AdvantageKit pipeline |

### Data Flow
```
Coprocessor → NT4 Server → IO.updateInputs() → Logger.processInputs() → Subsystem
```

### Rules
| Rule | Details |
|---|---|---|
| **NT4 Only** | No legacy NT3 patterns — use typed subscribers |
| **Read in updateInputs()** | NOT in periodic — ensures deterministic replay |
| **Check liveness** | If `timestamp - lastChange > 0.5s`, fall back to defaults |
| **Minimize bandwidth** | Use `@StructSerializable`, prune empty data, limit to 10-20Hz |

### Adding Coprocessor Integrations
1. Create IO interface with `@AutoLog` inputs
2. Create NT4 implementation reading subscribers in `updateInputs()`
3. Create sim implementation with test data
4. Add timeout check for `coprocessorConnected`
5. Consume only IO interface in subsystem

## 2. AdvantageKit Logging

| Component | Purpose |
|---|---|---|
| `@AutoLog` inner class | Auto-generates logging for IO inputs |
| `Logger.processInputs()` | Records IO inputs every periodic |
| `Logger.recordOutput()` | Records algorithm outputs |
| `LoggedTunableNumber` | Runtime-tunable with auto-logging |
| `.wpilog` files | Binary format for replay |

### Data Flow
```
Hardware → IO.updateInputs() → Logger.processInputs() → [Logged as Inputs]
                                    ↓
              Subsystem.periodic() algorithms → Logger.recordOutput() → [Logged as Outputs]
```

**Inputs** are replayed. **Outputs** are recomputed. This separation enables deterministic replay.

### Rules
| Rule | Details |
|---|---|---|
| **Never SmartDashboard** | All telemetry through `Logger` only |
| **Inputs ≠ Outputs** | Sensors in IO inputs, decisions in outputs |
| **Never branch on replay** | Algorithm must execute identically live/replay |
| **Match units (Sim vs Real)** | Unit mismatches cause "works in sim, fails on robot" |
| **Automated cloud logging** | `LogUploader` pushes `.wpilog` to GitHub when disabled |

### Adding Log Keys
1. For sensors: add field to `@AutoLog` inner class
2. For algorithms: `Logger.recordOutput("Subsystem/KeyName", value)`
3. Use nested paths: `"Superstructure/CollisionClamp"` not `"collision_clamp"`
4. Log structured types: `Pose2d`, `SwerveModuleState[]`, `Mechanism2d`

## 3. Telemetry Keys

**Coprocessor:**
- `{Coprocessor}/Connected` — Boolean: actively publishing
- `{Coprocessor}/Latency` — Time since last update
- `{Coprocessor}/FrameCount` — Cumulative frames
- `{Coprocessor}/Data` — Latest processed data

**Meta:**
- `Logger/Timestamp` — FPGA timestamp
- `Logger/LoopCycleMs` — Periodic cycle time
- `Logger/LogSizeBytes` — Cumulative log size
- `BuildConstants/Version` — Deployed version
- `BuildConstants/GitSHA` — Commit hash
