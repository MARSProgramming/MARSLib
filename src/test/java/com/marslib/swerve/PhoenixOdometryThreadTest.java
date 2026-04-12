package com.marslib.swerve;

import static org.junit.jupiter.api.Assertions.*;

import com.ctre.phoenix6.BaseStatusSignal;
import com.marslib.testing.MARSTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PhoenixOdometryThreadTest {

  @BeforeEach
  public void setUp() {
    MARSTestHarness.reset();
  }

  @Test
  public void testSingletonInitialization() {
    PhoenixOdometryThread instance1 = PhoenixOdometryThread.getInstance();
    assertNotNull(instance1);
    assertTrue(instance1.isAlive());

    PhoenixOdometryThread instance2 = PhoenixOdometryThread.getInstance();
    assertSame(instance1, instance2);
  }

  @Test
  public void testModuleRegistration() {
    PhoenixOdometryThread thread = PhoenixOdometryThread.getInstance();

    // Use simulated TalonFX to get real signals
    com.ctre.phoenix6.hardware.TalonFX driveMotor = new com.ctre.phoenix6.hardware.TalonFX(0);
    com.ctre.phoenix6.hardware.TalonFX turnMotor = new com.ctre.phoenix6.hardware.TalonFX(1);

    BaseStatusSignal drivePos = driveMotor.getPosition();
    BaseStatusSignal turnPos = turnMotor.getPosition();

    int id = thread.registerModule(drivePos, turnPos);
    assertEquals(0, id);

    // Verify registration returns sequential IDs
    int id2 = thread.registerModule(drivePos, turnPos);
    assertEquals(1, id2);
  }

  @Test
  public void testGyroRegistration() {
    PhoenixOdometryThread thread = PhoenixOdometryThread.getInstance();
    com.ctre.phoenix6.hardware.Pigeon2 pigeon = new com.ctre.phoenix6.hardware.Pigeon2(0);
    BaseStatusSignal gyroYaw = pigeon.getYaw();

    // Should not throw
    assertDoesNotThrow(() -> thread.registerGyro(gyroYaw));
  }

  @Test
  public void testGetSyncDataReturnsPreallocatedObject() {
    PhoenixOdometryThread thread = PhoenixOdometryThread.getInstance();
    com.ctre.phoenix6.hardware.TalonFX driveMotor = new com.ctre.phoenix6.hardware.TalonFX(2);
    com.ctre.phoenix6.hardware.TalonFX turnMotor = new com.ctre.phoenix6.hardware.TalonFX(3);

    BaseStatusSignal drivePos = driveMotor.getPosition();
    BaseStatusSignal turnPos = turnMotor.getPosition();

    int id = thread.registerModule(drivePos, turnPos);

    PhoenixOdometryThread.SyncData data1 = thread.getSyncData(id);
    assertNotNull(data1);
    assertEquals(0, data1.validCount);

    PhoenixOdometryThread.SyncData data2 = thread.getSyncData(id);
    assertSame(data1, data2, "SyncData object should be reused (zero-allocation)");
  }
}
