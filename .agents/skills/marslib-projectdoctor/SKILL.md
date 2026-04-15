---
name: marslib-projectdoctor
description: Helps architect subsystems and IO interfaces that cleanly pass the VS Code extension's ProjectDoctor linting without triggering false positive warnings. Includes strict nomenclature rules for `@AutoLog` implementations.
---

# MARSLib ProjectDoctor Guidelines

When writing code in MARSLib, you must ensure that your code perfectly complies with the `ProjectDoctor` linting rules built into the VS Code extension. Failure to do so will result in annoying terminal warnings for the user during `build` operations.

## Rule 9: The `@AutoLog` Boundary
If you are building a new AdvantageKit-compatible subsystem, you usually create an `IO` interface (e.g. `ElevatorIO`) and an internal data struct.

**Strict Name Compliance:**
The struct handling the data **MUST** end exactly with the word `Inputs` (e.g., `ElevatorIOInputs`).
This is because ProjectDoctor uses strict regular expressions to enforce that every `.java` file containing `Inputs` must be annotated with `@AutoLog`.

**Do NOT Hack the Linter:**
Hardware implementation files (like `ElevatorIOTalonFX.java` or `SwerveModuleIOSparkMax.java`) do not need `@AutoLog` because they only implement the interface; they do not hold loggable state schemas themselves.
Never put a fake `// @AutoLog` comment at the top of a hardware file just to suppress warnings. If a warning is occurring, it means your struct name incorrectly leaked into the hardware implementation's name or you violated the strict `Inputs` suffix rule.
