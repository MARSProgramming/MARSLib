/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.testing;

import com.marslib.faults.Alert;
import com.marslib.faults.MARSFaultManager;
import com.marslib.simulation.MARSPhysicsWorld;
import com.marslib.swerve.SwerveConfig;
import com.marslib.vision.AprilTagVisionIOSim;
import com.marslib.vision.VisionConfig;
import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

/** Centralized test setup utility that resets ALL MARSLib static singletons in one call. */
public final class MARSTestHarness {

  private MARSTestHarness() {}

  public static void reset() {
    HAL.initialize(500, 0);
    CommandScheduler.getInstance().cancelAll();
    CommandScheduler.getInstance().unregisterAllSubsystems();
    MARSPhysicsWorld.resetInstance();
    AprilTagVisionIOSim.resetSimulation();
    MARSFaultManager.clear();
    Alert.resetAll();
    DriverStationSim.setAllianceStationId(edu.wpi.first.hal.AllianceStationID.Blue1);
    DriverStationSim.setEnabled(true);
    DriverStationSim.notifyNewData();
    com.marslib.swerve.PhoenixOdometryThread.resetInstance();
    com.marslib.util.LoggedTunableNumber.clear();
  }

  public static void cleanup() {
    CommandScheduler.getInstance().cancelAll();
    CommandScheduler.getInstance().unregisterAllSubsystems();
  }

  /** Returns a standard SwerveConfig for testing. */
  public static SwerveConfig createSwerveConfig() {
    return new SwerveConfig(
        new edu.wpi.first.math.geometry.Translation2d[] {
          new edu.wpi.first.math.geometry.Translation2d(0.3, 0.3),
          new edu.wpi.first.math.geometry.Translation2d(0.3, -0.3),
          new edu.wpi.first.math.geometry.Translation2d(-0.3, 0.3),
          new edu.wpi.first.math.geometry.Translation2d(-0.3, -0.3)
        },
        4.5, // maxLinearSpeedMps
        10.0, // maxAngularSpeedRadPerSec
        0.05, // wheelRadiusMeters
        5.0, // turnKp
        0.5, // turnKd
        0.1, // driveKp
        0.0, // driveKd
        0.1, // driveKs
        0.12, // driveKv
        0.0, // driveKa
        10.0, // nominalVoltage
        8.0, // warningVoltage
        7.0, // criticalVoltage
        6.75, // driveGearRatio
        60.0, // driveStatorCurrentLimit
        50.0, // robotMassKg
        5.0, // robotMoiKgM2
        0.8, // bumperLengthMeters
        0.8, // bumperWidthMeters
        0.6, // wheelbaseMeters
        0.6, // trackWidthMeters
        1.2, // wheelCOFStatic
        0.02, // loopPeriodSecs
        15.0, // teleopLinearAccelLimit
        18.84, // teleopOmegaAccelLimit
        5.0, // headingKp
        0.0, // headingKd
        5.0, // autoTranslationKp
        0.0, // autoTranslationKd
        5.0, // autoRotationKp
        0.0, // autoRotationKd
        5.0, // alignTranslationKp
        0.1, // alignTranslationIZoneMeters
        5.0, // alignThetaKp
        0.1, // alignThetaIZoneRad
        50.0, // telemetryHz
        250.0, // odometryHz
        150.0 / 7.0, // turnGearRatio
        40.0, // turnStatorCurrentLimit
        50.0 / 14.0 // couplingRatio
        );
  }

  /** Returns a standard PowerConfig for testing. */
  public static com.marslib.power.PowerConfig createPowerConfig() {
    return new com.marslib.power.PowerConfig(10.0, 8.0, 7.0);
  }

  /** Returns a standard VisionConfig for testing. */
  public static VisionConfig createVisionConfig() {
    return new VisionConfig(
        () -> 0.5, // maxZHeight
        16.541, // fieldLength
        8.211, // fieldWidth
        () -> 0.5, // fieldMargin
        () -> 25.0, // maxTilt
        () -> 1000.0, // maxAngularAccel
        () -> 0.2, // maxAmbiguity
        () -> 0.01, // tagStdBase
        () -> 0.1, // multiTagStdMultiplier
        () -> 2.0, // angularStdMultiplier
        () -> 0.5, // linearVelocityStdMultiplier
        () -> 0.5, // angularVelocityStdMultiplier
        () -> 0.02, // slamStdDev
        () -> 0.1, // slamAngularStdDev
        0.02 // loopPeriod
        );
  }
}
