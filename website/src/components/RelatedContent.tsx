import React from 'react';
import './RelatedContent.css';

export interface RelatedPage {
  title: string;
  url: string;
  description: string;
  type: 'tutorial' | 'api' | 'guide' | 'reference';
  readTime?: string;
}

interface RelatedContentProps {
  currentUrl: string;
  relatedPages: RelatedPage[];
}

export default function RelatedContent({ currentUrl, relatedPages }: RelatedContentProps) {
  if (relatedPages.length === 0) {
    return null;
  }

  const getTypeIcon = (type: string) => {
    const icons: Record<string, string> = {
      tutorial: '📚',
      api: '🔧',
      guide: '📖',
      reference: '📋',
    };
    return icons[type] || '📄';
  };

  const getTypeLabel = (type: string) => {
    const labels: Record<string, string> = {
      tutorial: 'Tutorial',
      api: 'API',
      guide: 'Guide',
      reference: 'Reference',
    };
    return labels[type] || 'Document';
  };

  return (
    <aside className="related-content">
      <div className="related-content-header">
        <h3>Related Content</h3>
        <p>Continue learning with these related pages</p>
      </div>

      <div className="related-content-list">
        {relatedPages.map((page, index) => (
          <a
            key={index}
            href={page.url}
            className={`related-content-item ${page.url === currentUrl ? 'current' : ''}`}
            aria-current={page.url === currentUrl ? 'page' : undefined}
          >
            <div className="related-content-icon">
              {getTypeIcon(page.type)}
            </div>

            <div className="related-content-info">
              <div className="related-content-header-row">
                <span className={`related-content-type type-${page.type}`}>
                  {getTypeLabel(page.type)}
                </span>
                {page.readTime && (
                  <span className="related-content-readtime">
                    {page.readTime}
                  </span>
                )}
              </div>

              <h4 className="related-content-title">{page.title}</h4>

              {page.description && (
                <p className="related-content-description">
                  {page.description}
                </p>
              )}
            </div>
          </a>
        ))}
      </div>

      <div className="related-content-footer">
        <p>
          Can't find what you're looking for?
          {' '}
          <a href="/faq">Check our FAQ</a>
          {' '}
          or
          {' '}
          <a href="https://github.com/MARSProgramming/MARSLib/discussions">
            ask the community
          </a>
        </p>
      </div>
    </aside>
  );
}
