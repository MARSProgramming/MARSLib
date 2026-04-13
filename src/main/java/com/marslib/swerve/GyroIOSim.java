/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.swerve;

import static edu.wpi.first.units.Units.RadiansPerSecond;

import org.ironmaple.simulation.drivesims.GyroSimulation;
import org.ironmaple.simulation.drivesims.SwerveDriveSimulation;

/** Simulated gyro IO layer that derives yaw from the maple-sim physics engine. */
public class GyroIOSim implements GyroIO {
  private GyroSimulation gyroSim;

  private SwerveDriveSimulation simDrive;

  public void setGyroSimulation(GyroSimulation gyroSim) {
    this.gyroSim = gyroSim;
  }

  public void setSwerveDriveSimulation(SwerveDriveSimulation simDrive) {
    this.simDrive = simDrive;
  }

  public boolean enableCanStarvation = false;
  public double canStarvationProbability = 0.02;

  @Override
  public void updateInputs(GyroIOInputs inputs) {
    inputs.connected = true;

    if (enableCanStarvation) {
      if (Math.random() < canStarvationProbability) {
        inputs.connected = false;
      }
    }

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

    if (simDrive != null) {
      edu.wpi.first.math.geometry.Pose2d simPose = simDrive.getSimulatedDriveTrainPose();
      double[] tilt =
          org.ironmaple.simulation.seasonspecific.rebuilt2026.Arena2026Rebuilt
              .getSimulatedBumpTilt_rads(simPose);
      inputs.pitchPositionRad = tilt[0];
      inputs.rollPositionRad = tilt[1];
    } else {
      inputs.pitchPositionRad = 0.0;
      inputs.rollPositionRad = 0.0;
    }

    inputs.pitchVelocityRadPerSec = 0.0;
    inputs.rollVelocityRadPerSec = 0.0;
  }
}
