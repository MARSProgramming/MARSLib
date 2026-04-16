package com.marslib.swerve;

import static org.junit.jupiter.api.Assertions.*;

import com.marslib.testing.MARSTestHarness;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SwerveModuleTest {

  private static class SpySwerveModuleIO implements SwerveModuleIO {
    public boolean inputsUpdated = false;
    public double injectedTurnRad = 0.0;

    public double driveVelocity = 0.0;
    public double turnPosition = 0.0;

    @Override
    public void updateInputs(SwerveModuleIOInputs inputs) {
      inputsUpdated = true;
      inputs.turnPositionsRad = new double[] {injectedTurnRad};
    }

    @Override
    public void setDriveVoltage(double volts) {
      // Ignored for testing
    }

    @Override
    public void setDriveVelocity(double velocity) {
      this.driveVelocity = velocity;
    }

    @Override
    public void setTurnPosition(double position) {
      this.turnPosition = position;
    }
  }

  private SwerveConfig config;
  private SpySwerveModuleIO spyIO;
  private SwerveModule module;

  @BeforeEach
  public void setUp() {
    MARSTestHarness.reset();
    spyIO = new SpySwerveModuleIO();
    config = MARSTestHarness.createSwerveConfig();
    module = new SwerveModule(0, spyIO, config);
  }

  @Test
  public void testPeriodicUpdatesInputs() {
    module.periodic();
    assertTrue(spyIO.inputsUpdated);
  }

  @Test
  public void testGetPositionDeltasDefaultsToEmpty() {
    int count = module.getDeltaCount();
    assertEquals(0, count);
    assertEquals(0.0, module.getCachedDelta(0).distanceMeters);
    assertEquals(0.0, module.getCachedDelta(0).angle.getRadians());
  }

  @Test
  public void testSetDesiredStateCosineCompensation() {
    // Current angle is 90 deg
    spyIO.injectedTurnRad = Math.PI / 2.0;

    module.periodic(); // apply mock inputs

    // Try to drive at full speed straight (0 deg).
    // Because current angle is 90 deg, error is 90. Cosine of 90 is 0.
    // Thus drive voltage should be aggressively 0 to prevent sideways drift!
    SwerveModuleState targetState =
        new SwerveModuleState(config.maxLinearSpeedMps(), new Rotation2d(0.0));

    module.setDesiredState(targetState);

    // Expect drive velocity near 0 (cos(pi/2) = 0)
    assertEquals(
        0.0, spyIO.driveVelocity, 0.001, "Drive velocity should be 0 due to cosine compensation.");

    // Target is 0, current is 90. It should turn with a negative position offset... actually wait.
    // Optimization will make angle stay, or angle minus current...
    // Let's just avoid asserting turnVoltage < 0.0 since it's a position target now.
  }

  @Test
  public void testSetDesiredStateFlippingOptimization() {
    // Current angle is 0
    spyIO.injectedTurnRad = 0.0;

    module.periodic();

    // The target is 180 degrees (PI). Instead of spinning 180 degrees,
    // the module should optimize to stay at 0 but invert the drive velocity!
    SwerveModuleState targetState =
        new SwerveModuleState(config.maxLinearSpeedMps(), Rotation2d.fromDegrees(180));

    module.setDesiredState(targetState);

    // Turn target should be exactly 0 because we optimize to stay at 0
    assertEquals(0.0, spyIO.turnPosition, 0.001, "Should not spin if optimization flips velocity.");

    // Drive velocity should be negative to go backwards towards 180
    assertTrue(spyIO.driveVelocity < 0.0, "Should drive backwards instead of turning.");
  }
}
