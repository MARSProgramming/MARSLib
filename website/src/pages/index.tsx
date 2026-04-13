import React from 'react';
import Layout from '@theme/Layout';
import SwerveSim from '../components/SwerveSim';

export default function Home() {
  return (
    <Layout title="Home" description="MARSLib: Championship-tier FRC framework">
      <div className="legacy-mars">
        
        {/* Hero Section */}
        <section className="hero" style={{ paddingBottom: '30px' }}>
          <div className="container">
            <a href="https://www.marsfirst.org/" target="_blank" style={{ display: 'block', textDecoration: 'none' }}>
              <img src="/MARSLib/assets/mars-logo.png" alt="MARS Team 2614 Logo" className="hero-logo" />
            </a>
            <div className="hero-badge">MOUNTAINEER AREA ROBOTICS — TEAM 2614</div>
            <h1>MARS<span>Lib</span></h1>
            <p>
              A zero-allocation, physics-simulated FRC framework with deterministic AdvantageKit logging,
              250Hz odometry, and shot-on-the-move kinematics.
            </p>
            <div className="hero-actions">
              <a href="/MARSLib/docs/intro" className="btn btn-accent" style={{ boxShadow: '0 4px 20px rgba(179, 36, 22, 0.4)' }}>🎓 TUTORIALS</a>
              <a href="/MARSLib/standards" className="btn btn-accent" style={{ boxShadow: '0 4px 20px rgba(179, 36, 22, 0.4)' }}>📖 CORE STANDARDS</a>
              <a href="/MARSLib/javadoc/index.html" className="btn btn-accent" style={{ boxShadow: '0 4px 20px rgba(179, 36, 22, 0.4)' }}>BROWSE API DOCS</a>
            </div>
            
            {/* Swerve Simulator Embedded in Hero */}
            <div style={{ maxWidth: '800px', margin: '40px auto 0 auto', borderRadius: '8px', boxShadow: '0 8px 32px rgba(0,0,0,0.5)', overflow: 'hidden' }}>
                <SwerveSim />
            </div>
          </div>
        </section>

        {/* Hall of Fame */}
        <div className="container">
          <a href="https://www.firsthalloffame.org" target="_blank" style={{ textDecoration: 'none' }}>
            <div className="hof-banner">
              <img src="/MARSLib/assets/hall-of-fame.png" alt="FIRST Hall of Fame" />
              <div className="hof-text">
                <h3>FIRST HALL OF FAME INDUCTEE</h3>
                <p>Championship Chairman's Award Winner — recognized for transforming the culture of STEM in West Virginia.</p>
              </div>
              <div className="hof-year">2017</div>
            </div>
          </a>
        </div>

        {/* Modular Documentation Categories (WPILib Style) */}
        <section id="categories" style={{ paddingTop: '60px' }}>
          <div className="container">
            <div className="section-header">
              <h2>Documentation Hub</h2>
              <p>Explore the framework conceptually or dig right into tutorials.</p>
            </div>
            
            <div className="feature-grid">
              
              {/* Card 1: Zero to MARS */}
              <a href="/MARSLib/docs/tutorials/setup/getting-started" style={{ textDecoration: 'none', color: 'inherit' }}>
                <div className="feature-card" style={{ borderTop: '4px solid var(--green)' }}>
                  <h3>🚀 Zero to MARS</h3>
                  <p>Start here! Configure Vendordeps, construct your first `RobotContainer`, and scaffold a generic subsystem with MARSLib bindings.</p>
                </div>
              </a>
              
              {/* Card 2: Core Architecture */}
              <a href="/MARSLib/docs/tutorials/framework/architecture" style={{ textDecoration: 'none', color: 'inherit' }}>
                <div className="feature-card" style={{ borderTop: '4px solid var(--mars-red)' }}>
                  <h3>🏗️ Core Architecture</h3>
                  <p>Learn the IO Layer AdvantageKit abstraction pattern, thread-safe fault management, and strict zero-allocation loop rules.</p>
                </div>
              </a>

              {/* Card 3: Mechanism Abstraction */}
              <a href="/MARSLib/docs/tutorials/framework/hardware-abstraction" style={{ textDecoration: 'none', color: 'inherit' }}>
                <div className="feature-card" style={{ borderTop: '4px solid var(--orange)' }}>
                  <h3>🦾 Mechanism Abstraction</h3>
                  <p>Implement `RotaryMechanismIO` and `LinearMechanismIO` to dramatically speed up standard mechanism development.</p>
                </div>
              </a>

              {/* Card 4: Control Theory */}
              <a href="/MARSLib/docs/tutorials/framework/control-theory" style={{ textDecoration: 'none', color: 'inherit' }}>
                <div className="feature-card" style={{ borderTop: '4px solid var(--yellow)' }}>
                  <h3>🎮 Control Theory</h3>
                  <p>Dive into Shoot-on-the-Move (SOTM) math, `EliteShooterMath`, Feedforwards, and WPILib SysId tuning integration.</p>
                </div>
              </a>

              {/* Card 5: Simulation & Telemetry */}
              <a href="/MARSLib/docs/tutorials/framework/simulation" style={{ textDecoration: 'none', color: 'inherit' }}>
                <div className="feature-card" style={{ borderTop: '4px solid var(--purple)' }}>
                  <h3>🖥️ Simulation & Telemetry</h3>
                  <p>Configure Dyn4j 2D physics integration, AdvantageScope 3D field layouts, and automated GitHub log offloading.</p>
                </div>
              </a>
              
              {/* Card 6: AI Agents */}
              <a href="/MARSLib/docs/agent-skills" style={{ textDecoration: 'none', color: 'inherit' }}>
                <div className="feature-card" style={{ borderTop: '4px solid var(--ai-cyan)' }}>
                  <h3>🤖 AI Agents & Skills</h3>
                  <p>Integrate `.agent` Markdown skills into your IDE to autonomously scaffold subsystems, fix WPILib PID errors, and write unit tests.</p>
                </div>
              </a>
              
            </div>
            <div style={{ textAlign: 'center', marginTop: '40px' }}>
                 <a href="/MARSLib/docs/resources" className="btn btn-primary" style={{ background: 'var(--bg-secondary)', color: 'var(--text-primary)', border: '1px solid var(--border)' }}>🔗 View External Resources & Acknowledgments</a>
            </div>
          </div>
        </section>

        {/* Coverage - Kept for Dashboard functionality */}
        <section id="coverage" style={{ paddingTop: '60px', paddingBottom: '40px' }}>
          <div className="container">
            <div className="section-header">
              <h2>Build Dashboard</h2>
              <p>JaCoCo-measured coverage tracking across 44 test files.</p>
            </div>
            <div className="coverage-grid">
              <div className="coverage-card">
                <h4>📏 Line Coverage</h4>
                <div className="progress-bar">
                  <div className="progress-fill" style={{ width: '71%', background: 'linear-gradient(90deg, var(--mars-red), var(--yellow))' }}></div>
                </div>
                <div className="coverage-meta">2,742 / 3,878 lines covered (71%)</div>
              </div>
              <div className="coverage-card">
                <h4>🌿 Branch Coverage</h4>
                <div className="progress-bar">
                  <div className="progress-fill" style={{ width: '56%', background: 'linear-gradient(90deg, var(--green), var(--blue))' }}></div>
                </div>
                <div className="coverage-meta">487 / 865 branches covered (56%)</div>
              </div>
              <div className="coverage-card">
                <h4>⚙️ Instruction</h4>
                <div className="progress-bar">
                  <div className="progress-fill" style={{ width: '72%', background: 'linear-gradient(90deg, var(--purple), var(--blue))' }}></div>
                </div>
                <div className="coverage-meta">13,666 / 19,004 insts (72%)</div>
              </div>
              <div className="coverage-card">
                <h4>🏗️ Class Coverage</h4>
                <div className="progress-bar">
                  <div className="progress-fill" style={{ width: '75%', background: 'linear-gradient(90deg, var(--green), var(--mars-red))' }}></div>
                </div>
                <div className="coverage-meta">91 / 121 classes covered (75%)</div>
              </div>
            </div>
          </div>
        </section>

      </div>
    </Layout>
  );
}
