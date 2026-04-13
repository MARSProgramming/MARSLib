package com.marslib.vision;

import java.util.function.Supplier;

/**
 * Configuration record for the MARSVision subsystem.
 *
 * <p>This object encapsulates all filtering, rejection, and estimation parameters, allowing the
 * vision logic to remain generic across different robots and field layouts.
 */
public record VisionConfig(
    Supplier<Double> maxZHeight,
    double fieldLengthMeters,
    double fieldWidthMeters,
    Supplier<Double> fieldMarginMeters,
    Supplier<Double> maxTiltDeg,
    Supplier<Double> maxAngularAccelDegPerSec2,
    Supplier<Double> maxAmbiguity,
    Supplier<Double> tagStdBase,
    Supplier<Double> multiTagStdMultiplier,
    Supplier<Double> angularStdMultiplier,
    Supplier<Double> linearVelocityStdMultiplier,
    Supplier<Double> angularVelocityStdMultiplier,
    Supplier<Double> slamStdDev,
    Supplier<Double> slamAngularStdDev,
    double loopPeriodSecs) {}
