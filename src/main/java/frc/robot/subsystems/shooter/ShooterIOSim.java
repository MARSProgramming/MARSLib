package frc.robot.subsystems.shooter;

import edu.wpi.first.wpilibj.simulation.*;

/**
 * Simulation implementation for Shooter.
 *
 * <p>Uses WPILib FlywheelSim for physics modeling. Replace with dyn4j bodies for higher fidelity if
 * needed.
 */
public class ShooterIOSim implements ShooterIO {
  // TODO: Replace with actual FlywheelSim instance and configure parameters
  private double appliedVolts = 0.0;
  private double simPosition = 0.0;
  private double simVelocity = 0.0;

  public ShooterIOSim() {
    // TODO: Initialize FlywheelSim with physical parameters from Constants
  }

  @Override
  public void updateInputs(ShooterIOInputs inputs) {
    // TODO: Step the sim model forward by 0.020s
    inputs.positionRotations = simPosition;
    inputs.velocityRotationsPerSec = simVelocity;
    inputs.appliedVolts = appliedVolts;
    inputs.currentAmps = new double[] {0.0};
    inputs.temperatureCelsius = new double[] {25.0};
  }

  @Override
  public void setVoltage(double volts) {
    appliedVolts = volts;
    // TODO: Apply voltage to sim model
  }

  @Override
  public void stop() {
    appliedVolts = 0.0;
  }
}
