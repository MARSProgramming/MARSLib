import React, { useEffect } from 'react';
import Layout from '@theme/Layout';

export default function TutorialCi() {
  return (
    <Layout title="Tutorial Ci">
      <div className="legacy-mars">
        <div dangerouslySetInnerHTML={{ __html: `



<main class="container" style="padding-top: 100px; padding-bottom: 80px;">
  <div style="text-align: center; margin-bottom: 40px;">
    <a href="/MARSLib/tutorials" class="back-link">← BACK TO TUTORIALS</a>
    <h1>Intro to CI/CD &amp; Testing</h1>
  </div>

  <p>If you're new to software engineering, you might think the only way to know if your code works is to put the robot on a carpet, cross your fingers, and hit "Enable". In reality, the professional software industry—and MARSLib—uses a system called <strong>CI/CD</strong> to guarantee our code is safe <em>before</em> we ever touch the physical machine.</p>

  <h2>What is CI/CD?</h2>
  <p><strong>Continuous Integration (CI)</strong> is the practice of automatically testing and verifying every piece of code you write the moment you attempt to save or share it with the team.</p>

  <p>Instead of manually checking for misspelled variables, messy brackets, or logic flaws, the CI <strong>Pipeline</strong> is a robotic supervisor living in GitHub. Every time you push a file, the CI pipeline boots up an isolated computer in the cloud and runs a strict checklist on your work. Only if everything passes perfectly will your code be officially integrated into MARSLib.</p>

  <h2>Why is CI Important for FRC?</h2>
  <ul>
    <li><strong>Robot Safety:</strong> If you accidentally type <code>1.0</code> instead of <code>0.1</code> for motor speed, the CI simulator will detect an unsafe launch trajectory before the real robot launches a ball into the ceiling.</li>
    <li><strong>Zero Memory Leaks:</strong> The CI ensures no "garbage memory" allocations occur, meaning our 20ms physics loops never lag during a World Championship match.</li>
    <li><strong>Readability:</strong> A developer working at 3 AM might forget to indent their code cleanly. CI automatically reformats it so the whole team can read it easily the next day.</li>
  </ul>

  <h2>How MARSLib Implements CI</h2>
  <p>When you run <code>./gradlew build</code> locally, or push to GitHub, you trigger our rigorous pipeline. This is what it checks:</p>

  <h3>1. Spotless Formatting</h3>
  <p>Spotless is our automated grammar checker. It forces everything (brackets, spacing, import lines) into the strict Google Java format. If your code is messy, you can run <code>./gradlew spotlessApply</code> to have it instantly fixed.</p>

  <h3>2. PMD Static Analysis</h3>
  <p>PMD is a tool that reads your raw code looking for <strong>code smells</strong>. Did you create an array but never use it? Did you leave a variable open that causes a memory leak? PMD catches it and instantly fails your build until you fix it.</p>

  <h3>3. Dyn4j Physics Testing</h3>
  <p>The crown jewel of our pipeline is deterministic physics testing. We have over 89 automated tests (JUnit) that run against <strong>Dyn4j</strong>—a 2D collision engine. Rather than "mocking" or pretending the code worked, these CI tests actually spawn virtual swerve modules, drop a virtual ball, and run your Autonomous route to ensure physics limits (like motor stall torque) aren't violated.</p>

  <div class="callout">
    <h4>You are supported!</h4>
    <p>Don't be afraid of the CI failing your code—it's supposed to! A failed CI run isn't a bad grade; it is a vital safety net ensuring you feel confident when deploying your logic to the actual 2614 competition robot.</p>
  </div>
</main>




` }} />
      </div>
    </Layout>
  );
}
