/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.testing.performance;

/**
 * Base class for MARSLib performance benchmarks.
 *
 * <p>Provides common infrastructure and utilities for benchmarking critical code paths. All
 * benchmarks should extend this class to ensure consistent measurement and reporting.
 *
 * <p>Performance benchmarks focus on time-critical code paths:
 *
 * <ul>
 *   <li>Periodic() methods (50Hz = 20ms budget)
 *   <li>Odometry updates (250Hz = 4ms budget)
 *   <li>Vision processing (30Hz+ = 33ms budget)
 *   <li>High-frequency loops (control, telemetry)
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>
 * public class SwerveBenchmark extends MARSBenchmark {
 *     @Test
 *     public void benchmarkSwerveKinematics() {
 *         SwerveDrive drive = createTestSwerveDrive();
 *         ChassisSpeeds speeds = new ChassisSpeeds(1.0, 0.5, 0.3);
 *
 *         long duration = benchmark(() -> {
 *             drive.driveRobotRelative(speeds);
 *         }, 1000); // 1000 iterations
 *
 *         assertDurationLessThan(duration, 1_000_000); // 1ms max
 *     }
 * }
 * </pre>
 */
public class MARSBenchmark {

  /** Default number of warmup iterations for JVM optimization */
  protected static final int DEFAULT_WARMUP_ITERATIONS = 100;

  /** Default number of measurement iterations */
  protected static final int DEFAULT_MEASUREMENT_ITERATIONS = 1000;

  /**
   * Benchmark a runnable and return the average duration in nanoseconds.
   *
   * @param runnable The code to benchmark
   * @param iterations Number of times to run the benchmark
   * @return Average duration in nanoseconds
   */
  @SuppressWarnings("PMD.DoNotCallGarbageCollectionExplicitly")
  protected long benchmark(Runnable runnable, int iterations) {
    // Warmup JVM optimization
    for (int i = 0; i < DEFAULT_WARMUP_ITERATIONS; i++) {
      runnable.run();
    }

    // Force garbage collection before measurement
    System.gc();

    // Measure
    long startTime = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
      runnable.run();
    }
    long endTime = System.nanoTime();

    return (endTime - startTime) / iterations;
  }

  /**
   * Benchmark a runnable and return detailed statistics.
   *
   * @param runnable The code to benchmark
   * @param iterations Number of times to run the benchmark
   * @return BenchmarkResult with min, max, mean, and std dev
   */
  @SuppressWarnings("PMD.DoNotCallGarbageCollectionExplicitly")
  protected BenchmarkResult benchmarkDetailed(Runnable runnable, int iterations) {
    long[] durations = new long[iterations];

    // Warmup
    for (int i = 0; i < DEFAULT_WARMUP_ITERATIONS; i++) {
      runnable.run();
    }

    System.gc();

    // Measure each iteration
    for (int i = 0; i < iterations; i++) {
      long start = System.nanoTime();
      runnable.run();
      long end = System.nanoTime();
      durations[i] = (end - start);
    }

    return calculateStatistics(durations);
  }

  /**
   * Calculate statistics from duration measurements.
   *
   * @param durations Array of duration measurements in nanoseconds
   * @return BenchmarkResult with calculated statistics
   */
  protected BenchmarkResult calculateStatistics(long... durations) {
    long min = Long.MAX_VALUE;
    long max = Long.MIN_VALUE;
    long sum = 0;

    for (long duration : durations) {
      min = Math.min(min, duration);
      max = Math.max(max, duration);
      sum += duration;
    }

    double mean = (double) sum / durations.length;

    // Calculate standard deviation
    double variance = 0;
    for (long duration : durations) {
      variance += Math.pow(duration - mean, 2);
    }
    variance /= durations.length;
    double stdDev = Math.sqrt(variance);

    return new BenchmarkResult(min, max, mean, stdDev);
  }

  /**
   * Assert that a duration is less than a maximum threshold.
   *
   * @param durationNanos The measured duration in nanoseconds
   * @param maxNanos The maximum allowed duration in nanoseconds
   * @throws AssertionError if duration exceeds maximum
   */
  protected void assertDurationLessThan(long durationNanos, long maxNanos) {
    if (durationNanos > maxNanos) {
      throw new AssertionError(
          String.format(
              "Duration %d ns exceeded maximum %d ns (%.2f µs vs %.2f µs)",
              durationNanos, maxNanos, durationNanos / 1000.0, maxNanos / 1000.0));
    }
  }

  /**
   * Assert that a duration is within a range.
   *
   * @param durationNanos The measured duration in nanoseconds
   * @param minNanos The minimum expected duration in nanoseconds
   * @param maxNanos The maximum allowed duration in nanoseconds
   * @throws AssertionError if duration is outside range
   */
  protected void assertDurationInRange(long durationNanos, long minNanos, long maxNanos) {
    if (durationNanos < minNanos || durationNanos > maxNanos) {
      throw new AssertionError(
          String.format(
              "Duration %d ns outside range [%d, %d] ns (%.2f µs vs [%.2f, %.2f] µs)",
              durationNanos,
              minNanos,
              maxNanos,
              durationNanos / 1000.0,
              minNanos / 1000.0,
              maxNanos / 1000.0));
    }
  }

  /** Benchmark result with statistical measures. */
  public static class BenchmarkResult {
    private final long minNanos;
    private final long maxNanos;
    private final double meanNanos;
    private final double stdDevNanos;

    public BenchmarkResult(long minNanos, long maxNanos, double meanNanos, double stdDevNanos) {
      this.minNanos = minNanos;
      this.maxNanos = maxNanos;
      this.meanNanos = meanNanos;
      this.stdDevNanos = stdDevNanos;
    }

    public long getMinNanos() {
      return minNanos;
    }

    public long getMaxNanos() {
      return maxNanos;
    }

    public double getMeanNanos() {
      return meanNanos;
    }

    public double getStdDevNanos() {
      return stdDevNanos;
    }

    public double getMinMicros() {
      return minNanos / 1000.0;
    }

    public double getMaxMicros() {
      return maxNanos / 1000.0;
    }

    public double getMeanMicros() {
      return meanNanos / 1000.0;
    }

    public double getStdDevMicros() {
      return stdDevNanos / 1000.0;
    }

    @Override
    public String toString() {
      return String.format(
          "BenchmarkResult{min=%.2f µs, max=%.2f µs, mean=%.2f µs, stdDev=%.2f µs}",
          getMinMicros(), getMaxMicros(), getMeanMicros(), getStdDevMicros());
    }
  }

  /** Performance thresholds for common operations. */
  public static class Thresholds {
    /** Maximum time for periodic() methods (50Hz = 20ms budget) */
    public static final long PERIODIC_MAX = 20_000_000L; // 20ms

    /** Maximum time for odometry updates (250Hz = 4ms budget) */
    public static final long ODOMETRY_MAX = 4_000_000L; // 4ms

    /** Maximum time for vision processing (30Hz = 33ms budget) */
    public static final long VISION_MAX = 33_000_000L; // 33ms

    /** Maximum time for kinematics calculations */
    public static final long KINEMATICS_MAX = 1_000_000L; // 1ms

    /** Maximum time for state machine updates */
    public static final long STATE_MACHINE_MAX = 500_000L; // 500µs

    /** Maximum time for PID calculations */
    public static final long PID_MAX = 100_000L; // 100µs
  }
}
