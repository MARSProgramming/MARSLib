# Conventions

## Coding Style
- **Spotless Formatting**: Required. Enforced via `.githooks` and CI.
- **Never Nester**: Avoid deep nesting. Use early returns and guard clauses.
- **Naming**: Avoid Hungarian notation. Use explicit unit nomenclature in variable names (e.g., `distanceMeters`, `velocityRadPerSec`).

## Architectural Rules
- **No hardware calls in Subsystems**: Hardware must only be accessed in `IOReal` classes.
- **Strict Javadoc**: Championship-tier Javadoc standards for all public APIs. Detailed explanations of math and control theory.
- **Time/Threading**: Use strict time-unit sleeping and interrupt safeties for custom threads to prevent lockups.
