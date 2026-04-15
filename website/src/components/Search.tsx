import React, { useState, useCallback, useEffect, useRef } from 'react';
import { createPortal } from 'react-dom';
import './Search.css';

interface DocSearchHit {
  hierarchy: {
    lvl0: string;
    lvl1?: string;
    lvl2?: string;
    lvl3?: string;
  };
  content: string;
  url: string;
  anchor?: string;
}

export default function Search() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<DocSearchHit[]>([]);
  const [isOpen, setIsOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const searchRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  // Algolia DocSearch configuration
  const ALGOLIA_APP_ID = 'YOUR_APP_ID'; // Replace with actual Algolia App ID
  const ALGOLIA_INDEX_NAME = 'marstask';
  const ALGOLIA_API_KEY = 'YOUR_SEARCH_API_KEY'; // Replace with actual API key

  const searchDocs = useCallback(async (searchQuery: string) => {
    if (!searchQuery || searchQuery.length < 2) {
      setResults([]);
      return;
    }

    setIsLoading(true);

    try {
      // Using Algolia Search API
      const response = await fetch(
        `https://${ALGOLIA_APP_ID}-dsn.algolia.net/1/indexes/${ALGOLIA_INDEX_NAME}/query`,
        {
          method: 'POST',
          headers: {
            'X-Algolia-API-Key': ALGOLIA_API_KEY,
            'X-Algolia-Application-Id': ALGOLIA_APP_ID,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            query: searchQuery,
            hitsPerPage: 20,
            highlightPreTag: '<mark>',
            highlightPostTag: '</mark>',
          }),
        }
      );

      const data = await response.json();
      setResults(data.hits || []);
    } catch (error) {
      console.error('Search error:', error);
      // Fallback to local search if Algolia fails
      setResults([]);
    } finally {
      setIsLoading(false);
    }
  }, [ALGOLIA_APP_ID, ALGOLIA_API_KEY, ALGOLIA_INDEX_NAME]);

  useEffect(() => {
    const timeoutId = setTimeout(() => {
      if (query) {
        searchDocs(query);
      }
    }, 300);

    return () => clearTimeout(timeoutId);
  }, [query, searchDocs]);

  useEffect(() => {
    setSelectedIndex(0);
  }, [results]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (searchRef.current && !searchRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleKeyDown = useCallback((e: React.KeyboardEvent) => {
    if (!isOpen || results.length === 0) return;

    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault();
        setSelectedIndex((prev) => (prev + 1) % results.length);
        break;
      case 'ArrowUp':
        e.preventDefault();
        setSelectedIndex((prev) => (prev - 1 + results.length) % results.length);
        break;
      case 'Enter':
        e.preventDefault();
        if (results[selectedIndex]) {
          window.location.href = results[selectedIndex].url;
        }
        break;
      case 'Escape':
        setIsOpen(false);
        break;
    }
  }, [isOpen, results, selectedIndex]);

  const formatHitTitle = (hit: DocSearchHit) => {
    const parts = [hit.hierarchy.lvl0];
    if (hit.hierarchy.lvl1) parts.push(hit.hierarchy.lvl1);
    if (hit.hierarchy.lvl2) parts.push(hit.hierarchy.lvl2);
    if (hit.hierarchy.lvl3) parts.push(hit.hierarchy.lvl3);
    return parts.filter(Boolean).join(' > ');
  };

  const highlightText = (text: string) => {
    // Simple highlight without actual Algolia highlighting
    return text;
  };

  return (
    <div className="search-container" ref={searchRef}>
      <button
        className="search-button"
        onClick={() => {
          setIsOpen(true);
          inputRef.current?.focus();
        }}
        aria-label="Search documentation"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <circle cx="11" cy="11" r="8"></circle>
          <path d="m21 21-4.35-4.35"></path>
        </svg>
        <span>Search</span>
        <kbd className="search-kbd">⌘K</kbd>
      </button>

      {isOpen && createPortal(
        <div className="search-overlay">
          <div className="search-modal">
            <div className="search-input-wrapper">
              <svg className="search-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="11" cy="11" r="8"></circle>
                <path d="m21 21-4.35-4.35"></path>
              </svg>
              <input
                ref={inputRef}
                type="text"
                className="search-input"
                placeholder="Search documentation..."
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                onKeyDown={handleKeyDown}
                autoFocus
              />
              {query && (
                <button
                  className="search-clear"
                  onClick={() => setQuery('')}
                  aria-label="Clear search"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                  </svg>
                </button>
              )}
            </div>

            <div className="search-results">
              {isLoading && (
                <div className="search-loading">Loading...</div>
              )}

              {!isLoading && query.length < 2 && (
                <div className="search-empty">
                  <p>Type at least 2 characters to search</p>
                  <div className="search-shortcuts">
                    <div className="shortcut">
                      <kbd>↑</kbd> <kbd>↓</kbd> to navigate
                    </div>
                    <div className="shortcut">
                      <kbd>Enter</kbd> to select
                    </div>
                    <div className="shortcut">
                      <kbd>Esc</kbd> to close
                    </div>
                  </div>
                </div>
              )}

              {!isLoading && query.length >= 2 && results.length === 0 && (
                <div className="search-no-results">
                  <p>No results found for "{query}"</p>
                  <p className="search-suggestions">
                    Try different keywords or check the FAQ
                  </p>
                </div>
              )}

              {!isLoading && results.length > 0 && (
                <div className="search-results-list">
                  {results.map((hit, index) => (
                    <a
                      key={hit.objectID || index}
                      href={hit.url}
                      className={`search-result ${index === selectedIndex ? 'selected' : ''}`}
                      onMouseEnter={() => setSelectedIndex(index)}
                      onClick={() => setIsOpen(false)}
                    >
                      <div className="search-result-title">
                        {formatHitTitle(hit)}
                      </div>
                      {hit.content && (
                        <div className="search-result-content">
                          {highlightText(hit.content)}
                        </div>
                      )}
                    </a>
                  ))}
                </div>
              )}
            </div>

            <div className="search-footer">
              <span className="search-powered-by">
                Search by <a href="https://www.algolia.com" target="_blank" rel="noopener noreferrer">Algolia</a>
              </span>
            </div>
          </div>
        </div>,
        document.body
      )}
    </div>
  );
}
