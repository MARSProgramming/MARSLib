# MARSLib Controller Mappings

This document is automatically generated during compilation from `RobotBindings.java`.

## 🎮 Pilot (Driver) - Controller 0

> **Primary drive controls and high-level macro sequences.**

| Controller Input | Mapped Action |
| :--- | :--- |
| <kbd>Left Joystick</kbd> | **Holonomic Translation (X/Y)** |
| <kbd>Right Joystick</kbd> | **Holonomic Rotation** |
| <kbd>LeftTrigger</kbd> | **Run Intake** |
| <kbd>RightTrigger</kbd> | **Aim And Shoot On Move** |
| <kbd>B</kbd> | **Stationary Shoot** |
| <kbd>LeftBumper</kbd> | **Unjam** |
| <kbd>RightBumper</kbd> | **Aim And Shuttle** |
| <kbd>DPad_Right</kbd> | **Deploy Intake Only** |
| <kbd>DPad_Left</kbd> | **Retract Intake** |
| <kbd>A</kbd> | **Slamtake** |
| <kbd>Y</kbd> | **Align To Climb Position** |
| <kbd>X</kbd> | **Final Climb Lineup** |
| <kbd>DPad_Up</kbd> | **Manual Climber Up** |
| <kbd>DPad_Down</kbd> | **Manual Climber Down** |
| <kbd>Start</kbd> | **Diagnostic Check** |

---

## 🕹️ CoPilot (Operator) - Controller 1

> **Manual overrides, sub-mechanism control, and fault resets.**

| Controller Input | Mapped Action |
| :--- | :--- |
| <kbd>LeftTrigger</kbd> | **Manual Feed** |
| <kbd>RightTrigger</kbd> | **Fixed Score (Hub)** |
| <kbd>RightBumper</kbd> | **Fixed Score (Ladder)** |
| <kbd>LeftBumper</kbd> | **Cowl Home** |
| <kbd>DPad_Down</kbd> | **Climber Reverse** |
| <kbd>X</kbd> | **Drivetrain Stop** |

---
> [!NOTE]
> All automated scoring sequences natively return the superstructure to the 
> safe `STOWED` state immediately upon release of the binding.
