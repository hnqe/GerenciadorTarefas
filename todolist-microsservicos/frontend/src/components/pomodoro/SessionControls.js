import React, { useState } from "react";

const SessionControls = ({ 
  session, 
  onStart, 
  onPause, 
  onComplete, 
  onStop, 
  onLinkTask,
  loading 
}) => {
  const [showStopModal, setShowStopModal] = useState(false);
  const [showCompleteModal, setShowCompleteModal] = useState(false);
  const [notes, setNotes] = useState("");

  const handleStop = () => {
    onStop(notes);
    setShowStopModal(false);
    setNotes("");
  };

  const handleComplete = () => {
    onComplete(notes);
    setShowCompleteModal(false);
    setNotes("");
  };

  const getActionButtons = () => {
    if (!session) return null;

    switch (session.status) {
      case "WAITING":
        return (
          <div className="d-flex gap-2 flex-wrap justify-content-center">
            <button
              className="btn btn-success btn-lg px-4"
              onClick={onStart}
              disabled={loading}
            >
              <i className="bi bi-play-fill me-2"></i>
              Start Session
            </button>
            {session.type === "FOCUS" && onLinkTask && (
              <button
                className="btn btn-outline-primary btn-lg px-4"
                onClick={onLinkTask}
                disabled={loading}
              >
                <i className="bi bi-link me-2"></i>
                {(session.taskTitles && session.taskTitles.length > 0) || session.taskTitle ? 'Change Tasks' : 'Link Tasks'}
              </button>
            )}
          </div>
        );

      case "RUNNING":
        return (
          <div className="d-flex gap-2 flex-wrap justify-content-center">
            <button
              className="btn btn-warning btn-lg px-4"
              onClick={onPause}
              disabled={loading}
            >
              <i className="bi bi-pause-fill me-2"></i>
              Pause
            </button>
            <button
              className="btn btn-success btn-lg px-4"
              onClick={() => setShowCompleteModal(true)}
              disabled={loading}
            >
              <i className="bi bi-check-circle me-2"></i>
              Complete
            </button>
            <button
              className="btn btn-outline-danger btn-lg px-3"
              onClick={() => setShowStopModal(true)}
              disabled={loading}
            >
              <i className="bi bi-stop-fill"></i>
            </button>
          </div>
        );

      case "PAUSED":
        return (
          <div className="d-flex gap-2 flex-wrap justify-content-center">
            <button
              className="btn btn-success btn-lg px-4"
              onClick={onStart}
              disabled={loading}
            >
              <i className="bi bi-play-fill me-2"></i>
              Resume
            </button>
            <button
              className="btn btn-outline-primary btn-lg px-4"
              onClick={() => setShowCompleteModal(true)}
              disabled={loading}
            >
              <i className="bi bi-check-circle me-2"></i>
              Complete
            </button>
            <button
              className="btn btn-outline-danger btn-lg px-3"
              onClick={() => setShowStopModal(true)}
              disabled={loading}
            >
              <i className="bi bi-stop-fill"></i>
            </button>
          </div>
        );

      case "COMPLETED":
        return (
          <div className="text-center">
            <div className="alert alert-success d-inline-block mb-0">
              <i className="bi bi-check-circle me-2"></i>
              Session completed! Great job! 🎉
            </div>
          </div>
        );

      case "CANCELLED":
        return (
          <div className="text-center">
            <div className="alert alert-warning d-inline-block mb-0">
              <i className="bi bi-x-circle me-2"></i>
              Session was stopped early
            </div>
          </div>
        );

      default:
        return null;
    }
  };

  return (
    <>
      <div className="text-center">
        {getActionButtons()}
        
        {loading && (
          <div className="mt-3">
            <div className="spinner-border spinner-border-sm text-primary" role="status">
              <span className="visually-hidden">Loading...</span>
            </div>
          </div>
        )}
      </div>

      {/* Stop Session Modal */}
      {showStopModal && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">
                  <i className="bi bi-stop-circle me-2 text-danger"></i>
                  Stop Session
                </h5>
                <button
                  type="button"
                  className="btn-close"
                  onClick={() => setShowStopModal(false)}
                ></button>
              </div>
              <div className="modal-body">
                <p>Are you sure you want to stop this session early?</p>
                <div className="mb-3">
                  <label htmlFor="stopNotes" className="form-label">
                    Notes (optional)
                  </label>
                  <textarea
                    id="stopNotes"
                    className="form-control"
                    rows="3"
                    value={notes}
                    onChange={(e) => setNotes(e.target.value)}
                    placeholder="Add any notes about why you stopped..."
                  ></textarea>
                </div>
              </div>
              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setShowStopModal(false)}
                >
                  Cancel
                </button>
                <button
                  type="button"
                  className="btn btn-danger"
                  onClick={handleStop}
                  disabled={loading}
                >
                  <i className="bi bi-stop-fill me-2"></i>
                  Stop Session
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Complete Session Modal */}
      {showCompleteModal && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">
                  <i className="bi bi-check-circle me-2 text-success"></i>
                  Complete Session
                </h5>
                <button
                  type="button"
                  className="btn-close"
                  onClick={() => setShowCompleteModal(false)}
                ></button>
              </div>
              <div className="modal-body">
                <p>Great work! How did this session go?</p>
                <div className="mb-3">
                  <label htmlFor="completeNotes" className="form-label">
                    Session Notes (optional)
                  </label>
                  <textarea
                    id="completeNotes"
                    className="form-control"
                    rows="3"
                    value={notes}
                    onChange={(e) => setNotes(e.target.value)}
                    placeholder="What did you accomplish? How was your focus?"
                  ></textarea>
                </div>
              </div>
              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setShowCompleteModal(false)}
                >
                  Cancel
                </button>
                <button
                  type="button"
                  className="btn btn-success"
                  onClick={handleComplete}
                  disabled={loading}
                >
                  <i className="bi bi-check-circle me-2"></i>
                  Complete Session
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default SessionControls;