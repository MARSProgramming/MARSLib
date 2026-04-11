package com.marslib.util;

import org.littletonrobotics.junction.Logger;

/**
 * A Zero-GC, realtime Feedforward extractor that continuously monitors mechanism telemetry
 * (Voltage, Velocity, Acceleration) to compute empirical kV and kA values dynamically.
 *
 * <p>It uses a sliding-window Ordinary Least Squares (OLS) regression algorithm on a 2-variable
 * system (Velocity & Acceleration) against Effective Voltage (Voltage - kS). By operating strictly
 * over periods of contiguous movement (vel > threshold), it avoids static friction nonlinearities.
 *
 * <p>Students: You can use this to perfectly dial in your Feedforward models without running a
 * formal SysId sequence.
 */
public class OnlineFeedforwardEstimator {

  private final String name;
  private final int windowSize;
  private final double ksVolts;

  // Ring buffers
  private final double[] vBuffer;
  private final double[] aBuffer;
  private final double[] veffBuffer;

  private int index = 0;
  private int count = 0;

  // Running sums for OLS normal equations
  private double sumVv = 0.0;
  private double sumVa = 0.0;
  private double sumAa = 0.0;
  private double sumVVeff = 0.0;
  private double sumAVeff = 0.0;

  private double currentKV = 0.0;
  private double currentKA = 0.0;

  /**
   * Constructs the Online Estimator.
   *
   * @param name The AdvantageScope logging namespace (e.g. "SwerveDrive" or "Shooter").
   * @param windowSize The number of contiguous 20ms ticks to calculate over (e.g. 500 = 10sec of
   *     data).
   * @param ksVolts Theoretical or empirical kS (static friction voltage) to subtract from the
   *     model.
   */
  public OnlineFeedforwardEstimator(String name, int windowSize, double ksVolts) {
    this.name = name;
    this.windowSize = windowSize;
    this.ksVolts = ksVolts;

    this.vBuffer = new double[windowSize];
    this.aBuffer = new double[windowSize];
    this.veffBuffer = new double[windowSize];
  }

  /**
   * Feeds the live telemetry into the sliding regression window.
   *
   * @param appliedVoltage The total voltage passed to the motor.
   * @param velocity The velocity of the mechanism (m/s or rad/s).
   * @param acceleration The acceleration of the mechanism (m/s^2 or rad/s^2).
   */
  public void addMeasurement(double appliedVoltage, double velocity, double acceleration) {
    // We only accept data where the mechanism is actively moving past the noise floor
    if (Math.abs(velocity) < 0.1) {
      return;
    }

    // Effective voltage isolates the dynamic components (kV, kA)
    double vEff = appliedVoltage - (ksVolts * Math.signum(velocity));

    // If buffer is full, remove the oldest sample from the sums
    if (count == windowSize) {
      double oldV = vBuffer[index];
      double oldA = aBuffer[index];
      double oldVeff = veffBuffer[index];

      sumVv -= (oldV * oldV);
      sumVa -= (oldV * oldA);
      sumAa -= (oldA * oldA);
      sumVVeff -= (oldV * oldVeff);
      sumAVeff -= (oldA * oldVeff);
    } else {
      count++;
    }

    // Insert new sample
    vBuffer[index] = velocity;
    aBuffer[index] = acceleration;
    veffBuffer[index] = vEff;

    sumVv += (velocity * velocity);
    sumVa += (velocity * acceleration);
    sumAa += (acceleration * acceleration);
    sumVVeff += (velocity * vEff);
    sumAVeff += (acceleration * vEff);

    index = (index + 1) % windowSize;

    // Recalculate OLS
    solveOLS();
  }

  private void solveOLS() {
    if (count < 10) return; // Need statistically significant samples

    // Solve the 2x2 normal equations using Kramer's rule
    double det = (sumVv * sumAa) - (sumVa * sumVa);

    // Protect against singular/collinear matrices (e.g. constant acceleration of 0)
    if (Math.abs(det) > 1e-6) {
      currentKV = ((sumAa * sumVVeff) - (sumVa * sumAVeff)) / det;
      currentKA = ((sumVv * sumAVeff) - (sumVa * sumVVeff)) / det;
    }

    Logger.recordOutput(name + "/AutoTune/Estimated_kV", currentKV);
    Logger.recordOutput(name + "/AutoTune/Estimated_kA", currentKA);
  }

  /** Retrieves the latest fully-regressed Velocity Feedforward constant. */
  public double getEstimatedKV() {
    return currentKV;
  }

  /** Retrieves the latest fully-regressed Acceleration Feedforward constant. */
  public double getEstimatedKA() {
    return currentKA;
  }
}
