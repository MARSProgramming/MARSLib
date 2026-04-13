---
sidebar_position: 3
id: telemetry
title: "Telemetry Mastery"
---





<main className="container" >
  <div >

    <h1>Telemetry Mastery</h1>
  </div>

  <p>Driving blindly is a thing of the past. In MARSLib, we use Mechanical Advantage's <strong><a href="https://github.com/Mechanical-Advantage/AdvantageKit" target="_blank">AdvantageKit</a></strong>, which guarantees a structurally clean decoupling of hardware states, ensuring deterministic Replay of any past FRC match. All of this telemetry data is then visualized dynamically in <strong><a href="https://github.com/Mechanical-Advantage/AdvantageScope" target="_blank">AdvantageScope</a></strong>.</p>

  <h2>1. AdvantageKit Basics</h2>
  <p>Every real hardware output (e.g. TalonFX ticks, Pigeon2 yaw, or CANdle states) must be logged through an `IO` abstraction layer using an `AutoLogged` object.</p>

  <p>When you add a variable like <code>public double motorCurrentAmps = 0.0;</code> inside an IO struct, AdvantageKit immediately picks it up and pushes it to NetworkTables dynamically. The `Robot.java` loop uses this logged struct identically, whether running in physical competition or in Replay mode.</p>

  <h2>2. AdvantageScope vs. Shuffleboard</h2>
  <p>While MARSLib fundamentally relies on <strong><a href="https://github.com/Mechanical-Advantage/AdvantageScope" target="_blank">AdvantageScope</a></strong> for sophisticated 3D visualizations, log replay, and time-series graphing, we still utilize <strong><a href="https://docs.wpilib.org/en/stable/docs/software/dashboards/shuffleboard/index.html" target="_blank">Shuffleboard</a></strong> for certain live operations.</p>
  <p>Shuffleboard is typically reserved for simple boolean toggles (like selecting auto routines from the driver station), sending basic string configurations prior to a match, or when rapid drag-and-drop dashboarding is required without setting up rigid JSON layouts.</p>

  <h3>Installing AdvantageScope</h3>
  <ol >
    <li >Navigate to the official <a href="https://github.com/Mechanical-Advantage/AdvantageScope/releases" target="_blank" >AdvantageScope Releases</a> page.</li>
    <li >Download the installer specifically for your operative system.</li>
    <li >Install and open the application. When on the robot network, set the Data Source to <code>NetworkTables</code>.</li>
  </ol>

  <h2>3. Importing MARS Layout Templates</h2>
  <p>AdvantageScope utilizes <code>layout.json</code> files that arrange tabs specifically to enhance driver feedback. To avoid creating views from scratch every time, import our central template!</p>

  <div className="callout">
    <h4>Swerve &amp; Mechanisms Template</h4>
    <p>Our agentic skills dynamically generate the <code>MARS_Standard_Layout.json</code> layout file for the 2614 layout. You can find this inside the root directory under <code>.agents/layouts/</code>.</p>
  </div>

  <ol >
    <li >In AdvantageScope, go to the upper right Layouts icon and select <strong>Import Layout...</strong></li>
    <li >Locate the <code>layouts</code> folder in the root of the MARSLib directory you cloned earlier.</li>
    <li >Select <code>MARS_Standard_Layout.json</code> to load a unified driver and debugging UI explicitly designed for 2614.</li>
  </ol>

  <h2>4. Replay Mode &amp; Diagnostics</h2>
  <p>Did a mechanism break during a match? No problem. We have a bit-perfect replay of the software logic.</p>

  <ul >
    <li >Download the <code>.wpilog</code> natively from the Robot using the Log Auto-Uploader or AdvantageScope's file downloader.</li>
    <li >Open AdvantageScope locally, change the Source down arrow over to <strong>Log File</strong>, and target your `.wpilog` file.</li>
    <li >You can scrub back and forward using the slider—you'll see exactly what angles the Swerve modules were targeting right as the error occurred.</li>
  </ul>

  <h2>5. Simulation Replay Tuning</h2>
  <p>AdvantageKit provides an unparalleled workflow called <strong>Simulation Replay</strong>. Because all sensor inputs are completely decoupled from control logic, you can execute a recorded log, but inject modified robot logic <em>while the log replays</em>.</p>

  <div className="callout" >
    <h4 >Elite Tuning Workflow</h4>
    <p>Imagine your robot missed several shots because the Vision hallucination rejection filter failed. Instead of guessing parameters on the live robot:</p>
    <ul>
      <li>Download the `.wpilog` from the match.</li>
      <li>Boot MARSLib in <code>SimReplay</code> mode and target the log file.</li>
      <li>Alter your Pose Estimation code locally, rebuild, and watch the exact match play out with your newly tuned vision filters.</li>
    </ul>
  </div>
  <p>This approach allows engineers to endlessly tweak feedforward loops, vision confidence tuning, and superstructure kinematic logic using a single match's data until it's perfected—without ever turning on the physical robot.</p>

  <br /><hr /><br />
  <h2>📖 Further Reading & External Resources</h2>
  <ul>
    <li><a href="https://www.youtube.com/watch?v=yYn9NqJ6kXU">FRC Log Replay and Simulation</a> - Mechanical Advantage's definitive 2024 Championship presentation on the why behind the IO-Layer abstraction.</li>
    <li><a href="https://github.com/Mechanical-Advantage/AdvantageKit">AdvantageKit Architecture</a> - The core repository governing deterministic replay loops on the RIO.</li>
    <li><a href="https://github.com/Mechanical-Advantage/AdvantageScope">AdvantageScope Documentation</a> - Visualizing 3D logs natively in real-time.</li>
  </ul>
  </main>
