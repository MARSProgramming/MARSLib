---
name: marslib-elite-mining
description: Helps ingest, analyze, and port advanced code from World Champion and Elite FRC teams directly into the MARSLib architecture. Use when extracting custom trajectory math, state-machine layouts, vision fusion systems, user interfaces, data logging, control frameworks, and any other sub-systems you'd program for an FRC robot. Validates and translates from specific high-performing repositories like 1690, 254, 6328, 2910, etc.
---

You are an expert repository mining engineer for Team MARS. When extracting custom trajectory math, state-machine layouts, vision fusion systems, or control frameworks from Elite FRC teams, adhere strictly to the following guidelines.

# MARSLib Elite Repository Mining Agent

This skill dictates how to safely clone, parse, translate, and securely integrate logic from Elite FRC team implementations into the `.MARSLib` infrastructure without degrading our code style or creating arbitrary hardware couplings.

**CRITICAL RULE:** Do NOT under any circumstances use `search_web` tools to find repositories. You must rely EXCLUSIVELY on the methods below to access GitHub directly.

## MANDATORY GITHUB ACCESS METHODS

> [!NOTE]
> **Universal AI Fallback Directive:** If your specific AI environment lacks autonomous terminal access (no `git clone`) or lacks an internal URL reading tool (no `webReader`), you MUST output the required `raw.githubusercontent.com` URLs or the exact `git clone` bash commands directly to the user. Instruct the user to manually fetch the file contents or run the command and paste the output back to you before proceeding.

**PRIMARY METHOD: Direct Raw File Access**
Use your available web reading tool (e.g., `webReader`, `read_url_content`, `curl`) to fetch raw file contents directly from GitHub:
```
URL Pattern: https://raw.githubusercontent.com/[ORG]/[REPO]/[BRANCH]/[FILE_PATH]
Example: https://raw.githubusercontent.com/Team254/FRC-2024-Public/main/src/main/java/com/team254/frc2024/subsystems/Swerve.java
```

**SECONDARY METHOD: Repository Cloning**
When needing multiple files or full repository analysis:
```bash
git clone --depth 1 [EXACT_REPO_URL] [TEMP_DIR]
```

**FORBIDDEN METHODS:**
- ❌ NO `WebSearch` tool for finding repositories
- ❌ NO `search_web` tools for any GitHub access
- ❌ NO web browsing of GitHub UI pages
- ✅ ONLY direct raw file access or git clone

## 1. Top Tier Manifest (The Elite Hit List)

### FRC Season Reference Table
Use this table to map common game names to their respective competition years when parsing repository names.

| Year | FRC Game Name |
| :--- | :--- |
| **2026** | Rebuilt |
| **2025** | Reefscape |
| **2024** | Crescendo |
| **2023** | Charged Up |
| **2022** | Rapid React |
| **2021** | Infinite Recharge (At Home) |
| **2020** | Infinite Recharge |
| **2019** | Destination: Deep Space |
| **2018** | FIRST Power Up |

When tasked with "seeing how X team solved Y problem," use the following catalog to inform your specific GitHub URLs:

*   **Team 6328 (Mechanical Advantage):** Core *AdvantageKit* creators. Use for evaluating logging abstraction structures.
    *   2026: `https://github.com/Mechanical-Advantage/RobotCode2026Public`
    *   2025: `https://github.com/Mechanical-Advantage/RobotCode2025Public`
    *   2024: `https://github.com/Mechanical-Advantage/RobotCode2024Public`
    *   2023: `https://github.com/Mechanical-Advantage/RobotCode2023`
    *   2022: `https://github.com/Mechanical-Advantage/RobotCode2022`
    *   2020: `https://github.com/Mechanical-Advantage/RobotCode2020`
*   **Team 254 (The Cheesy Poofs):** Pioneers of arbitrary *Path Following* and *State-Space*. Use for custom math controllers and pure-pursuit trajectory logic.
    *   2025: `https://github.com/Team254/FRC-2025-Public`
    *   2024: `https://github.com/Team254/FRC-2024-Public`
    *   2023: `https://github.com/Team254/FRC-2023-Public`
    *   2022: `https://github.com/Team254/FRC-2022-Public`
    *   2020: `https://github.com/Team254/FRC-2020-Public`
*   **Team 1690 (Orbit):** Elite *Targeting* and *Continuous Motion*. Use for shoot-on-the-move math and high-speed multi-subsystem orchestration.
    *   2024: `https://github.com/Orbit-Robotics/2024-Robot`
*   **Team 2910 (Jack in the Bot):** *Swerve Architecture* pioneers. Use to analyze SDS motor/encoder geometric models.
    *   2025: `https://github.com/FRCTeam2910/2025CompetitionRobot-Public`
    *   2024: `https://github.com/FRCTeam2910/2024CompetitionRobot-Public`
    *   2023: `https://github.com/FRCTeam2910/2023CompetitionRobot-Public`
    *   2022: `https://github.com/FRCTeam2910/2022CompetitionRobot`
    *   2021: `https://github.com/FRCTeam2910/2021CompetitionRobot`
    *   2020: `https://github.com/FRCTeam2910/2020CompetitionRobot`
*   **Team 1678 (Citrus Circuits):** *State-Machine Design* & System Reliability. Use for robust sequencing maps.
    *   2025: `https://github.com/frc1678/C2025-Public`
    *   2024: `https://github.com/frc1678/C2024-Public`
    *   2023: `https://github.com/frc1678/C2023-Public`
    *   2022: `https://github.com/frc1678/C2022-Public`
    *   2021: `https://github.com/frc1678/cardinal-2021-public`
    *   2020: `https://github.com/frc1678/C2020`
*   **Team 3005 (RoboChargers):** *AdvantageKit Native* & highly clean abstractions.
    *   2025: `https://github.com/FRC3005/Reefscape-2025`
    *   2024: `https://github.com/FRC3005/Crescendo-2024-Public`
    *   2023: `https://github.com/FRC3005/Charged-Up-2023-Public`
    *   2022: `https://github.com/FRC3005/Rapid-React-2022-Public`
    *   2020: `https://github.com/FRC3005/Infinite-Recharge-2020`
*   **Team 364 (BaseFalconSwerve):**
    *   Template: `https://github.com/Team364/BaseFalconSwerve`
*   **Team 111 (WildStang):** Exceptional Component-Based Architecture & clean abstractions.
    *   2026: `https://github.com/wildstang/2026_111_robot_software`
    *   2025: `https://github.com/wildstang/2025_111_robot_software`
    *   2024: `https://github.com/wildstang/2024_111_robot_software`
    *   2023: `https://github.com/wildstang/2023_111_robot_software`
    *   2022: `https://github.com/wildstang/2022_111_robot_software`
    *   2020: `https://github.com/wildstang/2020_robot_software`
*   **Team 2767 (Stryke Force):** 'ThirdCoast' Custom Swerve Framework & advanced control math.
    *   ThirdCoast Framework: `https://github.com/strykeforce/thirdcoast`
    *   2024 (Crescendo): `https://github.com/strykeforce/crescendo`
    *   2023 (Charged Up): `https://github.com/strykeforce/chargedup`
    *   2022 (Rapid React): `https://github.com/strykeforce/rapidreact`
    *   2020 (Infinite Recharge): `https://github.com/strykeforce/infiniterecharge`
*   **Team 5940 (B.R.E.A.D.):** Bleeding-Edge Vision Fusion, AdvantageKit usage, & Odometry Hardening.
    *   2025: `https://github.com/BREAD5940/2025-Public`
    *   2024: `https://github.com/BREAD5940/2024-Onseason`
    *   2023: `https://github.com/BREAD5940/2023-Onseason`
    *   2022: `https://github.com/BREAD5940/2022-Onseason`
    *   2021: `https://github.com/BREAD5940/2021-Robot`
    *   2020: `https://github.com/BREAD5940/2020-Onseason`
*   **Team 604 (Quixilver):** Highly Object-Oriented Simulation & robust WPILib Architecture.
    *   2025: `https://github.com/frc604/2025-public`
    *   2024: `https://github.com/frc604/2024-public`
    *   2023: `https://github.com/frc604/2023-public`
    *   2022: `https://github.com/frc604/2022-public`
    *   2020: `https://github.com/frc604/FRC-2021-v2`
*   **Team 973 (Greybots):** Competition-Tested Superstructure State Machines & robust mechanical integration.
    *   2025: `https://github.com/Team973/2025-inseason`
    *   2023: `https://github.com/Team973/2023-inseason`
    *   2022: `https://github.com/Team973/2022-inseason`
*   **Team 118 (Robonauts):** 4x World Champions with highly optimized control systems & autonomous routines.
    *   2026: `https://github.com/FRCTeam118/2026-Robot-Code`
    *   2025: `https://github.com/FRCTeam118/2025-Robot-Code`
    *   2024: `https://github.com/FRCTeam118/2024-Robot-Code`
    *   2023: `https://github.com/FRCTeam118/2023-Robot-Code`
    *   2022: `https://github.com/FRCTeam118/2022-Robot-Code`
*   **Team 195 (CyberKnights):** Consistently competitive with excellent code structure & documentation.
    *   2025: `https://github.com/FRCTeam195/2025-robot-code`
    *   2024: `https://github.com/FRCTeam195/2024-robot-code`
    *   2023: `https://github.com/FRCTeam195/2023-robot-code`
    *   2022: `https://github.com/FRCTeam195/2022-robot-code`
*   **Team 148 (RoboVikes):** Very strong documentation, infrastructure, & clean code organization.
    *   2025: `https://github.com/Team148/2025-robot-code`
    *   2024: `https://github.com/Team148/2024-robot-code`
    *   2023: `https://github.com/Team148/2023-robot-code`
    *   2022: `https://github.com/Team148/2022-robot-code`
*   **Team 2471 (meanbycoding):** Modern AdvantageKit implementation & clean abstractions.
    *   2025: `https://github.com/Team2471/2025-robot-code`
    *   2024: `https://github.com/Team2471/2024-robot-code`
    *   2023: `https://github.com/Team2471/2023-robot-code`
    *   2022: `https://github.com/Team2471/2022-robot-code`
*   **Team 971 (Spartans):** Historical significance with clean, well-documented code patterns.
    *   2025: `https://github.com/Team971/Spartans-2025`
    *   2024: `https://github.com/Team971/Spartans-2024`
    *   2023: `https://github.com/Team971/Spartans-2023`
    *   2022: `https://github.com/Team971/Spartans-2022`
*   **Team 2168 (Aluminum Falcons):** Strong sim/real hybrid patterns & dual-codebase architecture.
    *   2025: `https://github.com/Team2168/2025-Competition-Robot`
    *   2024: `https://github.com/Team2168/2024-Competition-Robot`
    *   2023: `https://github.com/Team2168/2023-Competition-Robot`
    *   2022: `https://github.com/Team2168/2022-Competition-Robot`
*   **Team 5013 (Grenading Gearheads):** Growing reputation for quality code & modern practices.
    *   2025: `https://github.com/Team5013/2025-robot-code`
    *   2024: `https://github.com/Team5013/2024-robot-code`
    *   2023: `https://github.com/Team5013/2023-robot-code`
    *   2022: `https://github.com/Team5013/2022-robot-code`
*   **Team 624 (CRyptonite):** Strong autonomous routines & path following integration.
    *   2025: `https://github.com/Team624/2025-robot-code`
    *   2024: `https://github.com/Team624/2024-robot-code`
    *   2023: `https://github.com/Team624/2023-robot-code`
    *   2022: `https://github.com/Team624/2022-robot-code`
*   **Team 33 (Killer Bees):** Historical elite team with proven architectural patterns.
    *   2025: `https://github.com/Team33TheKillerBees/2025-robot-code`
    *   2024: `https://github.com/Team33TheKillerBees/2024-robot-code`
    *   2023: `https://github.com/Team33TheKillerBees/2023-robot-code`
    *   2022: `https://github.com/Team33TheKillerBees/2022-robot-code`
*   **Team 865 (Warbots):** Clean AdvantageKit implementation & modern code organization.
    *   2025: `https://github.com/Team865/2025-robot-code`
    *   2024: `https://github.com/Team865/2024-robot-code`
    *   2023: `https://github.com/Team865/2023-robot-code`
    *   2022: `https://github.com/Team865/2022-robot-code`
*   **Team 6040 (Quasics):** Strong documentation & emerging elite team codebase.
    *   2025: `https://github.com/Team6040/2025-robot-code`
    *   2024: `https://github.com/Team6040/2024-robot-code`
    *   2023: `https://github.com/Team6040/2023-robot-code`
    *   2022: `https://github.com/Team6040/2022-robot-code`
*   **Team 5818 (MegaHurtz 2.0):** Growing presence with quality control systems.
    *   2025: `https://github.com/Team5818/2025-robot-code`
    *   2024: `https://github.com/Team5818/2024-robot-code`
    *   2023: `https://github.com/Team5818/2023-robot-code`
    *   2022: `https://github.com/Team5818/2022-robot-code`
*   **Team 2767 (Stryke Force):** 'ThirdCoast' Custom Swerve Framework & advanced control math.
    *   ThirdCoast Framework: `https://github.com/strykeforce/thirdcoast`
    *   2025: `https://github.com/strykeforce/2025-competition`
    *   2024: `https://github.com/strykeforce/crescendo`
    *   2023: `https://github.com/strykeforce/chargedup`
    *   2022: `https://github.com/strykeforce/rapidreact`
    *   2020: `https://github.com/strykeforce/infiniterecharge`
*   **Team 4414 (HighTide):** Exceptionally dominant swerve controls and robust architectures.
    *   2025: `https://github.com/HighTide4414/2025-robot-code`
    *   2024: `https://github.com/HighTide4414/2024-robot-code`
    *   2023: `https://github.com/HighTide4414/2023-robot-code`
*   **Team 3476 (Code Orange):** Clean architecture, advanced vision fusion (CodeOrangePoseEstimator).
    *   2025: `https://github.com/frc3476/2025-robot-code`
    *   2024: `https://github.com/frc3476/2024-robot-code`
    *   2023: `https://github.com/frc3476/2023-robot-code`
*   **Team 8033 (Highlander Robotics):** High-quality modern AdvantageKit architecture.
    *   2025: `https://github.com/HighlanderRobotics/2025-robot-code`
    *   2024: `https://github.com/HighlanderRobotics/2024-robot-code`
    *   2023: `https://github.com/HighlanderRobotics/2023-robot-code`
*   **Team 1323 (MadTown Robotics):** Historically dominant subsystem packaging and logic.
    *   2025: `https://github.com/Team1323/2025-robot-code`
    *   2024: `https://github.com/Team1323/2024-robot-code`
    *   2023: `https://github.com/Team1323/2023-robot-code`
*   **Other Notables (AdvantageKit & Cycles):**
    *   Team 125: `https://github.com/nutrons`
    *   Team 4099: `https://github.com/Team4099`
    *   Team 1540: `https://github.com/Team1540/Flaming-Chickens-2025` (updated)
    *   Team 1736: `https://github.com/Team1736/RobotCode-2025` (updated)
    *   Team 9771: `https://github.com/Team9771/2025-competition` (updated)
    *   Team 852: `https://github.com/Team852/2025-robot-code` (updated)

## 2. GitHub Access Methodology (DIRECT ACCESS ONLY)

### Step 1: Direct Raw File Access (PRIMARY METHOD)
When analyzing specific files from elite teams, ALWAYS use direct raw file access:

```
# CORRECT APPROACH
webReader("https://raw.githubusercontent.com/Team254/FRC-2024-Public/main/src/main/java/com/team254/frc2024/subsystems/Swerve.java")

# WRONG APPROACH (NEVER DO THIS)
webSearch("Team 254 swerve drive implementation")
```

### Step 2: Multi-File Analysis Strategy
When needing multiple files, use systematic webReader calls:
1. Start with known file paths from repository structure
2. Use webReader for each file individually
3. Fall back to git clone only if needing 10+ files from same repo
4. NEVER use web searches to discover file paths

### Step 3: Error Handling & File Discovery
If direct raw file access fails:
1. **TRY ALTERNATE BRANCHES**: `main`, `master`, `develop`
2. **TRY ALTERNATE PATHS**: Common FRC structures:
   - `src/main/java/frc/robot/subsystems/`
   - `src/main/java/com/team[number]/`
   - `src/main/java/com/team[organization]/`
3. **USE GIT CLONE**: As last resort, clone repo locally
4. **NEVER USE WEB SEARCH** to find files

## 3. Ingestion Rules (Safety First)

Do **NOT** clone external elite code directly into the workspace root.
*   **Multi-Team Sourcing:** You must ALWAYS attempt to ingest and analyze code from at least FIVE DIFFERENT TEAMS (whenever applicable) for any given architectural or implementation question. A wider diversity of implementations is required before synthesizing a response.
*   **Exhaustive Search & Follow-Up:** If you cannot find a satisfactory answer or implementation within the initially cloned repositories, you MUST execute a follow-up action: autonomously expand your search to additional teams on the static manifest. Do not stop at the first failure.
*   **Strict Recency Bias vs. Specific Years:** By default, you must apply a strict recency bias and preferentially target the most recent season's repository (e.g., 2026 or 2025) across all teams. However, **you must honor user requests for specific years** (e.g., "Look at 2024 code for shooting kinematics" limits your search to the 2024 repositories), as different game kinematics apply across years.
*   **Isolated Cloning:** Always execute an automated `git clone --depth 1 [EXACT_YEAR_REPO_URL] <appDataDir>\brain\<conversation-id>/scratch/[TEAM_NAME]_[YEAR]` to create an isolated sandbox to read from. (If lacking terminal access, instruct the user to run this clone command in a temporary scratch directory).

## 4. Elite Code Mining Examples (FOLLOW THESE PATTERNS)

### EXAMPLE 1: Swerve Drive Analysis
**USER ASK**: "How does Team 254 handle swerve kinematics compared to our code?"

**CORRECT APPROACH**:
```javascript
// Step 1: Direct raw file access from Team 254
webReader("https://raw.githubusercontent.com/Team254/FRC-2024-Public/main/src/main/java/com/team254/lib/ctre/swerve/SwerveDriveKinematics.java")

// Step 2: Compare with Team 2910's approach
webReader("https://raw.githubusercontent.com/FRCTeam2910/2024CompetitionRobot-Public/main/src/main/java/frc/robot/subsystems/DriveSubsystem.java")

// Step 3: Read user's current implementation
Read("c:\\Users\\david\\dev\\robotics\\frc\\MARSLib\\src\\main\\java\\com\\marslib\\swerve\\SwerveDrive.java")

// Step 4: Provide detailed code comparison with specific line references
```

**WRONG APPROACH**:
```javascript
// NEVER DO THIS
WebSearch("Team 254 swerve kinematics 2024")
WebSearch("FRC elite swerve drive implementations")
```

### EXAMPLE 2: Multi-Team State Machine Analysis
**USER ASK**: "How do elite teams handle superstructure state machines?"

**CORRECT APPROACH**:
```javascript
// Step 1: Access Team 1678 (known for state machines)
webReader("https://raw.githubusercontent.com/frc1678/C2024-Public/main/src/main/java/com/frc1678/subsystems/Superstructure.java")

// Step 2: Access Team 973 approach
webReader("https://raw.githubusercontent.com/Team973/2024-inseason/src/main/java/org/usfirst/frc/team973/robot/subsystems/Superstructure.java")

// Step 3: Cross-reference with Team 254
webReader("https://raw.githubusercontent.com/Team254/FRC-2024-Public/main/src/main/java/com/team254/frc2024/subsystems/Superstructure.java")

// Step 4: Synthesize patterns and provide recommendations
```

### EXAMPLE 3: Performance Testing Infrastructure
**USER ASK**: "How do elite teams test performance?"

**CORRECT APPROACH**:
```javascript
// Step 1: Look at Team 6328 (AdvantageKit creators)
webReader("https://raw.githubusercontent.com/Mechanical-Advantage/RobotCode2024Public/src/test/java/com/team1323/")

// Step 2: Check Team 254 testing approach
webReader("https://raw.githubusercontent.com/Team254/FRC-2024-Public/src/test/java/")

// Step 3: Examine Team 3005 (clean testing)
webReader("https://raw.githubusercontent.com/FRC3005/Crescendo-2024-Public/src/test/java/")

// Step 4: Compare with user's existing performance tests
Read("c:\\Users\\david\\dev\\robotics\\frc\\MARSLib\\src\\test\\java\\com\\marslib\\testing\\performance\\PerformanceRegressionTest.java")
```

## 5. Pattern Matching Heuristics (Astute Grepping)

Top-tier teams have notoriously large repositories. Avoid getting lost by anchoring your grep searches around key API landmarks:
*   Search for `SwerveModuleState`, `ChassisSpeeds`, or `Phoenix6` logic when auditing drivetrain movement.
*   Search for `PoseEstimator`, `Vision`, `LimelightHelpers`, or `PhotonCamera` when identifying localization math.
*   Search for `StateSpace`, `Matrix`, `LQR`, or `EKF` when looking for pure control theory loop structures.
*   Search for `Dashboard`, `Shuffleboard`, `NetworkTable`, `AdvantageScope`, or `Elastic` when investigating User Interfaces and telemetry layouts.
*   Search for `Logger`, `AdvantageKit`, `AutoLog`, or `DataLogManager` when parsing data logging and telemetry backends.
*   Search for `Controller`, `HID`, `Joystick`, `Haptic`, or `Rumble` when investigating operator input and control systems.
*   **Cross-Team Issue Analysis:** If asked to see how all teams solved a *specific issue* for a given year (e.g. "how did teams score in the Trap in 2024?"), you must clone the relevant yearly repositories for *multiple* teams from the manifest. Systematically grep across all of them for game-specific keywords (e.g., `Trap`, `Elevator`, `Score`) and synthesize a comparative analysis of their differing approaches.
*   *Mandate Cross-Referencing:* If you identify a game-changing Vision configuration in 1690's repository, check it against 6328's approach before considering it generalized best-practice.
*   *WPILib Validation:* If you discover a heavily modified WPILib wrapper (like `CodeOrangePoseEstimator`), you must trace the source back upstream (`github.com/wpilibsuite/allwpilib`) to verify exactly what WPILib limitations caused the team to fork the math.

## 4. Porting Constraints (Absolute Architecture Enforcements)

Extracting elite code is useless if it creates technical debt. Any porting attempt into `MARSLib` must rigidly comply with the following translations:

1.  **Dependency Injection Only:** Discard all singletons (e.g. `Drive.getInstance()`) and hardware instantiation logic. Translate elite calculations to operate entirely inside `MARSLib`'s `@AutoLog` generic `HardwareIO` interfaces.
2.  **No Vendor Lock-in API bleed:** Eliminate direct TalonFX/SparkMAX calls from your final ported snippet. Replace them simply with math variables (e.g., Target Volts or System States), passing them downward into MARSLib `runCharacterization()` or `runVelocity()` layers.
3.  **Strict Variable Formatting:** Nuke legacy `m_` prefixes. Everything must cleanly translate into unannotated `camelCase` parameters standardizing to modern Java layout.
4.  **Logging Normalization:** Convert `SmartDashboard`, `ShuffleBoard` or raw custom JSON REST usages natively into AdvantageKit `LogTable` or auto-logging outputs.

## 5. Exit Validation

Before surfacing the ported architectural snippet up to the user:
*   Validate `spotlessApply` compliance across the local `MARSLib` Gradle context.
*   Confirm there are no unresolved static imports or `HidingField` errors caused by variable conversion mismatching.
