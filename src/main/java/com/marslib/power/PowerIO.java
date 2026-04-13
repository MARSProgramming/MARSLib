/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.power;

import org.littletonrobotics.junction.AutoLog;

/**
 * Hardware interface for power management reading. Provides inputs such as system voltage and total
 * current draw.
 */
public interface PowerIO {
  @AutoLog
  public static class PowerIOInputs {
    public double totalCurrentAmps = 0.0;
    public double voltage = 12.0;
    public double[] channelCurrentsAmps = new double[0];
    public double canBusUtilization = 0.0;
    public boolean isBrownedOut = false;
  }

  public default void updateInputs(PowerIOInputs inputs) {}
}
