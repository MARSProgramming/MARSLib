package com.marslib.swerve;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.marslib.power.MARSPowerManager;
import com.marslib.power.PowerIO;
import com.marslib.testing.MARSTestHarness;
import com.pathplanner.lib.auto.AutoBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SwerveAutoBuilderTest {

  private SwerveDrive swerveDrive;

  @BeforeEach
  public void setUp() {
    MARSTestHarness.reset();

    PowerIO spoofedVoltageIO =
        new PowerIO() {
          @Override
          public void updateInputs(PowerIOInputs inputs) {
            inputs.voltage = 12.0;
            inputs.isBrownedOut = false;
          }
        };
    MARSPowerManager spoofedPowerManager =
        new MARSPowerManager(spoofedVoltageIO, MARSTestHarness.createPowerConfig());

    GyroIOSim gyroIOSim = new GyroIOSim();
    SwerveModule[] modules = new SwerveModule[4];
    SwerveModuleIOSim[] simIOs = new SwerveModuleIOSim[4];

    SwerveConfig config = MARSTestHarness.createSwerveConfig();
    for (int i = 0; i < 4; i++) {
      simIOs[i] = new SwerveModuleIOSim(i, config);
      modules[i] = new SwerveModule(i, simIOs[i], config);
    }

    swerveDrive = new SwerveDrive(modules, gyroIOSim, spoofedPowerManager, config);
  }

  @Test
  public void testAutoBuilderConfiguresCorrectly() {
    assertDoesNotThrow(
        () -> SwerveAutoBuilder.configure(swerveDrive),
        "AutoBuilder configuration should not throw exceptions");

    assertTrue(AutoBuilder.isConfigured(), "PathPlanner AutoBuilder should be properly configured");
  }

  @Test
  public void testAlignToPointProducesValidCommand() {
    SwerveAutoBuilder.configure(swerveDrive);
    assertDoesNotThrow(
        () -> {
          var cmd = SwerveAutoBuilder.alignToPoint(swerveDrive, () -> new Pose2d());
          cmd.initialize();
        },
        "alignToPoint command should be built successfully without crashing");
  }
}
