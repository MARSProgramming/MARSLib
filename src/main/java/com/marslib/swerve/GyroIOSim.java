package com.marslib.swerve;

import static edu.wpi.first.units.Units.RadiansPerSecond;

import org.ironmaple.simulation.drivesims.GyroSimulation;

/** Simulated gyro IO layer that derives yaw from the maple-sim physics engine. */
public class GyroIOSim implements GyroIO {
  private GyroSimulation gyroSim;

  public void setGyroSimulation(GyroSimulation gyroSim) {
    this.gyroSim = gyroSim;
  }

  @Override
  public void updateInputs(GyroIOInputs inputs) {
    inputs.connected = true;

    if (gyroSim != null) {
      inputs.yawPositionRad = gyroSim.getGyroReading().getRadians();
      inputs.yawVelocityRadPerSec = gyroSim.getMeasuredAngularVelocity().in(RadiansPerSecond);

      // Use cached high-frequency values if you want, or just final
      inputs.odometryYawPositions = new double[] {inputs.yawPositionRad};
    } else {
      inputs.yawPositionRad = 0.0;
      inputs.yawVelocityRadPerSec = 0.0;
      inputs.odometryYawPositions = new double[] {0.0};
    }

    inputs.pitchVelocityRadPerSec = 0.0;
    inputs.rollVelocityRadPerSec = 0.0;
  }
}
