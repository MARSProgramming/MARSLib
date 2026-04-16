package com.marslib.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.marslib.mechanisms.FlywheelIOSim;
import com.marslib.mechanisms.RotaryMechanismIOSim;
import com.marslib.power.MARSPowerManager;
import com.marslib.power.PowerConfig;
import com.marslib.power.PowerIOSim;
import com.marslib.simulation.MARSPhysicsWorld;
import com.marslib.swerve.GyroIOSim;
import com.marslib.swerve.SwerveConfig;
import com.marslib.swerve.SwerveDrive;
import com.marslib.swerve.SwerveModule;
import com.marslib.swerve.SwerveModuleIOSim;
import com.marslib.testing.MARSTestHarness;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj.simulation.SimHooks;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.commands.ShootOnTheMoveCommand;
import frc.robot.subsystems.MARSCowl;
import frc.robot.subsystems.MARSIntakePivot;
import frc.robot.subsystems.MARSShooter;
import frc.robot.subsystems.MARSSuperstructure;
import java.lang.reflect.Field;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ShootOnTheMovePhysicsTest {

  private SwerveDrive swerveDrive;
  private MARSCowl cowl;
  private MARSIntakePivot intakePivot;
  private MARSShooter floorIntake;
  private MARSShooter shooter;
  private MARSShooter feeder;
  private MARSSuperstructure superstructure;
  private MARSPowerManager powerManager;

  @BeforeEach
  public void setUp() {
    MARSTestHarness.reset();
    edu.wpi.first.wpilibj2.command.CommandScheduler.getInstance().cancelAll();
    DriverStationSim.setAllianceStationId(edu.wpi.first.hal.AllianceStationID.Blue1);
    DriverStationSim.setAutonomous(true);
    DriverStationSim.setEnabled(true);

    PowerConfig powerConfig = MARSTestHarness.createPowerConfig();
    powerManager = new MARSPowerManager(new PowerIOSim(powerConfig), powerConfig);

    GyroIOSim gyroSim = new GyroIOSim();
    SwerveConfig config = MARSTestHarness.createSwerveConfig();

    SwerveModule[] modules = new SwerveModule[4];
    for (int i = 0; i < 4; i++) {
      modules[i] = new SwerveModule(i, new SwerveModuleIOSim(i, config), config);
    }

    swerveDrive = new SwerveDrive(modules, gyroSim, powerManager, config);
    swerveDrive.resetPose(new Pose2d(3.0, 4.0, new Rotation2d(0)));

    cowl = new MARSCowl(new RotaryMechanismIOSim("Cowl", 50.0, 0.5, 0.5), powerManager);
    intakePivot =
        new MARSIntakePivot(new RotaryMechanismIOSim("IntakePivot", 50.0, 0.5, 0.5), powerManager);
    floorIntake =
        new MARSShooter(
            "FloorIntake",
            new FlywheelIOSim(
                edu.wpi.first.math.system.plant.DCMotor.getKrakenX60Foc(1), 1.0, 0.05),
            powerManager);
    shooter =
        new MARSShooter(
            "Shooter",
            new FlywheelIOSim(
                edu.wpi.first.math.system.plant.DCMotor.getKrakenX60Foc(1), 1.0, 0.05),
            powerManager);
    feeder =
        new MARSShooter(
            "Feeder",
            new FlywheelIOSim(
                edu.wpi.first.math.system.plant.DCMotor.getKrakenX60Foc(1), 1.0, 0.05),
            powerManager);

    superstructure =
        new MARSSuperstructure(
            cowl,
            intakePivot,
            floorIntake,
            shooter,
            feeder,
            swerveDrive::getPose,
            () -> java.util.Optional.empty(),
            () -> 0.0,
            () ->
                ChassisSpeeds.fromRobotRelativeSpeeds(
                    swerveDrive.getChassisSpeeds(), swerveDrive.getPose().getRotation()));

    // Use reflection to load 10 balls into the fully simulated robot
    try {
      Field pieceCountField = MARSSuperstructure.class.getDeclaredField("internalPieceCount");
      pieceCountField.setAccessible(true);
      pieceCountField.set(superstructure, 10);
    } catch (Exception e) {
      fail("Failed to set internal game piece count for testing: " + e.getMessage());
    }
  }

  @AfterEach
  public void tearDown() {
    MARSTestHarness.cleanup();
  }

  @Test
  public void testShootOnTheMoveScoresGamePiecesInSim() {
    // We are simulating driving sideways past the blue hub while shooting
    ShootOnTheMoveCommand sotmCommand =
        new ShootOnTheMoveCommand(
            swerveDrive,
            superstructure,
            () -> 2.0, // translationX: Move along X axis at 2 m/s
            () -> 1.0 // translationY: Move along Y axis at 1 m/s
            );

    CommandScheduler.getInstance().schedule(sotmCommand);

    int maxTicks = 400; // Run for 8 seconds

    for (int tick = 0; tick < maxTicks; tick++) {
      DriverStationSim.notifyNewData();
      SimHooks.stepTiming(0.02);
      edu.wpi.first.wpilibj.DriverStation
          .refreshData(); // Synchronous HAL flush avoids Thread starvation
      CommandScheduler.getInstance().run();
      MARSPhysicsWorld.getInstance().update(0.02);
    }

    // We successfully scored pieces and depleted our reserve.
    try {
      Field pieceCountField = MARSSuperstructure.class.getDeclaredField("internalPieceCount");
      pieceCountField.setAccessible(true);
      int finalCount = (int) pieceCountField.get(superstructure);
      assertTrue(
          finalCount < 20,
          "Robot did not deduct fired balls from its inventory! Final count: " + finalCount);
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }
}
