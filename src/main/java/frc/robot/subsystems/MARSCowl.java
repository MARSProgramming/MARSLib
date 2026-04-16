package frc.robot.subsystems;

import static edu.wpi.first.units.Units.Volts;

import com.marslib.diagnostics.SystemTestable;
import com.marslib.mechanisms.*;
import com.marslib.power.MARSPowerManager;
import com.marslib.util.LoggedTunableNumber;
import com.marslib.util.OnlineFeedforwardEstimator;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import frc.robot.constants.ModeConstants;
import org.littletonrobotics.junction.Logger;

/**
 * High-level subsystem representing the shooter cowl rotating joint.
 *
 * <p>Handles physics simulation, angular position tracking, and dynamic load shedding to prevent
 * battery brownouts using MARSPowerManager data.
 */
public class MARSCowl extends SubsystemBase implements SystemTestable {

  private final RotaryMechanismIO io;
  private final RotaryMechanismIOInputsAutoLogged inputs = new RotaryMechanismIOInputsAutoLogged();

  private final LoggedTunableNumber kS = new LoggedTunableNumber("Cowl/kS", 0.0);
  private final LoggedTunableNumber kG = new LoggedTunableNumber("Cowl/kG", 0.0);
  private final LoggedTunableNumber kV = new LoggedTunableNumber("Cowl/kV", 0.0);
  private final LoggedTunableNumber kA = new LoggedTunableNumber("Cowl/kA", 0.0);

  private static final double NOMINAL_VOLTAGE = 12.0;
  private static final double CRITICAL_VOLTAGE = 9.0;

  private final MARSPowerManager powerManager;
  private final SysIdRoutine sysIdRoutine;
  private final OnlineFeedforwardEstimator estimator;
  private double lastVelocityForSysId = 0.0;
  private double lastTargetVel = 0.0;

  public MARSCowl(RotaryMechanismIO io, MARSPowerManager powerManager) {
    this.io = io;
    this.powerManager = powerManager;

    this.sysIdRoutine =
        new SysIdRoutine(
            new SysIdRoutine.Config(
                null,
                null,
                null,
                (state) -> Logger.recordOutput("SysIdTestState", state.toString())),
            new SysIdRoutine.Mechanism(
                (edu.wpi.first.units.measure.Voltage volts) -> {
                  io.setVoltage(volts.in(Volts));
                },
                null,
                this));

    this.estimator = new OnlineFeedforwardEstimator("Cowl", 500, 0.0);
  }

  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("Cowl", inputs);

    // Continuous TeleOp SysId Extraction
    double currentVelocity = inputs.velocityRadPerSec;
    double currentAccel = (currentVelocity - lastVelocityForSysId) / ModeConstants.LOOP_PERIOD_SECS;
    lastVelocityForSysId = currentVelocity;
    estimator.addMeasurement(inputs.appliedVolts, currentVelocity, currentAccel);
  }

  private double targetPositionRads = 0.0;

  /**
   * Commands the cowl to a target angular position using Motion Magic with dynamic feedforward.
   *
   * <p>The feedforward voltage is computed using the current cowl angle (for gravity compensation)
   * and the instantaneous Motion Magic profile velocity (for kV contribution).
   *
   * @param positionRads Target cowl angle in radians.
   */
  public void setTargetPosition(double positionRads) {
    this.targetPositionRads = positionRads;

    // Dynamic FF using actual physical angle and instantaneous profile target velocity from CTRE
    // Motion Magic
    double currentAngleRads = inputs.positionRad;
    double scale = powerManager.calculateVoltageScaleFactor(NOMINAL_VOLTAGE, CRITICAL_VOLTAGE);

    double targetVel = inputs.targetVelocityRadPerSec;
    double targetAccel = (targetVel - lastTargetVel) / ModeConstants.LOOP_PERIOD_SECS;
    lastTargetVel = targetVel;

    double ffVolts =
        (kS.get() * Math.signum(targetVel)
                + kG.get() * Math.cos(currentAngleRads)
                + kV.get() * targetVel
                + kA.get() * targetAccel)
            * scale;

    io.setClosedLoopPosition(positionRads, ffVolts);
    Logger.recordOutput("Cowl/TargetPositionRads", positionRads);
  }

  public boolean isAtTolerance() {
    return Math.abs(inputs.positionRad - targetPositionRads)
        < edu.wpi.first.math.util.Units.degreesToRadians(3.0);
  }

  /**
   * Returns the current measured cowl angle.
   *
   * @return Current cowl position in radians.
   */
  public double getPositionRads() {
    return inputs.positionRad;
  }

  /**
   * Generates a SysId Quasistatic characterization command.
   *
   * @param direction The direction of the quasistatic routine (Forward/Reverse).
   * @return The SysId Command to execute.
   */
  public Command sysIdQuasistatic(SysIdRoutine.Direction direction) {
    return sysIdRoutine.quasistatic(direction);
  }

  /**
   * Generates a SysId Dynamic characterization command.
   *
   * @param direction The direction of the dynamic routine (Forward/Reverse).
   * @return The SysId Command to execute.
   */
  public Command sysIdDynamic(SysIdRoutine.Direction direction) {
    return sysIdRoutine.dynamic(direction);
  }

  /**
   * Homes the cowl by driving at a constant voltage and checking the stator current. It resets the
   * encoder to 0.0 radians upon reaching the hard stop.
   *
   * @param homingVoltage Voltage to drive the motor (negative for downwards).
   * @param currentThresholdAmps Stator current threshold (i.e. 30.0).
   * @return The home command.
   */
  public Command homeWithCurrent(double homingVoltage, double currentThresholdAmps) {
    return run(() -> io.setVoltage(homingVoltage))
        .until(
            () -> {
              // Check if any motor exceeds the current threshold
              for (double current : inputs.currentAmps) {
                if (current > currentThresholdAmps) return true;
              }
              return false;
            })
        .andThen(
            () -> {
              io.setVoltage(0.0);
              io.setEncoderPosition(0.0);
              Logger.recordOutput("MARSCowl/Status", "Homing Complete!");
            });
  }

  /**
   * Homes the cowl using a standard default threshold (-2.0V, 15.0A).
   *
   * @return The home command.
   */
  public Command home() {
    return homeWithCurrent(-2.0, 15.0);
  }

  @Override
  public Command getSystemCheckCommand() {
    return edu.wpi.first.wpilibj2.command.Commands.defer(
            () -> {
              double initial = getPositionRads();
              return edu.wpi.first.wpilibj2.command.Commands.sequence(
                  edu.wpi.first.wpilibj2.command.Commands.runOnce(
                      () -> setTargetPosition(initial + 0.15)),
                  edu.wpi.first.wpilibj2.command.Commands.waitSeconds(1.5),
                  edu.wpi.first.wpilibj2.command.Commands.runOnce(
                      () -> {
                        if (Math.abs(getPositionRads() - (initial + 0.15)) > 0.08) {
                          new com.marslib.faults.Alert(
                                  "SystemCheck: Cowl did not rotate. Check CAN/gearbox.",
                                  com.marslib.faults.Alert.AlertType.CRITICAL)
                              .set(true);
                        }
                        setTargetPosition(initial);
                      }),
                  edu.wpi.first.wpilibj2.command.Commands.waitSeconds(1.0),
                  edu.wpi.first.wpilibj2.command.Commands.runOnce(
                      () -> {
                        if (Math.abs(getPositionRads() - initial) > 0.08) {
                          new com.marslib.faults.Alert(
                                  "SystemCheck: Cowl did not return to start.",
                                  com.marslib.faults.Alert.AlertType.CRITICAL)
                              .set(true);
                        }
                      }));
            },
            java.util.Set.of(this))
        .withName("CowlSystemTest");
  }
}
