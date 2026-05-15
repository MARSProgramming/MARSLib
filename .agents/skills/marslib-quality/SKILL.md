---
name: marslib-quality
description: Ensures code quality, build integrity, and tooling standards. Use when auditing code, fixing build errors, configuring CI, or adding VS Code extension rules.
---

# MARSLib Quality & Tooling

## 1. Build & CI (Gradle)

| Task | Purpose |
|---|---|
| `gradlew build` | Compile + test suite |
| `gradlew test` | JUnit 5 tests only |
| `gradlew spotlessApply` | Auto-format (Google Java Format) |
| `gradlew spotlessCheck` | Verify formatting (CI gate) |
| `gradlew javadoc` | Generate docs, catch broken `{@link}` |
| `gradlew deploy` | Deploy to roboRIO |

### CI Pipeline
1. `spotlessCheck` — formatting violations → reject PR
2. `build` — compile all source
3. `test` — run full physics suite
4. `javadoc` — catch doc errors

### Rules
- **Always run `spotlessApply` before committing** — no manual formatting
- **Tests must pass before deploy** — never `@Disabled` to work around failures
- **Javadoc enforced** — public classes/methods must have docs
- **Configuration cache MUST be `false`** — WPILib tasks incompatible

### Common Fixes
| Issue | Fix |
|---|---|
| Spotless failures | `.\gradlew.bat spotlessApply` locally |
| "already been configured" | AutoBuilder singleton in tests — see `marslib-swerve` |
| Javadoc HTML errors | Use `{@code text}` not `<code>text</code>` |
| vendordep conflicts | Delete `build/` and re-run `build` |

## 2. Code Audit Rules

### Zero-Allocation
| Pattern | Fix |
|---|---|
| `new` in periodic | Pre-allocate `static final` caches |
| `.toArray(new Type[0])` in hot paths | Statically pre-allocate with locks |
| `System.gc()` | Delete immediately |
| `e.printStackTrace()` | Use `DriverStation.reportError()` |

### Math Safety
| Pattern | Fix |
|---|---|
| Unprotected `/ x` | `Math.abs(x) < 1e-6` epsilon |
| `% 360` | `MathUtil.angleModulus()` |
| `Math.sqrt()`/`Math.asin()` out of domain | Clamp to valid range |
| Magic conversions like `* 0.0174533` | `Units.degreesToRadians()` |
| Interpolations without NaN check | Wrap in `Double.isFinite(guess)` |
| Negative speeds without flip | `speed < 0 ? rotateBy(180deg) : ...` |

### API Safety
| Pattern | Fix |
|---|---|
| `Optional.get()` in teleop | `.orElse()` or `.ifPresent()` |
| Classes without tests | Generate boundary-condition tests |

### Control Theory & Hardware
| Check | Fix |
|---|---|
| PID without `setIZone()` | Add zone boundaries |
| Missing `SupplyCurrentLimit` (40A) or `StatorCurrentLimit` (60-80A) | Add both limits |
| Non-critical signals at 100Hz+ | Drop to 4-10Hz |
| `waitForUpdate()` without timeout | Add `Timeout = 50ms` |

### CAN StatusCode Verification
Every `.apply()` and `setUpdateFrequency()` must:
1. Check `StatusCode.isOK()` and log failures, OR
2. Use retry loop (up to 5 for CAN contention)

```bash
grep -rn "\.apply(" src/main/java/com/marslib/ --include="*.java"
```

### Thread Safety
| Pattern | Fix |
|---|---|
| Non-final fields in `PhoenixOdometryThread` | Must be `volatile`, `Atomic*`, or locked |
| `synchronized` on 250Hz path | Remove — causes lock contention |
| `getInstance()` in periodic | Cache in constructor |

### Graceful Degradation
| Check | Fix |
|---|---|
| `hasHardwareConnected` not set | Set flag based on CAN status |
| `getValueAsDouble()` without `Double.isFinite()` | Add ternary checks |
| Vision/IMU without timestamp checks | Reject stale data |

### AdvantageKit Replay
```bash
# Verify no direct hardware reads
grep -rn "Timer.getFPGATimestamp\|RobotController.getBatteryVoltage\|DriverStation\." \
  src/main/java/com/marslib/ --include="*.java" \
  | grep -v "IO\.java\|IOSim\.java\|IOTalonFX\.java"
```

Every IO interface must have `@AutoLog` on `Inputs`:
```bash
grep -rn "class.*Inputs" src/main/java/com/marslib/ --include="*IO.java" | grep -B 1 "@AutoLog"
```

## 3. VS Code Extension (ProjectDoctor)

### Adding Linter Rules
1. Add rule in `projectDoctor.ts` with unique code (e.g., `MARS_010`)
2. If auto-fixable, add in `quickFixProvider.ts`
3. Register command in `package.json` under `contributes.commands`

### Mandatory Einstein-Tier Rules
| Rule | Pattern | Raise As |
|---|---|---|
| Zero-Allocation Hot Paths | `toArray(new Type[0])` in periodic | `MARS_ERROR_OOM` |
| NaN Guards | Interpolations without `Double.isFinite()` | `MARS_WARN_MATH` |
| Kinematic Reversals | Negative velocity without flip | `MARS_WARN_KINEMATICS` |
| Console Silence | `e.printStackTrace()` | `MARS_ERROR_TELEMETRY` |
| AScope Determinism | Missing `"game": "FRC:2026 Field"` | `MARS_ERROR_ASCOPE` |

### Build
- `npm run compile` — build extension
- `npm run package` — bundle with esbuild
- `npx -y @vscode/vsce package --no-dependencies` — build VSIX

## 4. Audit Workflow

```bash
# 1. Static analysis
./gradlew spotlessCheck pmdMain checkstyleMain

# 2. Zero-allocation scan
grep -rn "new " src/main/java/com/marslib/ | grep -i periodic

# 3. Math validation
grep -rn "% 360\|Optional.get()\|e.printStackTrace" src/main/java/com/marslib/

# 4. Coverage
./gradlew test jacocoTestReport
```
