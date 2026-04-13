import React, { useEffect } from 'react';
import Layout from '@theme/Layout';

export default function TutorialTesting() {
  return (
    <Layout title="Tutorial Testing">
      <div className="legacy-mars">
        <div dangerouslySetInnerHTML={{ __html: `



<main class="container" style="padding-top: 100px; padding-bottom: 80px;">
  <div style="text-align: center; margin-bottom: 40px;">
    <a href="/MARSLib/tutorials" class="back-link">← BACK TO TUTORIALS</a>
    <h1>Physics-Backed Testing</h1>
  </div>

  <p>Most FRC unit tests simply "mock" the hardware. They verify that a motor was told to run, but they never verify if the robot actually moves. In MARSLib, we run <strong>Physics-Backed Unit Tests</strong> that integrate tightly with Dyn4j, ensuring that our software actually solves real-world physics problems during Continuous Integration.</p>

  <h2>1. The MARSTestHarness</h2>
  <p>To run physics-backed tests, your JUnit 5 test class must initialize the <code>MARSTestHarness</code>. This harness overrides the internal HAL clock and resets static singletons, guaranteeing that every test starts with a clean slate.</p>

  <pre><code class="language-java">@BeforeEach
public void setup() {
    MARSTestHarness.setupTestEnvironment();
    // Initialize subsystems...
}</code></pre>

  <h2>2. Advancing Time</h2>
  <p>Because tests run instantly on your CPU, you must explicitly command the simulation clock to tick forward to watch physics happen.</p>

  <pre><code class="language-java">// Command the elevator to position 1.5 meters
elevator.setGoal(1.5);

// Tick the physics world forward by 2.0 seconds
for (int i = 0; i &lt; 100; i++) {
    elevator.periodic();
    MARSPhysicsWorld.getInstance().update(0.02);
}

// Assert the physical rigibody actually reached the goal
assertEquals(1.5, elevator.getPosition().in(Meters), 0.05);</code></pre>

  <div class="callout callout-warning">
    <h4>Beware the 20ms Loop</h4>
    <p>Never advance the clock by more than <code>0.02</code> seconds (20ms) at a time. The PID controllers assume a 50Hz execution cycle. Bypassing this will cause your integrators to explode.</p>
  </div>

  <h2>3. Testing Autonomous Paths</h2>
  <p>Because we have <code>IOSim</code> and Dyn4j, you can actually test a full PathPlanner macro in JUnit!</p>

  <pre><code class="language-java">Command autoRoutine = AutoBuilder.buildAuto("Four Fuel Ball Auto");
autoRoutine.initialize();

// Run the auto for 15 seconds
for (int i = 0; i &lt; 750; i++) {
    autoRoutine.execute();
    swerve.periodic();
    MARSPhysicsWorld.getInstance().update(0.02);
}

assertTrue(autoRoutine.isFinished());
// Assert that we scored exactly 4 times in the physics engine
assertEquals(4, GameField.getScoredHubCount());</code></pre>

  <h2>4. CI/CD Validation</h2>
  <p>Every time you push to GitHub, a GitHub Actions workflow executes <code>./gradlew test</code>. If a math commit accidentally breaks the chassis kinematics, the trajectory tests will fail, and the PR will be blocked. This is true Einstein-level safety.</p>
</main>






` }} />
      </div>
    </Layout>
  );
}
