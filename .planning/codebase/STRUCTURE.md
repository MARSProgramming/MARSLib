# Project Structure

## Directory Layout
- `.github/`: CI Pipelines and PR Templates
- `docs/`: Generated documentation site
- `src/main/java/com/marslib/`: Inner Architecture (Core framework logic)
  - `auto/`: PathPlanner Integration
  - `faults/`: Fault tracking and alerts
  - `mechanisms/`: IO Abstractions for Linear/Rotary/Flywheels
  - `power/`: Power/Voltage managers
  - `simulation/`: Dyn4j World Bounds and Meshes
  - `swerve/`: Odometry and Drive Base
  - `util/`: Math, Interpolation, State Machines
  - `vision/`: SLAM and AprilTag Pipelines
- `src/main/java/frc/robot/`: Competition Logic (User logic goes here)
  - `commands/`: Teleop and Auto commands
  - `constants/`: Tunable parameters
  - `simulation/`: Game specific physics bodies
  - `subsystems/`: Concrete implementations of mechanisms
- `ctre_sim/`: Phoenix 6 Simulation configs
- `vendordeps/`: WPI vendor dependencies (JSON files)
