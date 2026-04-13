/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.swerve;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.hardware.Pigeon2;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;

/**
 * Real hardware implementation of the gyro IO layer using a CTRE Pigeon2 IMU.
 *
 * <p>Students: The Pigeon2 is a 3-axis gyroscope connected over CAN bus. This class reads the yaw
 * (heading) at 250Hz for high-frequency odometry and the yaw velocity at 100Hz for general
 * telemetry.
 */
public class GyroIOPigeon2 implements GyroIO {
  private final Pigeon2 pigeon;
  private final StatusSignal<Angle> yaw;
  private final StatusSignal<Angle> pitch;
  private final StatusSignal<Angle> roll;
  private final StatusSignal<AngularVelocity> yawVelocity;
  private final StatusSignal<AngularVelocity> pitchVelocity;
  private final StatusSignal<AngularVelocity> rollVelocity;

  /**
   * Constructs a Pigeon2 gyro IO layer.
   *
   * @param canId The CAN ID of the Pigeon2.
   * @param canbus The CAN bus name (e.g. "rio" or "canivore").
   */
  public GyroIOPigeon2(int canId, String canbus) {
    pigeon = new Pigeon2(canId, canbus);

    yaw = pigeon.getYaw();
    pitch = pigeon.getPitch();
    roll = pigeon.getRoll();
    yawVelocity = pigeon.getAngularVelocityZWorld();
    pitchVelocity = pigeon.getAngularVelocityXWorld();
    rollVelocity = pigeon.getAngularVelocityYWorld();

    yawVelocity.setUpdateFrequency(100.0);
    pitchVelocity.setUpdateFrequency(100.0);
    rollVelocity.setUpdateFrequency(100.0);
    // yaw frequency is managed by the OdometryThread

    PhoenixOdometryThread.getInstance().registerGyro(yaw);

    pigeon.optimizeBusUtilization();
  }

  // Pre-allocated odometry yaw array — only reallocated when sample count changes
  private double[] cachedOdometryYaw = new double[0];

  @Override
  public void updateInputs(GyroIOInputs inputs) {
    BaseStatusSignal.refreshAll(yaw, pitch, roll, yawVelocity, pitchVelocity, rollVelocity);
    inputs.connected = yaw.getStatus().isOK();
    inputs.yawPositionRad = Units.degreesToRadians(yaw.getValueAsDouble());
    inputs.pitchPositionRad = Units.degreesToRadians(pitch.getValueAsDouble());
    inputs.rollPositionRad = Units.degreesToRadians(roll.getValueAsDouble());
    inputs.yawVelocityRadPerSec = Units.degreesToRadians(yawVelocity.getValueAsDouble());
    inputs.pitchVelocityRadPerSec = Units.degreesToRadians(pitchVelocity.getValueAsDouble());
    inputs.rollVelocityRadPerSec = Units.degreesToRadians(rollVelocity.getValueAsDouble());

    // Drain pre-allocated high-frequency yaw buffer
    PhoenixOdometryThread.GyroYawData yawData =
        PhoenixOdometryThread.getInstance().getGyroYawData();
    int count = yawData.validCount;

    // Only reallocate when sample count changes (typically stable at ~5 samples per drain)
    if (cachedOdometryYaw.length != count) {
      cachedOdometryYaw = new double[count];
    }

    for (int i = 0; i < count; i++) {
      cachedOdometryYaw[i] = Units.degreesToRadians(yawData.yawPositions[i]);
    }
    inputs.odometryYawPositions = cachedOdometryYaw;
  }
}
