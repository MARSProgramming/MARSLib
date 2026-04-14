---
sidebar:
  order: 16
id: uploader
title: "Automated Log Uploader"
---

  <p>Manually retrieving USB drives from the <a href="https://www.ni.com/en/shop/hardware/products/roborio.html" target="_blank">RoboRIO</a> is tedious. MARSLib introduces the <code>LogUploader</code>, an asynchronous daemon thread that quietly runs in the background. If it detects a tethered internet connection, it automatically pushes all new <a href="https://github.com/Mechanical-Advantage/AdvantageKit" target="_blank">AdvantageKit</a> <code>.wpilog</code> files directly to your team's <a href="https://github.com/" target="_blank">GitHub</a> repository!</p>

  <h2>1. Dedicated Log Repository</h2>
  <p>Do not upload logs directly into your main robot code repository! Telemetry logs are massive binary files and will quickly bloat your Git history. Create a brand new, empty repository solely for telemetry.</p>

  <h2>2. Configuration</h2>
  <p>To point the uploader toward your team's new repository, you must modify the static configuration fields inside <code>LogUploader.java</code>.</p>

  ```java
// Inside com/marslib/util/LogUploader.java
public final class LogUploader {
  private static final String GITHUB_OWNER = "YourTeamOrganization";
  private static final String GITHUB_REPO = "Your-Log-Repo";
  private static final int COOLDOWN_SECONDS = 10;
}
```

  <h2>3. Personal Access Token</h2>
  <p>The RoboRIO needs permission to upload directly to your team's GitHub organization.</p>
  <ol >
    <li >Navigate to: <em>Settings</em> &rarr; <em>Developer Settings</em> &rarr; <em><a href="https://github.com/settings/tokens" target="_blank">Personal Access Tokens</a></em>.</li>
    <li >Generate a new token with <strong>Read &amp; Write</strong> access to <strong>Contents</strong>.</li>
    <li >Copy the exact token string (it will start with <code>github_pat_</code>).</li>
  </ol>

  <div class="callout callout-warning">
    <h4>Robot Security</h4>
    <p>Never hardcode your PAT into Java! The <code>LogUploader</code> explicitly reads the token from a local text file that is ignored by Git.</p>
  </div>

  <h2>4. Deploying the PAT</h2>
  <p>You must place your token inside the RoboRIO's deploy directory. Create a new file locally in your project:</p>

  ```java
src/main/deploy/github_pat.txt
```

  <p>Paste the <code>github_pat_...</code> string directly into this file. The next time you deploy, the <code>LogUploader</code> will find it there automatically!</p>

  <h2>5. Execution</h2>
  <p>The system is completely automatic. It checks the <code>DriverStation.isFMSAttached()</code> flag to ensure it <strong>never uploads during real matches</strong>. During practice, whenever the robot is disabled and tethered, it will quietly push the logs to GitHub!</p>

  <br /><hr /><br />
  <h2>📖 Further Reading & External Resources</h2>
  <ul>
    <li><a href="https://www.youtube.com/watch?v=yYn9NqJ6kXU">FRC Log Replay and Simulation</a> - Mechanical Advantage's definitive 2024 Championship presentation on the why behind the IO-Layer abstraction.</li>
    <li><a href="https://github.com/Mechanical-Advantage/AdvantageKit">AdvantageKit Architecture</a> - The core repository governing deterministic replay loops on the RIO.</li>
    <li><a href="https://github.com/Mechanical-Advantage/AdvantageScope">AdvantageScope Documentation</a> - Visualizing 3D logs natively in real-time.</li>
  </ul>

