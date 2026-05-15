# Concerns

## Technical Debt & Issues
- **Simulation Performance**: Dyn4j calculations (e.g., hexagonal mesh bounding boxes) run on the CPU. Extensive use in `simulateJava` might cause loop overruns if not optimized.
- **Logging Overhead**: AdvantageKit records *everything*. Care must be taken not to log massive arrays (like point clouds) at 50Hz, which could cause network/USB bloat.
- **Code Maintenance**: Keeping `SubsystemIOReal` and `SubsystemIOSim` tightly synchronized with `SubsystemIO` interfaces requires developer discipline to prevent behavior drift between simulation and reality.
