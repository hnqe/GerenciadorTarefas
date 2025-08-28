import React, { useState, useEffect } from "react";
import api from "../../services/taskService";

const CreateSessionModal = ({ show, onHide, onCreateSession, userSettings }) => {
  const [sessionType, setSessionType] = useState("FOCUS");
  const [duration, setDuration] = useState(25);
  const [taskId, setTaskId] = useState("");
  const [taskTitle, setTaskTitle] = useState("");
  const [notes, setNotes] = useState("");
  const [availableTasks, setAvailableTasks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadingTasks, setLoadingTasks] = useState(false);

  // Update duration when session type changes
  useEffect(() => {
    if (userSettings) {
      switch (sessionType) {
        case "FOCUS":
          setDuration(userSettings.focusDurationMinutes || 25);
          break;
        case "SHORT_BREAK":
          setDuration(userSettings.shortBreakDurationMinutes || 5);
          break;
        case "LONG_BREAK":
          setDuration(userSettings.longBreakDurationMinutes || 15);
          break;
        case "CUSTOM":
          setDuration(25);
          break;
        default:
          setDuration(25);
      }
    }
  }, [sessionType, userSettings]);

  // Fetch available tasks
  useEffect(() => {
    if (show && sessionType === "FOCUS") {
      fetchTasks();
    }
  }, [show, sessionType]);

  const fetchTasks = async () => {
    setLoadingTasks(true);
    try {
      const response = await api.get("/tasks");
      setAvailableTasks(response.data || []);
    } catch (error) {
      console.error("Error fetching tasks:", error);
      setAvailableTasks([]);
    } finally {
      setLoadingTasks(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const sessionData = {
        type: sessionType,
        durationMinutes: parseInt(duration),
        notes: notes.trim() || undefined,
      };

      // Add task info if selected
      if (taskId && taskTitle) {
        sessionData.taskId = taskId;
        sessionData.taskTitle = taskTitle;
      }

      await onCreateSession(sessionData);
      handleClose();
    } catch (error) {
      console.error("Error creating session:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setSessionType("FOCUS");
    setDuration(25);
    setTaskId("");
    setTaskTitle("");
    setNotes("");
    onHide();
  };

  const handleTaskSelect = (e) => {
    const selectedTaskId = e.target.value;
    setTaskId(selectedTaskId);
    
    if (selectedTaskId) {
      const selectedTask = availableTasks.find(task => task.id === selectedTaskId);
      setTaskTitle(selectedTask ? selectedTask.title : "");
    } else {
      setTaskTitle("");
    }
  };

  const getSessionTypeInfo = (type) => {
    switch (type) {
      case "FOCUS":
        return { icon: "bi-circle-fill", color: "text-danger", label: "Focus Session" };
      case "SHORT_BREAK":
        return { icon: "bi-cup-hot-fill", color: "text-warning", label: "Short Break" };
      case "LONG_BREAK":
        return { icon: "bi-moon-stars-fill", color: "text-info", label: "Long Break" };
      case "CUSTOM":
        return { icon: "bi-gear-fill", color: "text-success", label: "Custom Timer" };
      default:
        return { icon: "bi-clock-fill", color: "text-secondary", label: "Timer" };
    }
  };

  if (!show) return null;

  return (
    <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">
              <i className="bi bi-plus-circle me-2 text-primary"></i>
              Create New Session
            </h5>
            <button
              type="button"
              className="btn-close"
              onClick={handleClose}
            ></button>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="modal-body">
              
              {/* Session Type Selection */}
              <div className="mb-4">
                <label className="form-label fw-bold">Session Type</label>
                <div className="row g-2">
                  {["FOCUS", "SHORT_BREAK", "LONG_BREAK", "CUSTOM"].map((type) => {
                    const info = getSessionTypeInfo(type);
                    return (
                      <div key={type} className="col-6">
                        <input
                          type="radio"
                          className="btn-check"
                          name="sessionType"
                          id={`type-${type}`}
                          value={type}
                          checked={sessionType === type}
                          onChange={(e) => setSessionType(e.target.value)}
                        />
                        <label
                          className={`btn btn-outline-primary w-100 d-flex align-items-center gap-2 ${
                            sessionType === type ? 'active' : ''
                          }`}
                          htmlFor={`type-${type}`}
                        >
                          <i className={`${info.icon} ${info.color}`}></i>
                          <span className="small">{info.label}</span>
                        </label>
                      </div>
                    );
                  })}
                </div>
              </div>

              {/* Duration */}
              <div className="mb-4">
                <label htmlFor="duration" className="form-label fw-bold">
                  Duration (minutes)
                </label>
                <div className="row g-2 align-items-center">
                  <div className="col">
                    <input
                      type="number"
                      id="duration"
                      className="form-control"
                      min="1"
                      max="999"
                      value={duration}
                      onChange={(e) => setDuration(e.target.value)}
                      required
                    />
                  </div>
                  <div className="col-auto">
                    <div className="btn-group" role="group">
                      <button
                        type="button"
                        className="btn btn-outline-secondary btn-sm"
                        onClick={() => setDuration(Math.max(1, parseInt(duration) - 5))}
                      >
                        -5
                      </button>
                      <button
                        type="button"
                        className="btn btn-outline-secondary btn-sm"
                        onClick={() => setDuration(Math.min(999, parseInt(duration) + 5))}
                      >
                        +5
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              {/* Task Selection (only for FOCUS sessions) */}
              {sessionType === "FOCUS" && (
                <div className="mb-4">
                  <label htmlFor="taskSelect" className="form-label fw-bold">
                    Link to Task (optional)
                  </label>
                  {loadingTasks ? (
                    <div className="text-center py-2">
                      <div className="spinner-border spinner-border-sm" role="status">
                        <span className="visually-hidden">Loading tasks...</span>
                      </div>
                    </div>
                  ) : (
                    <select
                      id="taskSelect"
                      className="form-select"
                      value={taskId}
                      onChange={handleTaskSelect}
                    >
                      <option value="">No task selected</option>
                      {availableTasks.map((task) => (
                        <option key={task.id} value={task.id}>
                          {task.title} ({task.status})
                        </option>
                      ))}
                    </select>
                  )}
                  
                  {availableTasks.length === 0 && !loadingTasks && (
                    <small className="text-muted">No tasks available</small>
                  )}
                </div>
              )}

              {/* Notes */}
              <div className="mb-3">
                <label htmlFor="sessionNotes" className="form-label fw-bold">
                  Notes (optional)
                </label>
                <textarea
                  id="sessionNotes"
                  className="form-control"
                  rows="3"
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  placeholder={
                    sessionType === "FOCUS" 
                      ? "What do you want to focus on?" 
                      : "How do you want to spend this break?"
                  }
                ></textarea>
              </div>

              {/* Preview */}
              <div className="alert alert-light">
                <div className="d-flex align-items-center gap-2">
                  <i className={`${getSessionTypeInfo(sessionType).icon} ${getSessionTypeInfo(sessionType).color}`}></i>
                  <strong>{duration} minute {getSessionTypeInfo(sessionType).label}</strong>
                </div>
                {taskTitle && (
                  <div className="small text-muted mt-1">
                    <i className="bi bi-list-task me-1"></i>
                    Working on: {taskTitle}
                  </div>
                )}
              </div>
            </div>

            <div className="modal-footer">
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
                disabled={loading || !duration || duration < 1}
              >
                {loading ? (
                  <>
                    <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                    Creating...
                  </>
                ) : (
                  <>
                    <i className="bi bi-plus-circle me-2"></i>
                    Create Session
                  </>
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default CreateSessionModal;