package frc.robot.subsystems.shooter;

import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.wpilibj.simulation.FlywheelSim;
import frc.robot.constants.ModeConstants;

/**
 * Simulation implementation for Shooter.
 *
 * <p>Uses WPILib FlywheelSim for physics modeling. Replace with dyn4j bodies for higher fidelity if
 * needed.
 */
public class ShooterIOSim implements ShooterIO {
  private final FlywheelSim flywheelSim;
  private double appliedVolts = 0.0;
  private double simPosition = 0.0;
  private double simVelocity = 0.0;

  public ShooterIOSim() {
    // Initialize FlywheelSim with Krakan X60, 1:1 gear ratio, and 0.05 kg*m^2 MOI
    flywheelSim =
        new FlywheelSim(
            LinearSystemId.createFlywheelSystem(DCMotor.getKrakenX60Foc(1), 0.05, 1.0),
            DCMotor.getKrakenX60Foc(1),
            1.0);
  }

  @Override
  public void updateInputs(ShooterIOInputs inputs) {
    flywheelSim.update(ModeConstants.LOOP_PERIOD_SECS);
    simVelocity = flywheelSim.getAngularVelocityRPM() / 60.0;
    simPosition += simVelocity * ModeConstants.LOOP_PERIOD_SECS;

    inputs.positionRotations = simPosition;
    inputs.velocityRotationsPerSec = simVelocity;
    inputs.appliedVolts = appliedVolts;
    inputs.currentAmps = new double[] {flywheelSim.getCurrentDrawAmps()};
    inputs.temperatureCelsius = new double[] {25.0};
  }

  @Override
  public void setVoltage(double volts) {
    appliedVolts = volts;
    flywheelSim.setInputVoltage(volts);
  }

  @Override
  public void stop() {
    appliedVolts = 0.0;
    flywheelSim.setInputVoltage(0.0);
  }
}
