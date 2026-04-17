import React from 'react';

export const RuleSection = ({ num, title, children }: { num: string, title?: React.ReactNode, children: React.ReactNode }) => {
    const renderedTitle = typeof title === 'string' && title.includes('[FIRST]')
        ? <>{title.split('[FIRST]')[0]}<i>FIRST</i>®{title.split('[FIRST]')[1]}</>
        : title;

    return (
        <section className="rule-section">
            <span className="rule-num">{num}</span>
            {renderedTitle && <h2>{renderedTitle}</h2>}
            {children}
        </section>
    );
};
