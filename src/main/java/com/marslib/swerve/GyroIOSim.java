/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.swerve;

import com.marslib.simulation.SwerveChassisPhysics;
import edu.wpi.first.math.geometry.Pose2d;

/** Simulated gyro IO layer that derives yaw from the native Dyn4j physics engine. */
public class GyroIOSim implements GyroIO {
  private SwerveChassisPhysics simChassis;

  public void setSwerveChassisPhysics(SwerveChassisPhysics simChassis) {
    this.simChassis = simChassis;
  }

  public boolean enableCanStarvation = false;
  public double canStarvationProbability = 0.02;

  private final double[] yawPositionsBuffer = new double[1];

  @Override
  public void updateInputs(GyroIOInputs inputs) {
    inputs.connected = true;

    if (enableCanStarvation) {
      if (Math.random() < canStarvationProbability) {
        inputs.connected = false;
      }
    }

    if (simChassis != null) {
      Pose2d simPose = simChassis.getPose();
      inputs.yawPositionRad = simPose.getRotation().getRadians();
      // Directly pull the angular velocity from the physics body
      inputs.yawVelocityRadPerSec = simChassis.getBody().getAngularVelocity();
    } else {
      inputs.yawPositionRad = 0.0;
      inputs.yawVelocityRadPerSec = 0.0;
    }

    // Reuse buffer to avoid double[] allocation
    yawPositionsBuffer[0] = inputs.yawPositionRad;
    inputs.odometryYawPositions = yawPositionsBuffer;

    if (simChassis != null) {
      double newPitch = simChassis.getSimPitch();
      double newRoll = simChassis.getSimRoll();

      // Calculate velocities via numeric derivative (assuming 20ms loop)
      inputs.pitchVelocityRadPerSec = (newPitch - inputs.pitchPositionRad) / 0.02;
      inputs.rollVelocityRadPerSec = (newRoll - inputs.rollPositionRad) / 0.02;

      inputs.pitchPositionRad = newPitch;
      inputs.rollPositionRad = newRoll;
    } else {
      inputs.pitchPositionRad = 0.0;
      inputs.rollPositionRad = 0.0;
      inputs.pitchVelocityRadPerSec = 0.0;
      inputs.rollVelocityRadPerSec = 0.0;
    }
  }
}
