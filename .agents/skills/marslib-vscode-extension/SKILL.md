---
name: marslib-vscode-extension
description: Helps architect, debug, and expand the MARSLib VS Code extension. Use when adding new diagnostic linting rules to ProjectDoctor, creating new sidebar views, registering commands, or modifying the AdvantageKit generation wizards.
---

# MARSLib VS Code Extension Skill

You are a tooling engineer responsible for the MARSLib VS Code Extension (`marslib-vscode` repository).

## 1. Extension Architecture
The extension is built using TypeScript and deployed via `esbuild`.
Key components are initialized in `src/extension.ts`. Heavy UI modules (like webviews) are lazy-loaded to ensure fast startup times.

- **ProjectDoctor (`src/projectDoctor.ts`)**: The real-time AST linting engine. It enforces the Elite Coding Standards (e.g., catching `Thread.sleep`, Hungarian notation, object allocation in periodic loops).
- **MARSQuickFixProvider (`src/quickFixProvider.ts`)**: CodeAction provider that implements one-click automated refactoring fixes for the ProjectDoctor diagnostics.
- **DeployManager (`src/deployManager.ts`)**: Handles pre-flight checks (compilation, dirty git tree warnings) and tracks deploy history in the sidebar.
- **SubsystemGenerator (`src/subsystemGenerator.ts`)**: A multi-step QuickPick wizard that scaffolds complete AdvantageKit IO layers (`*IO`, `*IOSim`, `*IOTalonFX/SparkMax`, and the Subsystem).
- **PathPlannerProvider (`src/pathplannerProvider.ts`)**: Sidebar view that validates PathPlanner JSONs against Java `NamedCommands` registrations.
- **CANBusVisualizer (`src/canBusVisualizer.ts`)**: Interactive Webview mapping project CAN topology and detecting ID conflicts.

## 2. Adding a New ProjectDoctor Linter Rule
To add a new Elite Coding Standard check:
1. Add the rule implementation inside `projectDoctor.ts` (usually checking `vscode.TextDocument` lines via regex or AST parsing).
2. Assign it a unique diagnostic code (e.g., `MARS_010`).
3. If it makes sense to auto-fix, add the corresponding quick fix in `quickFixProvider.ts` by checking for your diagnostic code and generating a `vscode.WorkspaceEdit`.

## 3. Adding a New Command
1. Register the command in `package.json` under `contributes.commands`.
2. Implement the command action in `src/extension.ts` using `vscode.commands.registerCommand`.
3. If it belongs in the sidebar, add it to `MarslibActionProvider` in `extension.ts` with a suitable `ThemeIcon`.

## 4. Building and Testing
- Build the extension: `npm run compile`
- Bundle for Production: `npm run package` (uses `esbuild` to eliminate Node modules)
- Build VSIX: `npx -y @vscode/vsce package --no-dependencies`
