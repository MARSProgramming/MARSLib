package frc.robot.subsystems;

import static edu.wpi.first.units.Units.Volts;

import com.marslib.diagnostics.SystemTestable;
import com.marslib.mechanisms.FlywheelIO;
import com.marslib.mechanisms.FlywheelIOInputsAutoLogged;
import com.marslib.mechanisms.FlywheelIOSim;
import com.marslib.mechanisms.FlywheelIOTalonFX;
import com.marslib.power.MARSPowerManager;
import com.marslib.util.LoggedTunableNumber;
import com.marslib.util.OnlineFeedforwardEstimator;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import frc.robot.constants.ModeConstants;
import org.littletonrobotics.junction.Logger;

/**
 * Generic flywheel subsystem used for the main shooter, floor intake, and feeder.
 *
 * <p>Despite its name, this class is a general-purpose velocity-controlled flywheel wrapper. In
 * {@code RobotContainer}, three separate instances are created:
 *
 * <ul>
 *   <li>{@code shooter} — the main scoring flywheel (4-motor, high-velocity)
 *   <li>{@code floorIntake} — ground pickup rollers
 *   <li>{@code feeder} — internal transfer mechanism
 * </ul>
 *
 * <p>Each instance accepts a {@link FlywheelIO} implementation via dependency injection, allowing
 * seamless switching between real hardware ({@link FlywheelIOTalonFX}) and physics simulation
 * ({@link FlywheelIOSim}).
 */
public class MARSShooter extends SubsystemBase implements SystemTestable {
  private final FlywheelIO io;
  private final FlywheelIOInputsAutoLogged inputs = new FlywheelIOInputsAutoLogged();

  private final String name;
  private final LoggedTunableNumber kS;
  private final LoggedTunableNumber kV;
  private final LoggedTunableNumber kA;

  private SimpleMotorFeedforward feedforward;
  private final SysIdRoutine sysIdRoutine;

  private static final double NOMINAL_VOLTAGE = 12.0;
  private static final double CRITICAL_VOLTAGE = 9.0;

  private final MARSPowerManager powerManager;
  private final OnlineFeedforwardEstimator estimator;
  private double lastVelocityForSysId = 0.0;

  /**
   * Constructs the shooter subsystem.
   *
   * @param name The unique identifier for this flywheel instance (e.g., Shooter, FloorIntake,
   *     Feeder)
   * @param io The hardware abstraction layer for the shooter flywheel motor.
   * @param powerManager The active power manager for load-shedding voltage queries.
   */
  public MARSShooter(String name, FlywheelIO io, MARSPowerManager powerManager) {
    this.name = name;
    this.kS = new LoggedTunableNumber(name + "/kS", 0.0);
    this.kV = new LoggedTunableNumber(name + "/kV", 0.0);
    this.kA = new LoggedTunableNumber(name + "/kA", 0.0);
    this.io = io;
    this.powerManager = powerManager;
    feedforward = new SimpleMotorFeedforward(kS.get(), kV.get(), kA.get());

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

    this.estimator = new OnlineFeedforwardEstimator(name, 500, 0.0);
  }

  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs(name, inputs);

    int id = this.hashCode();
    boolean sChanged = kS.hasChanged(id);
    boolean vChanged = kV.hasChanged(id);
    boolean aChanged = kA.hasChanged(id);

    if (sChanged || vChanged || aChanged) {
      feedforward = new SimpleMotorFeedforward(kS.get(), kV.get(), kA.get());
    }

    // Continuous TeleOp SysId Extraction
    double currentVelocity = inputs.velocityRadPerSec;
    double currentAccel = (currentVelocity - lastVelocityForSysId) / ModeConstants.LOOP_PERIOD_SECS;
    lastVelocityForSysId = currentVelocity;
    estimator.addMeasurement(inputs.appliedVolts, currentVelocity, currentAccel);
  }

  /**
   * Returns a command that continuously spins the flywheel at the default scoring velocity.
   *
   * @return A {@link Command} that runs the flywheel at ~400 rad/s using closed-loop control.
   */
  public Command spinUpCommand() {
    return this.run(() -> setClosedLoopVelocity(400.0));
  }

  /**
   * Directly sets the shooter motor voltage. Used for open-loop manual control.
   *
   * @param volts The voltage to apply.
   */
  public void setVoltage(double volts) {
    io.setVoltage(volts);
  }

  private double targetVelocityRadPerSec = 0.0;

  /**
   * Runs the flywheel at a target velocity using the motor's internal closed-loop controller and
   * dynamically injects the calculated feedforward voltage.
   *
   * @param speed Target velocity in radians per second.
   */
  public void setClosedLoopVelocity(double speed) {
    this.targetVelocityRadPerSec = speed;
    double scale = powerManager.calculateVoltageScaleFactor(NOMINAL_VOLTAGE, CRITICAL_VOLTAGE);
    double ffVolts = feedforward.calculate(speed) * scale;
    io.setClosedLoopVelocity(speed, ffVolts);
  }

  /**
   * Returns whether the flywheel velocity is within 20 rad/s of the target.
   *
   * @return {@code true} if the flywheel is at the commanded velocity within tolerance.
   */
  public boolean isAtTolerance() {
    return Math.abs(inputs.velocityRadPerSec - targetVelocityRadPerSec) < 20.0;
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
   * Gets the current velocity of the shooter flywheel.
   *
   * @return Velocity in radians per second.
   */
  public double getVelocityRadPerSec() {
    return inputs.velocityRadPerSec;
  }

  @Override
  public Command getSystemCheckCommand() {
    return edu.wpi.first.wpilibj2.command.Commands.sequence(
            edu.wpi.first.wpilibj2.command.Commands.runOnce(() -> setVoltage(6.0)),
            edu.wpi.first.wpilibj2.command.Commands.waitSeconds(0.75),
            edu.wpi.first.wpilibj2.command.Commands.runOnce(
                () -> {
                  double vel = getVelocityRadPerSec();
                  setVoltage(0.0);
                  if (Math.abs(vel) < 5.0) {
                    new com.marslib.faults.Alert(
                            "SystemCheck: Flywheel not spinning. Check wiring.",
                            com.marslib.faults.Alert.AlertType.CRITICAL)
                        .set(true);
                  }
                }))
        .withName("FlywheelSystemTest");
  }
}
