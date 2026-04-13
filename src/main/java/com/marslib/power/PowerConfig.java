package com.marslib.power;

/**
 * Configuration record for the MARSPowerManager.
 *
 * @param nominalVoltage The voltage at which load shedding begins (e.g., 10.0V).
 * @param warningVoltage The threshold for a low-voltage warning alert (e.g., 8.0V).
 * @param criticalVoltage The threshold for a critical-voltage alert and floor of shedding (e.g.,
 *     7.0V).
 */
public record PowerConfig(double nominalVoltage, double warningVoltage, double criticalVoltage) {}
