---
name: marslib-mining
description: Helps ingest, analyze, and port code from elite FRC teams and the maple-sim framework into MARSLib. Use when extracting trajectory math, state machines, vision systems, field layouts, or simulation physics.
---

# MARSLib Code Mining Skill

You are an expert repository mining engineer for Team MARS. When extracting code from external sources:

## 0. MANDATORY: Direct GitHub Access Only

**CRITICAL:** Do NOT use `WebSearch` or web browsing. Use ONLY:
1. **Direct raw file access:** `https://raw.githubusercontent.com/[ORG]/[REPO]/[BRANCH]/[FILE_PATH]`
2. **Git clone:** `git clone --depth 1 [URL] [TEMP_DIR]`

## 1. Elite FRC Team Manifest

### FRC Season Reference Table
| Year | Game Name |
|---|---|
| 2026 | Rebuilt |
| 2025 | Reefscape |
| 2024 | Crescendo |
| 2023 | Charged Up |
| 2022 | Rapid React |

### Top Tier Sources
| Team | Specialty | Repositories |
|---|---|---|
| **6328** (Mechanical Advantage) | AdvantageKit creators | `RobotCode2026Public`, `RobotCode2025Public`, `RobotCode2024Public`, `RobotCode2023`, `RobotCode2022`, `RobotCode2020` |
| **254** (Cheesy Poofs) | Path following, state-space | `FRC-2025-Public`, `FRC-2024-Public`, `FRC-2023-Public`, `FRC-2022-Public`, `FRC-2020-Public` |
| **1690** (Orbit) | Targeting, continuous motion | `2024-Robot` |
| **2910** (Jack in the Bot) | Swerve architecture | `2025CompetitionRobot-Public`, `2024CompetitionRobot-Public`, `2023CompetitionRobot-Public`, `2022CompetitionRobot`, `2021CompetitionRobot`, `2020CompetitionRobot` |
| **1678** (Citrus Circuits) | State-machine design | `C2025-Public`, `C2024-Public`, `C2023-Public`, `C2022-Public`, `cardinal-2021-public`, `C2020` |
| **3005** (RoboChargers) | AdvantageKit native, clean abstractions | `Reefscape-2025`, `Crescendo-2024-Public`, `Charged-Up-2023-Public`, `Rapid-React-2022-Public`, `Infinite-Recharge-2020` |
| **364** (BaseFalconSwerve) | Swerve template | `BaseFalconSwerve` |
| **111** (WildStang) | Component-based architecture | `2026_111_robot_software`, `2025_111_robot_software`, `2024_111_robot_software`, `2023_111_robot_software`, `2022_111_robot_software`, `2020_111_robot_software` |
| **2767** (Stryke Force) | ThirdCoast swerve, control math | `thirdcoast` framework, `2025-competition`, `crescendo`, `chargedup`, `rapidreact`, `infiniterecharge` |
| **5940** (B.R.E.A.D.) | Vision fusion, AdvantageKit, odometry | `2025-Public`, `2024-Onseason`, `2023-Onseason`, `2022-Onseason`, `2021-Robot`, `2020-Onseason` |
| **604** (Quixilver) | Simulation, WPILib architecture | `2025-public`, `2024-public`, `2023-public`, `2022-public`, `FRC-2021-v2` |
| **973** (Greybots) | Superstructure state machines | `2025-inseason`, `2023-inseason`, `2022-inseason` |
| **118** (Robonauts) | 4x World Champions, optimized systems | `2026-Robot-Code`, `2025-Robot-Code`, `2024-Robot-Code`, `2023-Robot-Code`, `2022-Robot-Code` |
| **195** (CyberKnights) | Competitive, documented | `2025-robot-code`, `2024-robot-code`, `2023-robot-code`, `2022-robot-code` |
| **148** (RoboVikes) | Documentation, infrastructure | `2025-robot-code`, `2024-robot-code`, `2023-robot-code`, `2022-robot-code` |
| **2471** (meanbycoding) | Modern AdvantageKit | `2025-robot-code`, `2024-robot-code`, `2023-robot-code`, `2022-robot-code` |
| **971** (Spartans) | Clean, documented | `Spartans-2025`, `Spartans-2024`, `Spartans-2023`, `Spartans-2022` |
| **2168** (Aluminum Falcons) | Sim/real hybrid patterns | `2025-Competition-Robot`, `2024-Competition-Robot`, `2023-Competition-Robot`, `2022-Competition-Robot` |
| **5013** (Grenading Gearheads) | Modern practices | `2025-robot-code`, `2024-robot-code`, `2023-robot-code`, `2022-robot-code` |
| **624** (CRyptonite) | Autonomous routines | `2025-robot-code`, `2024-robot-code`, `2023-robot-code`, `2022-robot-code` |
| **33** (Killer Bees) | Historical elite | `2025-robot-code`, `2024-robot-code`, `2023-robot-code`, `2022-robot-code` |
| **865** (Warbots) | AdvantageKit, modern | `2025-robot-code`, `2024-robot-code`, `2023-robot-code`, `2022-robot-code` |
| **6040** (Quasics) | Documentation, emerging | `2025-robot-code`, `2024-robot-code`, `2023-robot-code`, `2022-robot-code` |
| **5818** (MegaHurtz 2.0) | Quality control | `2025-robot-code`, `2024-robot-code`, `2023-robot-code`, `2022-robot-code` |
| **4414** (HighTide) | Swerve controls | `2025-robot-code`, `2024-robot-code`, `2023-robot-code` |
| **3476** (Code Orange) | Vision fusion (CodeOrangePoseEstimator) | `2025-robot-code`, `2024-robot-code`, `2023-robot-code` |
| **8033** (Highlander Robotics) | Modern AdvantageKit | `2025-robot-code`, `2024-robot-code`, `2023-robot-code` |
| **1323** (MadTown Robotics) | Subsystem packaging | `2025-robot-code`, `2024-robot-code`, `2023-robot-code` |
| **125** (Nutrons) | Various seasons | `nutrons` |
| **4099** | Various seasons | `Team4099` |
| **1540** (Flaming Chickens) | Updated for 2025 | `Flaming-Chickens-2025` |
| **1736** | Various seasons | `RobotCode-2025` |
| **9771** | Various seasons | `2025-competition` |
| **852** | Various seasons | `2025-robot-code` |

### Access Pattern
```javascript
// Correct - direct raw access
webReader("https://raw.githubusercontent.com/Team254/FRC-2024-Public/main/src/main/java/com/team254/frc2024/subsystems/Swerve.java")

// Wrong - NEVER use web search
WebSearch("Team 254 swerve implementation")
```

## 2. Maple-Sim Mining (Shenzhen Robotics Alliance)

**Repository:** `Shenzhen-Robotics-Alliance/maple-sim`

### The 2.5D Illusion Architecture
Maple-sim bridges 2D physics (dyn4j) with 3D telemetry:
1. **Grounded Pieces (2D)** — `dyn4j.Body` with friction, mass, collision
2. **In-Flight Pieces (3D)** — Removed from dyn4j, handed to custom 3D kinematics integrator with gravity ($g = -9.8 m/s^2$)
3. **Re-entry Hook** — When $Z \le \text{piece radius}$, destroy projectile and inject new `dyn4j.Body` at $XY$ with ground velocity

### Extracting Season-Specific Fields
- Look under `org.ironmaple.simulation.seasonspecific`
- Extract 3D target coordinates for goals
- Scoring is via volumetric intersection: `Math.pow(Z - targetZ, 2) + Math.pow(Y - targetY, 2) < Math.pow(GoalRadius, 2)`
- Successful scoring deletes projectile and cascades new one downward with randomized velocity

### Implementing in MARSLib
- `MARSPhysicsWorld` maintains `List<SimulationProjectile>`
- Call `.update(dtSeconds)` for each active projectile during world update
- Perform volumetric scoring BEFORE ground-collision checks
- Serialize both grounded bodies and active projectiles to `PhysicsWorld/GamePieces`

## 3. Ingestion Rules

- **Multi-Team Sourcing:** Analyze at least FIVE different teams for any architectural question
- **Exhaustive Search:** If initial repos don't have answers, expand to additional teams from manifest
- **Strict Recency Bias:** Prefer most recent season (2026/2025), but honor user requests for specific years
- **Isolated Cloning:** Always clone to temp scratch directory, never workspace root

## 4. Porting Constraints (Non-Negotiable)

1. **Dependency Injection Only:** Discard singletons (`Drive.getInstance()`). Translate to operate inside `@AutoLog` `HardwareIO` interfaces.
2. **No Vendor Lock-in API Bleed:** Eliminate direct TalonFX/SparkMAX calls. Replace with math variables (Target Volts, System States) passed to IO layers.
3. **Strict Variable Formatting:** Nuke `m_` prefixes. Use unannotated `camelCase` standardizing to modern Java.
4. **Logging Normalization:** Convert `SmartDashboard`, `ShuffleBoard` to AdvantageKit `LogTable` or auto-logging outputs.

## 5. Pattern Matching Heuristics

When searching large repos, anchor around key API landmarks:
- **Drivetrain:** `SwerveModuleState`, `ChassisSpeeds`, `Phoenix6`
- **Localization:** `PoseEstimator`, `Vision`, `LimelightHelpers`, `PhotonCamera`
- **Control:** `StateSpace`, `Matrix`, `LQR`, `EKF`
- **UI:** `Dashboard`, `Shuffleboard`, `NetworkTable`, `AdvantageScope`, `Elastic`
- **Logging:** `Logger`, `AdvantageKit`, `AutoLog`, `DataLogManager`
- **Input:** `Controller`, `HID`, `Joystick`, `Haptic`, `Rumble`

## 6. Exit Validation

Before surfacing ported code:
- Validate `spotlessApply` compliance
- Confirm no unresolved static imports or `HidingField` errors
- Verify integration with MARSLib's `SwerveConfig` injection pattern (no `frc.robot.*` imports in `com.marslib.*`)
- Cross-reference: If adopting from one elite team, verify against at least two others before considering it best practice
