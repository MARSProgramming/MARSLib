# GitHub Copilot Instructions for MARSLib

You are operating within the **MARSLib World-Class FRC Framework**.

MARSLib uses a highly specific architecture designed around a strict 20ms AdvantageKit physics loop, zero runtime garbage collection, and custom IO simulation layers. 

## Agentic Skill Integration

MARSLib is equipped with specialized `.agent` Markdown skills located in the `.agents/skills/` directory.

Before writing code for any major robotic subsystem or utility, you **MUST** automatically read and apply the associated SKILL.md file if it matches the user's request. 

### Key Skill Mappings to Auto-Fetch:
- **Writing Subsystems:** Always read `.agents/skills/marslib-mechanisms/SKILL.md`
- **Swerve Drive:** Always read `.agents/skills/marslib-swerve/SKILL.md`
- **Simulation/Physics:** Always read `.agents/skills/marslib-simulation/SKILL.md`
- **AdvantageKit/Telemetry:** Always read `.agents/skills/marslib-telemetry/SKILL.md`
- **Writing Unit Tests:** Always read `.agents/skills/marslib-testing/SKILL.md`
- **Diagnosing Build Errors:** Always read `.agents/skills/marslib-ci/SKILL.md`
- **Control Theory / PID:** Always read `.agents/skills/marslib-control-theory/SKILL.md`
- **Code Audits:** Always read `.agents/skills/marslib-audit/SKILL.md`

### Core Principles
1. NEVER use the `new` keyword inside `periodic()`, `execute()`, or any loop structure. MARSLib prohibits dynamic allocations during runtime.
2. All hardware must be abstracted via the `IO` (e.g., `IOReal`, `IOSim`) interface to maintain AdvantageKit deterministic replay capability.
3. Obey the rules from `.agents/skills/marslib-core-standards/SKILL.md` unconditionally.
