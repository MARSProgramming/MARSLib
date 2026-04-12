package frc.robot.constants;

public final class ShooterConstants {
  public static final int LM_MOTOR_ID = 9;
  public static final int LF_MOTOR_ID = 10;
  public static final int RM_MOTOR_ID = 11;
  public static final int RF_MOTOR_ID = 12;
  public static final int FEEDER_MOTOR_ID = 20;
  public static final String CANBUS = "CAN2";
  public static final double PROJECTILE_SPEED_MPS = 15.0;
  public static final double FEEDER_GEAR_RATIO = 4.0;

  /**
   * Radius of the shooter flywheel in meters. Used for v = ωr conversion from launch speed (m/s) to
   * flywheel angular velocity (rad/s). 0.0508m = 2 inch radius (4 inch diameter wheel).
   */
  public static final double SHOOTER_WHEEL_RADIUS_METERS = 0.0508;
}
