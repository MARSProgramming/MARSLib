package frc.robot.subsystems.shooter;

import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;

/** Real hardware implementation for Shooter using TalonFX. */
public class ShooterIOReal implements ShooterIO {
  private final TalonFX motor;
  private final VoltageOut voltageRequest = new VoltageOut(0);

  /**
   * Creates a new ShooterIOReal.
   *
   * @param canId the CAN bus ID of the TalonFX
   */
  public ShooterIOReal(int canId) {
    motor = new TalonFX(canId);

    var motorConfig = new TalonFXConfiguration();
    motorConfig.MotorOutput.NeutralMode = NeutralModeValue.Brake;

    motorConfig.CurrentLimits.StatorCurrentLimit = 40.0;
    motorConfig.CurrentLimits.StatorCurrentLimitEnable = true;
    motorConfig.CurrentLimits.SupplyCurrentLimit = 40.0;
    motorConfig.CurrentLimits.SupplyCurrentLimitEnable = true;

    motor.getConfigurator().apply(motorConfig);
  }

  @Override
  public void updateInputs(ShooterIOInputs inputs) {
    inputs.positionRotations = motor.getPosition().getValueAsDouble();
    inputs.velocityRotationsPerSec = motor.getVelocity().getValueAsDouble();
    inputs.appliedVolts = motor.getMotorVoltage().getValueAsDouble();
    inputs.currentAmps = new double[] {motor.getStatorCurrent().getValueAsDouble()};
    inputs.temperatureCelsius = new double[] {motor.getDeviceTemp().getValueAsDouble()};
  }

  @Override
  public void setVoltage(double volts) {
    motor.setControl(voltageRequest.withOutput(volts));
  }

  @Override
  public void stop() {
    motor.stopMotor();
  }
}
