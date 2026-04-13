/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.hmi;

import com.marslib.faults.MARSFaultManager;
import com.marslib.power.MARSPowerManager;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.constants.PowerConstants;

/** Centralized manager for robot LED logic, translating robot states into visual feedback. */
public class LEDManager extends SubsystemBase {
  private final LEDIO io;
  private final MARSPowerManager powerManager;

  public LEDManager(LEDIO io, MARSPowerManager powerManager) {
    this.io = io;
    this.powerManager = powerManager;
  }

  @Override
  public void periodic() {
    if (MARSFaultManager.hasActiveCriticalFaults()) {
      io.setCriticalFaultFlash();
    } else if (powerManager.getVoltage() < PowerConstants.WARNING_VOLTAGE) {
      io.setLoadSheddingColors();
    } else {
      io.setDefaultColors();
    }

    io.update();
  }
}
