import React, { useEffect } from 'react';
import Layout from '@theme/Layout';

export default function TutorialStateMachines() {
  return (
    <Layout title="Tutorial State Machines">
      <div className="legacy-mars">
        <div dangerouslySetInnerHTML={{ __html: `



<main class="container" style="padding-top: 100px; padding-bottom: 80px;">
  <div style="text-align: center; margin-bottom: 40px;">
    <a href="/MARSLib/tutorials" class="back-link">← BACK TO TUTORIALS</a>
    <h1>State Machine Logic</h1>
  </div>

  <p>Coordinating multiple mechanisms (like an intake, a pivot, and a shooter) is incredibly complex. If you use simple if-statements, you'll eventually end up with "spaghetti code" where the robot crashes into itself. MARSLib uses <code>MARSStateMachine</code> within the overarching <code>MARSSuperstructure</code> to manage these transitions safely.</p>

  <h2>1. Defining Your States</h2>
  <p>Start by creating an enum that represents every possible state of your superstructure for the 2026 game.</p>

  <pre><code class="language-java">public enum SuperstructureState {
  STOW,
  INTAKING_FLOOR,
  LOADED_FUEL_BALL,
  HUB_SCORE,
  UNJAMMING
}</code></pre>

  <h2>2. The Transition Table</h2>
  <p>In your Subsystem's constructor, you define which transitions are legal. This prevents the robot from trying to shoot into the Hub if it hasn't successfully acquired a Fuel Ball yet!</p>

  <pre><code class="language-java">stateMachine = new MARSStateMachine&lt;&gt;("Superstructure", SuperstructureState.class, SuperstructureState.STOW);

// Setup the table
stateMachine.addTransition(STOW, INTAKING_FLOOR);
stateMachine.addTransition(INTAKING_FLOOR, LOADED_FUEL_BALL);
stateMachine.addBidirectional(LOADED_FUEL_BALL, HUB_SCORE);
stateMachine.addWildcardTo(STOW); // Can always go back to STOW in an emergency</code></pre>

  <div class="callout callout-warning">
    <h4>Pre-Match Diagnostics Mandatory</h4>
    <p>Before ever running a physical state machine transition on the field, the involved subsystems must pass their <code>SystemTestable</code> pre-match assertions. Ensure the hardware actually works via <code>MARSFaultManager</code> sweeps to prevent hardware collisions.</p>
  </div>

  <h2>3. Entry & Exit Actions</h2>
  <p>Often, you want something to happen exactly once when a state changes. For example, when entering <code>INTAKING_FLOOR</code>, you want to deploy the pivot.</p>

  <pre><code class="language-java">stateMachine.setEntryAction(INTAKING_FLOOR, () -> {
  pivot.setGoal(IntakeConstants.DEPLOY_POS);
  intakeWheels.run(1.0);
});

stateMachine.setExitAction(INTAKING_FLOOR, () -> {
  pivot.setGoal(IntakeConstants.STOW_POS);
});</code></pre>

  <div id="sm-sim" style="background: #050505; border: 1px solid var(--border); border-radius: 12px; margin: 30px 0; overflow: hidden; box-shadow: inset 0 0 40px rgba(0,0,0,0.8);">
    <div style="font-family: 'Orbitron'; font-size: 0.8rem; color: var(--ai-cyan); letter-spacing: 1px; padding: 16px 20px; border-bottom: 1px solid var(--border); background: rgba(255,255,255,0.03); text-transform: uppercase;">Superstructure Collision Sequencer</div>
    
    <div style="display: flex; gap: 10px; padding: 16px 20px; flex-wrap: wrap;">
       <button id="smStow" style="flex:1; background:var(--ai-cyan); color:#000; border:none; padding:10px; border-radius:6px; cursor:pointer; font-family:'Orbitron'; font-weight:700;">STOW</button>
       <button id="smIntake" style="flex:1; background:#222; color:#fff; border:1px solid #444; padding:10px; border-radius:6px; cursor:pointer; font-family:'Orbitron'; transition:0.2s;">INTAKING</button>
       <button id="smScore" style="flex:1; background:#222; color:#fff; border:1px solid #444; padding:10px; border-radius:6px; cursor:pointer; font-family:'Orbitron'; transition:0.2s;">HUB SCORE</button>
    </div>
    
    <div style="padding: 0 20px 20px 20px; display: flex; gap: 20px; align-items: stretch; flex-wrap: wrap;">
      <!-- Active Logic Log -->
      <div style="flex: 1; min-width: 200px; font-family:'JetBrains Mono'; font-size:11px; background:#111; border: 1px solid #333; padding:15px; border-radius:6px; height:200px; display: flex; flex-direction: column; justify-content: space-between;">
        <div>
            <div style="color:var(--text-secondary); margin-bottom: 5px;">SUPERSTRUCTURE TARGET: <span id="smTargetLbl" style="color:#fff;">STOW</span></div>
            <div style="color:var(--text-secondary);">ACTIVE CONSTRAINTS:</div>
            <ul id="smLogList" style="list-style-type: none; padding: 0; color: #ffb300; margin-top: 5px; height: 90px;">
            </ul>
        </div>
        <div style="color:#4caf50; font-weight:700;" id="smFinalLbl">✔ MECHANISMS ALIGNED</div>
      </div>
      
      <!-- Mechanism Canvas -->
      <div style="flex: 0 0 250px; position:relative; background:#111; height: 200px; border-radius:6px; border:1px solid #333;">
          <canvas id="smCanvas" width="250" height="200" style="width:100%; height:100%; display:block;"></canvas>
      </div>
    </div>
  </div>

  <h2>4. Live Mermaid Visualization</h2>
  <p>One of the most powerful features of <code>MARSStateMachine</code> is that it automatically generates a <strong>Mermaid.js flowchart</strong>. This graph is sent over telemetry and can be viewed live in <strong>AdvantageScope</strong>.</p>

  <div class="callout">
    <h4>Visual Debugging</h4>
    <p>Open the "Mermaid" tab in AdvantageScope and drag the <code>Superstructure/StateMachine/MermaidGraph</code> field into it. You will see a live diagram where the current state is highlighted in glowing green.</p>
  </div>
</main>







` }} />
      </div>
    </Layout>
  );
}
