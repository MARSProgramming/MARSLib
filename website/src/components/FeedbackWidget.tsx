import React, { useState } from 'react';
import './FeedbackWidget.css';

interface FeedbackWidgetProps {
  pageUrl: string;
  pageTitle: string;
}

export default function FeedbackWidget({ pageUrl, pageTitle }: FeedbackWidgetProps) {
  const [feedback, setFeedback] = useState<'positive' | 'negative' | null>(null);
  const [comment, setComment] = useState('');
  const [submitted, setSubmitted] = useState(false);
  const [showCommentBox, setShowCommentBox] = useState(false);

  const handleFeedback = (type: 'positive' | 'negative') => {
    setFeedback(type);
    if (type === 'negative') {
      setShowCommentBox(true);
    } else {
      submitFeedback(type, '');
    }
  };

  const submitFeedback = (type: 'positive' | 'negative', userComment: string) => {
    // In production, this would send to a backend API
    const feedbackData = {
      pageUrl,
      pageTitle,
      type,
      comment: userComment,
      timestamp: new Date().toISOString(),
      userAgent: navigator.userAgent,
    };

    // Log feedback (in production, send to API)
    console.log('Feedback submitted:', feedbackData);

    // Store in localStorage for demo purposes
    const existingFeedback = JSON.parse(localStorage.getItem('marstask_feedback') || '[]');
    existingFeedback.push(feedbackData);
    localStorage.setItem('marstask_feedback', JSON.stringify(existingFeedback));

    setSubmitted(true);
    setShowCommentBox(false);

    // Reset after 5 seconds
    setTimeout(() => {
      setFeedback(null);
      setSubmitted(false);
      setComment('');
    }, 5000);
  };

  const handleSubmitComment = () => {
    if (feedback === 'negative') {
      submitFeedback('negative', comment);
    }
  };

  if (submitted) {
    return (
      <div className="feedback-widget feedback-widget-success">
        <div className="feedback-icon">✅</div>
        <div className="feedback-message">
          <strong>Thanks for your feedback!</strong>
          <p>We use your feedback to improve our documentation.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="feedback-widget">
      <div className="feedback-header">
        <h4>Was this helpful?</h4>
        <p>Help us improve our documentation</p>
      </div>

      {!feedback ? (
        <div className="feedback-buttons">
          <button
            className="feedback-btn feedback-btn-positive"
            onClick={() => handleFeedback('positive')}
            aria-label="This page was helpful"
          >
            <span className="feedback-icon">👍</span>
            <span>Yes</span>
          </button>
          <button
            className="feedback-btn feedback-btn-negative"
            onClick={() => handleFeedback('negative')}
            aria-label="This page was not helpful"
          >
            <span className="feedback-icon">👎</span>
            <span>No</span>
          </button>
        </div>
      ) : (
        <div className="feedback-confirmation">
          <div className="feedback-icon">
            {feedback === 'positive' ? '👍' : '👎'}
          </div>
          <p>
            {feedback === 'positive'
              ? 'Glad we could help!'
              : 'Sorry to hear that. Please let us know how we can improve.'}
          </p>
        </div>
      )}

      {showCommentBox && (
        <div className="feedback-comment-box">
          <textarea
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            placeholder="What was confusing or missing? (Optional)"
            className="feedback-textarea"
            rows={3}
            aria-label="Feedback comments"
          />
          <div className="feedback-comment-actions">
            <button
              className="feedback-submit-btn"
              onClick={handleSubmitComment}
              disabled={!comment.trim()}
            >
              Submit Feedback
            </button>
            <button
              className="feedback-cancel-btn"
              onClick={() => {
                setShowCommentBox(false);
                setFeedback(null);
              }}
            >
              Cancel
            </button>
          </div>
        </div>
      )}

      <div className="feedback-footer">
        <a
          href="https://github.com/MARSProgramming/MARSLib/issues/new?template=documentation-feedback.md"
          target="_blank"
          rel="noopener noreferrer"
          className="feedback-report-link"
        >
          Report an issue 📝
        </a>
      </div>
    </div>
  );
}
