import React, { useEffect } from 'react';
import Layout from '@theme/Layout';

export default function ControllerMappings() {
  return (
    <Layout title="Controller_Mappings">
      <div className="legacy-mars">
        <div dangerouslySetInnerHTML={{ __html: `



<main class="container" style="padding-top: 100px; padding-bottom: 80px;">
  <div style="text-align: center; margin-bottom: 60px;">
    <h1>Controller Mappings</h1>
    <p class="subtitle">This document is automatically generated during compilation from <code>RobotBindings.java</code>.</p>
  </div>

  <section style="margin-bottom: 60px;">
    <h2>🎮 Pilot (Driver) - Controller 0</h2>
    <p class="subtitle">Primary drive controls and high-level macro sequences.</p>
    <table>
      <thead>
        <tr><th>Controller Input</th><th>Mapped Action</th></tr>
      </thead>
      <tbody>
        <tr><td><kbd>Left Joystick</kbd></td><td><b>Holonomic Translation (X/Y)</b></td></tr>
        <tr><td><kbd>Right Joystick</kbd></td><td><b>Holonomic Rotation</b></td></tr>
        <tr><td><kbd>LeftTrigger</kbd></td><td><b>Run Intake</b></td></tr>
        <tr><td><kbd>RightTrigger</kbd></td><td><b>Aim And Shoot On Move</b></td></tr>
        <tr><td><kbd>B</kbd></td><td><b>Stationary Shoot</b></td></tr>
        <tr><td><kbd>LeftBumper</kbd></td><td><b>Unjam</b></td></tr>
        <tr><td><kbd>RightBumper</kbd></td><td><b>Aim And Shuttle</b></td></tr>
        <tr><td><kbd>DPad_Right</kbd></td><td><b>Deploy Intake Only</b></td></tr>
        <tr><td><kbd>DPad_Left</kbd></td><td><b>Retract Intake</b></td></tr>
        <tr><td><kbd>A</kbd></td><td><b>Slamtake</b></td></tr>
        <tr><td><kbd>Y</kbd></td><td><b>Align To Climb Position</b></td></tr>
        <tr><td><kbd>X</kbd></td><td><b>Final Climb Lineup</b></td></tr>
        <tr><td><kbd>DPad_Up</kbd></td><td><b>Manual Climber Up</b></td></tr>
        <tr><td><kbd>DPad_Down</kbd></td><td><b>Manual Climber Down</b></td></tr>
        <tr><td><kbd>Start</kbd></td><td><b>Diagnostic Check</b></td></tr>
      </tbody>
    </table>
  </section>

  <section style="margin-bottom: 60px;">
    <h2>🕹️ CoPilot (Operator) - Controller 1</h2>
    <p class="subtitle">Manual overrides, sub-mechanism control, and fault resets.</p>
    <table>
      <thead>
        <tr><th>Controller Input</th><th>Mapped Action</th></tr>
      </thead>
      <tbody>
        <tr><td><kbd>LeftTrigger</kbd></td><td><b>Manual Feed</b></td></tr>
        <tr><td><kbd>RightTrigger</kbd></td><td><b>Fixed Score (Hub)</b></td></tr>
        <tr><td><kbd>RightBumper</kbd></td><td><b>Fixed Score (Ladder)</b></td></tr>
        <tr><td><kbd>LeftBumper</kbd></td><td><b>Cowl Home</b></td></tr>
        <tr><td><kbd>DPad_Down</kbd></td><td><b>Climber Reverse</b></td></tr>
        <tr><td><kbd>X</kbd></td><td><b>Drivetrain Stop</b></td></tr>
      </tbody>
    </table>
  </section>

  <div class="note">
    <strong style="color: var(--ai-cyan);">Note:</strong> All automated scoring sequences natively return the superstructure to the safe <code>STOWED</code> state immediately upon release of the binding.
  </div>
</main>




` }} />
      </div>
    </Layout>
  );
}
