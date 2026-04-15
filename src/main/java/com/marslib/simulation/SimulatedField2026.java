package com.marslib.simulation;

import edu.wpi.first.math.util.Units;
import java.util.ArrayList;
import java.util.List;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;

/**
 * Native Dyn4j models of the 2026 FRC Field (Rebuilt). Provides static boundaries and dynamic game
 * pieces that can be injected into the MARSPhysicsWorld.
 */
public class SimulatedField2026 {

  private static final double FIELD_X_MIN = 0.00000;
  private static final double FIELD_X_MAX = 16.54105;
  private static final double FIELD_Y_MIN = 0.00000;
  private static final double FIELD_Y_MAX = 8.06926;

  private static final double HUB_X_LEN = 1.19380;
  private static final double HUB_Y_LEN = 1.19380;
  private static final double HUB_X = 4.625594;
  private static final double HUB_Y = 4.03463;
  private static final double HUB_RAMP_LENGTH = Units.inchesToMeters(73.0);

  private static final double UPRIGHT_X_LEN = Units.inchesToMeters(3.5);
  private static final double UPRIGHT_Y_LEN = Units.inchesToMeters(1.5);
  private static final double UPRIGHT_OFFSET_FROM_END_WALL = 1.06204;
  private static final double UPRIGHT_OFFSET_FROM_SIDE_WALL = 3.31524;
  private static final double UPRIGHT_Y_SPACING = Units.inchesToMeters(33.75);

  private static final double TRENCH_WALL_Y_LEN = Units.inchesToMeters(12.0);
  private static final double TRENCH_WALL_X_LEN = Units.inchesToMeters(47.0);
  private static final double TRENCH_WALL_OFFSET_FROM_END_WALL = 4.61769;
  private static final double TRENCH_WALL_OFFSET_FROM_SIDE_WALL = 1.43113;

  private static final double BOUNDARY_THICKNESS = 0.2; // 20cm thick invisible borders

  // Fuel Constants
  private static final double FUEL_RADIUS_METERS = 0.075; // 7.5cm radius ~15cm diameter
  private static final double FUEL_MASS_KG = 0.2268; // ~0.5 lbs
  private static final double FUEL_FRICTION = 0.8;
  private static final double FUEL_RESTITUTION = 0.5;

  /**
   * Generates the static bodies for the 2026 Rebuilt field boundaries.
   *
   * @param addRampCollider Whether or not the ramps should be added as colliders.
   * @return A list of static Dyn4j bodies.
   */
  public static List<Body> getFieldBoundaries(boolean addRampCollider) {
    List<Body> bodies = new ArrayList<>();

    // Border Walls
    // Left (Blue) Wall
    bodies.add(
        createWall(
            FIELD_X_MIN - (BOUNDARY_THICKNESS / 2.0),
            FIELD_Y_MAX / 2.0,
            BOUNDARY_THICKNESS,
            FIELD_Y_MAX));
    // Right (Red) Wall
    bodies.add(
        createWall(
            FIELD_X_MAX + (BOUNDARY_THICKNESS / 2.0),
            FIELD_Y_MAX / 2.0,
            BOUNDARY_THICKNESS,
            FIELD_Y_MAX));
    // Bottom Wall
    bodies.add(
        createWall(
            FIELD_X_MAX / 2.0,
            FIELD_Y_MIN - (BOUNDARY_THICKNESS / 2.0),
            FIELD_X_MAX,
            BOUNDARY_THICKNESS));
    // Top Wall
    bodies.add(
        createWall(
            FIELD_X_MAX / 2.0,
            FIELD_Y_MAX + (BOUNDARY_THICKNESS / 2.0),
            FIELD_X_MAX,
            BOUNDARY_THICKNESS));

    // Blue Tower Uprights
    bodies.add(
        createWall(
            UPRIGHT_OFFSET_FROM_END_WALL,
            UPRIGHT_OFFSET_FROM_SIDE_WALL,
            UPRIGHT_X_LEN,
            UPRIGHT_Y_LEN));
    bodies.add(
        createWall(
            UPRIGHT_OFFSET_FROM_END_WALL,
            UPRIGHT_OFFSET_FROM_SIDE_WALL + UPRIGHT_Y_SPACING,
            UPRIGHT_X_LEN,
            UPRIGHT_Y_LEN));

    // Red Tower Uprights
    bodies.add(
        createWall(
            FIELD_X_MAX - UPRIGHT_OFFSET_FROM_END_WALL,
            FIELD_Y_MAX - UPRIGHT_OFFSET_FROM_SIDE_WALL,
            UPRIGHT_X_LEN,
            UPRIGHT_Y_LEN));
    bodies.add(
        createWall(
            FIELD_X_MAX - UPRIGHT_OFFSET_FROM_END_WALL,
            FIELD_Y_MAX - UPRIGHT_OFFSET_FROM_SIDE_WALL - UPRIGHT_Y_SPACING,
            UPRIGHT_X_LEN,
            UPRIGHT_Y_LEN));

    // Blue Trench Wall
    bodies.add(
        createWall(
            TRENCH_WALL_OFFSET_FROM_END_WALL,
            TRENCH_WALL_OFFSET_FROM_SIDE_WALL,
            TRENCH_WALL_X_LEN,
            TRENCH_WALL_Y_LEN));
    bodies.add(
        createWall(
            TRENCH_WALL_OFFSET_FROM_END_WALL,
            FIELD_Y_MAX - TRENCH_WALL_OFFSET_FROM_SIDE_WALL,
            TRENCH_WALL_X_LEN,
            TRENCH_WALL_Y_LEN));

    // Red Trench Wall
    bodies.add(
        createWall(
            FIELD_X_MAX - TRENCH_WALL_OFFSET_FROM_END_WALL,
            TRENCH_WALL_OFFSET_FROM_SIDE_WALL,
            TRENCH_WALL_X_LEN,
            TRENCH_WALL_Y_LEN));
    bodies.add(
        createWall(
            FIELD_X_MAX - TRENCH_WALL_OFFSET_FROM_END_WALL,
            FIELD_Y_MAX - TRENCH_WALL_OFFSET_FROM_SIDE_WALL,
            TRENCH_WALL_X_LEN,
            TRENCH_WALL_Y_LEN));

    // Hubs
    if (addRampCollider) {
      double hubTotalY = HUB_Y_LEN + 2.0 * HUB_RAMP_LENGTH;
      bodies.add(createWall(HUB_X, HUB_Y, HUB_X_LEN, hubTotalY));
      bodies.add(createWall(FIELD_X_MAX - HUB_X, HUB_Y, HUB_X_LEN, hubTotalY));
    } else {
      bodies.add(createWall(HUB_X, HUB_Y, HUB_X_LEN, HUB_Y_LEN));
      bodies.add(createWall(FIELD_X_MAX - HUB_X, HUB_Y, HUB_X_LEN, HUB_Y_LEN));
    }

    return bodies;
  }

  private static Body createWall(double centerX, double centerY, double width, double height) {
    Body wall = new Body();
    Rectangle rect = new Rectangle(width, height);
    BodyFixture fixture = new BodyFixture(rect);
    fixture.setFriction(0.2);
    fixture.setRestitution(0.1); // Slightly bouncy walls
    wall.addFixture(fixture);
    wall.setMass(MassType.INFINITE);
    wall.translate(centerX, centerY);
    return wall;
  }

  /**
   * Generates the dynamic bodies for the fuel balls placed around the 2026 Rebuilt field.
   *
   * @param efficiencyMode Whether to cull the number of center-field fuel balls spawned for
   *     performance.
   * @return A list of dynamic Dyn4j bodies representing fuel balls.
   */
  public static List<Body> getFuelBodies(boolean efficiencyMode) {
    List<Body> fuelBodies = new ArrayList<>();

    double centerPieceBottomRightCornerX = 7.35737;
    double centerPieceBottomRightCornerY = 1.724406;
    double redDepotBottomRightCornerX = 0.02;
    double redDepotBottomRightCornerY = 5.53;
    double blueDepotBottomRightCornerX = 16.0274;
    double blueDepotBottomRightCornerY = 1.646936;

    // Spawn center fuel
    for (int xIndex = 0; xIndex < 12; xIndex++) {
      for (int yIndex = 0; yIndex < 30; yIndex += efficiencyMode ? 3 : 1) {
        double px = centerPieceBottomRightCornerX + Units.inchesToMeters(5.991 * xIndex);
        double py = centerPieceBottomRightCornerY + Units.inchesToMeters(5.95 * yIndex);
        fuelBodies.add(createFuel(px, py));
      }
    }

    // Spawn blue depot fuel
    for (int xIndex = 0; xIndex < 4; xIndex++) {
      for (int yIndex = 0; yIndex < 6; yIndex++) {
        double px = blueDepotBottomRightCornerX + Units.inchesToMeters(5.991 * xIndex);
        double py = blueDepotBottomRightCornerY + Units.inchesToMeters(5.95 * yIndex);
        fuelBodies.add(createFuel(px, py));
      }
    }

    // Spawn red depot fuel
    for (int xIndex = 0; xIndex < 4; xIndex++) {
      for (int yIndex = 0; yIndex < 6; yIndex++) {
        double px = redDepotBottomRightCornerX + Units.inchesToMeters(5.991 * xIndex);
        double py = redDepotBottomRightCornerY + Units.inchesToMeters(5.95 * yIndex);
        fuelBodies.add(createFuel(px, py));
      }
    }

    return fuelBodies;
  }

  private static Body createFuel(double x, double y) {
    org.dyn4j.geometry.Circle circle = new org.dyn4j.geometry.Circle(FUEL_RADIUS_METERS);
    BodyFixture fixture = new BodyFixture(circle);
    fixture.setFriction(FUEL_FRICTION);
    fixture.setRestitution(FUEL_RESTITUTION); // Bouncy fuel balls
    fixture.setDensity(FUEL_MASS_KG / (Math.PI * FUEL_RADIUS_METERS * FUEL_RADIUS_METERS));

    Body fuel = new Body();
    fuel.addFixture(fixture);
    fuel.setMass(MassType.NORMAL);
    fuel.translate(x, y);
    fuel.setLinearDamping(1.8);
    fuel.setAngularDamping(0.8);

    // Add user data for advantage scope serialization if needed
    fuel.setUserData("Fuel");

    return fuel;
  }
}
