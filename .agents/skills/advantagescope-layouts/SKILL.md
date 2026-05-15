---
name: advantagescope-layouts
description: Exposes rules for building AdvantageScope layout.json configurations using advantagescope-mcp tools. Use when deploying new telemetry or simulation features.
---

# AdvantageScope Layout Structuring

## 1. CRITICAL: MCP Tool Limitations

**DO NOT use** `mcp_advantagescope-mcp_create_layout`, `update_tab`, or `add_source` — they break serialization on AdvantageScope 3.10+ by dropping internal references (`version`, `leftLockedRange`, `renderer`).

**Workflow:** Read user's exported `advantagescope_layout.json` and edit manually, or instruct user to configure in AS desktop.

## 2. Zod Schema Validation by Controller

Every source type has strict `options` requirements. Wrong keys = crash:

| Controller | Type | Required Options |
|---|---|---|
| Field3d | `robot` | `{ "model": "Robot" }` |
| Field3d | `ghost` | `{ "model": "Robot", "color": "#ff0000" }` |
| Field3d | `vision` | `{ "color": "#00ffff", "size": "normal" }` |
| Field2d | `robot` | `{ "bumpers": "" }` — NO model |
| Field2d | `ghost` | `{ "color": "#ff0000" }` — NO model |
| Swerve | `states` | `{ "color": "#ff0000", "arrangement": "0,1,2,3" }` |
| LineGraph | `stepped`/`smooth` | `{ "color": "#ff0000", "size": "normal" }` |

## 3. Safe Game Piece Rendering

`gamePiece` mesh renderer uses hardcoded, undocumented year-specific strings (`"Cargo"`, `"Coral"`). Invalid strings crash schema.

**Fallback:** Render as `type: "ghost"` (translucent bounding box) for forward compatibility.

## 4. Competition-Grade Tab Ordering

Priority order (default selected = #1):
1. Odometry Fusion 2D — primary match view
2. Playback 3D — log review
3. Simulation 3D — sim-only
4. Swerve Diagnostics — module health
5. System Health — loop time, CAN, battery
6. Power Draw — current, voltage
7. Fault Alerts — critical/warning/info
8. Tuning Table — feedforward gains
9. Documentation — reference only (ALWAYS last)

## 5. Safe Robot Model

**ALWAYS use** `"model": "Robot"` — the built-in default guaranteed to render. Custom team models often don't exist locally.

## 6. Known Valid Game Pieces (2026)

- `"Fuel"` — 2026 game piece

When in doubt, use `type: "ghost"` instead of `type: "gamePiece"`.

## 7. No Redundant Tabs

Every `logKey` appears in at most one line graph tab. If duplicate keys exist, merge into single tab with left/right axis separation.

## 8. Log Key Completeness Audit

Every `Logger.recordOutput()` key should have visualization. Critical keys MUST be present:

- `System/LoopRunTime_ms`, `System/BatteryVoltage`, `System/CANBusUtilization`, `System/CANivoreUtilization`
- `Teleop/NaNDetected`, `SwerveDrive/OdometryTrustMultiplier`
- `Alerts/Critical`, `Alerts/Warning`, `Alerts/Info`
- `Power/TotalCurrentDraw_A`

```powershell
# Audit script
$layout = Get-Content "advantagescope_layout.json" -Raw
# Extract keys and cross-reference against Java source
```

## 9. Dashboard Parity

`marsteam_dashboard.json` must mirror critical tabs:
1. 3D Field View with `SwerveDrive/Pose`
2. Swerve Diagnostics (Measured + Desired)
3. System Health (loop time, CAN)
4. Fault Alerts (all severities)
5. Power Draw (current, voltage)

Both files must have matching `"version"` strings.
