import React, { useEffect } from 'react';
import Layout from '@theme/Layout';

export default function TutorialUploader() {
  return (
    <Layout title="Tutorial Uploader">
      <div className="legacy-mars">
        <div dangerouslySetInnerHTML={{ __html: `



<main class="container" style="padding-top: 100px; padding-bottom: 80px;">
  <div style="text-align: center; margin-bottom: 40px;">
    <a href="/MARSLib/tutorials" class="back-link">← BACK TO TUTORIALS</a>
    <h1>Automated Log Uploader</h1>
  </div>

  <p>Manually retrieving USB drives from the RoboRIO is tedious. MARSLib introduces the <code>LogUploader</code>, an asynchronous daemon thread that quietly runs in the background. If it detects a tethered internet connection, it automatically pushes all new AdvantageKit <code>.wpilog</code> files directly to your team's GitHub repository!</p>

  <h2>1. Dedicated Log Repository</h2>
  <p>Do not upload logs directly into your main robot code repository! Telemetry logs are massive binary files and will quickly bloat your Git history. Create a brand new, empty repository solely for telemetry.</p>

  <h2>2. Configuration</h2>
  <p>To point the uploader toward your team's new repository, you must modify the static configuration fields inside <code>LogUploader.java</code>.</p>

  <pre><code class="language-java">// Inside com/marslib/util/LogUploader.java
public final class LogUploader {
  private static final String GITHUB_OWNER = "YourTeamOrganization";
  private static final String GITHUB_REPO = "Your-Log-Repo";
  private static final int COOLDOWN_SECONDS = 10;
}</code></pre>

  <h2>3. Personal Access Token</h2>
  <p>The RoboRIO needs permission to upload directly to your team's GitHub organization.</p>
  <ol style="margin-bottom: 20px; padding-left: 30px; color: var(--text-secondary);">
    <li style="margin-bottom: 15px;">Navigate to: <em>Settings</em> &rarr; <em>Developer Settings</em> &rarr; <em>Personal Access Tokens</em>.</li>
    <li style="margin-bottom: 15px;">Generate a new token with <strong>Read &amp; Write</strong> access to <strong>Contents</strong>.</li>
    <li style="margin-bottom: 15px;">Copy the exact token string (it will start with <code>github_pat_</code>).</li>
  </ol>

  <div class="callout callout-warning">
    <h4>Robot Security</h4>
    <p>Never hardcode your PAT into Java! The <code>LogUploader</code> explicitly reads the token from a local text file that is ignored by Git.</p>
  </div>

  <h2>4. Deploying the PAT</h2>
  <p>You must place your token inside the RoboRIO's deploy directory. Create a new file locally in your project:</p>

  <pre><code class="language-plaintext">src/main/deploy/github_pat.txt</code></pre>

  <p>Paste the <code>github_pat_...</code> string directly into this file. The next time you deploy, the <code>LogUploader</code> will find it there automatically!</p>

  <h2>5. Execution</h2>
  <p>The system is completely automatic. It checks the <code>DriverStation.isFMSAttached()</code> flag to ensure it <strong>never uploads during real matches</strong>. During practice, whenever the robot is disabled and tethered, it will quietly push the logs to GitHub!</p>
</main>






` }} />
      </div>
    </Layout>
  );
}
