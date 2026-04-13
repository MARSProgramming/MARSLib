---
sidebar_position: 2
id: hardware
title: "2. The Hardware Paradigm"
---

<main className="container">
  <div>
    <h1>The Hardware Paradigm</h1>
  </div>

  <p>To program a physical FRC mechanism effectively, you must understand the constraints of the electrical signals traveling exactly underneath your code. In FRC, code doesn't just run; it actively bridges power systems and data packets.</p>

  <h2>1. The RoboRIO (The Brain)</h2>
  <p>The <strong><a href="https://www.ni.com/en-us/shop/model/roborio-2-0.html" target="_blank">NI RoboRIO 2.0</a></strong> is an embedded Linux computer. This is where your compiled Java code actually lives and executes. Every 20 milliseconds, the RoboRIO evaluates your code, parses sensor data, and issues torque commands outward.</p>

  <h2>2. The FRC Radio (The Network)</h2>
  <p>The RoboRIO does not have native WiFi. A separate module—the Radio (like the <a href="https://vivid-hosting.net/product/vh-109-radio/" target="_blank">Vivid-Hosting VH-109</a> or <a href="https://docs.wpilib.org/en/stable/docs/zero-to-robot/step-2/radio-configuration.html" target="_blank">OpenMesh</a>)—must be physically ethernet-connected to the RIO. Your laptop talks securely to the Radio, which then pipelines the connection.</p>

  <div className="callout callout-warning">
    <h4>FMS vs Pit Strategy</h4>
    <p>When you are in the Pit or at home, you configure the Radio to emit a standard 5GHz WiFi signal or you plug an Ethernet/USB-B cord directly into the RoboRIO to deploy code.</p>
    <p>At official competitions, you must use the <strong>Radio Kiosk</strong>. This physically rewrites your Radio's firmware to bind directly to the official <strong>Field Management System (FMS)</strong>. Once hooked to FMS, your laptop connects via Ethernet to a Driver Station pedestal on the field boundary, ensuring extreme security.</p>
  </div>

  <h2>3. The CAN Bus (The Nervous System)</h2>
  <p>The RoboRIO does not directly spin motors. Instead, it "talks" to them using a protocol called <strong><a href="https://docs.wpilib.org/en/stable/docs/software/can-devices/index.html" target="_blank">CAN</a></strong> (Controller Area Network).</p>
  
  <ul>
    <li>The CAN Bus is a literal yellow and green wire daisy-chained sequentially through every motor controller.</li>
    <li>Because data flows serially, every motor requires a unique <strong>CAN ID</strong> (a number from 1 to 60) so the RoboRIO can target exactly who to command.</li>
    <li><strong>The CANivore</strong>: MARSLib utilizes CTRE hardware heavily. Standard RoboRIO CAN processing becomes bogged down around 50% utilization. We route all drivetrain components into a separate USB <strong><a href="https://v6.docs.ctr-electronics.com/en/stable/docs/canivore/canivore-intro.html" target="_blank">CANivore</a></strong> card, isolating high-bandwidth torque streams and speeding up loop times substantially.</li>
  </ul>

  <h2>4. Power Shedding & Brownouts</h2>
  <p>An FRC battery supplies ~12V of DC electricity. However, when 14 different high-torque motors try to pull max acceleration simultaneously, the physical laws of electric load cause the battery voltage to momentarily crash down toward 6.0V.</p>
  <p>If the voltage drops below 6.3V, the RoboRIO forcefully severs all motor connection to prevent the main CPU from dying. This is known as a <strong><a href="https://docs.wpilib.org/en/stable/docs/software/roborio-info/roborio-brownouts.html" target="_blank">Brownout</a></strong>. In MARSLib, we must dynamically shed power and strictly enforce motor current caps to avoid stalling the battery.</p>


  <br /><hr /><br />
  <h2>📖 Further Reading & External Resources</h2>
  <ul>
    <li><a href="https://docs.wpilib.org/en/stable/docs/zero-to-robot/introduction.html">WPILib "Zero to Robot" Guide</a> - The official foundational pipeline for FRC control systems.</li>
    <li><a href="https://www.youtube.com/watch?v=8319J1BEHwM">FRC 0 to Auto Youtube Series</a> - Outstanding video tutorials exploring command-based programming for novices.</li>
    <li><a href="https://docs.wpilib.org/en/stable/docs/software/commandbased/index.html">WPILib Command-Based Documentation</a> - Deep dive into Subsystem and Command scheduling architecture.</li>
  </ul>
  </main>
