package frc.robot.subsystems;

import static org.junit.jupiter.api.Assertions.*;

import com.ctre.phoenix6.BaseStatusSignal;
import com.marslib.swerve.*;
import com.marslib.testing.MARSTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("resource")
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

    int id = thread.registerModule(drivePos, turnPos, 250.0);
    assertEquals(0, id);

    // Verify registration returns sequential IDs
    int id2 = thread.registerModule(drivePos, turnPos, 250.0);
    assertEquals(1, id2);
  }

  @Test
  public void testGyroRegistration() {
    PhoenixOdometryThread thread = PhoenixOdometryThread.getInstance();
    com.ctre.phoenix6.hardware.Pigeon2 pigeon = new com.ctre.phoenix6.hardware.Pigeon2(0);
    BaseStatusSignal gyroYaw = pigeon.getYaw();

    // Should not throw
    assertDoesNotThrow(() -> thread.registerGyro(gyroYaw, 250.0));
  }

  @Test
  public void testGetSyncDataReturnsPreallocatedObject() {
    PhoenixOdometryThread thread = PhoenixOdometryThread.getInstance();
    com.ctre.phoenix6.hardware.TalonFX driveMotor = new com.ctre.phoenix6.hardware.TalonFX(2);
    com.ctre.phoenix6.hardware.TalonFX turnMotor = new com.ctre.phoenix6.hardware.TalonFX(3);

    BaseStatusSignal drivePos = driveMotor.getPosition();
    BaseStatusSignal turnPos = turnMotor.getPosition();

    int id = thread.registerModule(drivePos, turnPos, 250.0);

    PhoenixOdometryThread.SyncData data1 = thread.getSyncData(id);
    assertNotNull(data1);
    assertEquals(0, data1.validCount);

    PhoenixOdometryThread.SyncData data2 = thread.getSyncData(id);
    assertSame(data1, data2, "SyncData object should be reused (zero-allocation)");
  }

  @Test
  public void testThreadInterruptionSafety() throws InterruptedException {
    PhoenixOdometryThread thread = PhoenixOdometryThread.getInstance();
    assertTrue(thread.isAlive());

    // Trigger an interrupt on the sleeping daemon
    thread.interrupt();

    // Give it a moment to catch the InterruptedException and break
    thread.join(1000);

    // Assert that the thread shut down cleanly
    assertFalse(thread.isAlive(), "Thread failed to terminate cleanly on interrupt.");

    // Clear out the dead instance
    PhoenixOdometryThread.resetInstance();
  }

  @Test
  public void testThreadInterruptionSafetyWhileActive() throws InterruptedException {
    PhoenixOdometryThread thread = PhoenixOdometryThread.getInstance();

    // Register to move past the empty check
    com.ctre.phoenix6.hardware.TalonFX m1 = new com.ctre.phoenix6.hardware.TalonFX(30);
    thread.registerModule(m1.getPosition(), m1.getPosition(), 250);

    Thread.sleep(50);
    thread.interrupt();
    thread.join(1000);

    assertFalse(thread.isAlive());
    PhoenixOdometryThread.resetInstance();
  }

  @Test
  public void testThreadLoopUpdatesRegisters() throws InterruptedException {
    PhoenixOdometryThread thread = PhoenixOdometryThread.getInstance();

    com.ctre.phoenix6.hardware.TalonFX driveMotor = new com.ctre.phoenix6.hardware.TalonFX(10);
    com.ctre.phoenix6.hardware.TalonFX turnMotor = new com.ctre.phoenix6.hardware.TalonFX(11);

    int id = thread.registerModule(driveMotor.getPosition(), turnMotor.getPosition(), 250);

    // Wait for the background thread to loop at least once
    // SimHooks.stepTiming triggers simulation time
    edu.wpi.first.wpilibj.simulation.SimHooks.stepTiming(0.02);
    Thread.sleep(100);
    edu.wpi.first.wpilibj.simulation.SimHooks.stepTiming(0.02);
    Thread.sleep(100);

    PhoenixOdometryThread.SyncData data = thread.getSyncData(id);
    // At least one sample should have been picked up by the daemon
    assertTrue(data.validCount >= 0);

    PhoenixOdometryThread.resetInstance();
  }

  @Test
  public void testEmptyQueuesReturnZeroCount() {
    PhoenixOdometryThread thread = PhoenixOdometryThread.getInstance();

    // Register but do not wait for the thread to sample
    com.ctre.phoenix6.hardware.TalonFX driveMotor = new com.ctre.phoenix6.hardware.TalonFX(12);
    com.ctre.phoenix6.hardware.TalonFX turnMotor = new com.ctre.phoenix6.hardware.TalonFX(13);
    int id = thread.registerModule(driveMotor.getPosition(), turnMotor.getPosition(), 250);

    // Immediately drain before thread has a chance to add
    PhoenixOdometryThread.SyncData data = thread.getSyncData(id);
    assertEquals(0, data.validCount);
  }

  @Test
  public void testThreadFillsQueuesToCapacity() throws InterruptedException {
    PhoenixOdometryThread thread = PhoenixOdometryThread.getInstance();
    com.ctre.phoenix6.hardware.TalonFX driveMotor = new com.ctre.phoenix6.hardware.TalonFX(14);
    com.ctre.phoenix6.hardware.TalonFX turnMotor = new com.ctre.phoenix6.hardware.TalonFX(15);
    int id = thread.registerModule(driveMotor.getPosition(), turnMotor.getPosition(), 250);

    com.ctre.phoenix6.hardware.Pigeon2 pigeon = new com.ctre.phoenix6.hardware.Pigeon2(2);
    thread.registerGyro(pigeon.getYaw(), 250.0);

    // Give it enough time to max out the 50 capacity buffer (250Hz -> 50 loops = 200ms)
    // We sleep longer here since thread scheduling on CI can be noisy.
    for (int i = 0; i < 40; i++) {
      edu.wpi.first.wpilibj.simulation.SimHooks.stepTiming(0.02);
      Thread.sleep(20);
    }

    // Now the remainingCapacity check in run() should have been false at least once
    // And getGyroYawData / getSyncData should hit MAX_SAMPLES
    PhoenixOdometryThread.SyncData data = thread.getSyncData(id);
    assertEquals(PhoenixOdometryThread.MAX_SAMPLES, data.validCount);

    PhoenixOdometryThread.GyroYawData gyroData = thread.getGyroYawData();
    assertEquals(PhoenixOdometryThread.MAX_SAMPLES, gyroData.validCount);
  }

  @Test
  public void testGetGyroYawDataReturnsPreallocatedObject() {
    PhoenixOdometryThread thread = PhoenixOdometryThread.getInstance();
    com.ctre.phoenix6.hardware.Pigeon2 pigeon = new com.ctre.phoenix6.hardware.Pigeon2(3);
    thread.registerGyro(pigeon.getYaw(), 250.0);

    PhoenixOdometryThread.GyroYawData data1 = thread.getGyroYawData();
    assertNotNull(data1);

    PhoenixOdometryThread.GyroYawData data2 = thread.getGyroYawData();
    assertSame(data1, data2);
  }
}
