/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.testing.load;

import static org.junit.jupiter.api.Assertions.*;

import com.marslib.power.MARSPowerManager;
import com.marslib.power.PowerConfig;
import com.marslib.power.PowerIO;
import com.marslib.power.PowerIOSim;
import com.marslib.swerve.GyroIOSim;
import com.marslib.swerve.SwerveConfig;
import com.marslib.swerve.SwerveDrive;
import com.marslib.swerve.SwerveModule;
import com.marslib.swerve.SwerveModuleIO;
import com.marslib.swerve.SwerveModuleIOSim;
import com.marslib.testing.MARSTestHarness;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.littletonrobotics.junction.Logger;

/**
 * High-frequency loop stress tests.
 *
 * <p>Tests system behavior under extreme loop frequencies to ensure MARSLib can handle worst-case
 * timing scenarios.
 */
class HighFrequencyLoopTest {

  private SwerveDrive drive;
  private SwerveConfig config;
  private PowerConfig powerConfig;

  @BeforeEach
  void setUp() {
    MARSTestHarness.reset();
    config = MARSTestHarness.createSwerveConfig();
    powerConfig = MARSTestHarness.createPowerConfig();

    PowerIO powerIO = new PowerIOSim(powerConfig);
    MARSPowerManager powerManager = new MARSPowerManager(powerIO, powerConfig);
    GyroIOSim gyroIO = new GyroIOSim();

    SwerveModule[] modules = new SwerveModule[4];
    for (int i = 0; i < 4; i++) {
      SwerveModuleIO moduleIO = new SwerveModuleIOSim(i);
      modules[i] = new SwerveModule(i, moduleIO, config);
    }

    drive = new SwerveDrive(modules, gyroIO, powerManager, config);
  }

  @Test
  void test1000HzOdometryUpdates() {
    // Test 1000Hz drive updates (extreme stress test)
    ChassisSpeeds speeds = new ChassisSpeeds(1.0, 0.5, 0.3);

    long startTime = System.nanoTime();
    int iterations = 1000;

    for (int i = 0; i < iterations; i++) {
      drive.runVelocity(speeds);
      drive.periodic(); // Odometry updates internally
    }

    long duration = System.nanoTime() - startTime;
    double avgTime = duration / (double) iterations;

    Logger.recordOutput("LoadTest/1000Hz/AvgTimeUs", avgTime / 1000.0);
    Logger.recordOutput("LoadTest/1000Hz/TotalTimeMs", duration / 1_000_000.0);

    // Should complete 1000 iterations in reasonable time (< 5 seconds)
    assertTrue(
        duration < 5_000_000_000L,
        "1000Hz odometry took too long: " + duration / 1_000_000.0 + " ms");

    // Average time per iteration should be reasonable (< 1ms)
    assertTrue(
        avgTime < 1_000_000L, "Average iteration time too high: " + avgTime / 1000.0 + " µs");
  }

  @Test
  void test500HzRobotLoop() {
    // Test 500Hz full robot loop (10x normal frequency)
    ChassisSpeeds speeds = new ChassisSpeeds(1.0, 0.5, 0.3);

    long startTime = System.nanoTime();
    int iterations = 500;

    for (int i = 0; i < iterations; i++) {
      drive.runVelocity(speeds);
      drive.periodic();
      DriverStationSim.notifyNewData();
      CommandScheduler.getInstance().run();
    }

    long duration = System.nanoTime() - startTime;
    double avgTime = duration / (double) iterations;

    Logger.recordOutput("LoadTest/500Hz/AvgTimeUs", avgTime / 1000.0);
    Logger.recordOutput("LoadTest/500Hz/TotalTimeMs", duration / 1_000_000.0);

    // Should complete 500 iterations in reasonable time
    assertTrue(duration < 10_000_000_000L, "500Hz loop took too long");

    // Average time per iteration should be < 2ms (for 500Hz)
    assertTrue(
        avgTime < 2_000_000L, "Average iteration time too high: " + avgTime / 1000.0 + " µs");
  }

  @Test
  void testSustained250HzOdometry() {
    // Test sustained 250Hz drive updates (normal max frequency)
    ChassisSpeeds speeds = new ChassisSpeeds(1.0, 0.5, 0.3);

    long startTime = System.nanoTime();
    int iterations = 5000; // 20 seconds at 250Hz

    for (int i = 0; i < iterations; i++) {
      drive.runVelocity(speeds);
      drive.periodic(); // Odometry updates internally
    }

    long duration = System.nanoTime() - startTime;
    double avgTime = duration / (double) iterations;
    double actualHz = 1_000_000_000.0 / avgTime;

    Logger.recordOutput("LoadTest/250HzSustained/AvgTimeUs", avgTime / 1000.0);
    Logger.recordOutput("LoadTest/250HzSustained/ActualHz", actualHz);
    Logger.recordOutput("LoadTest/250HzSustained/TotalTimeMs", duration / 1_000_000.0);

    // Should maintain close to 250Hz
    assertTrue(actualHz > 200, "Actual Hz too low: " + actualHz);

    // Average time should be close to 4ms (250Hz = 4ms per iteration)
    assertTrue(
        avgTime < 5_000_000L, "Average iteration time too high: " + avgTime / 1000.0 + " µs");
  }

  @Test
  void testBurstLoad() {
    // Test burst load pattern (common in matches)
    ChassisSpeeds speeds = new ChassisSpeeds(1.0, 0.5, 0.3);

    // Simulate burst: 10 cycles of heavy load, 10 cycles of light load
    long startTime = System.nanoTime();
    int burstCycles = 10;
    int iterations = 1000;

    for (int i = 0; i < iterations; i++) {
      // Heavy load (complex calculations)
      for (int j = 0; j < burstCycles; j++) {
        drive.runVelocity(speeds);
        drive.periodic(); // Odometry updates internally
        drive.getPose();
        drive.getChassisSpeeds();
      }

      // Light load (simple updates)
      drive.periodic();
      DriverStationSim.notifyNewData();
      CommandScheduler.getInstance().run();
    }

    long duration = System.nanoTime() - startTime;
    double totalTimeMs = duration / 1_000_000.0;

    Logger.recordOutput("LoadTest/Burst/TotalTimeMs", totalTimeMs);
    Logger.recordOutput("LoadTest/Burst/AvgTimePerCycleUs", duration / (iterations * 1000.0));

    // Should complete burst load in reasonable time
    assertTrue(totalTimeMs < 30_000, "Burst load took too long: " + totalTimeMs + " ms");
  }

  @Test
  void testWorstCaseScenario() {
    // Test worst-case scenario: all subsystems active, high frequency
    ChassisSpeeds maxSpeeds = new ChassisSpeeds(5.0, 5.0, 10.0);

    long startTime = System.nanoTime();
    int iterations = 1000;

    for (int i = 0; i < iterations; i++) {
      // Maximum load operations
      drive.runVelocity(maxSpeeds);
      drive.periodic(); // Odometry updates internally
      drive.getPose();
      drive.getChassisSpeeds();
      // Skip vision measurement as it requires proper Pose2d and covariance
      DriverStationSim.notifyNewData();
      CommandScheduler.getInstance().run();
    }

    long duration = System.nanoTime() - startTime;
    double avgTime = duration / (double) iterations;

    Logger.recordOutput("LoadTest/WorstCase/AvgTimeUs", avgTime / 1000.0);
    Logger.recordOutput("LoadTest/WorstCase/MaxFreqHz", 1_000_000_000.0 / avgTime);

    // Even worst case should maintain > 100Hz
    double maxFreq = 1_000_000_000.0 / avgTime;
    assertTrue(maxFreq > 100, "Worst case frequency too low: " + maxFreq + " Hz");

    // Average time should be < 10ms
    assertTrue(
        avgTime < 10_000_000L, "Worst case iteration time too high: " + avgTime / 1000.0 + " µs");
  }

  @Test
  void testMemoryStability() {
    // Test that memory usage stays stable under high frequency
    ChassisSpeeds speeds = new ChassisSpeeds(1.0, 0.5, 0.3);

    System.gc();
    long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

    // Run 10,000 iterations
    for (int i = 0; i < 10_000; i++) {
      drive.runVelocity(speeds);
      drive.periodic(); // Odometry updates internally
    }

    System.gc();
    long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

    long memDelta = memAfter - memBefore;
    Logger.recordOutput("LoadTest/MemoryDeltaBytes", memDelta);
    Logger.recordOutput("LoadTest/MemoryDeltaPerIterationBytes", memDelta / 10_000.0);

    // Memory delta should be minimal (< 10MB for 10k iterations)
    assertTrue(
        memDelta < 10_000_000L,
        "Memory usage increased by " + memDelta + " bytes over 10,000 iterations");

    // Per-iteration allocation should be very small
    double perIteration = memDelta / 10_000.0;
    assertTrue(
        perIteration < 1000, "Per-iteration allocation too high: " + perIteration + " bytes");
  }
}
