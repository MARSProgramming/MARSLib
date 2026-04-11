package com.marslib.swerve;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.marslib.power.MARSPowerManager;
import com.marslib.power.PowerIO;
import com.marslib.simulation.MARSPhysicsWorld;
import com.marslib.testing.MARSTestHarness;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.simulation.SimHooks;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.constants.PowerConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SwerveDriveTest {

  private double simulatedVoltageOverride = 12.0;
  private SwerveDrive swerveDrive;
  private SwerveModuleIOSim[] simIOs;

  @BeforeEach
  public void setUp() {
    MARSTestHarness.reset();
    simulatedVoltageOverride = 12.0;

    // Use a custom PowerIO to manipulate testing voltages independently
    PowerIO spoofedVoltageIO =
        new PowerIO() {
          @Override
          public void updateInputs(PowerIOInputs inputs) {
            inputs.voltage = simulatedVoltageOverride;
            inputs.isBrownedOut = simulatedVoltageOverride < 6.0;
          }
        };
    MARSPowerManager spoofedPowerManager = new MARSPowerManager(spoofedVoltageIO);

    GyroIOSim gyroIOSim = new GyroIOSim();
    SwerveModule[] modules = new SwerveModule[4];
    simIOs = new SwerveModuleIOSim[4];

    for (int i = 0; i < 4; i++) {
      simIOs[i] = new SwerveModuleIOSim(i);
      modules[i] = new SwerveModule(i, simIOs[i]);
    }

    swerveDrive = new SwerveDrive(modules, gyroIOSim, spoofedPowerManager);
  }

  @Test
  public void testSwerveDriveIntegrationPhysicallyMovesRobot() {
    // 1. Initial State
    swerveDrive.periodic();
    swerveDrive.simulationPeriodic();
    assertEquals(0.0, swerveDrive.getPose().getX(), 0.1);

    // 2. Command forward velocity mapping
    ChassisSpeeds targetSpeeds = new ChassisSpeeds(3.0, 0.0, 0.0);

    // 3. Fast-forward simulation by 1.0 second
    for (int i = 0; i < 50; i++) {
      swerveDrive.runVelocity(targetSpeeds);
      CommandScheduler.getInstance().run();
      swerveDrive.periodic();
      swerveDrive.simulationPeriodic();
      MARSPhysicsWorld.getInstance().update(0.02);
      SimHooks.stepTiming(0.02);
    }

    // 4. Assert robot physically attained the intended distance purely mathematically through dyn4j
    Pose2d finalPose = swerveDrive.getPose();
    assertEquals(
        2.0,
        finalPose.getX(),
        0.5,
        "Robot failed to traverse ~2m natively within 1 sec through maple-sim");
  }

  @Test
  public void testLoadSheddingRestrictsKinematicsOutputs() {
    // Drop voltage to trigger protective shutdown mode
    simulatedVoltageOverride = PowerConstants.CRITICAL_VOLTAGE - 0.5;

    // Command drive forward
    ChassisSpeeds targetSpeeds = new ChassisSpeeds(3.0, 0.0, 0.0);
    double initialX = swerveDrive.getPose().getX();

    for (int i = 0; i < 50; i++) {
      swerveDrive.runVelocity(targetSpeeds);
      CommandScheduler.getInstance().run();
      swerveDrive.periodic(); // Polls the power manager
      swerveDrive.simulationPeriodic();
      MARSPhysicsWorld.getInstance().update(0.02);
      SimHooks.stepTiming(0.02);
    }

    // Because voltage scales to 0.0 in deep brownout, the kinematics should have completely
    // halted and the robot shouldn't have moved.
    assertEquals(
        initialX,
        swerveDrive.getPose().getX(),
        0.05,
        "Robot moved despite critical brownout voltage ceiling clamping.");
  }
}
