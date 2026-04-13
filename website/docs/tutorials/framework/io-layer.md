---
sidebar_position: 5
id: io-layer
title: "The IO Layer Pattern"
---

  <p>The most important architectural rule in MARSLib is the **separation of hardware from logic**. We use the <strong><a href="https://github.com/Mechanical-Advantage/AdvantageKit" target="_blank">AdvantageKit</a></strong> IO abstraction pattern to ensure that our robot code is deterministic, testable, and capable of bit-perfect log replay.</p>

  <div class="callout">
    <h4>Architectural Immunity</h4>
    <p>If you call <code>motor.getVelocity()</code> directly in your subsystem, your code depends on physical hardware. You can't run it in sim accurately, you can't test it easily, and your logs won't capture what the motor was "actually" doing versus what your code "saw".</p>
  </div>

  <h2>1. The Interface</h2>
  <p>First, we define an interface that lists all inputs the subsystem needs from the hardware. We use <code>@AutoLog</code> from <strong><a href="https://github.com/Mechanical-Advantage/AdvantageKit" target="_blank">AdvantageKit</a></strong> to automatically generate the logging boilerplate.</p>

  ```java
public interface FlywheelIO {
  @AutoLog
  public static class FlywheelIOInputs {
    public double positionRad = 0.0;
    public double velocityRadPerSec = 0.0;
    public double appliedVolts = 0.0;
    public double currentAmps = 0.0;
  }

  public default void updateInputs(FlywheelIOInputs inputs) {}
  public default void setVoltage(double volts) {}
}
```

  <h2>2. Hardware Implementation</h2>
  <p>The "Real" implementation talks to actual motors. It has no logicÃ¢â‚¬â€it only moves data between the hardware and the Inputs object.</p>

  ```java
public class FlywheelIOTalonFX implements FlywheelIO {
  private final <a href="https://v6.docs.ctr-electronics.com/en/stable/docs/api-reference/api-usage/talonfx.html" target="_blank">TalonFX</a> motor;

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
}
```

  <h2>3. Physics Implementation</h2>
  <p>The "Sim" implementation uses math to calculate how a motor would behave. MARSLib uses <strong><a href="https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/robot-simulation/introduction.html" target="_blank">WPILib Physics Sims</a></strong> (like <code>DCMotorSim</code>) and <strong><a href="https://marsprogramming.github.io/MARSLib/docs/tutorials/framework/simulation">MARSPhysicsWorld</a></strong> for high-fidelity physics.</p>

  ```java
public class FlywheelIOSim implements FlywheelIO {
  private final DCMotorSim sim = new DCMotorSim(DCMotor.getFalcon500(1), 1.0, 0.01);

  @Override
  public void updateInputs(FlywheelIOInputs inputs) {
    sim.update(0.020);
    inputs.positionRad = sim.getAngularPositionRad();
    inputs.velocityRadPerSec = sim.getAngularVelocityRadPerSec();
  }
}
```

  <h2>4. Dependency Injection</h2>
  <p>In <code>RobotContainer</code>, we decide which version to use based on the robot mode. The Subsystem itself only ever sees the <code>FlywheelIO</code> interface!</p>

  ```java
FlywheelIO io;
if (isReal) {
  io = new FlywheelIOTalonFX(1);
} else {
  io = new FlywheelIOSim();
}
subsystem = new FlywheelSubsystem(io);
```

  <div class="callout" >
    <h4 >Student Pro-Tip</h4>
    <p>Always keep your IO implementations "dumb". All logic belongs in the Subsystem and Commands. If you add math to your hardware layer, you've broken the abstraction.</p>
  </div>

  <br /><hr /><br />
  <h2>📖 Further Reading & External Resources</h2>
  <ul>
    <li><a href="https://www.youtube.com/watch?v=yYn9NqJ6kXU">FRC Log Replay and Simulation</a> - Mechanical Advantage's definitive 2024 Championship presentation on the why behind the IO-Layer abstraction.</li>
    <li><a href="https://github.com/Mechanical-Advantage/AdvantageKit">AdvantageKit Architecture</a> - The core repository governing deterministic replay loops on the RIO.</li>
    <li><a href="https://github.com/Mechanical-Advantage/AdvantageScope">AdvantageScope Documentation</a> - Visualizing 3D logs natively in real-time.</li>
  </ul>
