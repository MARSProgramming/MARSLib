import React from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import Heading from '@theme/Heading';

import styles from './index.module.css';

function HeroSection() {
  return (
    <header className={clsx('hero hero--primary', styles.heroBanner)}>
      <div className="container">
        <Link to="https://www.marsfirst.org/" target="_blank">
          <img src="img/mars-logo.png" alt="MARS Logo" className={styles.heroLogo} />
        </Link>
        <div className={styles.heroBadge}>MOUNTAINEER AREA ROBOTICS — TEAM 2614</div>
        <Heading as="h1" className="hero__title">
          MARS<span>Lib</span>
        </Heading>
        <p className="hero__subtitle">
          A zero-allocation, physics-simulated FRC framework with deterministic AdvantageKit logging,
          250Hz odometry, and shot-on-the-move kinematics.
        </p>
        <div className={styles.buttons}>
          <Link
            className="button button--secondary button--lg"
            to="/docs/tutorials"
            style={{boxShadow: '0 4px 20px rgba(179, 36, 22, 0.4)', background: 'var(--mars-red)', color: 'white', border: 'none'}}>
            🎓 TUTORIALS
          </Link>
          <Link
            className="button button--secondary button--lg"
            to="/docs/standards"
            style={{boxShadow: '0 4px 20px rgba(179, 36, 22, 0.4)', background: 'var(--mars-red)', color: 'white', border: 'none', marginLeft: '16px'}}>
            📖 CORE STANDARDS
          </Link>
        </div>
      </div>
    </header>
  );
}

function HofBanner() {
  return (
    <div className="container">
      <Link to="https://www.firsthalloffame.org" target="_blank" style={{textDecoration: 'none'}}>
        <div className={styles.hofBanner}>
          <img src="img/hall-of-fame.png" alt="FIRST Hall of Fame" />
          <div className={styles.hofText}>
            <h3>FIRST HALL OF FAME INDUCTEE</h3>
            <p>Championship Chairman's Award Winner — recognized for transforming the culture of STEM in West Virginia.</p>
          </div>
          <div className={styles.hofYear}>2017</div>
        </div>
      </Link>
    </div>
  );
}

function StatsSection() {
  const stats = [
    { value: '90', label: 'Source Files' },
    { value: '145', label: 'Unit Tests' },
    { value: '71%', label: 'Instruction Coverage' },
    { value: '250Hz', label: 'Odometry Rate' },
    { value: '0', label: 'Hot-Path Allocs' },
  ];

  return (
    <div className="container">
      <div className={styles.stats}>
        {stats.map((stat, idx) => (
          <div key={idx} className={styles.stat}>
            <div className={styles.statValue}>{stat.value}</div>
            <div className={styles.statLabel}>{stat.label}</div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default function Home(): JSX.Element {
  const {siteConfig} = useDocusaurusContext();
  return (
    <Layout
      title={`${siteConfig.title}`}
      description="MARSLib is a championship-tier FRC software framework with zero-allocation performance, dyn4j physics simulation, and AdvantageKit deterministic logging.">
      <HeroSection />
      <main>
        <HofBanner />
        <StatsSection />
        <section className={styles.features}>
            <div className="container">
                <div className="text--center margin-bottom--lg">
                    <Heading as="h2">Framework Features</Heading>
                    <p>Every system is designed for deterministic, replay-safe operation under competition stress.</p>
                </div>
                {/* We'll port the feature cards here in a final pass or keep it simple for the prototype */}
                <div className="row">
                    <div className="col col--4">
                        <div className="card margin-bottom--lg" style={{background: 'var(--ifm-card-background-color)', border: '1px solid var(--ifm-color-emphasis-200)', borderRadius: '16px', padding: '24px'}}>
                            <Heading as="h3">ZERO-ALLOCATION HOT PATH</Heading>
                            <p>Eliminate JVM Garbage Collection spikes from the 20ms hot-path using ephemeral proxy references.</p>
                        </div>
                    </div>
                </div>
            </div>
        </section>
      </main>
    </Layout>
  );
}
