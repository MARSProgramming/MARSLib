package com.marslib.swerve;

import edu.wpi.first.math.geometry.Translation2d;

/**
 * Configuration record for the SwerveDrive subsystem.
 *
 * <p>This object encapsulates all physical, kinematic, and structural parameters, allowing the
 * library to remain data-driven and independent of the robot's constant files.
 */
public record SwerveConfig(
    Translation2d[] moduleLocations,
    double maxLinearSpeedMps,
    double maxAngularSpeedRadPerSec,
    double wheelRadiusMeters,
    double turnKp,
    double turnKd,
    double driveKp,
    double driveKd,
    double driveKs,
    double driveKv,
    double driveKa,
    double nominalVoltage,
    double warningVoltage,
    double criticalVoltage,
    double driveGearRatio,
    double driveStatorCurrentLimit,
    double robotMassKg,
    double robotMoiKgM2,
    double bumperLengthMeters,
    double bumperWidthMeters,
    double wheelbaseMeters,
    double trackWidthMeters,
    double wheelCOFStatic,
    double loopPeriodSecs,
    double teleopLinearAccelLimit,
    double teleopOmegaAccelLimit,
    double headingKp,
    double headingKd,
    double autoTranslationKp,
    double autoTranslationKd,
    double autoRotationKp,
    double autoRotationKd,
    double alignTranslationKp,
    double alignTranslationIZoneMeters,
    double alignThetaKp,
    double alignThetaIZoneRad,
    double telemetryHz,
    double odometryHz,
    double turnGearRatio,
    double turnStatorCurrentLimit,
    double couplingRatio) {}
