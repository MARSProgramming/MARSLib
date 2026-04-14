import React from 'react';

export const CodeComparison = ({ children }: { children: React.ReactNode }) => {
    return (
        <div className="code-comparison">
            {children}
        </div>
    );
};

export const CodeViolation = ({ children }: { children: React.ReactNode }) => {
    return (
        <div className="code-card code-bad">
            <div className="code-header">VIOLATION</div>
            {children}
        </div>
    );
};

export const CodeStandard = ({ children }: { children: React.ReactNode }) => {
    return (
        <div className="code-card code-good">
            <div className="code-header">MARS STANDARD</div>
            {children}
        </div>
    );
};
