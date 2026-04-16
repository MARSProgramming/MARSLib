package frc.robot.subsystems.shooter;

import edu.wpi.first.units.Units;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import org.littletonrobotics.junction.Logger;

/**
 * Shooter subsystem with AdvantageKit IO abstraction.
 *
 * <p>Supports hardware, simulation, and replay modes via the IO interface pattern.
 */
public class Shooter extends SubsystemBase {
  private final ShooterIO io;
  private final ShooterIOInputsAutoLogged inputs = new ShooterIOInputsAutoLogged();
  private final SysIdRoutine sysIdRoutine;

  /**
   * Creates a new Shooter subsystem.
   *
   * @param io the hardware IO implementation (real, sim, or replay)
   */
  public Shooter(ShooterIO io) {
    this.io = io;
    sysIdRoutine =
        new SysIdRoutine(
            new SysIdRoutine.Config(),
            new SysIdRoutine.Mechanism(
                (voltage) -> io.setVoltage(voltage.in(Units.Volts)),
                null, // Use AdvantageKit logging instead of WPILib default log
                this));
  }

  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("Shooter", inputs);
  }

  /** Run open loop at the specified voltage. */
  public void setVoltage(double volts) {
    io.setVoltage(volts);
  }

  /** Stop the mechanism immediately. */
  public void stop() {
    io.stop();
  }

  /** Returns the current position in rotations. */
  public double getPosition() {
    return inputs.positionRotations;
  }

  /** Returns the current velocity in rot/s. */
  public double getVelocity() {
    return inputs.velocityRotationsPerSec;
  }

  /**
   * Returns a command that runs a SysId quasistatic characterization routine.
   *
   * @param direction the direction to run the routine
   * @return the characterization command
   */
  public Command sysIdQuasistatic(SysIdRoutine.Direction direction) {
    return sysIdRoutine.quasistatic(direction);
  }

  /**
   * Returns a command that runs a SysId dynamic characterization routine.
   *
   * @param direction the direction to run the routine
   * @return the characterization command
   */
  public Command sysIdDynamic(SysIdRoutine.Direction direction) {
    return sysIdRoutine.dynamic(direction);
  }
}
