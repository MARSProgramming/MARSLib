package com.marslib.swerve;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import org.junit.jupiter.api.Test;

public class SwerveKinematicsExtendedTest {

  @Test
  public void testTeleopMathDeadband() {
    ChassisSpeeds out = new ChassisSpeeds();

    // Inside deadband (0.1)
    TeleopDriveMath.computeFieldRelativeSpeeds(0.05, 0.05, 0.05, false, out);
    assertEquals(0.0, out.vxMetersPerSecond, 1e-6);
    assertEquals(0.0, out.vyMetersPerSecond, 1e-6);
    assertEquals(0.0, out.omegaRadiansPerSecond, 1e-6);

    // Outside deadband
    TeleopDriveMath.computeFieldRelativeSpeeds(-0.5, 0.0, 0.0, false, out);
    // Stick forward (negative Y in HID) -> Positive X (forward) in Field
    assertTrue(out.vxMetersPerSecond > 0, "Forward stick should produce positive X velocity");
  }

  @Test
  public void testTeleopMathCubicScaling() {
    ChassisSpeeds out1 = new ChassisSpeeds();
    ChassisSpeeds out2 = new ChassisSpeeds();

    TeleopDriveMath.computeFieldRelativeSpeeds(-0.4, 0.0, 0.0, false, out1);
    TeleopDriveMath.computeFieldRelativeSpeeds(-0.8, 0.0, 0.0, false, out2);

    // 0.8 is 2x 0.4. Cubic scaling means 0.8 should produce 8x the scaled output of 0.4
    // (Wait, MathUtil.applyDeadband shifts the input first, so it's not EXACTLY 8x, but much
    // larger)
    assertTrue(
        out2.vxMetersPerSecond > out1.vxMetersPerSecond * 4.0,
        "Cubic scaling should provide high granularity at low speeds");
  }

  @Test
  public void testAllianceFlip() {
    ChassisSpeeds blue = new ChassisSpeeds();
    ChassisSpeeds red = new ChassisSpeeds();

    // Pull stick back (Positive Y in HID) -> Negative X (Backward) in Field
    TeleopDriveMath.computeFieldRelativeSpeeds(0.5, 0.5, 0.0, false, blue);
    TeleopDriveMath.computeFieldRelativeSpeeds(0.5, 0.5, 0.0, true, red);

    assertEquals(
        -blue.vxMetersPerSecond, red.vxMetersPerSecond, 1e-6, "X should flip for Red alliance");
    assertEquals(
        -blue.vyMetersPerSecond, red.vyMetersPerSecond, 1e-6, "Y should flip for Red alliance");
    assertEquals(
        blue.omegaRadiansPerSecond,
        red.omegaRadiansPerSecond,
        1e-6,
        "Omega should NOT flip for Red alliance");
  }
}
