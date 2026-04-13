---
name: advantagescope-layouts
description: Exposes standardized execution rules for properly building and structuring `layout.json` configurations using the `advantagescope-mcp` tools when deploying new telemetry or simulation features.
license: MIT
compatibility: Claude Code, Codex CLI, VS Code Copilot, Cursor
metadata:
  author: marslib-agent
  version: "1.0.0"
  category: tools
---

# AdvantageScope Automated Layout Structuring

As the agent, you have direct programmatic access to the `advantagescope-mcp` architecture via attached specialized tools.
Because AdvantageScope is heavily integrated into the debugging telemetry for `MARSLib`, and users often request advanced 3D visual abstractions (Lidars, Swerve wheels, AprilTags), you MUST correctly hook into the MCP.

## Invocation Conventions
When requested to create or update an AdvantageScope layout to visualize a new feature, follow this flow:

### 1. Generating State Files
- **CRITICAL**: Do NOT use `mcp_advantagescope-mcp_create_layout`, `update_tab`, or `add_source` tools. The current MCP layout generator fundamentally breaks serialization on AdvantageScope 3.10+ by dropping internal dictionary references (like `leftLockedRange`, `renderer`, `controlsHeight`) and obliterating the `version` attribute, causing the fatal "unrecognized format" error.
- **3. Strict Zod Schema Validation by Tab Type:** AdvantageScope 3.10 uses an extremely strict Zod-based internal schema validator. Every single source type (`robot`, `ghost`, `stepped`, `vision`) across different controllers (`Field2d`, `Field3d`, `LineGraph`) has a rigidly validated `options` dictionary. If you provide an `options` key that the controller's `_Config.ts` schema does not statically define, the entire layout JSON will crash upon load with a "File format not supported" error.
  - **Field3d Controllers:**
    - `"robot"` Type MUST have `"options": { "model": "Robot" }`.
    - `"ghost"` Type MUST have `"options": { "model": "Robot", "color": "#ff0000" }`.
    - `"vision"` Type MUST have `"options": { "color": "#00ffff", "size": "normal" }`.
  - **Field2d (Odometry) Controllers:** These do NOT support 3D models. Injecting `model` causes a fatal schema violation.
    - `"robot"` Type MUST map to bumpers: `"options": { "bumpers": "" }`.
    - `"ghost"` Type MUST map ONLY to color: `"options": { "color": "#ff0000" }`.
  - **Swerve Diagnostics:**
    - `"states"` Type MUST have `"options": { "color": "#ff0000", "arrangement": "0,1,2,3" }`.
  - **LineGraph Controllers:**
    - `"stepped"` or `"smooth"` Types MUST have both color and line thickness: `"options": { "color": "#ff0000", "size": "normal" }`.
- **4. Safe Game Piece Rendering:** The explicit `gamePiece` mesh renderer relies on internally hardcoded, undocumented, year-specific variant strings (e.g., `"Cargo"`, `"Coral"`). Passing an invalid missing string crashes the schema validator. When constructing pre-season layouts or dynamically mapping unknown simulated game pieces, ALWAYS default to rendering field objects as 3D arrays with `type: "ghost"` (which natively renders cleanly as translucent bounding boxes) rather than `type: "gamePiece"` to ensure forward compatibility without needing exact asset strings.
- **5. Separating Sim vs Playback Architecture:** When designing advanced telemetry workflows, utilize multiple Field3D layouts. Separate real Match Playback (combining SwervePose, Vision Poses, and Vision Frustums) from Headless Simulation tracking (SwervePose, PathPlanner target intent, and PhysicsWorld GamePieces) onto separate independent 3D tracker tabs to maximize visibility without log overlap.
- **Workflow / Fallback Strategy**: If the user requests a layout change natively, you can read the user's exported `advantagescope_layout.json` to insert/append field nodes (`tabs`) via native scripting. If manual injection continues to drop layout elements due to undocumented Zod constraints, instruct the user to open AS desktop and manually configure it. Do NOT use the MCP `advantagescope-mcp` write commands.
### 2. Configuring Point Clouds
When visualizing `ArrayLidarIOSim` or dynamic dynamic fields:
- Use `mcp_advantagescope-mcp_add_source` to attach the field `[AutoLog Inputs Path]/LidarArray` array.
- **Log Type**: You must explicitly set `log_type: "double[]"` to force AdvantageScope to deserialize it linearly.
- Set the render option to **Points** in the Tab configuration, NOT a "Swerve State" visualization.

### 3. Rendering Gamepads
AdvantageScope now supports Gamepad Joystick rendering overlays in 3D. When mapping virtual joysticks from the HUD, you must push inputs into `/DriverStation/Joystick[0]` paths in our AdvantageKit logic before linking that schema into the AdvantageScope layout!

## Competition-Grade Layout Standards

### 4. Tab Ordering — Competition Priority
Tabs MUST be ordered by **competition debugging priority**, not by when they were created. The default selected tab (`"selected": 0`) should be the most useful view during a live match. The canonical ordering is:

1. **Odometry Fusion 2D** — primary competition view (default selected)
2. **Playback 3D** — match log review
3. **Simulation 3D** — sim-only debugging
4. **Swerve Diagnostics** — module health at a glance
5. **System Health** — loop time, CAN, battery, NaN detection
6. **Power Draw** — current, voltage, brownout proximity
7. **Fault Alerts** — critical/warning/info table
8. **Tuning Table** — feedforward gains and vision thresholds
9. **Documentation** — reference only, last tab

**Rule**: The `Documentation` tab (type 0) must ALWAYS be the last tab. It is never needed during competition.

### 5. Safe Robot Model Naming
**NEVER use custom model strings** like `"2026 KitBot"`, `"MyRobot"`, or team-specific names. If the asset doesn't exist in the user's AdvantageScope installation, the robot renders as invisible with no error.

- **Always use**: `"model": "Robot"` — the built-in safe default that is guaranteed to render.
- If the team has imported a custom `.glb` model into their local AdvantageScope, they can manually change the model string in the AS GUI. The layout file should always ship with the safe default.

### 6. Known Valid Game Piece Variants (2026 Season)
The following `gamePiece` variant strings are confirmed valid:
- `"Fuel"` — 2026 game piece (also used in 2017 Steamworks)

When in doubt about a variant string, use `type: "ghost"` instead of `type: "gamePiece"` as documented in §1.4.

### 7. No Redundant Tabs
Every `logKey` should appear in **at most one line graph tab**. If two tabs graph the same key, merge them into a single tab with left/right axis separation.

**Example**: `PhysicsWorld/ComputedVoltage` should NOT have its own "Power Bounds" tab AND appear in a "Power Draw" tab. Consolidate into one tab with current draw on the left axis and voltage on the right.

### 8. Log Key Completeness — Cross-Reference Audit
Every `Logger.recordOutput()` key in the Java codebase should have a corresponding visualization in the layout. Run this audit check when modifying layout files:

```powershell
# Extract all logKeys from the layout
$layout = Get-Content "advantagescope_layout.json" -Raw
# Extract all recordOutput keys from Java
# Cross-reference for missing coverage
```

Critical keys that MUST be visualized (see marslib-audit §16):
- `System/LoopRunTime_ms`, `System/BatteryVoltage`, `System/CANBusUtilization`, `System/CANivoreUtilization`
- `Teleop/NaNDetected`, `SwerveDrive/OdometryTrustMultiplier`
- `Alerts/Critical`, `Alerts/Warning`, `Alerts/Info`
- `Power/TotalCurrentDraw_A`

### 9. Dashboard Parity
The team dashboard (`marsteam_dashboard.json`) must mirror the critical tabs from the main layout. At minimum it must include:
1. 3D Field View with `SwerveDrive/Pose`
2. Swerve Diagnostics with both Measured and Desired states
3. System Health with loop time and CAN utilization
4. Fault Alerts with all alert severity levels
5. Power Draw with current and voltage

Both files must have matching `"version"` strings.
