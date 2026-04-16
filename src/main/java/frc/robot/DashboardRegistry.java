package frc.robot;

import com.marslib.diagnostics.SystemCheckCommand;
import com.marslib.power.MARSPowerManager;
import com.marslib.swerve.GyroIOInputsAutoLogged;
import com.marslib.swerve.SwerveDrive;
import com.marslib.util.LogUploader;
import com.marslib.util.LoggedTunableNumber;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.MARSClimber;
import frc.robot.subsystems.MARSCowl;
import frc.robot.subsystems.MARSIntakePivot;
import frc.robot.subsystems.MARSShooter;
import frc.robot.subsystems.MARSSuperstructure;
import org.littletonrobotics.junction.networktables.LoggedDashboardChooser;

/**
 * Registry class that handles extracting dashboard initialization logic out of RobotContainer.
 * Scaffolds Competition, Practice, and Tuning layouts efficiently.
 */
public final class DashboardRegistry {

  private DashboardRegistry() {}

  public static void configure(
      boolean buildDashboards,
      LoggedDashboardChooser<Command> autoChooser,
      SwerveDrive swerveDrive,
      MARSSuperstructure superstructure,
      MARSPowerManager powerManager,
      MARSClimber climber,
      MARSCowl cowl,
      MARSShooter shooter,
      MARSIntakePivot intakePivot) {

    // Expose utility commands directly on SmartDashboard for generic access
    SmartDashboard.putData("Dump Tunables", LoggedTunableNumber.getDumpCommand());
    SmartDashboard.putData(
        "Offload Logs to USB", LoggedTunableNumber.getDumpCommand()); // wait, it was LogUploader
    SmartDashboard.putData("Offload Logs to USB", LogUploader.getUsbOffloadCommand());

    if (buildDashboards) {
      configureCompetitionDashboard(autoChooser, swerveDrive, superstructure);
      configurePracticeDashboard(
          autoChooser, powerManager, climber, cowl, shooter, intakePivot, swerveDrive);
      LoggedTunableNumber.buildTuningDashboard();
    }
  }

  /**
   * Scaffolds an explicitly lightweight WPILib Native Dashboard (Shuffleboard/Glass) specifically
   * designed for FMS-tethered matches where 3D 60FPS renders drop DriveStation CPU bandwidth.
   */
  private static void configureCompetitionDashboard(
      LoggedDashboardChooser<Command> autoChooser,
      SwerveDrive swerveDrive,
      MARSSuperstructure superstructure) {
    ShuffleboardTab matchTab = Shuffleboard.getTab("Match");

    matchTab
        .add("Auto Routine", autoChooser.getSendableChooser())
        .withWidget(BuiltInWidgets.kComboBoxChooser)
        .withSize(3, 1)
        .withPosition(0, 0);

    matchTab
        .addString(
            "Match Time",
            () -> {
              int remaining = (int) Timer.getMatchTime();
              return (remaining < 0 || !DriverStation.isFMSAttached()) ? "N/A" : remaining + " s";
            })
        .withSize(2, 2)
        .withPosition(0, 1);

    matchTab
        .addString(
            "FMS Alliance",
            () ->
                DriverStation.getAlliance()
                    .map(DriverStation.Alliance::toString)
                    .orElse("UNCALIBRATED"))
        .withSize(2, 1)
        .withPosition(3, 0);

    matchTab
        .addBoolean(
            "Gyro Connected",
            () -> {
              GyroIOInputsAutoLogged gyro = swerveDrive.getGyroInputs();
              return gyro.connected;
            })
        .withWidget(BuiltInWidgets.kBooleanBox)
        .withSize(1, 1)
        .withPosition(3, 1);

    matchTab
        .addString(
            "Superstructure State",
            () -> superstructure != null ? superstructure.getCurrentState().toString() : "BOOTING")
        .withSize(3, 1)
        .withPosition(5, 0);

    matchTab
        .add("Emergency USB Offload", LogUploader.getUsbOffloadCommand())
        .withSize(2, 1)
        .withPosition(5, 1);
  }

  /**
   * Extends the UI for practice matches and un-tethered development where manual sequence
   * triggering, module zeroing, and detailed odometry overrides sit alongside the generic Match
   * widgets.
   */
  private static void configurePracticeDashboard(
      LoggedDashboardChooser<Command> autoChooser,
      MARSPowerManager powerManager,
      MARSClimber climber,
      MARSCowl cowl,
      MARSShooter shooter,
      MARSIntakePivot intakePivot,
      SwerveDrive swerveDrive) {
    ShuffleboardTab practiceTab = Shuffleboard.getTab("Practice");

    practiceTab
        .add("Auto Routine Override", autoChooser.getSendableChooser())
        .withWidget(BuiltInWidgets.kComboBoxChooser)
        .withSize(3, 1)
        .withPosition(0, 0);

    practiceTab
        .add(
            "Full System Check",
            new SystemCheckCommand(
                powerManager::getVoltage, climber, cowl, shooter, intakePivot, swerveDrive))
        .withPosition(3, 0)
        .withSize(2, 1);

    practiceTab
        .addString(
            "FMS Alliance",
            () ->
                DriverStation.getAlliance()
                    .map(DriverStation.Alliance::toString)
                    .orElse("UNCALIBRATED"))
        .withSize(2, 1)
        .withPosition(5, 0);

    practiceTab
        .addBoolean(
            "Swerve Odometry Synchronized",
            () -> {
              GyroIOInputsAutoLogged gyro = swerveDrive.getGyroInputs();
              return gyro.connected;
            })
        .withSize(2, 1)
        .withPosition(3, 1);
  }
}
