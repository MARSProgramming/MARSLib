import React, { useState, useEffect } from 'react';
import './InteractiveTutorial.css';

export interface TutorialStep {
  id: string;
  title: string;
  content: string;
  codeExample?: string;
  checkpoint?: boolean;
  estimatedTime: string;
}

interface InteractiveTutorialProps {
  title: string;
  description: string;
  steps: TutorialStep[];
  onComplete?: () => void;
}

export default function InteractiveTutorial({ title, description, steps, onComplete }: InteractiveTutorialProps) {
  const [currentStep, setCurrentStep] = useState(0);
  const [completedSteps, setCompletedSteps] = useState<Set<string>>(new Set());
  const [showCode, setShowCode] = useState(false);

  const currentStepData = steps[currentStep];
  const progress = ((currentStep + 1) / steps.length) * 100;

  const handleNext = () => {
    if (currentStep < steps.length - 1) {
      setCurrentStep(currentStep + 1);
      setShowCode(false);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  const handlePrevious = () => {
    if (currentStep > 0) {
      setCurrentStep(currentStep - 1);
      setShowCode(false);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  const handleCheckpoint = () => {
    if (currentStepData.checkpoint) {
      const newCompleted = new Set(completedSteps);
      newCompleted.add(currentStepData.id);
      setCompletedSteps(newCompleted);

      // Save progress to localStorage
      localStorage.setItem(`tutorial-${title}-progress`, JSON.stringify([...newCompleted]));
    }
  };

  const handleStepSelect = (index: number) => {
    setCurrentStep(index);
    setShowCode(false);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  // Load saved progress
  useEffect(() => {
    const saved = localStorage.getItem(`tutorial-${title}-progress`);
    if (saved) {
      setCompletedSteps(new Set(JSON.parse(saved)));
    }
  }, [title]);

  // Check if tutorial is complete
  useEffect(() => {
    if (completedSteps.size === steps.filter(s => s.checkpoint).length && steps.length > 0) {
      onComplete?.();
    }
  }, [completedSteps, steps, onComplete]);

  const isStepCompleted = completedSteps.has(currentStepData.id);
  const isLastStep = currentStep === steps.length - 1;
  const isFirstStep = currentStep === 0;

  return (
    <div className="interactive-tutorial">
      {/* Tutorial Header */}
      <div className="tutorial-header">
        <div className="tutorial-title">
          <h1>{title}</h1>
          <p className="tutorial-description">{description}</p>
        </div>

        {/* Progress Bar */}
        <div className="tutorial-progress">
          <div className="progress-info">
            <span>Step {currentStep + 1} of {steps.length}</span>
            <span>{Math.round(progress)}% Complete</span>
          </div>
          <div className="progress-bar">
            <div className="progress-fill" style={{ width: `${progress}%` }}></div>
          </div>
        </div>
      </div>

      <div className="tutorial-content-wrapper">
        {/* Step Navigation Sidebar */}
        <div className="tutorial-sidebar">
          <h3>Steps</h3>
          <div className="tutorial-steps-list">
            {steps.map((step, index) => {
              const isCompleted = completedSteps.has(step.id);
              const isCurrent = index === currentStep;

              return (
                <button
                  key={step.id}
                  className={`tutorial-step-item ${isCurrent ? 'current' : ''} ${
                    isCompleted ? 'completed' : ''
                  }`}
                  onClick={() => handleStepSelect(index)}
                  aria-current={isCurrent ? 'step' : undefined}
                >
                  <div className="step-indicator">
                    {isCompleted ? '✓' : index + 1}
                  </div>
                  <div className="step-info">
                    <div className="step-title">{step.title}</div>
                    <div className="step-time">{step.estimatedTime}</div>
                  </div>
                </button>
              );
            })}
          </div>
        </div>

        {/* Main Content Area */}
        <div className="tutorial-main">
          <div className="tutorial-step-card">
            {/* Step Header */}
            <div className="step-header">
              <h2>{currentStepData.title}</h2>
              <div className="step-meta">
                <span className="step-time">⏱️ {currentStepData.estimatedTime}</span>
                {currentStepData.checkpoint && (
                  <span className="step-checkpoint">📍 Checkpoint</span>
                )}
              </div>
            </div>

            {/* Step Content */}
            <div className="step-content">
              <div dangerouslySetInnerHTML={{ __html: currentStepData.content }} />

              {currentStepData.codeExample && (
                <div className="code-example">
                  <button
                    className="code-toggle"
                    onClick={() => setShowCode(!showCode)}
                  >
                    {showCode ? 'Hide' : 'Show'} Code Example
                  </button>

                  {showCode && (
                    <pre className="code-block">
                      <code>{currentStepData.codeExample}</code>
                    </pre>
                  )}
                </div>
              )}

              {/* Checkpoint Confirmation */}
              {currentStepData.checkpoint && (
                <div className="checkpoint-section">
                  <div className="checkbox-wrapper">
                    <input
                      type="checkbox"
                      id="checkpoint"
                      checked={isStepCompleted}
                      onChange={handleCheckpoint}
                      className="checkpoint-checkbox"
                    />
                    <label htmlFor="checkpoint" className="checkpoint-label">
                      I've completed this step
                    </label>
                  </div>
                </div>
              )}
            </div>

            {/* Navigation Buttons */}
            <div className="step-navigation">
              <button
                className="nav-btn nav-btn-previous"
                onClick={handlePrevious}
                disabled={isFirstStep}
              >
                ← Previous
              </button>

              {isLastStep ? (
                <button
                  className="nav-btn nav-btn-complete"
                  onClick={onComplete}
                  disabled={currentStepData.checkpoint && !isStepCompleted}
                >
                  ✨ Complete Tutorial
                </button>
              ) : (
                <button
                  className="nav-btn nav-btn-next"
                  onClick={handleNext}
                  disabled={currentStepData.checkpoint && !isStepCompleted}
                >
                  Next →
                </button>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
