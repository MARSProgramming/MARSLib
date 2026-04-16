package com.marslib.swerve;

import edu.wpi.first.math.geometry.*;
import edu.wpi.first.math.kinematics.*;

public class TracingTest {
  public static void main(String[] args) {
    Translation2d[] locs =
        new Translation2d[] {
          new Translation2d(0.5, 0.5), new Translation2d(0.5, -0.5),
          new Translation2d(-0.5, 0.5), new Translation2d(-0.5, -0.5)
        };
    SwerveDriveKinematics kin = new SwerveDriveKinematics(locs);
    SwerveSetpointGenerator gen = new SwerveSetpointGenerator(kin);

    SwerveSetpointGenerator.KinematicLimits lim = new SwerveSetpointGenerator.KinematicLimits();
    lim.maxDriveVelocity = 4.0;
    lim.maxDriveAcceleration = 20.0;
    lim.maxSteeringVelocity = Math.PI * 4;

    SwerveModuleState[] states =
        new SwerveModuleState[] {
          new SwerveModuleState(1.0, new Rotation2d()),
          new SwerveModuleState(1.0, new Rotation2d()),
          new SwerveModuleState(1.0, new Rotation2d()),
          new SwerveModuleState(1.0, new Rotation2d())
        };
    SwerveSetpointGenerator.SwerveSetpoint setpoint =
        new SwerveSetpointGenerator.SwerveSetpoint(new ChassisSpeeds(1, 0, 0), states);

    for (int i = 0; i < 20; i++) {
      setpoint = gen.generateSetpoint(lim, setpoint, new ChassisSpeeds(-1, 0, 0), 0.02);
      System.out.println(
          "i="
              + i
              + " speed="
              + setpoint.moduleStates[0].speedMetersPerSecond
              + " angle="
              + setpoint.moduleStates[0].angle.getDegrees());
    }
  }
}
