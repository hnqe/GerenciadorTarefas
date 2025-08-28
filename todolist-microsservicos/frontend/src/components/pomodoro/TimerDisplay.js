import React from "react";

const TimerDisplay = ({ session, remainingTime, onTaskComplete }) => {
  const formatTime = (minutes) => {
    if (minutes < 0 || isNaN(minutes)) return "00:00";
    
    // Ensure we have a valid number
    const totalMinutes = Math.max(0, minutes);
    const mins = Math.floor(totalMinutes);
    const secs = Math.floor((totalMinutes - mins) * 60);
    
    // Format with proper padding
    const formattedMins = mins.toString().padStart(2, "0");
    const formattedSecs = Math.max(0, Math.min(59, secs)).toString().padStart(2, "0");
    
    return `${formattedMins}:${formattedSecs}`;
  };

  const getSessionTypeInfo = (type) => {
    switch (type) {
      case "FOCUS":
        return { 
          icon: "bi-circle-fill", 
          color: "text-danger", 
          bgColor: "bg-danger", 
          label: "Focus Time",
          description: "Time to concentrate and get things done!"
        };
      case "SHORT_BREAK":
        return { 
          icon: "bi-cup-hot-fill", 
          color: "text-warning", 
          bgColor: "bg-warning", 
          label: "Short Break",
          description: "Take a quick break and recharge"
        };
      case "LONG_BREAK":
        return { 
          icon: "bi-moon-stars-fill", 
          color: "text-info", 
          bgColor: "bg-info", 
          label: "Long Break",
          description: "Time for a longer rest"
        };
      case "CUSTOM":
        return { 
          icon: "bi-gear-fill", 
          color: "text-success", 
          bgColor: "bg-success", 
          label: "Custom Session",
          description: "Your personalized timer"
        };
      default:
        return { 
          icon: "bi-clock-fill", 
          color: "text-secondary", 
          bgColor: "bg-secondary", 
          label: "Timer",
          description: "Focus session"
        };
    }
  };

  const getStatusInfo = (status) => {
    switch (status) {
      case "WAITING":
        return { icon: "bi-play-circle", color: "text-primary", label: "Ready to start" };
      case "RUNNING":
        return { icon: "bi-stopwatch", color: "text-success", label: "In progress" };
      case "PAUSED":
        return { icon: "bi-pause-circle", color: "text-warning", label: "Paused" };
      case "COMPLETED":
        return { icon: "bi-check-circle", color: "text-success", label: "Completed" };
      case "CANCELLED":
        return { icon: "bi-x-circle", color: "text-danger", label: "Stopped" };
      default:
        return { icon: "bi-circle", color: "text-secondary", label: "Unknown" };
    }
  };

  if (!session) {
    return (
      <div className="text-center py-5">
        <div className="mb-4">
          <i className="bi bi-clock-history display-1 text-muted"></i>
        </div>
        <h3 className="text-muted">No Active Session</h3>
        <p className="text-muted">Create a new pomodoro session to get started</p>
      </div>
    );
  }

  const typeInfo = getSessionTypeInfo(session.type);
  const statusInfo = getStatusInfo(session.status);
  const progress = session.plannedDurationMinutes > 0 
    ? Math.max(0, Math.min(100, ((session.plannedDurationMinutes - remainingTime) / session.plannedDurationMinutes) * 100))
    : 0;

  return (
    <div className="text-center">
      {/* Session Type Header */}
      <div className="mb-4">
        <div className={`d-inline-flex align-items-center gap-2 px-3 py-2 rounded-pill ${typeInfo.bgColor} bg-opacity-10 border border-2`} 
             style={{ borderColor: `var(--bs-${typeInfo.color.replace('text-', '')})`}}>
          <i className={`${typeInfo.icon} ${typeInfo.color} fs-5`}></i>
          <span className={`fw-bold ${typeInfo.color}`}>{typeInfo.label}</span>
        </div>
        <p className="text-muted mt-2 mb-0">{typeInfo.description}</p>
      </div>

      {/* Main Timer Display */}
      <div className="position-relative mb-4">
        {/* Circular Progress */}
        <div className="position-relative d-inline-block">
          <svg width="200" height="200" className="timer-circle">
            {/* Background circle */}
            <circle
              cx="100"
              cy="100"
              r="90"
              fill="none"
              stroke="currentColor"
              strokeWidth="8"
              className="text-light opacity-25"
            />
            {/* Progress circle */}
            <circle
              cx="100"
              cy="100"
              r="90"
              fill="none"
              stroke="currentColor"
              strokeWidth="8"
              strokeLinecap="round"
              className={typeInfo.color}
              style={{
                strokeDasharray: `${2 * Math.PI * 90}`,
                strokeDashoffset: `${2 * Math.PI * 90 * (1 - progress / 100)}`,
                transform: "rotate(-90deg)",
                transformOrigin: "100px 100px",
                transition: "stroke-dashoffset 1s ease-in-out"
              }}
            />
          </svg>
          
          {/* Timer Text */}
          <div className="position-absolute top-50 start-50 translate-middle">
            <div className="display-4 fw-bold text-dark">
              {formatTime(remainingTime)}
            </div>
            <div className={`small ${statusInfo.color}`}>
              <i className={`${statusInfo.icon} me-1`}></i>
              {statusInfo.label}
            </div>
          </div>
        </div>
      </div>

      {/* Session Info */}
      <div className="row g-3">
        <div className="col-6">
          <div className="card border-0 bg-light">
            <div className="card-body py-2 px-3">
              <div className="small text-muted">Planned Duration</div>
              <div className="fw-bold">{session.plannedDurationMinutes} min</div>
            </div>
          </div>
        </div>
        <div className="col-6">
          <div className="card border-0 bg-light">
            <div className="card-body py-2 px-3">
              <div className="small text-muted">Progress</div>
              <div className="fw-bold">{Math.round(progress)}%</div>
            </div>
          </div>
        </div>
      </div>

      {/* Interactive Tasks Section */}
      {((session.taskTitles && session.taskTitles.length > 0) || session.taskTitle) && (
        <div className="mt-4">
          <div className="card border-0 bg-primary bg-opacity-10">
            <div className="card-header bg-transparent border-0 pb-2">
              <div className="d-flex align-items-center gap-2">
                <i className="bi bi-list-task text-primary"></i>
                <span className="fw-bold text-primary">
                  Focus Tasks {session.taskTitles && session.taskTitles.length > 1 && `(${session.taskTitles.length})`}
                </span>
              </div>
            </div>
            <div className="card-body pt-0">
              {session.taskTitles && session.taskTitles.length > 0 ? (
                <div className="d-grid gap-2">
                  {session.taskIds?.map((taskId, index) => {
                    return (
                      <div key={taskId} className="d-flex align-items-center gap-3 p-2 rounded bg-white bg-opacity-50">
                        <div className="flex-grow-1">
                          <div className="fw-medium small">{session.taskTitles[index]}</div>
                        </div>
                        {session.status === "RUNNING" && onTaskComplete && (
                          <button
                            className="btn btn-sm btn-outline-success"
                            onClick={() => onTaskComplete(taskId)}
                            title="Mark as completed"
                          >
                            <i className="bi bi-check2"></i>
                          </button>
                        )}
                      </div>
                    );
                  })}
                </div>
              ) : (
                <div className="d-flex align-items-center gap-3 p-2 rounded bg-white bg-opacity-50">
                  <div className="flex-grow-1">
                    <div className="fw-medium small">{session.taskTitle}</div>
                  </div>
                  {session.status === "RUNNING" && onTaskComplete && session.taskId && (
                    <button
                      className="btn btn-sm btn-outline-success"
                      onClick={() => onTaskComplete(session.taskId)}
                      title="Mark as completed"
                    >
                      <i className="bi bi-check2"></i>
                    </button>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Notes */}
      {session.notes && (
        <div className="mt-3">
          <div className="card border-0 bg-light">
            <div className="card-body py-2 px-3 text-start">
              <div className="small text-muted mb-1">Notes</div>
              <div className="small">{session.notes}</div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default TimerDisplay;