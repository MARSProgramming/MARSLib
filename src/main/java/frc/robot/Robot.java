// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot;

import edu.wpi.first.networktables.BooleanPublisher;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.constants.ModeConstants;
import org.littletonrobotics.junction.LogFileUtil;
import org.littletonrobotics.junction.LoggedRobot;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.NT4Publisher;
import org.littletonrobotics.junction.wpilog.WPILOGReader;
import org.littletonrobotics.junction.wpilog.WPILOGWriter;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends LoggedRobot {
  private RobotContainer robotContainer;
  private Command autonomousCommand;

  /** NT4 flag for tethered log-download tools. True when disabled and not on FMS. */
  private final BooleanPublisher logsReadyPublisher =
      NetworkTableInstance.getDefault().getBooleanTopic("System/LogsReadyForDownload").publish();

  public Robot() {
    super(ModeConstants.LOOP_PERIOD_SECS);

    // Record metadata
    Logger.recordMetadata("ProjectName", BuildConstants.MAVEN_NAME);
    Logger.recordMetadata("Framework", "MARSLib 2.0");
    Logger.recordMetadata("Authors", "FRC Team 2614 MARS (Mountaineer Area RoboticS)");
    Logger.recordMetadata("Website", "https://marsfirst.org");
    Logger.recordMetadata("BuildDate", BuildConstants.BUILD_DATE);
    Logger.recordMetadata("GitSHA", BuildConstants.GIT_SHA);
    Logger.recordMetadata("GitDate", BuildConstants.GIT_DATE);
    Logger.recordMetadata("GitBranch", BuildConstants.GIT_BRANCH);
    Logger.recordMetadata(
        "GitDirty",
        switch (BuildConstants.DIRTY) {
          case 0 -> "All changes committed";
          case 1 -> "Uncommitted changes";
          default -> "Unknown";
        });

    // Set up data receivers & replay source
    switch (ModeConstants.CURRENT_MODE) {
      case REAL:
        // Running on a real robot, log to a USB stick ("/U/logs")
        Logger.addDataReceiver(new WPILOGWriter());
        Logger.addDataReceiver(new NT4Publisher());

        // Elite CTRE Logging: Phoenix natively records all data at 250Hz+ into .hoot files
        com.ctre.phoenix6.SignalLogger.setPath("/U/logs");
        com.ctre.phoenix6.SignalLogger.start();
        break;

      case SIM:
        // Running a physics simulator, log to NT
        Logger.addDataReceiver(new WPILOGWriter("logs/"));
        Logger.addDataReceiver(new NT4Publisher());
        break;

      case REPLAY:
        // Replaying a log, set up replay source
        setUseTiming(false); // Run as fast as possible
        String logPath = LogFileUtil.findReplayLog();
        Logger.setReplaySource(new WPILOGReader(logPath));
        Logger.addDataReceiver(new WPILOGWriter(LogFileUtil.addPathSuffix(logPath, "_sim")));
        break;
      default:
        break;
    }

    // Start AdvantageKit logger
    Logger.start();
  }

  @Override
  @SuppressWarnings("PMD.SystemPrintln")
  public void robotInit() {
    System.out.println("-------------------------------------------------------");
    System.out.println("  __  __          _____   _____ _      _ _     ");
    System.out.println(" |  \\/  |   /\\   |  __ \\ / ____| |    (_) |    ");
    System.out.println(" | \\  / |  /  \\  | |__) | (___ | |     _| |__  ");
    System.out.println(" | |\\/| | / /\\ \\ |  _  / \\___ \\| |    | | '_ \\ ");
    System.out.println(" | |  | |/ ____ \\| | \\ \\ ____) | |____| | |_) |");
    System.out.println(" |_|  |_/_/    \\_\\_|  \\_\\_____/|______|_|_.__/ ");
    System.out.println("                                               ");
    System.out.println(" MARSLib Core Framework - Initializing Subsystems");
    System.out.println(" Powered by Mountaineer Area RoboticS - Team 2614");
    System.out.println("-------------------------------------------------------");

    edu.wpi.first.wpilibj.DriverStation.reportWarning(
        "[MARSLib] Framework Initialized - Mountaineer Area RoboticS Team 2614", false);
    robotContainer = new RobotContainer();
  }

  /** This function is called periodically during all modes. */
  @Override
  public void robotPeriodic() {
    double loopStartSeconds = edu.wpi.first.wpilibj.Timer.getFPGATimestamp();

    CommandScheduler.getInstance().run();

    double loopEndSeconds = edu.wpi.first.wpilibj.Timer.getFPGATimestamp();
    Logger.recordOutput("System/LoopRunTime_ms", (loopEndSeconds - loopStartSeconds) * 1000.0);
    Logger.recordOutput(
        "System/BatteryVoltage", edu.wpi.first.wpilibj.RobotController.getBatteryVoltage());
    Logger.recordOutput(
        "System/BrownoutVoltage", edu.wpi.first.wpilibj.RobotController.getBrownoutVoltage());
    Logger.recordOutput(
        "System/WatchdogActive", edu.wpi.first.wpilibj.RobotController.isSysActive());
    Logger.recordOutput(
        "System/CANBusUtilization",
        edu.wpi.first.wpilibj.RobotController.getCANStatus().percentBusUtilization);

    // Track CANivore separately because FRC swerve relies heavily on it
    Logger.recordOutput(
        "System/CANivoreUtilization",
        new com.ctre.phoenix6.CANBus("CAN2").getStatus().BusUtilization);
  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {
    // Signal tethered laptops that logs are available for download
    logsReadyPublisher.set(!DriverStation.isFMSAttached());
  }

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {
    // No-op. GC is triggered once in disabledInit() on mode transition,
    // which is sufficient to reclaim post-auto garbage without risking
    // a GC pause during the disabled→auto transition window.

    // Attempt to upload logs safely in background
    com.marslib.util.LogUploader.tryUploadLogsAsync();
  }

  /** This autonomous runs the selected autonomous command. */
  @Override
  public void autonomousInit() {
    // Clear the download flag when entering any active mode
    logsReadyPublisher.set(false);

    if (robotContainer != null) {
      autonomousCommand = robotContainer.getAutonomousCommand();
      if (autonomousCommand != null) {
        CommandScheduler.getInstance().schedule(autonomousCommand);
      }
    }
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {}

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
    // Clear the download flag when entering any active mode
    logsReadyPublisher.set(false);

    if (autonomousCommand != null) {
      autonomousCommand.cancel();
    }
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {}

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {}

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {
    edu.wpi.first.wpilibj.simulation.DriverStationSim.setAllianceStationId(
        edu.wpi.first.hal.AllianceStationID.Blue1);

    // Programmatically enable teleop mode so the robot is drivable immediately
    edu.wpi.first.wpilibj.simulation.DriverStationSim.setEnabled(true);
    edu.wpi.first.wpilibj.simulation.DriverStationSim.setAutonomous(false);

    // Register full Xbox Controller axis count to prevent unplugged warnings
    edu.wpi.first.wpilibj.simulation.DriverStationSim.setJoystickAxisCount(0, 6);

    edu.wpi.first.wpilibj.simulation.DriverStationSim.notifyNewData();
  }

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {
    com.marslib.simulation.MARSPhysicsWorld.getInstance().update(ModeConstants.LOOP_PERIOD_SECS);
  }
}
