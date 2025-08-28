import React, { useState, useEffect } from "react";

const PomodoroSettings = ({ show, onHide, onSettingsUpdate, userSettings }) => {
  const [settings, setSettings] = useState({
    focusDurationMinutes: 25,
    shortBreakDurationMinutes: 5,
    longBreakDurationMinutes: 15,
  });
  const [loading, setLoading] = useState(false);
  const [hasChanges, setHasChanges] = useState(false);
  const [showResetConfirm, setShowResetConfirm] = useState(false);
  const [showCloseConfirm, setShowCloseConfirm] = useState(false);

  useEffect(() => {
    if (userSettings) {
      setSettings(userSettings);
      setHasChanges(false);
    }
  }, [userSettings]);

  const handleInputChange = (field, value) => {
    setSettings(prev => ({
      ...prev,
      [field]: value
    }));
    setHasChanges(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      await onSettingsUpdate(settings);
      setHasChanges(false);
    } catch (error) {
      console.error("Error updating settings:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setShowResetConfirm(true);
  };

  const confirmReset = async () => {
    setShowResetConfirm(false);
    setLoading(true);
    try {
      await onSettingsUpdate("reset");
      setHasChanges(false);
    } catch (error) {
      console.error("Error resetting settings:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (hasChanges) {
      setShowCloseConfirm(true);
    } else {
      onHide();
    }
  };

  const confirmClose = () => {
    setShowCloseConfirm(false);
    // Reset settings to original values when discarding changes
    if (userSettings) {
      setSettings(userSettings);
    }
    setHasChanges(false);
    onHide();
  };

  const presetConfigs = [
    {
      name: "Classic Pomodoro",
      description: "Traditional 25/5/15 minute intervals",
      settings: { focusDurationMinutes: 25, shortBreakDurationMinutes: 5, longBreakDurationMinutes: 15 }
    },
    {
      name: "Extended Focus",
      description: "Longer focus sessions for deep work",
      settings: { focusDurationMinutes: 45, shortBreakDurationMinutes: 10, longBreakDurationMinutes: 30 }
    },
    {
      name: "Quick Sprints",
      description: "Short bursts for high energy tasks",
      settings: { focusDurationMinutes: 15, shortBreakDurationMinutes: 3, longBreakDurationMinutes: 15 }
    }
  ];

  const applyPreset = (preset) => {
    setSettings(prev => ({
      ...prev,
      ...preset.settings
    }));
    setHasChanges(true);
  };

  if (!show) return null;

  return (
    <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
      <div className="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">
              <i className="bi bi-gear me-2 text-primary"></i>
              Pomodoro Settings
            </h5>
            <button
              type="button"
              className="btn-close"
              onClick={handleClose}
              aria-label="Close"
            ></button>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="modal-body">
              
              {/* Quick Presets */}
              <div className="mb-4">
                <h6 className="fw-bold mb-3">
                  <i className="bi bi-lightning me-2"></i>
                  Quick Presets
                </h6>
                <div className="row g-2">
                  {presetConfigs.map((preset, index) => (
                    <div key={index} className="col-md-4">
                      <div className="card h-100">
                        <div className="card-body p-3">
                          <h6 className="card-title small fw-bold">{preset.name}</h6>
                          <p className="card-text small text-muted">{preset.description}</p>
                          <div className="small mb-2">
                            <div>Focus: {preset.settings.focusDurationMinutes}min</div>
                            <div>Break: {preset.settings.shortBreakDurationMinutes}min</div>
                            <div>Long: {preset.settings.longBreakDurationMinutes}min</div>
                          </div>
                          <button
                            type="button"
                            className="btn btn-outline-primary btn-sm w-100"
                            onClick={() => applyPreset(preset)}
                          >
                            Apply
                          </button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              <hr />

              {/* Duration Settings */}
              <div className="mb-4">
                <h6 className="fw-bold mb-3">
                  <i className="bi bi-clock me-2"></i>
                  Timer Durations
                </h6>
                
                <div className="row g-3">
                  <div className="col-md-4">
                    <label className="form-label">
                      <i className="bi bi-circle-fill text-danger me-1"></i>
                      Focus Duration
                    </label>
                    <div className="input-group">
                      <input
                        type="number"
                        className="form-control"
                        min="1"
                        max="180"
                        value={settings.focusDurationMinutes}
                        onChange={(e) => handleInputChange('focusDurationMinutes', parseInt(e.target.value))}
                      />
                      <span className="input-group-text">min</span>
                    </div>
                  </div>
                  
                  <div className="col-md-4">
                    <label className="form-label">
                      <i className="bi bi-cup-hot-fill text-warning me-1"></i>
                      Short Break
                    </label>
                    <div className="input-group">
                      <input
                        type="number"
                        className="form-control"
                        min="1"
                        max="60"
                        value={settings.shortBreakDurationMinutes}
                        onChange={(e) => handleInputChange('shortBreakDurationMinutes', parseInt(e.target.value))}
                      />
                      <span className="input-group-text">min</span>
                    </div>
                  </div>
                  
                  <div className="col-md-4">
                    <label className="form-label">
                      <i className="bi bi-moon-stars-fill text-info me-1"></i>
                      Long Break
                    </label>
                    <div className="input-group">
                      <input
                        type="number"
                        className="form-control"
                        min="1"
                        max="120"
                        value={settings.longBreakDurationMinutes}
                        onChange={(e) => handleInputChange('longBreakDurationMinutes', parseInt(e.target.value))}
                      />
                      <span className="input-group-text">min</span>
                    </div>
                  </div>
                </div>

              </div>


              {/* Current Settings Preview */}
              <div className="alert alert-light">
                <h6 className="mb-2">
                  <i className="bi bi-eye me-2"></i>
                  Current Configuration
                </h6>
                <div className="row text-center">
                  <div className="col-4">
                    <div className="fw-bold text-danger">{settings.focusDurationMinutes}min</div>
                    <small className="text-muted">Focus</small>
                  </div>
                  <div className="col-4">
                    <div className="fw-bold text-warning">{settings.shortBreakDurationMinutes}min</div>
                    <small className="text-muted">Short Break</small>
                  </div>
                  <div className="col-4">
                    <div className="fw-bold text-info">{settings.longBreakDurationMinutes}min</div>
                    <small className="text-muted">Long Break</small>
                  </div>
                </div>
              </div>
            </div>

            <div className="modal-footer">
              <button
                type="button"
                className="btn btn-outline-danger me-auto"
                onClick={handleReset}
                disabled={loading}
              >
                <i className="bi bi-arrow-clockwise me-2"></i>
                Reset to Defaults
              </button>
              
              <button
                type="button"
                className="btn btn-secondary"
                onClick={handleClose}
              >
                Cancel
              </button>
              
              <button
                type="submit"
                className="btn btn-primary"
                disabled={loading || !hasChanges}
              >
                {loading ? (
                  <>
                    <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                    Saving...
                  </>
                ) : (
                  <>
                    <i className="bi bi-check2 me-2"></i>
                    Save Settings
                  </>
                )}
              </button>
            </div>
          </form>
        </div>
      </div>

      {/* Reset Confirmation Modal */}
      {showResetConfirm && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.7)', zIndex: 1055 }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content">
              <div className="modal-header border-0">
                <h5 className="modal-title text-danger">
                  <i className="bi bi-exclamation-triangle-fill me-2"></i>
                  Reset Settings
                </h5>
                <button
                  type="button"
                  className="btn-close"
                  onClick={() => setShowResetConfirm(false)}
                ></button>
              </div>
              <div className="modal-body">
                <p className="mb-3">
                  Are you sure you want to reset all settings to their default values?
                </p>
                <div className="alert alert-warning">
                  <small>
                    <i className="bi bi-info-circle me-1"></i>
                    This action cannot be undone. All your custom settings will be lost.
                  </small>
                </div>
              </div>
              <div className="modal-footer border-0">
                <button
                  type="button"
                  className="btn btn-outline-secondary"
                  onClick={() => setShowResetConfirm(false)}
                >
                  Keep Settings
                </button>
                <button
                  type="button"
                  className="btn btn-danger"
                  onClick={confirmReset}
                  disabled={loading}
                >
                  {loading ? (
                    <>
                      <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                      Resetting...
                    </>
                  ) : (
                    <>
                      <i className="bi bi-arrow-clockwise me-2"></i>
                      Reset to Defaults
                    </>
                  )}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Close Confirmation Modal */}
      {showCloseConfirm && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.7)', zIndex: 1055 }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content">
              <div className="modal-header border-0">
                <h5 className="modal-title text-warning">
                  <i className="bi bi-exclamation-circle-fill me-2"></i>
                  Unsaved Changes
                </h5>
                <button
                  type="button"
                  className="btn-close"
                  onClick={() => setShowCloseConfirm(false)}
                ></button>
              </div>
              <div className="modal-body">
                <p className="mb-3">
                  You have unsaved changes. Are you sure you want to close without saving?
                </p>
                <div className="alert alert-info">
                  <small>
                    <i className="bi bi-lightbulb me-1"></i>
                    Tip: Click "Save Settings" to keep your changes before closing.
                  </small>
                </div>
              </div>
              <div className="modal-footer border-0">
                <button
                  type="button"
                  className="btn btn-outline-secondary"
                  onClick={() => setShowCloseConfirm(false)}
                >
                  Continue Editing
                </button>
                <button
                  type="button"
                  className="btn btn-warning"
                  onClick={confirmClose}
                >
                  <i className="bi bi-x-circle me-2"></i>
                  Discard Changes
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default PomodoroSettings;