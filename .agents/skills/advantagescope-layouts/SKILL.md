---
name: advantagescope-layouts
description: Exposes standardized execution rules for properly building and structuring `layout.json` configurations using the `advantagescope-mcp` tools when deploying new telemetry or simulation features.
license: MIT
compatibility: Claude Code, Codex CLI, VS Code Copilot, Cursor
metadata:
  author: areslib-agent
  version: "1.0.0"
  category: tools
---

# AdvantageScope Automated Layout Structuring

As the agent, you have direct programmatic access to the `advantagescope-mcp` architecture via attached specialized tools.
Because AdvantageScope is heavily integrated into the debugging telemetry for `ARESLib`, and users often request advanced 3D visual abstractions (Lidars, Swerve wheels, AprilTags), you MUST correctly hook into the MCP.

## Invocation Conventions
When requested to create or update an AdvantageScope layout to visualize a new feature, follow this flow:

### 1. Generating State Files
- **CRITICAL**: Do NOT use `mcp_advantagescope-mcp_create_layout`, `update_tab`, or `add_source` tools. The current MCP layout generator fundamentally breaks serialization on AdvantageScope 3.10+ by dropping internal dictionary references (like `leftLockedRange`, `renderer`, `controlsHeight`) and obliterating the `version` attribute, causing the fatal "unrecognized format" error.
- **3. Zod Schema Validation:** AdvantageScope 3.10 uses an extremely strict Zod-based internal schema validator when loading layout JSON files. If a telemetry source binding (like `Field3d` or `Swerve`) requires hidden internal fallback parameters (e.g., `model`, `color`, `arrangement`), giving it an empty dictionary (`"options": {}`) will violently crash the renderer and silently drop the entire tab from the layout.
    - **Field3d `"robot"` Type:** MUST have `"options": { "model": "Robot" }`.
    - **Field3d `"ghost"` Type:** MUST have `"options": { "model": "Robot", "color": "#ff0000" }`.
    - **Swerve `"states"` Type:** MUST have `"options": { "color": "#ff0000", "arrangement": "0,1,2,3" }`.
    - **LineGraph Type:** MUST have `"options": { "color": "#ff0000" }`.
- **4. Layout Fallback Strategy:** If manual dictionary injection continues to drop tabs due to undocumented Zod constraints, the safest fallback is to export a structurally empty template (`"sources": []`) directly from AdvantageScope and NOT programmatically populate the telemetry keys. The layout frame will bind accurately, and you simply instruct the user to manually drag the log mappings into the visualizer from the sidebar.
- **Workflow**: If the user requests a layout change natively, instruct the user to open their local AdvantageScope desktop app, click **File -> Export Layout** into the repository manually.
- **Modification**: Use Python dictionary injection natively against the user's exported `advantagescope_layout.json` to insert/append field nodes (`tabs`), rather than relying on the MCP backend tools to write the file. The MCP should only be used in a read-only capacity (`get_tab_type_schema`) to query required fields.

### 2. Configuring Point Clouds
When visualizing `ArrayLidarIOSim` or dynamic dynamic fields:
- Use `mcp_advantagescope-mcp_add_source` to attach the field `[AutoLog Inputs Path]/LidarArray` array.
- **Log Type**: You must explicitly set `log_type: "double[]"` to force AdvantageScope to deserialize it linearly.
- Set the render option to **Points** in the Tab configuration, NOT a "Swerve State" visualization.

### 3. Rendering Gamepads
AdvantageScope now supports Gamepad Joystick rendering overlays in 3D. When mapping virtual joysticks from the HUD, you must push inputs into `/DriverStation/Joystick[0]` paths in our AdvantageKit logic before linking that schema into the AdvantageScope layout!
