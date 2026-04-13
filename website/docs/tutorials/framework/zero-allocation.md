---
sidebar_position: 8
id: zero-allocation
title: "Strict Zero-Allocation"
---

import ZeroAllocationSim from '@site/src/components/ZeroAllocationSim';

<main class="container" >
  <div >

    <h1>Strict Zero-Allocation</h1>
  </div>

  <p>In high-frequency robotics (250Hz+), the #1 cause of sudden, non-deterministic jitter is the Java Garbage Collector (GC). Every time you use the <code>new</code> keyword inside a loop, you are creating a tiny bit of "trash" that the CPU eventually has to stop everything and clean up.</p>

  <div class="callout callout-warning">
      <h4>The 20ms Deadline</h4>
      <p>If the GC triggers during a critical autonomous spline, your 20ms control loop might stretch to 40ms. This causes the robot's pose estimator to miss reality, leading to a missed shot or a collision.</p>
  </div>

  <h2>1. The Ephemeral Proxy Pattern</h2>
  <p>MARSLib enforces zero-allocation using <strong>Ephemeral Proxies</strong>. Instead of creating new <code>Translation2d</code> objects every loop for math, we reuse static, pre-allocated memory buffers.</p>

  <p>Try clicking <strong>ALLOCATE OBJECT</strong> in the simulator below to see how heavy object creation "spikes" the loop time and forces the GC to halt the robot!</p>

  <ZeroAllocationSim />

  <h2>2. MARSLib Constraints</h2>
  <p>The <code>marslib-audit</code> skill strictly forbids the following patterns in "Hot Paths" (periodic loops):</p>
  <ul>
      <li><strong>No <code>new</code>:</strong> Use <code>Inputs</code> structs or static caches.</li>
      <li><strong>No Lambdas:</strong> Use pre-defined <code>Runnable</code> or <code>Consumer</code> fields.</li>
      <li><strong>No String Building:</strong> Use the <code>Logger.recordOutput</code> raw numeric overloads.</li>
  </ul>

  <br /><hr /><br />
  <h2>📖 Further Reading & External Resources</h2>
  <ul>
    <li><a href="https://github.com/Mechanical-Advantage/AdvantageKit/blob/main/docs/RECORDING-AND-REPLAY.md">AdvantageKit: Performance & Benchmarking</a> - Technical specifications for zero-allocation logging.</li>
    <li><a href="https://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/">Java Garbage Collection Tuning</a> - Understanding the impact of object allocation on real-time systems.</li>
  </ul>
  </main>
