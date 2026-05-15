# Architecture

## Design Pattern
**AdvantageKit Dependency Injection (DI) Pattern**
MARSLib completely abstracts logical subsystems from their physical hardware. This separation guarantees 100% deterministic logging and simulation capability.

## Layers
1. **Subsystem**: The top-level logical class. Handles state machines, high-level commands, and periodic updates.
2. **SubsystemIO (Interface)**: Defines the data inputs/outputs required by the subsystem.
3. **SubsystemIOReal**: The concrete implementation for physical hardware (e.g., TalonFX, NavX).
4. **SubsystemIOSim**: The concrete implementation for desktop simulation (wraps Dyn4j physics).
5. **Logger**: AdvantageKit records every piece of data passing through the IO interface, allowing perfect post-match replay.

## Key Subsystems
- **MARSPowerManager**: Load-shedding daemon for voltage sag prevention.
- **MARSFaultManager**: Alert system for hardware issues.
- **Swerve Odometry Thread**: High-frequency (250Hz) odometry computations.
