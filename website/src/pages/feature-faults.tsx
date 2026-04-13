import React, { useEffect } from 'react';
import Layout from '@theme/Layout';

export default function FeatureFaults() {
  return (
    <Layout title="Feature Faults">
      <div className="legacy-mars">
        <div dangerouslySetInnerHTML={{ __html: `



<main class="container" style="padding-top: 100px; padding-bottom: 80px;">
  <div style="text-align: center; margin-bottom: 40px;">
    <a href="/MARSLib/#features" class="back-link">← BACK TO FEATURES</a>
    <h1>Thread-Safe Fault Management</h1>
  </div>
  
  <div class="card" style="background: var(--bg-card); border: 1px solid var(--border); padding: 30px; border-radius: 12px; margin-top: 30px;">
    <h2 style="font-family: 'Orbitron', sans-serif; color: var(--mars-red-light); margin-bottom: 20px; font-size: 1.8rem; margin-top: 0;">Why Fault Management?</h2>
    <p style="margin-bottom: 16px; color: var(--text-secondary); font-size: 1.1rem;">A single disconnected sensor network cable during an intense playoff match can completely blind a robot. The software framework needs an incredibly fast, thread-safe way to detect hardware non-compliance, alert the drive team, and fallback to safe routines.</p>
    
    <h2 style="font-family: 'Orbitron', sans-serif; color: var(--mars-red-light); margin-bottom: 20px; font-size: 1.8rem; margin-top: 40px;">The Implementation</h2>
    <p style="margin-bottom: 16px; color: var(--text-secondary); font-size: 1.1rem;">MARSLib's fault manager uses concurrent Java data structures (like <code>ConcurrentHashMap</code> and Atomic primitives) to track errors asynchronously:</p>
    <ul style="margin-bottom: 20px; padding-left: 20px; color: var(--text-secondary); font-size: 1.1rem;">
      <li style="margin-bottom: 10px;"><strong>CAN Bus Drops:</strong> Hardware IO layers constantly sweep motor controller status signals. If a device misses 3 periodic frames in a row, a critical fault is logged.</li>
      <li style="margin-bottom: 10px;"><strong>Automatic Fallbacks:</strong> If a Vision Coprocessor drops off the network, Odometry ignores vision updates automatically. If a swerve module's absolute encoder fails, we gracefully degrade to relative encoder states.</li>
      <li style="margin-bottom: 10px;"><strong>Driver Feedback:</strong> Active faults dynamically trigger Xbox controller rumble patterns based on fault severity. Simultaneously, our CANdle addressable LED system renders flashing red indicators on the robot so the drive coach is aware hardware is malfunctioning.</li>
    </ul>

    <h2 style="font-family: 'Orbitron', sans-serif; color: var(--mars-red-light); margin-bottom: 20px; font-size: 1.8rem; margin-top: 40px;">Pre-Match Checks</h2>
    <p style="margin-bottom: 16px; color: var(--text-secondary); font-size: 1.1rem;">Before every match, MARSLib offers automatic system checks on the field that sweep all sensors, verifying their current position matches last-known states to prevent catastrophic failure immediately at auto initialization.</p>
  </div>
</main>




` }} />
      </div>
    </Layout>
  );
}
