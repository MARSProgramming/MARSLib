import React, { useState } from 'react';
import './VersionSelector.css';

export interface Version {
  version: string;
  url: string;
  status: 'current' | 'stable' | 'legacy' | 'dev';
  releaseDate?: string;
}

interface VersionSelectorProps {
  versions: Version[];
  currentVersion: string;
}

export default function VersionSelector({ versions, currentVersion }: VersionSelectorProps) {
  const [isOpen, setIsOpen] = useState(false);

  const currentVersionData = versions.find(v => v.version === currentVersion) || versions[0];

  const getStatusIcon = (status: string) => {
    const icons: Record<string, string> = {
      current: '🎯',
      stable: '✓',
      legacy: '📚',
      dev: '🚧',
    };
    return icons[status] || '📄';
  };

  const getStatusLabel = (status: string) => {
    const labels: Record<string, string> = {
      current: 'Current',
      stable: 'Stable',
      legacy: 'Legacy',
      dev: 'Development',
    };
    return labels[status] || 'Other';
  };

  const handleVersionChange = (version: Version) => {
    if (version.version !== currentVersion) {
      // Navigate to new version
      window.location.href = version.url;
    }
    setIsOpen(false);
  };

  return (
    <div className="version-selector">
      <button
        className="version-selector-button"
        onClick={() => setIsOpen(!isOpen)}
        aria-expanded={isOpen}
        aria-haspopup="listbox"
      >
        <span className="version-selector-current">
          <span className="version-icon">{getStatusIcon(currentVersionData.status)}</span>
          <span className="version-text">{currentVersion}</span>
        </span>
        <svg
          className={`version-selector-chevron ${isOpen ? 'open' : ''}`}
          xmlns="http://www.w3.org/2000/svg"
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
        >
          <polyline points="6 9 12 15 18 9"></polyline>
        </svg>
      </button>

      {isOpen && (
        <div
          className="version-selector-dropdown"
          role="listbox"
          aria-label="Select documentation version"
        >
          <div className="version-selector-header">
            <span>Version</span>
            <span>Status</span>
          </div>

          <div className="version-selector-list">
            {versions.map((version) => (
              <button
                key={version.version}
                className={`version-selector-item ${
                  version.version === currentVersion ? 'selected' : ''
                }`}
                onClick={() => handleVersionChange(version)}
                role="option"
                aria-selected={version.version === currentVersion}
              >
                <div className="version-info">
                  <span className="version-icon">{getStatusIcon(version.status)}</span>
                  <span className="version-text">{version.version}</span>
                  {version.releaseDate && (
                    <span className="version-date">{version.releaseDate}</span>
                  )}
                </div>
                <span className={`version-status status-${version.status}`}>
                  {getStatusLabel(version.status)}
                </span>
              </button>
            ))}
          </div>

          <div className="version-selector-footer">
            <a
              href="https://github.com/MARSProgramming/MARSLib/releases"
              target="_blank"
              rel="noopener noreferrer"
              className="version-selector-link"
            >
              View all releases →
            </a>
          </div>
        </div>
      )}
    </div>
  );
}
