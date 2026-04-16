/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.testing.performance;

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
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.littletonrobotics.junction.Logger;

/**
 * Performance regression detection system.
 *
 * <p>Tests current performance against established baselines and detects regressions. Used in CI to
 * prevent performance degradation.
 */
class PerformanceRegressionTest extends MARSBenchmark {

  @TempDir Path tempDir;

  private File baselineFile;
  private Properties baselines;
  private SwerveDrive drive;
  private SwerveConfig config;
  private PowerConfig powerConfig;

  @BeforeEach
  void setUp() throws IOException {
    MARSTestHarness.reset();
    config = MARSTestHarness.createSwerveConfig();
    powerConfig = MARSTestHarness.createPowerConfig();

    // Create baseline file in temp directory
    baselineFile = tempDir.resolve("performance-baselines.properties").toFile();
    baselines = new Properties();

    // Create test swerve drive
    PowerIO powerIO = new PowerIOSim(powerConfig);
    MARSPowerManager powerManager = new MARSPowerManager(powerIO, powerConfig);
    GyroIOSim gyroIO = new GyroIOSim();

    SwerveModule[] modules = new SwerveModule[4];
    for (int i = 0; i < 4; i++) {
      SwerveModuleIO moduleIO = new SwerveModuleIOSim(i, config);
      modules[i] = new SwerveModule(i, moduleIO, config);
    }

    drive = new SwerveDrive(modules, gyroIO, powerManager, config);
  }

  /** Helper to store baselines with proper try-with-resources to prevent Windows file leaks. */
  private void storeBaselines(String comment) throws IOException {
    try (java.io.Writer fw =
        java.nio.file.Files.newBufferedWriter(
            baselineFile.toPath(), java.nio.charset.StandardCharsets.UTF_8)) {
      baselines.store(fw, comment);
    }
  }

  /** Helper to load baselines with proper try-with-resources. */
  private Properties loadBaselines() throws IOException {
    Properties loaded = new Properties();
    try (java.io.Reader fr =
        java.nio.file.Files.newBufferedReader(
            baselineFile.toPath(), java.nio.charset.StandardCharsets.UTF_8)) {
      loaded.load(fr);
    }
    return loaded;
  }

  @Test
  void testEstablishBaseline() throws IOException {
    // Establish performance baseline
    ChassisSpeeds speeds = new ChassisSpeeds(1.0, 0.5, 0.3);
    long duration = benchmark(() -> drive.runVelocity(speeds), 1000);

    // Store baseline
    baselines.setProperty("swerve.kinematics.mean", String.valueOf(duration));
    storeBaselines("MARSLib Performance Baselines");

    // Verify file was created
    assertTrue(baselineFile.exists(), "Baseline file should be created");

    // Load and verify
    Properties loaded = loadBaselines();
    assertEquals(String.valueOf(duration), loaded.getProperty("swerve.kinematics.mean"));
  }

  @Test
  void testDetectRegression() throws IOException {
    // Set baseline (simulating a historically reasonable performance)
    long baselineDuration = 500_000; // 500 microseconds baseline
    baselines.setProperty("swerve.kinematics.mean", String.valueOf(baselineDuration));
    storeBaselines("MARSLib Performance Baselines");

    // Load baseline
    Properties loadedBaselines = loadBaselines();
    long baseline = Long.parseLong(loadedBaselines.getProperty("swerve.kinematics.mean"));

    // Measure current performance
    ChassisSpeeds speeds = new ChassisSpeeds(1.0, 0.5, 0.3);
    long currentDuration = benchmark(() -> drive.runVelocity(speeds), 1000);

    // Check for regression (>10% slower)
    double regressionThreshold = 1.1; // 10% tolerance
    double regressionRatio = (double) currentDuration / baseline;

    Logger.recordOutput("Performance/BaselineUs", baseline / 1000.0);
    Logger.recordOutput("Performance/CurrentUs", currentDuration / 1000.0);
    Logger.recordOutput("Performance/RegressionRatio", regressionRatio);

    if (regressionRatio > regressionThreshold) {
      throw new AssertionError(
          String.format(
              "PERFORMANCE REGRESSION DETECTED: Current performance %.2f µs is %.1f%% slower than baseline %.2f µs",
              currentDuration / 1000.0, (regressionRatio - 1.0) * 100, baseline / 1000.0));
    }

    // Test passes if within tolerance
    assertTrue(
        regressionRatio <= regressionThreshold, "Performance should not regress more than 10%");
  }

  @Test
  void testMultipleBenchmarks() throws IOException {
    // Test multiple performance characteristics
    ChassisSpeeds speeds = new ChassisSpeeds(1.0, 0.5, 0.3);

    // Benchmark different operations
    long kinematicsDuration = benchmark(() -> drive.runVelocity(speeds), 1000);
    long periodicDuration = benchmark(() -> drive.periodic(), 1000);
    long poseDuration = benchmark(() -> drive.getPose(), 1000);

    // Store all baselines
    baselines.setProperty("swerve.kinematics.mean", String.valueOf(kinematicsDuration));
    baselines.setProperty("swerve.periodic.mean", String.valueOf(periodicDuration));
    baselines.setProperty("swerve.getPose.mean", String.valueOf(poseDuration));
    storeBaselines("MARSLib Performance Baselines");

    // Verify all stored
    Properties loaded = loadBaselines();

    assertEquals(3, loaded.size(), "Should have 3 baseline measurements");
    assertEquals(String.valueOf(kinematicsDuration), loaded.getProperty("swerve.kinematics.mean"));
    assertEquals(String.valueOf(periodicDuration), loaded.getProperty("swerve.periodic.mean"));
    assertEquals(String.valueOf(poseDuration), loaded.getProperty("swerve.getPose.mean"));
  }

  @Test
  void testPerformanceImprovement() throws IOException {
    // Set a deliberately slow baseline (10 milliseconds)
    long slowBaseline = 10_000_000; // 10 milliseconds in nanoseconds
    baselines.setProperty("swerve.kinematics.mean", String.valueOf(slowBaseline));
    storeBaselines("MARSLib Performance Baselines");

    // Load baseline
    Properties loadedBaselines = loadBaselines();
    long baseline = Long.parseLong(loadedBaselines.getProperty("swerve.kinematics.mean"));

    // Measure improved performance (should be much faster)
    ChassisSpeeds speeds = new ChassisSpeeds(1.0, 0.5, 0.3);
    long currentDuration = benchmark(() -> drive.runVelocity(speeds), 1000);

    // Calculate improvement ratio
    double improvementRatio = (double) currentDuration / baseline;

    Logger.recordOutput("Performance/BaselineUs", baseline / 1000.0);
    Logger.recordOutput("Performance/CurrentUs", currentDuration / 1000.0);
    Logger.recordOutput("Performance/ImprovementRatio", improvementRatio);

    // Improvement is good, should not fail the test
    assertTrue(improvementRatio <= 1.1, "Significant performance improvement should pass");

    // Optionally update baseline if improvement is significant
    if (improvementRatio < 0.9) { // More than 10% improvement
      Logger.recordOutput("Performance/ImprovementDetected", true);
    }
  }

  @Test
  void testBaselineValidation() throws IOException {
    // Create invalid baseline file
    baselines.setProperty("swerve.kinematics.mean", "invalid");
    storeBaselines("Invalid Baseline");

    // Should handle gracefully
    Properties loadedBaselines = loadBaselines();

    assertThrows(
        NumberFormatException.class,
        () -> {
          Long.parseLong(loadedBaselines.getProperty("swerve.kinematics.mean"));
        });
  }

  @Test
  void testDetailedPerformanceTracking() throws IOException {
    ChassisSpeeds speeds = new ChassisSpeeds(1.0, 0.5, 0.3);

    // Get detailed statistics
    BenchmarkResult result = benchmarkDetailed(() -> drive.runVelocity(speeds), 1000);

    // Store detailed baseline
    baselines.setProperty("swerve.kinematics.min", String.valueOf(result.getMinNanos()));
    baselines.setProperty("swerve.kinematics.max", String.valueOf(result.getMaxNanos()));
    baselines.setProperty("swerve.kinematics.mean", String.valueOf(result.getMeanNanos()));
    baselines.setProperty("swerve.kinematics.stddev", String.valueOf(result.getStdDevNanos()));
    storeBaselines("Detailed Performance Baselines");

    Logger.recordOutput("Performance/DetailedResult", result.toString());

    // Load and verify
    Properties loaded = loadBaselines();

    assertEquals(String.valueOf(result.getMinNanos()), loaded.getProperty("swerve.kinematics.min"));
    assertEquals(String.valueOf(result.getMaxNanos()), loaded.getProperty("swerve.kinematics.max"));
    assertEquals(
        String.valueOf(result.getMeanNanos()), loaded.getProperty("swerve.kinematics.mean"));
    assertEquals(
        String.valueOf(result.getStdDevNanos()), loaded.getProperty("swerve.kinematics.stddev"));
  }
}
