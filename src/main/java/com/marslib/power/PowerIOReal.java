/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.power;

import edu.wpi.first.wpilibj.PowerDistribution;

/** Hardware implementation for reading physical Power Distribution Hub (PDH) data. */
public class PowerIOReal implements PowerIO {
  private final PowerDistribution pdh;

  public PowerIOReal() {
    pdh = new PowerDistribution();
  }

  @Override
  public void updateInputs(PowerIOInputs inputs) {
    inputs.totalCurrentAmps = pdh.getTotalCurrent();
    inputs.voltage = pdh.getVoltage();

    int numChannels = pdh.getNumChannels();
    if (inputs.channelCurrentsAmps.length != numChannels) {
      inputs.channelCurrentsAmps = new double[numChannels];
    }
    for (int i = 0; i < numChannels; i++) {
      inputs.channelCurrentsAmps[i] = pdh.getCurrent(i);
    }
  }
}
