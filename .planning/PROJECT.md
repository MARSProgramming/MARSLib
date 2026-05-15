# MARSLib FRC Framework

## Context
MARSLib is a championship-tier FRC robot code framework developed by Mountaineer Area RoboticS (FRC Team 2614). It aggressively enforces the AdvantageKit Dependency Injection pattern to ensure 100% deterministic logging and testing. It features full integration with Dyn4j for realistic 2D/2.5D physics simulation, PathPlanner for trajectories, and custom abstractions for FRC robot mechanisms.

## Core Value
Provide a hardened, deterministic, and highly testable framework that allows for seamless transitions between physical hardware (Real) and desktop physics simulation (Sim).

## Requirements

### Validated
- ✓ AdvantageKit Dependency Injection and Log Replay
- ✓ Dyn4j Collision Physics (Hexagonal meshes, obstacles)
- ✓ PathPlanner Integration
- ✓ Time-Of-Flight Aiming
- ✓ Voltage Load-Shedding (MARSPowerManager)

### Active
- [ ] Implement and verify new features requested by user.

### Out of Scope
- Direct hardware calls inside logical subsystems (bypassing IO layers).

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Strict DI via IO layers | Enables 100% simulated logic and exact log replay | Implemented |
| Use Dyn4j | Native WPILib physics lack robust multi-body collision detection | Implemented |

---
*Last updated: 2026-04-28 after initialization*
