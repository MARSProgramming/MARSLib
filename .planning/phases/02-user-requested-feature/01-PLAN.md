---
wave: 1
depends_on: []
files_modified:
  - ".planning/ROADMAP.md"
autonomous: false
---

# Plan 01: Define and Scaffold User Requested Feature

## Objective
Clarify the specific user-requested feature to be implemented in Phase 2 and update the roadmap/requirements accordingly.

<tasks>
<task>
<read_first>
- .planning/ROADMAP.md
- .planning/phases/02-user-requested-feature/02-RESEARCH.md
</read_first>
<action>
Prompt the user to specify the feature they wish to implement. Once specified, update the `ROADMAP.md` Phase 2 goal to reflect the actual feature, and create specific implementation tasks (e.g., IO layers, Subsystems, Commands) following MARSLib's AdvantageKit patterns.
</action>
<acceptance_criteria>
- ROADMAP.md contains the specific feature details instead of "User Requested Feature".
- A new, concrete plan is drafted or this plan is updated for implementing the feature.
</acceptance_criteria>
</task>
</tasks>

<verification>
- The user has provided details for the feature.
- The ROADMAP.md file reflects the true goal of Phase 2.
</verification>

<must_haves>
<truths>
- The feature must be clearly defined before implementation code is written.
</truths>
</must_haves>
