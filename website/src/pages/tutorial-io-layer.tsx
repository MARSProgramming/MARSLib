import React, { useEffect } from 'react';
import Layout from '@theme/Layout';

export default function TutorialIoLayer() {
  return (
    <Layout title="Tutorial Io Layer">
      <div className="legacy-mars">
        <div dangerouslySetInnerHTML={{ __html: `



<main class="container" style="padding-top: 100px; padding-bottom: 80px;">
  <div style="text-align: center; margin-bottom: 40px;">
    <a href="/MARSLib/tutorials" class="back-link">← BACK TO TUTORIALS</a>
    <h1>The IO Layer Pattern</h1>
  </div>

  <p>The most important architectural rule in MARSLib is the **separation of hardware from logic**. We use the AdvantageKit IO abstraction pattern to ensure that our robot code is deterministic, testable, and capable of bit-perfect log replay.</p>

  <div class="callout">
    <h4>Architectural Immunity</h4>
    <p>If you call <code>motor.getVelocity()</code> directly in your subsystem, your code depends on physical hardware. You can't run it in sim accurately, you can't test it easily, and your logs won't capture what the motor was "actually" doing versus what your code "saw".</p>
  </div>

  <h2>1. The Interface</h2>
  <p>First, we define an interface that lists all inputs the subsystem needs from the hardware. We use <code>@AutoLog</code> to automatically generate the logging boilerplate.</p>

  <pre><code class="language-java">public interface FlywheelIO {
  @AutoLog
  public static class FlywheelIOInputs {
    public double positionRad = 0.0;
    public double velocityRadPerSec = 0.0;
    public double appliedVolts = 0.0;
    public double currentAmps = 0.0;
  }

  public default void updateInputs(FlywheelIOInputs inputs) {}
  public default void setVoltage(double volts) {}
}</code></pre>

  <h2>2. Hardware Implementation</h2>
  <p>The "Real" implementation talks to actual motors. It has no logic—it only moves data between the hardware and the Inputs object.</p>

  <pre><code class="language-java">public class FlywheelIOTalonFX implements FlywheelIO {
  private final TalonFX motor;

  public FlywheelIOTalonFX(int id) {
    motor = new TalonFX(id);
  }

  @Override
  public void updateInputs(FlywheelIOInputs inputs) {
    inputs.positionRad = motor.getPosition().getValueAsDouble();
    inputs.velocityRadPerSec = motor.getVelocity().getValueAsDouble();
  }

  @Override
  public void setVoltage(double volts) {
    motor.setControl(new VoltageOut(volts));
  }
}</code></pre>

  <h2>3. Physics Implementation</h2>
  <p>The "Sim" implementation uses math to calculate how a motor would behave. MARSLib uses <code>DCMotorSim</code> and <code>dyn4j</code> for high-fidelity physics.</p>

  <pre><code class="language-java">public class FlywheelIOSim implements FlywheelIO {
  private final DCMotorSim sim = new DCMotorSim(DCMotor.getFalcon500(1), 1.0, 0.01);

  @Override
  public void updateInputs(FlywheelIOInputs inputs) {
    sim.update(0.020);
    inputs.positionRad = sim.getAngularPositionRad();
    inputs.velocityRadPerSec = sim.getAngularVelocityRadPerSec();
  }
}</code></pre>

  <h2>4. Dependency Injection</h2>
  <p>In <code>RobotContainer</code>, we decide which version to use based on the robot mode. The Subsystem itself only ever sees the <code>FlywheelIO</code> interface!</p>

  <pre><code class="language-java">FlywheelIO io;
if (isReal) {
  io = new FlywheelIOTalonFX(1);
} else {
  io = new FlywheelIOSim();
}
subsystem = new FlywheelSubsystem(io);</code></pre>

  <div class="callout" style="border-left-color: var(--ai-cyan); background: rgba(41, 182, 246, 0.05);">
    <h4 style="color: var(--ai-cyan);">Student Pro-Tip</h4>
    <p>Always keep your IO implementations "dumb". All logic belongs in the Subsystem and Commands. If you add math to your hardware layer, you've broken the abstraction.</p>
  </div>
</main>






` }} />
      </div>
    </Layout>
  );
}
