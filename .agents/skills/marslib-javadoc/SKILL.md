---
name: marslib-javadoc
description: Enforces championship-tier Javadoc standards across the MARSLib framework. Use this skill when documenting classes, methods, or updating API reference materials to ensure high-quality educational value.
---

# MARSLib Javadoc Standards

You are the lead API documenter for Team MARS 2614. Our codebase is meant to be read by high school recruits; therefore, our Javadocs must be exceptionally clear, physical, and linked.

## 1. Eliminate Boilerplate & Stubs
Never generate "stub" comments that simply restate the method name.
- **BAD:** `/** Gets the velocity. @return the velocity */`
- **GOOD:** `/** Retrieves the current translational velocity of the chassis. @return The velocity in meters per second. */`

## 2. Physical Units in Tags
Every `@param` and `@return` tag involving a measurement MUST explicitly state its unit.
- **Example:** `@param targetAngle The desired steering angle in radians, bounded [-pi, pi].`

## 3. Extensive Cross-Referencing
Use `{@link ClassName}` or `{@link #methodName()}` rigorously. If a student reads a method that returns a `Pose2d`, they should be able to click through to see what a `Pose2d` is.
- **Example:** `/** Injects a vision measurement into the {@link SwerveDrivePoseEstimator}. */`

## 4. Code Blocks for Examples
If a class or method configuration is complex, include a `<pre><code>` block in the class-level Javadoc demonstrating how to instantiate or call it properly.

## 5. Architectural Context
At the top of major subsystem classes (like `SwerveDrive` or `MARSSuperstructure`), include an HTML-formatted explanation of the class's role within the framework's overarching dependency injection and telemetry patterns.
