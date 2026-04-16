package frc.robot.constants;

public final class DriveConstants {
  public static final String CANBUS = "canivore";

  // Front Left
  public static final int FL_DRIVE_ID = 21;
  public static final int FL_TURN_ID = 22;
  // Front Right
  public static final int FR_DRIVE_ID = 23;
  public static final int FR_TURN_ID = 24;
  // Back Left
  public static final int BL_DRIVE_ID = 25;
  public static final int BL_TURN_ID = 26;
  // Back Right
  public static final int BR_DRIVE_ID = 27;
  public static final int BR_TURN_ID = 28;

  public static final int PIGEON2_ID = 20;

  public static final double TELEOP_LINEAR_ACCEL_LIMIT = 15.0;
  public static final double TELEOP_OMEGA_ACCEL_LIMIT = Math.PI * 6.0;
  public static final double HEADING_KP = 2.5;
  public static final double HEADING_KD = 0.2;

  public static final double TELEMETRY_HZ = 50.0;
  public static final double ODOMETRY_HZ = 250.0;
}
