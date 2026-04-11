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
