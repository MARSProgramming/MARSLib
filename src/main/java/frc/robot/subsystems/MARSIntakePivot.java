package frc.robot.subsystems;

import static edu.wpi.first.units.Units.Volts;

import com.marslib.diagnostics.SystemTestable;
import com.marslib.mechanisms.*;
import com.marslib.power.MARSPowerManager;
import com.marslib.util.LoggedTunableNumber;
import com.marslib.util.OnlineFeedforwardEstimator;
import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import frc.robot.constants.ModeConstants;
import org.littletonrobotics.junction.Logger;

/**
 * High-level subsystem representing the intake deployment pivot.
 *
 * <p>Handles physics simulation, angular position tracking, and dynamic load shedding to prevent
 * battery brownouts using MARSPowerManager data.
 */
public class MARSIntakePivot extends SubsystemBase implements SystemTestable {

  private final RotaryMechanismIO io;
  private final RotaryMechanismIOInputsAutoLogged inputs = new RotaryMechanismIOInputsAutoLogged();

  private final LoggedTunableNumber kS = new LoggedTunableNumber("IntakePivot/kS", 0.0);
  private final LoggedTunableNumber kG = new LoggedTunableNumber("IntakePivot/kG", 0.0);
  private final LoggedTunableNumber kV = new LoggedTunableNumber("IntakePivot/kV", 0.0);
  private final LoggedTunableNumber kA = new LoggedTunableNumber("IntakePivot/kA", 0.0);

  private ArmFeedforward feedforward;

  private static final double NOMINAL_VOLTAGE = 12.0;
  private static final double CRITICAL_VOLTAGE = 9.0;

  private final MARSPowerManager powerManager;
  private final SysIdRoutine sysIdRoutine;
  private final OnlineFeedforwardEstimator estimator;
  private double lastVelocityForSysId = 0.0;

  public MARSIntakePivot(RotaryMechanismIO io, MARSPowerManager powerManager) {
    this.io = io;
    this.powerManager = powerManager;
    feedforward = new ArmFeedforward(kS.get(), kG.get(), kV.get(), kA.get());

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

    this.estimator = new OnlineFeedforwardEstimator("IntakePivot", 500, 0.0);
  }

  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("IntakePivot", inputs);

    // Update Feedforward if TUNING mode constants are changed
    int id = this.hashCode();
    boolean sChanged = kS.hasChanged(id);
    boolean gChanged = kG.hasChanged(id);
    boolean vChanged = kV.hasChanged(id);
    boolean aChanged = kA.hasChanged(id);

    if (sChanged || gChanged || vChanged || aChanged) {
      feedforward = new ArmFeedforward(kS.get(), kG.get(), kV.get(), kA.get());
    }

    // Continuous TeleOp SysId Extraction
    double currentVelocity = inputs.velocityRadPerSec;
    double currentAccel = (currentVelocity - lastVelocityForSysId) / ModeConstants.LOOP_PERIOD_SECS;
    lastVelocityForSysId = currentVelocity;
    estimator.addMeasurement(inputs.appliedVolts, currentVelocity, currentAccel);
  }

  private double targetPositionRads = 0.0;

  /**
   * Commands the intake pivot to a target angular position using Motion Magic with dynamic
   * feedforward.
   *
   * @param positionRads Target angle in radians.
   */
  public void setTargetPosition(double positionRads) {
    this.targetPositionRads = positionRads;
    double currentAngleRads = inputs.positionRad;
    double scale = powerManager.calculateVoltageScaleFactor(NOMINAL_VOLTAGE, CRITICAL_VOLTAGE);
    double ffVolts =
        feedforward.calculate(currentAngleRads, inputs.targetVelocityRadPerSec) * scale;
    io.setClosedLoopPosition(positionRads, ffVolts);
    Logger.recordOutput("IntakePivot/TargetPositionRads", positionRads);
  }

  public boolean isAtTolerance() {
    return Math.abs(inputs.positionRad - targetPositionRads)
        < edu.wpi.first.math.util.Units.degreesToRadians(3.0);
  }

  public double getPositionRads() {
    return inputs.positionRad;
  }

  public Command sysIdQuasistatic(SysIdRoutine.Direction direction) {
    return sysIdRoutine.quasistatic(direction);
  }

  public Command sysIdDynamic(SysIdRoutine.Direction direction) {
    return sysIdRoutine.dynamic(direction);
  }

  public Command homeWithCurrent(double homingVoltage, double currentThresholdAmps) {
    return run(() -> io.setVoltage(homingVoltage))
        .until(
            () -> {
              for (double current : inputs.currentAmps) {
                if (current > currentThresholdAmps) return true;
              }
              return false;
            })
        .andThen(
            () -> {
              io.setVoltage(0.0);
              io.setEncoderPosition(0.0);
              Logger.recordOutput("MARSIntakePivot/Status", "Homing Complete!");
            });
  }

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
                                  "SystemCheck: IntakePivot did not rotate. Check CAN/gearbox.",
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
                                  "SystemCheck: IntakePivot did not return to start.",
                                  com.marslib.faults.Alert.AlertType.CRITICAL)
                              .set(true);
                        }
                      }));
            },
            java.util.Set.of(this))
        .withName("IntakePivotSystemTest");
  }
}
