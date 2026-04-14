import React from 'react';

export const StandardHeader = () => {
    return (
        <div style={{ textAlign: 'center', marginBottom: '60px', marginTop: '60px' }}>
          <a href="/" className="back-link">← RETURN TO BASE</a>
          <h1 style={{ fontFamily: 'Orbitron, sans-serif' }}>THE MARSLIB STANDARD</h1>
          <p style={{ fontSize: '1.25rem', color: 'var(--sl-color-text-muted)', maxWidth: '700px', margin: '0 auto' }}>
            The definitive software engineering ruleset governing our World-Champion grade infrastructure. Code that violates these rules is rejected.
          </p>
        </div>
    );
};

export const SplashContainer = ({ children }: { children: React.ReactNode }) => {
    return (
        <main style={{ maxWidth: '900px', margin: '0 auto', paddingBottom: '80px' }}>
            {children}
        </main>
    );
};
