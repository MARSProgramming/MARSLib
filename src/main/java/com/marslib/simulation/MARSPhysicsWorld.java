package com.marslib.simulation;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.wpilibj.simulation.BatterySim;
import edu.wpi.first.wpilibj.simulation.RoboRioSim;
import frc.robot.constants.FieldConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dyn4j.dynamics.Body;
import org.dyn4j.world.World;
import org.ironmaple.simulation.seasonspecific.rebuilt2026.Arena2026Rebuilt;
import org.littletonrobotics.junction.Logger;

/**
 * Singleton 2D physics world managing all rigid-body interactions on the FRC field.
 *
 * <p>This class acts as an adapter, owning the maple-sim {@link Arena2026Rebuilt} instance and is
 * responsible for:
 *
 * <ul>
 *   <li>Initializing the field boundaries and obstacles via maple-sim.
 *   <li>Stepping the physics simulation and computing battery voltage sag.
 *   <li>Exporting all body poses to AdvantageKit for 3D visualization.
 * </ul>
 */
public class MARSPhysicsWorld {

  private static MARSPhysicsWorld instance;
  private static int accessCountSinceReset = 0;

  /** Maximum getInstance() calls before a stale-state warning is logged. */
  private static final int STALE_ACCESS_THRESHOLD = 500;

  private static final int STALE_BODY_THRESHOLD = 20;

  /** Internal wrapper to expose the protected dyn4j world from SimulatedArena. */
  private static class ArenaWrapper extends Arena2026Rebuilt {
    public ArenaWrapper(boolean addRampCollider) {
      super(addRampCollider);
    }

    public World<Body> getDyn4jWorld() {
      return this.physicsWorld;
    }
  }

  public static MARSPhysicsWorld getInstance() {
    if (instance == null) {
      instance = new MARSPhysicsWorld();
      accessCountSinceReset = 0;
    }
    accessCountSinceReset++;
    if (accessCountSinceReset == STALE_ACCESS_THRESHOLD
        && instance.getDyn4jWorld().getBodyCount() > STALE_BODY_THRESHOLD) {
      Logger.recordOutput(
          "PhysicsSim/StaleWarning",
          String.format(
              "WARNING: MARSPhysicsWorld has %d bodies after %d accesses without reset."
                  + " Did you forget MARSPhysicsWorld.resetInstance() in @BeforeEach?",
              instance.getDyn4jWorld().getBodyCount(), accessCountSinceReset));
    }
    return instance;
  }

  @SuppressWarnings("PMD.NullAssignment")
  public static void resetInstance() {
    if (instance != null && instance.arena != null) {
      // Safe tear down if needed
      org.ironmaple.simulation.motorsims.SimulatedBattery.clearElectricalAppliances();
    }
    instance = null;
    accessCountSinceReset = 0;
  }

  public World<Body> getDyn4jWorld() {
    return arena.getDyn4jWorld();
  }

  public int getBodyCount() {
    return arena.getDyn4jWorld().getBodyCount();
  }

  private final ArenaWrapper arena;
  private final Map<String, Body> mechanismBodies;

  private double frameCurrentDrawAmps = 0.0;
  private double simulatedVoltage = 12.0;

  @SuppressWarnings("PMD.AssignmentToNonFinalStatic")
  private MARSPhysicsWorld() {
    instance = this;
    // By default, full realism mode (efficiency mode OFF) unless toggled via FieldConstants
    arena = new ArenaWrapper(true);
    arena.setEfficiencyMode(FieldConstants.MAPLE_SIM_EFFICIENCY_MODE);

    mechanismBodies = new HashMap<>();

    // Populate the field with game pieces immediately upon initialization
    arena.resetFieldForAuto();
  }

  /** Return the underlying maple-sim Arena2026Rebuilt instance. */
  public Arena2026Rebuilt getArena() {
    return arena;
  }

  public World<Body> getWorld() {
    return getDyn4jWorld();
  }

  public void registerMechanismBody(String name, Body body) {
    mechanismBodies.put(name, body);
    arena.getDyn4jWorld().addBody(body);
  }

  public void addFrameCurrentDrawAmps(double amps) {
    frameCurrentDrawAmps += amps;
  }

  public void addCustomSimulation(org.ironmaple.simulation.SimulatedArena.Simulatable simulatable) {
    arena.addCustomSimulation(simulatable);
  }

  public void update(double dtSeconds) {
    // Delegate to maple-sim's simulation step
    arena.simulationPeriodic();

    // Compute battery voltage sag
    Logger.recordOutput("PhysicsWorld/FrameCurrentDraw_A", frameCurrentDrawAmps);
    double loadedVoltage = BatterySim.calculateDefaultBatteryLoadedVoltage(frameCurrentDrawAmps);
    loadedVoltage = Math.max(6.0, loadedVoltage);
    simulatedVoltage = loadedVoltage;
    RoboRioSim.setVInVoltage(loadedVoltage);
    Logger.recordOutput("PhysicsWorld/ComputedVoltage", loadedVoltage);
    Logger.recordOutput("PhysicsWorld/Heartbeat", edu.wpi.first.wpilibj.Timer.getFPGATimestamp());

    frameCurrentDrawAmps = 0.0;

    exportToAdvantageScope();
  }

  public double getSimulatedVoltage() {
    return simulatedVoltage;
  }

  private void exportToAdvantageScope() {
    // Export named mechanism bodies
    for (Map.Entry<String, Body> entry : mechanismBodies.entrySet()) {
      String mechanismName = entry.getKey();
      Body body = entry.getValue();

      double xMeters = body.getTransform().getTranslationX();
      double yMeters = body.getTransform().getTranslationY();
      double yawRads = body.getTransform().getRotationAngle();

      Pose3d pose3d = new Pose3d(xMeters, yMeters, 0.0, new Rotation3d(0.0, 0.0, yawRads));
      Logger.recordOutput("PhysicsWorld/" + mechanismName, pose3d);
    }

    // Export field game pieces
    List<Pose3d> fuelPoses = arena.getGamePiecesPosesByType("Fuel");
    Logger.recordOutput("PhysicsWorld/FuelCount", fuelPoses.size());
    Logger.recordOutput("Simulation/GamePieces", fuelPoses.toArray(new Pose3d[0]));
    Logger.recordOutput("PhysicsWorld/Fuel", fuelPoses.toArray(new Pose3d[0]));
  }
}
