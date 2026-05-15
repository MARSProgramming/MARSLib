---
name: marslib-tools
description: Helps with external tools — diagnostic sweeps, elite team code mining, and AdvantageScope layouts. Use when ingesting code, debugging hardware, or configuring telemetry visualization.
---

# MARSLib External Tools Integration

## 1. Diagnostics (`MARSFaultManager`)

All hardware errors route through central fault manager:
- `MARSFaultManager.report(String)` — Log fault
- `MARSFaultManager.hasActiveCriticalFaults()` — Check for critical faults
- `MARSFaultManager.clear()` — Reset on enable

### Fault Levels
- **CRITICAL** — Robot should not continue (e.g., all modules disconnected)
- **WARNING** — Degraded operation (e.g., one sensor stale)
- **INFO** — Advisory (e.g., mechanism at software limit)

### Pre-Match Diagnostic Check
`MARSDiagnosticCheck` command sweeps all hardware before match start:
1. Verify all CAN devices responsive
2. Check gyro calibration complete
3. Validate vision has valid AprilTag detections
4. Report any faults to Shuffleboard/DriverStation

## 2. Elite Team Code Mining

**CRITICAL:** Do NOT use `WebSearch`. Use ONLY:
1. Direct raw file access: `https://raw.githubusercontent.com/[ORG]/[REPO]/[BRANCH]/[FILE_PATH]`
2. Git clone: `git clone --depth 1 [URL] [TEMP_DIR]`

### Top Tier Sources
| Team | Specialty | 2026 Repo |
|---|---|---|
| 6328 (Mechanical Advantage) | AdvantageKit creators | `RobotCode2026Public` |
| 254 (Cheesy Poofs) | Path following, state-space | `FRC-2025-Public` |
| 1690 (Orbit) | Targeting, continuous motion | `2024-Robot` |
| 2910 (Jack in the Bot) | Swerve architecture | `2025CompetitionRobot-Public` |
| 1678 (Citrus Circuits) | State-machine design | `C2025-Public` |
| 5940 (B.R.E.A.D.) | Vision fusion, odometry | `2025-Public` |

### Maple-Sim (Shenzhen Robotics Alliance)
**Repository:** `Shenzhen-Robotics-Alliance/maple-sim`

**2.5D Illusion:** Grounded pieces use dyn4j (2D), in-flight pieces use custom 3D kinematics with gravity ($g = -9.8 m/s^2$), re-enter dyn4j when $Z \le \text{radius}$.

### Ingestion Rules
- Analyze at least FIVE teams for any architectural question
- Prefer most recent season (2026/2025)
- Clone to temp scratch, never workspace root

### Porting Constraints
1. **Dependency Injection Only** — Discard singletons, translate to `@AutoLog` `HardwareIO`
2. **No Vendor Lock-in** — Eliminate direct TalonFX/SparkMAX, replace with math variables
3. **Strict Variable Formatting** — Nuke `m_` prefixes, use `camelCase`
4. **Logging Normalization** — Convert to AdvantageKit `LogTable`

## 3. AdvantageScope Layouts

**CRITICAL:** Do NOT use MCP `advantagescope-mcp_create_layout`/`update_tab`/`add_source` — they break serialization on 3.10+.

### Zod Schema by Controller
| Controller | Type | Required Options |
|---|---|---|
| Field3d | `robot` | `{ "model": "Robot" }` |
| Field3d | `ghost` | `{ "model": "Robot", "color": "#ff0000" }` |
| Field3d | `vision` | `{ "color": "#00ffff", "size": "normal" }` |
| Field2d | `robot` | `{ "bumpers": "" }` — NO model |
| Swerve | `states` | `{ "color": "#ff0000", "arrangement": "0,1,2,3" }` |
| LineGraph | `stepped`/`smooth` | `{ "color": "#ff0000", "size": "normal" }` |

### Competition Tab Order (default = #1)
1. Odometry Fusion 2D — primary match view
2. Playback 3D — log review
3. Simulation 3D — sim-only
4. Swerve Diagnostics — module health
5. System Health — loop time, CAN, battery
6. Power Draw — current, voltage
7. Fault Alerts — critical/warning/info
8. Tuning Table — feedforward gains
9. Documentation — reference only (ALWAYS last)

### Safe Defaults
- **Robot model:** ALWAYS use `"model": "Robot"` — built-in default
- **Game pieces:** Use `type: "ghost"` if variant string unknown (2026: `"Fuel"`)
- **No redundant tabs:** Each `logKey` in at most one line graph

### Log Key Completeness
Critical keys MUST be visualized:
- `Swerve/Pose`, `Swerve/MeasuredStates`, `Swerve/DesiredStates`
- `System/LoopRunTime_ms`, `System/BatteryVoltage`, `System/CANBusUtilization`
- `Alerts/Critical`, `Alerts/Warning`, `Alerts/Info`
- `TunableNumbers/*` (all feedforward gains)
