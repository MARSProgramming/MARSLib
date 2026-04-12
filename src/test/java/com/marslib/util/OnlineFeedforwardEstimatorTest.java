package com.marslib.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class OnlineFeedforwardEstimatorTest {

  @Test
  public void testConvergence() {
    double trueKS = 0.5;
    double trueKV = 1.2;
    double trueKA = 0.1;

    OnlineFeedforwardEstimator estimator = new OnlineFeedforwardEstimator("Test", 100, trueKS);

    // Simulate constant acceleration movement
    // V = kS + kV*v + kA*a
    double velocity = 0.0;
    double acceleration = 2.0;
    double dt = 0.02;

    for (int i = 0; i < 50; i++) {
      velocity += acceleration * dt;
      double voltage = trueKS + (trueKV * velocity) + (trueKA * acceleration);
      estimator.addMeasurement(voltage, velocity, acceleration);
    }

    // After 50 frames, it should have converged closely
    assertEquals(trueKV, estimator.getEstimatedKV(), 0.01, "kV should converge");
    assertEquals(trueKA, estimator.getEstimatedKA(), 0.05, "kA should converge");
  }

  @Test
  public void testZeroVelocityRejection() {
    OnlineFeedforwardEstimator estimator = new OnlineFeedforwardEstimator("Test", 100, 0.5);

    // Stalled motor
    estimator.addMeasurement(1.0, 0.0, 0.0);

    assertEquals(0.0, estimator.getEstimatedKV(), "Should not update at zero velocity");
    assertEquals(0.0, estimator.getEstimatedKA(), "Should not update at zero velocity");
  }

  @Test
  public void testCollinearDataHandling() {
    OnlineFeedforwardEstimator estimator = new OnlineFeedforwardEstimator("Test", 100, 0.0);

    // Pure constant velocity (acceleration = 0)
    // This makes the matrix singular (det = 0)
    for (int i = 0; i < 20; i++) {
      estimator.addMeasurement(2.0, 1.0, 0.0);
    }

    // We should not crash, and KV/KA should stay at 0 or last known
    assertEquals(0.0, estimator.getEstimatedKV(), "Should handle singular matrix gracefully");
  }
}
