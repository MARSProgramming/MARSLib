# Testing

## Framework
- **JUnit 5**: Primary testing framework.
- **Dyn4j Simulation**: Tests run against the actual Dyn4j physics engine rather than simple mocks. This ensures physical behaviors are validated before hitting real hardware.

## Coverage & Enforcement
- Tested heavily in CI/CD before any merge.
- Coverage reports are generated to ensure mathematical logic and critical paths are covered.
- Spotless ensures code format before compilation.
