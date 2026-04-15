import React from 'react';
import './TagCloud.css';

export interface TutorialTag {
  name: string;
  color?: 'green' | 'yellow' | 'red' | 'blue' | 'purple' | 'orange';
}

interface TagCloudProps {
  tags: TutorialTag[];
  size?: 'small' | 'medium' | 'large';
}

export default function TagCloud({ tags, size = 'medium' }: TagCloudProps) {
  const defaultColor = 'blue';

  const getColorClass = (color?: string) => {
    const colors: Record<string, string> = {
      green: 'tag-green',
      yellow: 'tag-yellow',
      red: 'tag-red',
      blue: 'tag-blue',
      purple: 'tag-purple',
      orange: 'tag-orange',
    };
    return colors[color || defaultColor] || colors[defaultColor];
  };

  return (
    <div className={`tag-cloud tag-cloud-${size}`}>
      {tags.map((tag, index) => (
        <span
          key={index}
          className={`tag ${getColorClass(tag.color)}`}
        >
          {tag.name}
        </span>
      ))}
    </div>
  );
}
