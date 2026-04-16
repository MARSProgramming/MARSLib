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
import com.marslib.util.CANUtil;
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
  private final PhoenixOdometryThread odometryThread;
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
  public GyroIOPigeon2(int canId, String canbus, SwerveConfig config) {
    pigeon = new Pigeon2(canId, new com.ctre.phoenix6.CANBus(canbus));

    yaw = pigeon.getYaw();
    pitch = pigeon.getPitch();
    roll = pigeon.getRoll();
    yawVelocity = pigeon.getAngularVelocityZWorld();
    pitchVelocity = pigeon.getAngularVelocityXWorld();
    rollVelocity = pigeon.getAngularVelocityYWorld();

    CANUtil.setUpdateFrequencyWithRetry(
        config.telemetryHz(), yawVelocity, pitchVelocity, rollVelocity);
    // yaw frequency is managed by the OdometryThread

    odometryThread = PhoenixOdometryThread.getInstance();
    odometryThread.registerGyro(yaw, config.odometryHz());

    pigeon.optimizeBusUtilization();
  }

  // Pre-allocated odometry yaw array — only reallocated when sample count changes
  private double[] cachedOdometryYaw = new double[0];

  @Override
  public void updateInputs(GyroIOInputs inputs) {
    BaseStatusSignal.refreshAll(yaw, pitch, roll, yawVelocity, pitchVelocity, rollVelocity);
    inputs.connected = yaw.getStatus().isOK();

    double rawYaw = yaw.getValueAsDouble();
    double rawPitch = pitch.getValueAsDouble();
    double rawRoll = roll.getValueAsDouble();
    double rawYawVel = yawVelocity.getValueAsDouble();
    double rawPitchVel = pitchVelocity.getValueAsDouble();
    double rawRollVel = rollVelocity.getValueAsDouble();

    inputs.yawPositionRad = Double.isFinite(rawYaw) ? Units.degreesToRadians(rawYaw) : 0.0;
    inputs.pitchPositionRad = Double.isFinite(rawPitch) ? Units.degreesToRadians(rawPitch) : 0.0;
    inputs.rollPositionRad = Double.isFinite(rawRoll) ? Units.degreesToRadians(rawRoll) : 0.0;
    inputs.yawVelocityRadPerSec =
        Double.isFinite(rawYawVel) ? Units.degreesToRadians(rawYawVel) : 0.0;
    inputs.pitchVelocityRadPerSec =
        Double.isFinite(rawPitchVel) ? Units.degreesToRadians(rawPitchVel) : 0.0;
    inputs.rollVelocityRadPerSec =
        Double.isFinite(rawRollVel) ? Units.degreesToRadians(rawRollVel) : 0.0;

    // Drain pre-allocated high-frequency yaw buffer
    PhoenixOdometryThread.GyroYawData yawData = odometryThread.getGyroYawData();
    int count = yawData.validCount;

    // Only reallocate when sample count changes (typically stable at ~5 samples per drain)
    if (cachedOdometryYaw.length != count) {
      cachedOdometryYaw = new double[count];
    }

    for (int i = 0; i < count; i++) {
      double rawOdomYaw = yawData.yawPositions[i];
      cachedOdometryYaw[i] = Double.isFinite(rawOdomYaw) ? Units.degreesToRadians(rawOdomYaw) : 0.0;
    }
    inputs.odometryYawPositions = cachedOdometryYaw;
  }
}
