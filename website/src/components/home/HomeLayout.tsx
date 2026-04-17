import React from 'react';

export const HomeHero = () => {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center', marginTop: '20px', marginBottom: '40px' }}>
      <img src="/img/mars-logo.png" alt="MARSLib" width="300" style={{ marginBottom: '20px', objectFit: 'contain' }} />
      <h1 style={{ fontSize: '3rem', margin: '0 0 10px 0' }}>
        <span style={{ color: '#ef4435' }}>MARS</span><span style={{ color: 'white' }}>Lib</span> Framework
      </h1>
      <p style={{ fontSize: '1.2rem', color: 'var(--sl-color-text-muted)', maxWidth: '700px', margin: '0 auto 30px auto' }}>A zero-allocation, physics-simulated FRC framework with deterministic AdvantageKit logging, 250Hz odometry, and shot-on-the-move kinematics.</p>
      
      <div style={{ display: 'flex', gap: '20px', justifyContent: 'center' }}>
        <a href="/getting-started/01-setup-and-infrastructure/" style={{ background: 'var(--sl-color-accent)', color: 'white', padding: '12px 24px', borderRadius: '8px', textDecoration: 'none', fontWeight: 'bold', letterSpacing: '0.05em' }}>🎓 TUTORIALS</a>
        <a href="https://MARSProgramming.github.io/MARSLib/javadoc/index.html" style={{ background: 'transparent', border: '1px solid var(--sl-color-accent)', color: 'white', padding: '12px 24px', borderRadius: '8px', textDecoration: 'none', fontWeight: 'bold', letterSpacing: '0.05em' }}>BROWSE API DOCS</a>
      </div>
    </div>
  );
};

export const HomeSimulatorContainer = ({ children }: { children: React.ReactNode }) => {
  return (
    <div style={{ maxWidth: '800px', margin: '40px auto 0 auto', borderRadius: '8px', boxShadow: '0 8px 32px rgba(179,36,22,0.2)', overflow: 'hidden' }}>
       {children}
    </div>
  );
};

export const HomeHallOfFame = () => {
  return (
    <div style={{ maxWidth: '1000px', margin: '60px auto 0 auto' }}>
      <a href="https://www.firsthalloffame.org" target="_blank" rel="noreferrer" style={{ textDecoration: 'none', color: 'inherit' }}>
        <div style={{ display: 'flex', alignItems: 'center', background: 'var(--sl-color-bg-nav)', border: '1px solid var(--sl-color-hairline)', borderRadius: '12px', padding: '20px', gap: '20px' }}>
          <img src="/img/hall-of-fame.png" alt="" aria-hidden="true" style={{ width: '100px', height: '100px', objectFit: 'contain' }} />
          <div>
            <h2 style={{ margin: '0 0 8px 0', color: 'var(--mars-red)', fontSize: '1.2rem' }}>FIRST HALL OF FAME INDUCTEE</h2>
            <p style={{ margin: 0, fontSize: '0.9rem', color: 'var(--sl-color-text-muted)' }}>Championship Chairman's Award Winner — recognized for transforming the culture of STEM in West Virginia.</p>
          </div>
          <div style={{ marginLeft: 'auto', fontSize: '2rem', fontWeight: 'bold', color: 'var(--sl-color-text-muted)', opacity: 0.5 }}>2017</div>
        </div>
      </a>
    </div>
  );
};

export const HomeCoreValues = () => {
  const values = [
    { title: 'Discovery', color: '#ef4435', href: '/contributing/core-values/' },
    { title: 'Innovation', color: '#3498db', href: '/subsystems/simulation/' },
    { title: 'Impact', color: '#2ecc71', href: '/contributing/' },
    { title: 'Teamwork', color: '#e67e22', href: '/framework-architecture/teamwork-abstraction/' },
    { title: 'Inclusion', color: '#9b59b6', href: '/contributing/accessibility/' },
    { title: 'Fun', color: '#f1c40f', href: '/subsystems/swerve/' },
  ];

  return (
    <div style={{ maxWidth: '800px', margin: '60px auto 0 auto', textAlign: 'center', padding: '0 20px' }}>
      <p style={{ color: 'var(--sl-color-text-muted)', fontSize: '0.85rem', letterSpacing: '0.15em', textTransform: 'uppercase', marginBottom: '12px' }}>
        Built on the <i>FIRST</i>® Core Values
      </p>
      <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: '6px 16px' }}>
        {values.map(v => (
          <a key={v.title} href={v.href} style={{ color: v.color, fontWeight: 600, fontSize: '0.9rem', textDecoration: 'none' }}>{v.title}</a>
        ))}
      </div>
    </div>
  );
};

export const HomeTutorialGrid = () => {
  return (
    <div style={{ maxWidth: '1000px', margin: '60px auto 0 auto', paddingBottom: '60px' }}>
      <h2 style={{ textAlign: 'center', marginBottom: '8px' }}>Documentation Hub</h2>
      <p style={{ textAlign: 'center', color: 'var(--sl-color-text-muted)', marginBottom: '40px' }}>Explore the framework conceptually or dig right into tutorials.</p>
      
      <div className="tutorial-grid">
        <a href="/getting-started/01-setup-and-infrastructure/" className="tutorial-card" style={{ borderTop: '4px solid #2ecc71' }}>
          <h3>🚀 Zero to MARS</h3>
          <p>Start here! Configure Vendordeps, construct your first RobotContainer, and scaffold subsystems automatically with our VS Code extension.</p>
        </a>
        
        <a href="/framework-architecture/architecture/" className="tutorial-card" style={{ borderTop: '4px solid var(--mars-red)' }}>
          <h3>🏗️ Core Architecture</h3>
          <p>Learn the IO Layer AdvantageKit abstraction pattern, thread-safe fault management, and strict zero-allocation loop rules.</p>
        </a>

        <a href="/framework-architecture/io-layer/" className="tutorial-card" style={{ borderTop: '4px solid #e67e22' }}>
          <h3>🦾 Mechanism Abstraction</h3>
          <p>Implement RotaryMechanismIO and LinearMechanismIO to dramatically speed up standard mechanism development.</p>
        </a>

        <a href="/subsystems/control-theory/" className="tutorial-card" style={{ borderTop: '4px solid #f1c40f' }}>
          <h3>🎮 Control Theory</h3>
          <p>Dive into Shoot-on-the-Move (SOTM) math, EliteShooterMath, Feedforwards, and WPILib SysId tuning integration.</p>
        </a>

        <a href="/subsystems/simulation/" className="tutorial-card" style={{ borderTop: '4px solid #9b59b6' }}>
          <h3>🖥️ Simulation & Telemetry</h3>
          <p>Configure Dyn4j 2D physics integration, AdvantageScope 3D field layouts, and automated GitHub log offloading.</p>
        </a>
        
        <a href="/advanced/ai-agents/" className="tutorial-card" style={{ borderTop: '4px solid var(--ai-cyan)' }}>
          <h3>🤖 AI Agents & Tooling</h3>
          <p>Install the MARSLib VS Code extension to access .agent skills, automate subsystem scaffolding, and run championship-grade logic audits.</p>
        </a>

        <a href="/framework-architecture/sysid/" className="tutorial-card" style={{ borderTop: '4px solid #e74c3c' }}>
          <h3>📻 SysId Characterization</h3>
          <p>Automated system identification for calculating perfect feedforward constants passively during real matches.</p>
        </a>

        <a href="/framework-architecture/fault-resilience/" className="tutorial-card" style={{ borderTop: '4px solid #e84393' }}>
          <h3>🛡️ Fault Resilience</h3>
          <p>Discover the military-grade two-layer fault system, real-time driver alerts, and structural hardware fallbacks.</p>
        </a>

        <a href="/troubleshooting/robot-wont-move/" className="tutorial-card" style={{ borderTop: '4px solid #f39c12' }}>
          <h3>🔧 Troubleshooting</h3>
          <p>Is the CAN bus dropping out? Robot not driving? Compilation errors? Follow our mechanical issue triage wizards.</p>
        </a>

        <a href="/contributing/accessibility/" className="tutorial-card" style={{ borderTop: '4px solid #3498db' }}>
          <h3>♿ Accessible by Design</h3>
          <p>Read our commitment to inclusive engineering. Learn how we use AI agents and Pa11y to enforce WCAG 2.1 AA DOM parity.</p>
        </a>
      </div>
    </div>
  );
}
