<div align="center">

# 🪐 MARSLib
### FRC AdvantageKit Abstraction & Physics Template

[![CI Build](https://github.com/MARSProgramming/MARSLib/actions/workflows/ci.yml/badge.svg)](https://github.com/MARSProgramming/MARSLib/actions/workflows/ci.yml)
[![Coverage](https://raw.githubusercontent.com/MARSProgramming/MARSLib/main/.github/badges/jacoco.svg)](https://github.com/MARSProgramming/MARSLib/actions/workflows/ci.yml)
[![Branches](https://raw.githubusercontent.com/MARSProgramming/MARSLib/main/.github/badges/branches.svg)](https://github.com/MARSProgramming/MARSLib/actions/workflows/ci.yml)
[![Spotless](https://img.shields.io/badge/style-spotless-brightgreen)](https://github.com/diffplug/spotless)
[![Team](https://img.shields.io/badge/FRC-2614-B32416)](https://www.thebluealliance.com/team/2614)
[![AdvantageKit](https://img.shields.io/badge/Powered%20By-AdvantageKit-yellow)](https://github.com/Mechanical-Advantage/AdvantageKit)
[![Dyn4j](https://img.shields.io/badge/Physics-Dyn4j-blue)](https://dyn4j.org/)
[![Docs](https://img.shields.io/badge/docs-MARSLib-B32416)](https://MARSProgramming.github.io/MARSLib/)
[![Accessibility](https://img.shields.io/badge/A11y-WCAG_AA-brightgreen)](https://github.com/MARSProgramming/MARSLib/actions/workflows/a11y.yml)

**A championship-tier software template for Mountaineer Area RoboticS — FRC Team 2614.**
</div>

---

Welcome to MARSLib, an aggressively hardened framework that enables pure, deterministic AdvantageKit logging while bridging seamless 2D physics simulations via `dyn4j`.

### 🪐 Our Foundation: FIRST Core Values
MARSLib is built upon the **FIRST Core Values**. We believe that technical excellence is inseparable from character:
- **Discovery & Innovation**: We explore new technologies and use creative persistence to solve "impossible" problems.
- **Impact & Teamwork**: We build together, ensuring our software has a measurable impact on our team's success.
- **Inclusion & Fun**: We ensure our code and culture are welcoming to all, and we celebrate every breakthrough!

This architecture is built so that students can develop completely offline. Our simulation logic doesn't just run mathematical encoders—it simulates hexagonal REBUILT obstacles, voltage sag limits, and bounding box superstructure collisions.

## 🚀 Key Features

*   **100% Simulated Logic:** Run `./gradlew simulateJava` and visualize your robot mathematically navigating the REBUILT field before you even touch a real battery.
*   **Time-Of-Flight Aiming:** Native quadratic kinematic intersections mean the robot shoots accurately while pulling full-speed swerve maneuvers.
*   **Voltage Load-Shedding:** A native Stator Current allocation daemon statically bounds TalonFX modules to actively prevent robotic brownouts when pushing against defense.
*   **Continuous Automation:** Every push to GitHub runs a spotless lint check and validates physics-backed JUnit tests against the dyn4j simulation engine before compiling and logging an uploadable JAR.

## 📖 Documentation

Full API documentation is available at **[MARSProgramming.github.io/MARSLib](https://MARSProgramming.github.io/MARSLib/)**

To generate documentation locally:
```bash
./gradlew generateDocs
# Open docs/index.html in your browser
```

## 🧬 Architecture Diagram

The codebase strictly enforces the AdvantageKit **Dependency Injection** pattern, isolating the logical robot from the physical/simulated hardware.

```mermaid
graph TD
    classDef io fill:#2b66a2,stroke:#1f4a76,stroke-width:2px,color:white;
    classDef logic fill:#003f00,stroke:#002900,stroke-width:2px,color:white;
    classDef ext fill:#4a4a4a,stroke:#333333,stroke-width:2px,color:white;

    Subsystem[Subsystem Logic / State Machines]:::logic
    IO[SubsystemIO Interface]:::io
    Real[SubsystemIOReal: TalonFX / Limelight / QuestNav]:::ext
    Sim[SubsystemIOSim: Dyn4j Physics Engine]:::ext
    Log[(AdvantageKit Logger)]:::ext

    Subsystem -->|Injects| IO
    IO -.->|Physical Robot| Real
    IO -.->|Desktop Sim| Sim
    Subsystem -->|Records State| Log
```

## 📂 Repository Layout

```text
MARSLib/
├── .github/                 # CI Pipelines, Dependabot, and PR Templates
├── .wpilib/                 # FRC 2614 Team Radio Configurations
├── docs/                    # Generated documentation site (GitHub Pages)
├── com.marslib/             # Inner Architecture (Do Not Edit Routine Logic Here)
│   ├── auto/                # PathPlanner Integration & Diagnostic Checks
│   ├── faults/              # MARSFaultManager & Alert System
│   ├── mechanisms/          # Linear/Rotary/Flywheel IO Abstractions
│   ├── power/               # MARSPowerManager Load-Shedding Daemon
│   ├── simulation/          # Dyn4j World Bounds and Hexagonal Meshes
│   ├── swerve/              # 250Hz Odometry Thread & Odometry Computations
│   ├── util/                # Time-Of-Flight Interpolation, State Machines, Alliance Utils
│   └── vision/              # AprilTag & SLAM Fusion Pipelines
└── frc.robot/               # Competition Logic (Edit Your Logic Here!)
    ├── commands/            # PathPlanner routines and Teleop Commands
    ├── constants/           # All tunable parameters (Vision, Field, Shooter, etc.)
    ├── simulation/          # Game Piece Physics Bodies
    ├── subsystems/          # Implementations of your Superstructure/Arm
    └── RobotContainer.java  # Controller Mapping and Subsystem bindings
```

## 🛠 Usage & Setup

### 1. Developer Formatting
To ensure your code never gets rejected by GitHub's automated CI, run the included batch script to initialize a spotless Git Hook!
```bash
# Windows
.\install-git-hooks.bat
```
*(This forces your VS Code to auto-format `build.gradle` structures before you push!)*

### 2. Creating Subsystems
MARSLib abstracts the `Real` hardware from the `Sim` hardware using pure Dependency Injection interfaces.
1. `SubsystemIO` - The Interface (What data does this mechanism need?)
2. `SubsystemIOReal` - The Hardware (TalonFX / CANSparkMax / NavX)
3. `SubsystemIOSim` - The Physics (Dyn4j wrappers, friction calculations)

### 3. Firing up AdvantageScope
Want to analyze a bug or replay a match?
1. Open AdvantageScope
2. Click `File > Open Layout` and select the `advantagescope_layout.json` located at the root of this repository!
3. You now have a fully operational 3D Dashboard monitoring battery voltage limits alongside Hexagonal Field boundaries.

## 🐛 Found a Bug?
Use our customized [GitHub Issue Templates](.github/ISSUE_TEMPLATE) to let the software leads know exactly what went wrong in your simulation or physical robot code! Whether it's a new PathPlanner routine request or an odometry jitter bug, the templates will automatically guide you through attaching your `.wpilog` telemetry data.

## ⚖️ Open Source Acknowledgements

MARSLib stands upon the shoulders of giants. We extend our deepest gratitude to the **MapleSim** project for their groundbreaking simulation patterns, and to the following open-source maintainers and vendors who make modern FRC possible:

<p align="center">
  <a href="https://github.com/wpilibsuite/allwpilib" target="_blank"><img src="https://raw.githubusercontent.com/wpilibsuite/branding/main/wpilib-icon.svg" width="40" height="40" alt="WPILib" style="margin: 10px;"></a>
  <a href="https://github.com/Mechanical-Advantage" target="_blank"><img src="https://docs.advantagekit.org/img/logo.png" width="40" height="40" alt="AdvantageKit" style="margin: 10px;"></a>
  <a href="https://dyn4j.org/" target="_blank"><img src="https://raw.githubusercontent.com/dyn4j/dyn4j/master/dyn4j.png" width="40" height="40" alt="Dyn4j" style="margin: 10px;"></a>
  <a href="https://pathplanner.dev/" target="_blank"><img src="https://pathplanner.dev/img/logo.png" width="40" height="40" alt="PathPlanner" style="margin: 10px;"></a>
  <a href="https://photonvision.org/" target="_blank"><img src="https://raw.githubusercontent.com/PhotonVision/photonvision-branding/master/PhotonVision%20Branding/Logos/Icon/PNG/PhotonVision-Icon-noBG.png" width="40" height="40" alt="PhotonVision" style="margin: 10px;"></a>
  <a href="https://store.ctr-electronics.com/" target="_blank"><img src="https://v6.docs.ctr-electronics.com/en/stable/_static/ctre.png" width="40" height="40" alt="CTRE" style="margin: 10px;"></a>
  <a href="https://www.revrobotics.com/" target="_blank"><img src="https://avatars.githubusercontent.com/u/13215904?s=200&v=4" width="40" height="40" alt="REV" style="margin: 10px;"></a>
  <a href="https://shenzhen-robotics-alliance.github.io/maple-sim/rebuilt/" target="_blank"><img src="https://raw.githubusercontent.com/Shenzhen-Robotics-Alliance/maple-sim/main/docs/media/icon.png" width="40" height="40" alt="MapleSim" style="margin: 10px;"></a>
</p>

- [Mechanical Advantage (AdvantageKit)](AdvantageKit-License.md)
- [WPILib Core](WPILib-License.md)
- [Dyn4j Collision Physics](Dyn4j-License.md)
- [PathPlanner](PathPlanner-License.md)
- [MapleSim](https://shenzhen-robotics-alliance.github.io/maple-sim/rebuilt/)
