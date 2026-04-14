import React from 'react';

export const RuleSection = ({ num, title, children }: { num: string, title?: string, children: React.ReactNode }) => {
    return (
        <section className="rule-section">
            <span className="rule-num">{num}</span>
            {title && <h2>{title}</h2>}
            {children}
        </section>
    );
};
