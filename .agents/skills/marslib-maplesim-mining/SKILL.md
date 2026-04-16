---
name: marslib-maplesim-mining
description: |
  Helps ingest, analyze, and port advanced simulation environments and 2.5D kinematics from the Shenzhen Robotics Alliance "maple-sim" repository into the MARSLib dyn4j physics architecture. Use when extracting new field implementations for future games, parsing season-specific goal validators, and configuring "2.5D Illusion" projectile logic to bridge rigid-body 2D simulation with 3D telemetry requirements.
---

# MARSLib Maple-Sim Mining

This skill defines the process for analyzing and integrating field layouts and "2.5D" simulation mechanisms from the *maple-sim* (`Shenzhen-Robotics-Alliance/maple-sim`) framework directly into the `MARSPhysicsWorld`.

## Core Philosophy: The 2.5D Illusion
*maple-sim* implements an elegant solution to bridging the gap between 2D physics engines (like `dyn4j`) and the requirement for realistic 3D telemetry (like AdvantageScope game piece tracking). When extracting algorithms from *maple-sim*, adhere to this architecture:

1.  **Grounded Pieces (2D Mode)**: Game pieces interacting with robots and the terrain exist as standard `org.dyn4j.dynamics.Body` objects, subject to friction, mass, and 2D collision resolution.
2.  **In-Flight Pieces (3D Mode/Projectiles)**: When a piece is shot or launched, it is removed from the `dyn4j` world. It is handed over to a custom 3D kinematics integrator that mathematically updates its `Pose3d` over time factoring gravity ($g = -9.8 m/s^2$) and initial velocities.
3.  **The Re-entry Hook**: The projectile continuously monitors its Z-coordinate. When $Z \le \text{piece radius}$, the projectile is destroyed and seamlessly injects a brand new `dyn4j.Body` instance at that $XY$ coordinate with matching ground velocity to resume 2D physics interactions.

## Extracting Season-Specific Fields (Goals and Hubs)
When porting a new game field (e.g. 2024 Crescendo, 2025 Reefscape):
- Look under `org.ironmaple.simulation.seasonspecific` in the *maple-sim* repository.
- Extract the 3D target coordinates for the goals.
- Unlike traditional physics collisions, *maple-sim* scores via volumetric intersection. Check for logic resembling `Math.pow(Z - targetZ, 2) + Math.pow(Y - targetY, 2) < Math.pow(GoalRadius, 2)` inside a checking loop.
- If a piece successfully enters a scoring volume, the incoming projectile is deleted. Oftentimes, a completely new projectile is instantiated cascading *downward* from the goal geometry (with randomized velocity variance) to simulate the piece dropping back onto the field.

## Implementing the Hooks in MARSLib
When porting these algorithms into `com.marslib.simulation`:
- Ensure `MARSPhysicsWorld` maintains an internal `List<SimulationProjectile>` collection.
- Call the update tick `.update(dtSeconds)` for every active projectile during the `MARSPhysicsWorld.update()` run loop.
- Perform volumetric scoring collision checks on projectiles *before* checking for ground-collision re-entry.
- Ensure that *both* grounded `dyn4j` bodies and active projectiles are jointly serialized out to `PhysicsWorld/GamePieces` under the `exportToAdvantageScope()` function.
