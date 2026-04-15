import React, { useState } from 'react';
import './CommunityShowcase.css';

export interface TeamShowcase {
  teamNumber: string;
  teamName: string;
  location: string;
  rookieYear: number;
  achievements: string[];
  robots: string[];
  quote: string;
  quoteAuthor: string;
  imageUrl?: string;
  social?: {
    website?: string;
    github?: string;
    youtube?: string;
  };
}

interface CommunityShowcaseProps {
  teams: TeamShowcase[];
}

export default function CommunityShowcase({ teams }: CommunityShowcaseProps) {
  const [selectedTeam, setSelectedTeam] = useState<TeamShowcase | null>(null);
  const [filter, setFilter] = useState<'all' | 'champions' | 'rookies' | 'international'>('all');

  const filteredTeams = teams.filter(team => {
    if (filter === 'all') return true;
    if (filter === 'champions') return team.achievements.some(a => a.includes('Champion'));
    if (filter === 'rookies') return team.rookieYear >= 2023;
    if (filter === 'international') return team.location !== 'USA';
    return true;
  });

  const filterOptions = [
    { id: 'all', label: 'All Teams', icon: '🌍' },
    { id: 'champions', label: 'Champions', icon: '🏆' },
    { id: 'rookies', label: 'Rookies', icon: '🌟' },
    { id: 'international', label: 'International', icon: '🌎' },
  ];

  return (
    <div className="community-showcase">
      {/* Header */}
      <div className="showcase-header">
        <div className="header-content">
          <h1>Community Showcase</h1>
          <p>Meet the teams achieving success with MARSLib</p>
        </div>

        {/* Filter Tabs */}
        <div className="filter-tabs">
          {filterOptions.map(option => (
            <button
              key={option.id}
              className={`filter-tab ${filter === option.id ? 'active' : ''}`}
              onClick={() => setFilter(option.id as any)}
            >
              <span className="filter-icon">{option.icon}</span>
              <span className="filter-label">{option.label}</span>
            </button>
          ))}
        </div>
      </div>

      {/* Stats */}
      <div className="showcase-stats">
        <div className="stat-item">
          <div className="stat-value">{teams.length}</div>
          <div className="stat-label">Teams Using MARSLib</div>
        </div>
        <div className="stat-item">
          <div className="stat-value">{teams.filter(t => t.achievements.some(a => a.includes('Champion'))).length}</div>
          <div className="stat-label">Regional Champions</div>
        </div>
        <div className="stat-item">
          <div className="stat-value">{new Set(teams.map(t => t.location)).size}</div>
          <div className="stat-label">Countries/Regions</div>
        </div>
        <div className="stat-item">
          <div className="stat-value">{teams.reduce((sum, t) => sum + t.robots.length, 0)}</div>
          <div className="stat-label">Robots Built</div>
        </div>
      </div>

      {/* Teams Grid */}
      <div className="teams-grid">
        {filteredTeams.map((team, index) => (
          <div
            key={team.teamNumber}
            className="team-card"
            onClick={() => setSelectedTeam(team)}
            style={{ animationDelay: `${index * 50}ms` }}
          >
            <div className="team-card-header">
              <div className="team-number">FRC {team.teamNumber}</div>
              <div className="team-location">{team.location}</div>
            </div>

            <h3 className="team-name">{team.teamName}</h3>

            <p className="team-quote">"{team.quote}"</p>
            <p className="team-quote-author">- {team.quoteAuthor}</p>

            <div className="team-achievements">
              {team.achievements.slice(0, 3).map((achievement, i) => (
                <span key={i} className="achievement-badge">
                  {achievement}
                </span>
              ))}
              {team.achievements.length > 3 && (
                <span className="achievement-more">+{team.achievements.length - 3} more</span>
              )}
            </div>

            <div className="team-footer">
              <span className="team-rookie">Since {team.rookieYear}</span>
              <span className="team-robots-count">{team.robots.length} robots</span>
            </div>
          </div>
        ))}
      </div>

      {/* Team Detail Modal */}
      {selectedTeam && (
        <div className="team-modal-overlay" onClick={() => setSelectedTeam(null)}>
          <div className="team-modal" onClick={e => e.stopPropagation()}>
            <button
              className="modal-close"
              onClick={() => setSelectedTeam(null)}
              aria-label="Close modal"
            >
              ✕
            </button>

            <div className="modal-header">
              <div>
                <h2>FRC {selectedTeam.teamNumber}</h2>
                <h3>{selectedTeam.teamName}</h3>
                <p className="modal-location">{selectedTeam.location}</p>
              </div>

              {selectedTeam.imageUrl && (
                <div className="team-image">
                  <img src={selectedTeam.imageUrl} alt={selectedTeam.teamName} />
                </div>
              )}
            </div>

            <div className="modal-content">
              <div className="modal-quote">
                <p>"{selectedTeam.quote}"</p>
                <p className="quote-author">- {selectedTeam.quoteAuthor}</p>
              </div>

              <div className="modal-section">
                <h4>🏆 Achievements</h4>
                <div className="achievements-list">
                  {selectedTeam.achievements.map((achievement, i) => (
                    <div key={i} className="achievement-item">
                      {achievement}
                    </div>
                  ))}
                </div>
              </div>

              <div className="modal-section">
                <h4>🤖 Robots</h4>
                <div className="robots-list">
                  {selectedTeam.robots.map((robot, i) => (
                    <div key={i} className="robot-item">
                      {robot}
                    </div>
                  ))}
                </div>
              </div>

              {selectedTeam.social && (
                <div className="modal-section">
                  <h4>🔗 Connect</h4>
                  <div className="social-links">
                    {selectedTeam.social.website && (
                      <a href={selectedTeam.social.website} target="_blank" rel="noopener noreferrer">
                        Website
                      </a>
                    )}
                    {selectedTeam.social.github && (
                      <a href={selectedTeam.social.github} target="_blank" rel="noopener noreferrer">
                        GitHub
                      </a>
                    )}
                    {selectedTeam.social.youtube && (
                      <a href={selectedTeam.social.youtube} target="_blank" rel="noopener noreferrer">
                        YouTube
                      </a>
                    )}
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Call to Action */}
      <div className="showcase-cta">
        <h2>Using MARSLib? Join the Showcase!</h2>
        <p>We'd love to feature your team's success story.</p>
        <a
          href="https://github.com/MARSProgramming/MARSLib/discussions"
          target="_blank"
          rel="noopener noreferrer"
          className="cta-button"
        >
          Share Your Story 📝
        </a>
      </div>
    </div>
  );
}
