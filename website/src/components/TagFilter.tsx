import React, { useState, useMemo } from 'react';
import './TagFilter.css';

export interface Tag {
  name: string;
  count: number;
  category: 'level' | 'topic' | 'system';
}

interface TagFilterProps {
  tags: Tag[];
  selectedTags: string[];
  onTagToggle: (tag: string) => void;
  onClearAll: () => void;
}

export default function TagFilter({ tags, selectedTags, onTagToggle, onClearAll }: TagFilterProps) {
  const [activeCategory, setActiveCategory] = useState<'all' | 'level' | 'topic' | 'system'>('all');

  const filteredTags = useMemo(() => {
    if (activeCategory === 'all') return tags;
    return tags.filter(tag => tag.category === activeCategory);
  }, [tags, activeCategory]);

  const categories = [
    { id: 'all', label: 'All Tags', icon: '🏷️' },
    { id: 'level', label: 'Difficulty', icon: '📊' },
    { id: 'topic', label: 'Topics', icon: '📚' },
    { id: 'system', label: 'Systems', icon: '⚙️' },
  ];

  return (
    <div className="tag-filter">
      <div className="tag-filter-header">
        <h3>Filter by Tags</h3>
        {selectedTags.length > 0 && (
          <button className="clear-all-btn" onClick={onClearAll}>
            Clear All ({selectedTags.length})
          </button>
        )}
      </div>

      {/* Category tabs */}
      <div className="category-tabs">
        {categories.map(category => (
          <button
            key={category.id}
            className={`category-tab ${activeCategory === category.id ? 'active' : ''}`}
            onClick={() => setActiveCategory(category.id as any)}
          >
            <span className="category-icon">{category.icon}</span>
            <span className="category-label">{category.label}</span>
          </button>
        ))}
      </div>

      {/* Tag cloud */}
      <div className="tag-cloud">
        {filteredTags.map(tag => {
          const isSelected = selectedTags.includes(tag.name);
          return (
            <button
              key={tag.name}
              className={`tag ${isSelected ? 'selected' : ''}`}
              onClick={() => onTagToggle(tag.name)}
              aria-pressed={isSelected}
            >
              <span className="tag-name">{tag.name}</span>
              <span className="tag-count">{tag.count}</span>
            </button>
          );
        })}
      </div>

      {filteredTags.length === 0 && (
        <div className="no-tags">
          No tags in this category
        </div>
      )}
    </div>
  );
}
