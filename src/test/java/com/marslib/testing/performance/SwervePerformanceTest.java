/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.testing.performance;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.marslib.power.MARSPowerManager;
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

/**
 * Performance benchmarks for swerve drive system.
 *
 * <p>Tests performance characteristics of critical swerve operations.
 */
class SwervePerformanceTest extends MARSBenchmark {

  private SwerveDrive drive;
  private SwerveConfig config;
  private SwerveModule[] modules;
  private GyroIOSim gyroIO;
  private MARSPowerManager powerManager;

  @BeforeEach
  void setUp() {
    MARSTestHarness.reset();
    config = MARSTestHarness.createSwerveConfig();
    var powerConfig = MARSTestHarness.createPowerConfig();

    PowerIO powerIO = new PowerIOSim(powerConfig);
    powerManager = new MARSPowerManager(powerIO, powerConfig);
    gyroIO = new GyroIOSim();

    modules = new SwerveModule[4];
    for (int i = 0; i < 4; i++) {
      SwerveModuleIO moduleIO = new SwerveModuleIOSim(i);
      modules[i] = new SwerveModule(i, moduleIO, config);
    }

    drive = new SwerveDrive(modules, gyroIO, powerManager, config);
  }

  @Test
  void testSwerveKinematicsPerformance() {
    ChassisSpeeds speeds = new ChassisSpeeds(1.0, 0.5, 0.3);

    long duration =
        benchmark(
            () -> {
              drive.runVelocity(speeds);
            },
            1000);

    assertDurationLessThan(duration, Thresholds.KINEMATICS_MAX);
    System.out.printf("Swerve kinematics: %.2f µs per call%n", duration / 1000.0);
  }

  @Test
  void testSwerveOdometryPerformance() {
    long duration =
        benchmark(
            () -> {
              drive.periodic();
            },
            1000);

    assertDurationLessThan(duration, Thresholds.PERIODIC_MAX);
    System.out.printf("Swerve odometry/periodic: %.2f µs per update%n", duration / 1000.0);
  }

  @Test
  void testSwervePeriodicPerformance() {
    long duration =
        benchmark(
            () -> {
              drive.periodic();
            },
            100);

    assertDurationLessThan(duration, Thresholds.PERIODIC_MAX / 10);
    System.out.printf("Swerve periodic: %.2f µs per call%n", duration / 1000.0);
  }

  @Test
  void testSwerveDetailedPerformance() {
    ChassisSpeeds speeds = new ChassisSpeeds(1.0, 0.5, 0.3);

    // Extensive warmup to trigger C2 JIT and stabilize execution
    for (int i = 0; i < 20000; i++) {
      drive.runVelocity(speeds);
    }

    BenchmarkResult result =
        benchmarkDetailed(
            () -> {
              drive.runVelocity(speeds);
            },
            5000);

    System.out.printf("Swerve kinematics detailed: %s%n", result);

    assertTrue(
        result.getStdDevMicros() < result.getMeanMicros() * 0.8,
        "High variance in kinematics calculations ("
            + result.getStdDevMicros()
            + " vs "
            + result.getMeanMicros()
            + ")");
    assertDurationLessThan((long) result.getMaxNanos(), Thresholds.KINEMATICS_MAX * 10);
  }

  @Test
  void testHighFrequencyOperation() {
    ChassisSpeeds speeds = new ChassisSpeeds(1.0, 0.5, 0.3);

    long startTime = System.nanoTime();
    for (int i = 0; i < 1000; i++) {
      drive.runVelocity(speeds);
      drive.periodic();
    }
    long duration = System.nanoTime() - startTime;

    double avgTime = duration / 1000.0;
    System.out.printf("High-frequency operation: %.2f µs per cycle%n", avgTime / 1000.0);

    assertTrue(
        duration < 100_000_000L,
        "High-frequency operation too slow: " + avgTime / 1000.0 + " µs per cycle");
  }

  @Test
  void testFullRobotLoopPerformance() {
    ChassisSpeeds speeds = new ChassisSpeeds(1.0, 0.5, 0.3);

    long duration =
        benchmark(
            () -> {
              drive.runVelocity(speeds);
              drive.periodic();
              DriverStationSim.notifyNewData();
              CommandScheduler.getInstance().run();
            },
            100);

    System.out.printf("Full robot loop: %.2f µs per cycle%n", duration / 1000.0);
    assertDurationLessThan(duration, Thresholds.PERIODIC_MAX);
  }

  @Test
  void testMemoryAllocation() {
    ChassisSpeeds speeds = new ChassisSpeeds(1.0, 0.5, 0.3);

    // Warmup to trigger JIT and stabilize heap
    for (int i = 0; i < 2000; i++) {
      drive.runVelocity(speeds);
      drive.periodic();
    }

    // Measure memory before with multi-pass GC
    for (int i = 0; i < 3; i++) {
      System.gc();
      try {
        Thread.sleep(20);
      } catch (InterruptedException ignored) {
      }
    }
    long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

    // Run many iterations
    for (int i = 0; i < 10000; i++) {
      drive.runVelocity(speeds);
      drive.periodic();
    }

    // Measure memory after
    for (int i = 0; i < 3; i++) {
      System.gc();
      try {
        Thread.sleep(20);
      } catch (InterruptedException ignored) {
      }
    }
    long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

    long memDelta = memAfter - memBefore;
    System.out.printf("Memory delta: %d bytes for 10,000 iterations%n", memDelta);

    // Should allocate minimal memory (< 2.0MB for 10k iterations = < 200 bytes per iteration)
    // Residual allocation is primarily WPILib PoseEstimator internals, essential telemetry
    // (Pose2d/Pose3d), and instrumentation overhead
    assertTrue(
        memDelta < 2_000_000,
        "Excessive memory allocation: " + memDelta + " bytes for 10,000 iterations");
  }
}
