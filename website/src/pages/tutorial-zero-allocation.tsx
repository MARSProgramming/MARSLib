import React, { useEffect } from 'react';
import Layout from '@theme/Layout';

export default function TutorialZeroAllocation() {
  return (
    <Layout title="Tutorial Zero Allocation">
      <div className="legacy-mars">
        <div dangerouslySetInnerHTML={{ __html: `



<main class="container" style="padding-top: 100px; padding-bottom: 80px;">
  <div style="text-align: center; margin-bottom: 40px;">
    <a href="/MARSLib/tutorials" class="back-link">← BACK TO TUTORIALS</a>
    <h1>Zero-Allocation Engineering</h1>
  </div>

  <p>The single greatest threat to determinism in a Java-based FRC robot is the JVM's Garbage Collector (GC). Every time you allocate memory inside your 50Hz or 250Hz loops, memory is allocated on the heap. Eventually, the JVM pauses the entire robot to clean it up.</p>

  <div id="gc-sim" style="background: #050505; border: 1px solid var(--border); border-radius: 12px; margin: 30px 0; display: flex; flex-direction: column; overflow: hidden; box-shadow: inset 0 0 40px rgba(0,0,0,0.8);">
    <div style="display: flex; gap: 20px; padding: 16px 20px; background: rgba(255,255,255,0.03); border-bottom: 1px solid var(--border); align-items: center; justify-content: space-between; flex-wrap: wrap;">
      <div style="display: flex; gap: 10px; width: 100%;">
        <button id="btnStd" style="flex:1; background:#222; color:#fff; border:1px solid #444; padding:12px; border-radius:6px; cursor:pointer; font-family:'Orbitron'; font-weight:700; transition:0.2s;">STANDARD FRC (Allocating)</button>
        <button id="btnMars" style="flex:1; background:var(--ai-cyan); color:#000; border:none; padding:12px; border-radius:6px; cursor:pointer; font-family:'Orbitron'; font-weight:700; transition:0.2s;">MARSLIB (Zero-Allocation)</button>
      </div>
    </div>
    <div style="display: flex; padding: 20px; gap: 20px; align-items: stretch; height: 400px;">
      <div style="flex: 1; position: relative;">
        <canvas id="loopCanvas" style="width: 100%; height: 100%; display: block; border: 1px solid #333; background: #111; border-radius: 6px;"></canvas>
        <div style="position:absolute; top:10px; left:15px; font-family:'JetBrains Mono'; font-size:11px; color:#e8e8e8;">
          LOOP TIME: <span id="loopTxt" style="color:var(--ai-cyan)">20.0ms</span>
        </div>
      </div>
      <div style="flex: 0 0 100px; display: flex; flex-direction: column; position: relative;">
        <canvas id="heapCanvas" style=" flex: 1; display: block; border: 1px solid #333; background: #111; border-radius: 6px;"></canvas>
        <div style="text-align: center; font-family:'JetBrains Mono'; font-size:10px; color:#999; margin-top: 5px;">JVM HEAP</div>
      </div>
    </div>
  </div>

  <h2>1. The 'Ephemeral Struct' Pattern</h2>
  <p>To eliminate GC spikes, MARSLib enforces a strict zero-allocation architecture. We achieve this using pre-allocated <strong>Mutable Proxy References</strong>.</p>

  <pre><code class="language-java">// BAD: Allocates 250 objects per second!
public void periodic() {
    Pose2d currentPose = new Pose2d(x, y, new Rotation2d(theta));
    updatePose(currentPose);
}

// GOOD: Pre-allocated Ephemeral Structs mutated in place
private final Pose2d mutablePose = new Pose2d();
private final Rotation2d mutableRotation = new Rotation2d();

public void periodic() {
    mutableRotation.setRadians(theta);
    mutablePose.set(x, y, mutableRotation);
    updatePose(mutablePose);
}</code></pre>

  <h2>2. The Pointer Danger</h2>
  <p>When you pass a mutable proxy reference into another class, you are passing the <em>memory pointer</em>. Overwriting it next tick will affect any class holding that pointer.</p>

  <div class="callout callout-warning">
    <h4>The Clone Rule</h4>
    <p>If a downstream class requires a persistent snapshot or history of data, it must explicitly <strong>clone</strong> the proxy object. Assume any struct reference is poisoned once the function returns.</p>
  </div>

  <h2>3. Static Array Caching</h2>
  <p>In Swerve Kinematics, we frequently convert <code>ChassisSpeeds</code> into arrays. MARSLib uses static pre-allocated array caches, mutating elements in place to save thousands of heap allocations per minute.</p>
</main>







` }} />
      </div>
    </Layout>
  );
}
