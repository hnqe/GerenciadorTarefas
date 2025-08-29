import React, { useState, useEffect, useRef } from "react";
import Header from "../components/Header";
import Footer from "../components/Footer";
import TimerDisplay from "../components/pomodoro/TimerDisplay";
import SessionControls from "../components/pomodoro/SessionControls";
import CreateSessionModal from "../components/pomodoro/CreateSessionModal";
import PomodoroSettings from "../components/pomodoro/PomodoroSettings";
import pomodoroService from "../services/pomodoroService";
import api from "../services/taskService";

const Pomodoro = () => {
  const [currentSession, setCurrentSession] = useState(null);
  const [remainingTime, setRemainingTime] = useState(0);
  const [userSettings, setUserSettings] = useState(null);
  const [recentSessions, setRecentSessions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showSettingsModal, setShowSettingsModal] = useState(false);
  const [showSessionReplaceModal, setShowSessionReplaceModal] = useState(false);
  const [pendingSessionData, setPendingSessionData] = useState(null);
  const [timerInterval, setTimerInterval] = useState(null);
  const [showTaskLinkModal, setShowTaskLinkModal] = useState(false);
  const [availableTasks, setAvailableTasks] = useState([]);
  const [selectedTasks, setSelectedTasks] = useState([]);
  const [loadingTasks, setLoadingTasks] = useState(false);
  
  // Use ref to avoid closure issues in timer interval
  const currentSessionRef = useRef(null);

  useEffect(() => {
    initializePomodoro();
    return () => {
      if (timerInterval) {
        clearInterval(timerInterval);
      }
    };
  }, []);

  useEffect(() => {
    // Update ref whenever currentSession changes
    currentSessionRef.current = currentSession;
    
    if (currentSession && currentSession.status === "RUNNING") {
      startTimer();
    } else {
      stopTimer();
    }
  }, [currentSession]);

  const initializePomodoro = async () => {
    try {
      // Load user settings
      const settings = await pomodoroService.getUserSettings();
      setUserSettings(settings);

      // Load current session
      const session = await pomodoroService.getCurrentSession();
      
      if (session) {
        // Try to restore task info from sessionStorage
        const savedTaskInfo = sessionStorage.getItem(`pomodoro_tasks_${session.id}`);
        if (savedTaskInfo) {
          try {
            const taskInfo = JSON.parse(savedTaskInfo);
            const sessionWithTasks = { ...session, ...taskInfo };
            setCurrentSession(sessionWithTasks);
            calculateRemainingTime(sessionWithTasks);
          } catch (e) {
            console.error("Error parsing saved task info:", e);
            setCurrentSession(session);
            calculateRemainingTime(session);
          }
        } else {
          setCurrentSession(session);
          calculateRemainingTime(session);
        }
      } else {
        setCurrentSession(null);
      }

      // Load recent sessions
      const sessions = await pomodoroService.getUserSessions();
      setRecentSessions(sessions.slice(0, 5));
    } catch (error) {
      console.error("Error initializing pomodoro:", error);
    }
  };

  const startTimer = () => {
    stopTimer(); // Clear any existing timer
    
    const interval = setInterval(() => {
      // Use ref to get latest session data and avoid closure issues
      const session = currentSessionRef.current;
      if (session && session.status === "RUNNING") {
        calculateRemainingTime(session);
      }
    }, 1000);
    
    setTimerInterval(interval);
  };

  const stopTimer = () => {
    if (timerInterval) {
      clearInterval(timerInterval);
      setTimerInterval(null);
    }
  };

  const calculateRemainingTime = (session) => {
    if (!session || !session.startTime) {
      const remaining = session?.plannedDurationMinutes || 0;
      setRemainingTime(remaining);
      return remaining;
    }

    // TIMEZONE FIX: Backend sends LocalDateTime, but frontend parses as UTC
    let startTime = new Date(session.startTime);
    
    // If the parsed time seems wrong (causes negative elapsed), adjust for timezone
    const now = new Date();
    const rawElapsedMs = now - startTime;
    
    if (rawElapsedMs < -60000) { // If more than 1 minute in the "future", it's a timezone issue
      const timezoneOffsetMs = now.getTimezoneOffset() * 60 * 1000;
      startTime = new Date(startTime.getTime() - timezoneOffsetMs);
    }
    
    const elapsedMs = now - startTime;
    const elapsedMinutes = Math.max(0, elapsedMs / (1000 * 60));
    const plannedMinutes = session.plannedDurationMinutes || 0;
    const pausedMinutes = session.totalPausedMinutes || 0;
    
    let remaining;
    
    // UNIFIED CALCULATION: Use same logic for both PAUSED and RUNNING
    // remaining = plannedMinutes - (time_actually_active)
    // where time_actually_active = total_elapsed_time - total_paused_time
    
    if (session.status === "PAUSED" && session.pausedAt) {
      // For paused sessions, calculate time until pause happened
      let pausedAt = new Date(session.pausedAt);
      
      // Apply same timezone fix for pausedAt
      const pausedElapsedMs = now - pausedAt;
      if (pausedElapsedMs < -60000) {
        const timezoneOffsetMs = now.getTimezoneOffset() * 60 * 1000;
        pausedAt = new Date(pausedAt.getTime() - timezoneOffsetMs);
      }
      
      const totalElapsedMs = pausedAt - startTime;
      const totalElapsedMinutes = Math.max(0, totalElapsedMs / (1000 * 60));
      const activeMinutes = Math.max(0, totalElapsedMinutes - pausedMinutes);
      remaining = plannedMinutes - activeMinutes;
    } else {
      // For running sessions, use current time
      const activeMinutes = Math.max(0, elapsedMinutes - pausedMinutes);
      remaining = plannedMinutes - activeMinutes;
    }

    // If timer has gone negative (session should have ended), auto-complete it
    if (remaining <= 0 && session.status === 'RUNNING') {
      handleAutoComplete(session);
      setRemainingTime(0);
      return 0;
    }
    
    const finalRemaining = Math.max(0, remaining);
    setRemainingTime(finalRemaining);
    return finalRemaining;
  };

  const handleAutoComplete = async (session) => {
    try {
      const completedSession = await pomodoroService.completeSession(session.id, "Auto-completed when timer reached zero");
      setCurrentSession(completedSession);
      

      // Refresh recent sessions
      refreshRecentSessions();
    } catch (error) {
      console.error("Error auto-completing session:", error);
    }
  };

  const refreshRecentSessions = async () => {
    try {
      const sessions = await pomodoroService.getUserSessions();
      setRecentSessions(sessions.slice(0, 5));
    } catch (error) {
      console.error("Error refreshing sessions:", error);
    }
  };

  // Session Management Functions
  const handleCreateSession = async (sessionData) => {
    // Check if there's an active session and warn user
    if (currentSession && !["COMPLETED", "CANCELLED"].includes(currentSession.status)) {
      setPendingSessionData(sessionData);
      setShowSessionReplaceModal(true);
      return;
    }
    
    // Create session directly if no active session
    await createNewSession(sessionData);
  };

  const createNewSession = async (sessionData) => {
    setLoading(true);
    try {
      const newSession = await pomodoroService.createSession(sessionData);
      setCurrentSession(newSession);
      setRemainingTime(newSession.plannedDurationMinutes);
    } catch (error) {
      console.error("Error creating session:", error);
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const confirmReplaceSession = async () => {
    setShowSessionReplaceModal(false);
    await createNewSession(pendingSessionData);
    setPendingSessionData(null);
  };

  // Link Task to Current Session
  const handleLinkTaskToSession = async () => {
    await fetchTasks();
    // Pre-select current tasks if any
    if (currentSession?.taskIds && currentSession.taskIds.length > 0) {
      setSelectedTasks(currentSession.taskIds);
    } else if (currentSession?.taskId) {
      // Support legacy single task
      setSelectedTasks([currentSession.taskId]);
    }
    setShowTaskLinkModal(true);
  };

  const fetchTasks = async () => {
    setLoadingTasks(true);
    try {
      const response = await api.get("/tasks");
      // Filter out completed tasks - only show TODO and IN_PROGRESS
      const activeTasks = (response.data || []).filter(task => 
        task.status === "TODO" || task.status === "IN_PROGRESS"
      );
      setAvailableTasks(activeTasks);
    } catch (error) {
      console.error("Error fetching tasks:", error);
      setAvailableTasks([]);
    } finally {
      setLoadingTasks(false);
    }
  };

  const handleTaskLinkConfirm = async () => {
    try {
      let updatedSession = { ...currentSession };
      
      if (selectedTasks.length > 0) {
        const tasks = availableTasks.filter(t => selectedTasks.includes(t.id));
        updatedSession.taskIds = selectedTasks;
        updatedSession.taskTitles = tasks.map(t => t.title);
        // Keep legacy support
        updatedSession.taskId = selectedTasks[0];
        updatedSession.taskTitle = tasks[0]?.title;

        // Tasks will be moved to IN_PROGRESS only when session starts

      } else {
        // Remove task links if no tasks selected
        updatedSession.taskIds = [];
        updatedSession.taskTitles = [];
        updatedSession.taskId = null;
        updatedSession.taskTitle = null;
      }

      // Update session with task info (this would need a backend endpoint)
      // For now, just update locally
      setCurrentSession(updatedSession);
      
      // Save task info for persistence
      saveTaskInfo(updatedSession);
      
      setShowTaskLinkModal(false);
      setSelectedTasks([]);
    } catch (error) {
      console.error("Error linking tasks to session:", error);
    }
  };

  const handleTaskLinkCancel = () => {
    setShowTaskLinkModal(false);
    setSelectedTasks([]);
  };

  // Complete Task During Focus Session
  const handleTaskComplete = async (taskId) => {
    
    try {
      // Find task title for success message
      let completedTaskTitle = "";
      if (currentSession) {
        if (currentSession.taskIds) {
          const taskIndex = currentSession.taskIds.indexOf(taskId);
          completedTaskTitle = currentSession.taskTitles?.[taskIndex] || `Task ${taskId}`;
        } else if (currentSession.taskId === taskId) {
          completedTaskTitle = currentSession.taskTitle || `Task ${taskId}`;
        }
      }

      
      // First, get the current task details
      const taskResponse = await api.get(`/tasks`);
      const currentTask = taskResponse.data.find(task => task.id === taskId);
      
      if (!currentTask) {
        throw new Error(`Task with ID ${taskId} not found`);
      }
      
      
      // Update task status to COMPLETED (send full task object)
      const updatedTask = {
        ...currentTask,
        status: "COMPLETED"
      };
      
      const response = await api.put(`/tasks/edit/${taskId}`, updatedTask);

      // Notify other pages that a task was completed
      const taskUpdateEvent = {
        type: 'TASK_COMPLETED',
        taskId: taskId,
        newStatus: 'COMPLETED',
        timestamp: Date.now()
      };
      
      // Use localStorage to trigger cross-tab communication
      localStorage.setItem('taskUpdate', JSON.stringify(taskUpdateEvent));
      // Remove immediately to trigger the event
      localStorage.removeItem('taskUpdate');
      
      // Also dispatch custom event for same-tab communication
      const customEvent = new CustomEvent('taskUpdate', {
        detail: taskUpdateEvent
      });
      window.dispatchEvent(customEvent);

      // Update current session to remove completed task
      if (currentSession) {
        let updatedSession = { ...currentSession };
        
        if (updatedSession.taskIds && updatedSession.taskIds.length > 1) {
          // Remove from multiple tasks
          const taskIndex = updatedSession.taskIds.indexOf(taskId);
          if (taskIndex > -1) {
            updatedSession.taskIds.splice(taskIndex, 1);
            updatedSession.taskTitles.splice(taskIndex, 1);
            
            // Update legacy single task reference
            if (updatedSession.taskIds.length > 0) {
              updatedSession.taskId = updatedSession.taskIds[0];
              updatedSession.taskTitle = updatedSession.taskTitles[0];
            } else {
              updatedSession.taskId = null;
              updatedSession.taskTitle = null;
            }
          }
        } else if (updatedSession.taskId === taskId) {
          // Remove single task
          updatedSession.taskId = null;
          updatedSession.taskTitle = null;
          updatedSession.taskIds = [];
          updatedSession.taskTitles = [];
        }
        
        setCurrentSession(updatedSession);
        
        // Save updated task info to sessionStorage
        saveTaskInfo(updatedSession);
      }

      // Show success feedback
      
    } catch (error) {
      console.error("❌ Error completing task:", error);
      console.error("❌ Error details:", error.response?.data || error.message);
      console.error("❌ Error status:", error.response?.status);
      alert(`Error completing task: ${error.response?.data?.message || error.message}`);
    }
  };

  const cancelReplaceSession = () => {
    setShowSessionReplaceModal(false);
    setPendingSessionData(null);
  };

  // Helper function to preserve task info during backend calls
  const preserveTaskInfo = (updatedSession) => {
    if (!currentSession) return updatedSession;
    
    const taskInfo = {
      taskId: currentSession.taskId,
      taskTitle: currentSession.taskTitle,
      taskIds: currentSession.taskIds,
      taskTitles: currentSession.taskTitles
    };
    
    return {
      ...updatedSession,
      ...taskInfo
    };
  };

  // Save task info to sessionStorage for persistence across navigation
  const saveTaskInfo = (session) => {
    if (!session || !session.id) return;
    
    const taskInfo = {
      taskId: session.taskId,
      taskTitle: session.taskTitle,
      taskIds: session.taskIds,
      taskTitles: session.taskTitles
    };
    
    // Only save if there are tasks
    if (taskInfo.taskId || (taskInfo.taskIds && taskInfo.taskIds.length > 0)) {
      sessionStorage.setItem(`pomodoro_tasks_${session.id}`, JSON.stringify(taskInfo));
    }
  };

  const handleStartSession = async () => {
    if (!currentSession) return;
    
    setLoading(true);
    try {
      // Move linked TODO tasks to IN_PROGRESS when session starts
      if (currentSession.taskIds && currentSession.taskIds.length > 0) {
        const linkedTasks = availableTasks.filter(t => currentSession.taskIds.includes(t.id));
        const pendingTasks = linkedTasks.filter(task => task.status === "TODO");
        
        if (pendingTasks.length > 0) {
          // Update each pending task to IN_PROGRESS
          const updatePromises = pendingTasks.map(async (task) => {
            const updatedTask = {
              ...task,
              status: "IN_PROGRESS"
            };
            
            try {
              await api.put(`/tasks/edit/${task.id}`, updatedTask);
              
              // Notify Tasks page about the status change
              const taskUpdateEvent = {
                type: 'TASK_STATUS_UPDATED',
                taskId: task.id,
                newStatus: 'IN_PROGRESS',
                timestamp: Date.now()
              };
              
              localStorage.setItem('taskUpdate', JSON.stringify(taskUpdateEvent));
              localStorage.removeItem('taskUpdate');
              
              const customEvent = new CustomEvent('taskUpdate', {
                detail: taskUpdateEvent
              });
              window.dispatchEvent(customEvent);
              
            } catch (error) {
              console.error(`❌ Error moving task ${task.id} to IN PROGRESS:`, error);
            }
          });
          
          // Wait for all status updates to complete
          await Promise.all(updatePromises);
        }
      }

      const updatedSession = await pomodoroService.startSession(currentSession.id);
      const sessionWithTasks = preserveTaskInfo(updatedSession);
      
      setCurrentSession(sessionWithTasks);
      
      // Ensure timer calculation uses the updated session
      calculateRemainingTime(sessionWithTasks);
    } catch (error) {
      console.error("Error starting session:", error);
    } finally {
      setLoading(false);
    }
  };

  const handlePauseSession = async () => {
    if (!currentSession) return;
    
    setLoading(true);
    try {
      const updatedSession = await pomodoroService.pauseSession(currentSession.id);
      const sessionWithTasks = preserveTaskInfo(updatedSession);
      
      setCurrentSession(sessionWithTasks);
    } catch (error) {
      console.error("Error pausing session:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleCompleteSession = async (notes) => {
    if (!currentSession) return;
    
    setLoading(true);
    try {
      const completedSession = await pomodoroService.completeSession(currentSession.id, notes);
      setCurrentSession(completedSession);
      refreshRecentSessions();
    } catch (error) {
      console.error("Error completing session:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleStopSession = async (notes) => {
    if (!currentSession) return;
    
    setLoading(true);
    try {
      const stoppedSession = await pomodoroService.stopSession(currentSession.id, notes);
      setCurrentSession(stoppedSession);
      refreshRecentSessions();
    } catch (error) {
      console.error("Error stopping session:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleSettingsUpdate = async (settings) => {
    setLoading(true);
    try {
      let updatedSettings;
      if (settings === "reset") {
        updatedSettings = await pomodoroService.resetUserSettings();
      } else {
        updatedSettings = await pomodoroService.updateUserSettings(settings);
      }
      setUserSettings(updatedSettings);
    } catch (error) {
      console.error("Error updating settings:", error);
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const formatSessionTime = (session) => {
    if (!session.startTime) return "Not started";
    
    const startTime = new Date(session.startTime);
    const timeStr = startTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    
    if (session.endTime) {
      const endTime = new Date(session.endTime);
      const endTimeStr = endTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      return `${timeStr} - ${endTimeStr}`;
    }
    
    return `Started at ${timeStr}`;
  };

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case "COMPLETED": return "bg-success";
      case "CANCELLED": return "bg-warning";
      case "RUNNING": return "bg-primary";
      case "PAUSED": return "bg-warning";
      default: return "bg-secondary";
    }
  };

  return (
    <div className="d-flex flex-column min-vh-100">
      <Header />
      
      <main className="flex-grow-1 py-4">
        <div className="container">
          
          {/* Page Header */}
          <div className="row mb-4">
            <div className="col">
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <h1 className="display-6 fw-bold mb-2">
                    <i className="bi bi-stopwatch me-3 text-danger"></i>
                    Pomodoro Timer
                  </h1>
                  <p className="text-muted mb-0">Stay focused and productive with customizable timer sessions</p>
                </div>
                
                <div className="d-flex gap-2">
                  <button
                    className="btn btn-outline-secondary"
                    onClick={() => setShowSettingsModal(true)}
                  >
                    <i className="bi bi-gear me-2"></i>
                    Settings
                  </button>
                  
                  <button
                    className="btn btn-primary"
                    onClick={() => setShowCreateModal(true)}
                  >
                    <i className="bi bi-plus-circle me-2"></i>
                    New Session
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div className="row">
            {/* Main Timer Section */}
            <div className="col-lg-8">
              <div className="card border-0 shadow-sm h-100">
                <div className="card-body p-4">
                  <TimerDisplay 
                    session={currentSession} 
                    remainingTime={remainingTime}
                    onTaskComplete={handleTaskComplete}
                  />
                  
                  <div className="mt-4">
                    <SessionControls
                      session={currentSession}
                      onStart={handleStartSession}
                      onPause={handlePauseSession}
                      onComplete={handleCompleteSession}
                      onStop={handleStopSession}
                      onLinkTask={handleLinkTaskToSession}
                      loading={loading}
                    />
                  </div>
                </div>
              </div>
            </div>

            {/* Sidebar */}
            <div className="col-lg-4">
              {/* Quick Actions */}
              <div className="card border-0 shadow-sm mb-4">
                <div className="card-header bg-transparent border-0 pb-0">
                  <h6 className="fw-bold mb-0">
                    <i className="bi bi-lightning me-2"></i>
                    Quick Start
                  </h6>
                </div>
                <div className="card-body pt-2">
                  <div className="d-grid gap-2">
                    <button 
                      className={`btn btn-sm quick-start-btn ${currentSession?.type === "FOCUS" ? "btn-danger" : "btn-outline-danger"}`}
                      onClick={() => handleCreateSession({ 
                        type: "FOCUS", 
                        durationMinutes: userSettings?.focusDurationMinutes || 25 
                      })}
                    >
                      <i className="bi bi-circle-fill me-2"></i>
                      {userSettings?.focusDurationMinutes || 25}min Focus
                    </button>
                    <button 
                      className={`btn btn-sm quick-start-btn ${currentSession?.type === "SHORT_BREAK" ? "btn-warning" : "btn-outline-warning"}`}
                      onClick={() => handleCreateSession({ 
                        type: "SHORT_BREAK", 
                        durationMinutes: userSettings?.shortBreakDurationMinutes || 5 
                      })}
                    >
                      <i className="bi bi-cup-hot-fill me-2"></i>
                      {userSettings?.shortBreakDurationMinutes || 5}min Break
                    </button>
                    <button 
                      className={`btn btn-sm quick-start-btn ${currentSession?.type === "LONG_BREAK" ? "btn-info" : "btn-outline-info"}`}
                      onClick={() => handleCreateSession({ 
                        type: "LONG_BREAK", 
                        durationMinutes: userSettings?.longBreakDurationMinutes || 15 
                      })}
                    >
                      <i className="bi bi-moon-stars-fill me-2"></i>
                      {userSettings?.longBreakDurationMinutes || 15}min Long Break
                    </button>
                  </div>
                </div>
              </div>

              {/* Recent Sessions */}
              <div className="card border-0 shadow-sm">
                <div className="card-header bg-transparent border-0 pb-0">
                  <h6 className="fw-bold mb-0">
                    <i className="bi bi-clock-history me-2"></i>
                    Recent Sessions
                  </h6>
                </div>
                <div className="card-body pt-2">
                  {recentSessions.length === 0 ? (
                    <div className="text-center text-muted py-3">
                      <i className="bi bi-clock-history fs-4 d-block mb-2"></i>
                      <small>No sessions yet</small>
                    </div>
                  ) : (
                    <div className="list-group list-group-flush">
                      {recentSessions.map((session, index) => (
                        <div key={session.id} className="list-group-item px-0 py-2 border-0">
                          <div className="d-flex justify-content-between align-items-start">
                            <div className="flex-grow-1">
                              <div className="d-flex align-items-center gap-2 mb-1">
                                <span className={`badge ${getStatusBadgeClass(session.status)} badge-sm`}>
                                  {session.status.toLowerCase()}
                                </span>
                                <small className="text-muted">{session.type.toLowerCase()}</small>
                                <small className="text-muted">•</small>
                                <small className="text-muted">{session.plannedDurationMinutes}min</small>
                              </div>
                              
                              {((session.taskTitles && session.taskTitles.length > 0) || session.taskTitle) && (
                                <div className="small mb-1">
                                  <i className="bi bi-list-task me-1 text-primary"></i>
                                  {session.taskTitles && session.taskTitles.length > 1 ? (
                                    <span>{session.taskTitles.length} tasks: {session.taskTitles.join(', ')}</span>
                                  ) : (
                                    <span>{session.taskTitles ? session.taskTitles[0] : session.taskTitle}</span>
                                  )}
                                </div>
                              )}
                              
                              <div className="small text-muted">
                                {formatSessionTime(session)}
                              </div>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>

      <Footer />

      {/* Modals */}
      <CreateSessionModal
        show={showCreateModal}
        onHide={() => setShowCreateModal(false)}
        onCreateSession={handleCreateSession}
        userSettings={userSettings}
      />

      <PomodoroSettings
        show={showSettingsModal}
        onHide={() => setShowSettingsModal(false)}
        onSettingsUpdate={handleSettingsUpdate}
        userSettings={userSettings}
      />

      {/* Task Link Modal for Quick Focus */}
      {showTaskLinkModal && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">
                  <i className="bi bi-link text-primary me-2"></i>
                  Link Task to Session
                </h5>
                <button
                  type="button"
                  className="btn-close"
                  onClick={handleTaskLinkCancel}
                ></button>
              </div>
              <div className="modal-body">
                <div className="mb-3">
                  <p className="mb-2">
                    Link a task to your current <strong>{currentSession?.type?.toLowerCase()?.replace('_', ' ')} session</strong>
                  </p>
                </div>
                
                <div className="mb-4">
                  <div className="d-flex justify-content-between align-items-center mb-2">
                    <label className="form-label fw-bold mb-0">
                      Select Tasks (optional)
                    </label>
                    {availableTasks.length > 0 && (
                      <div className="btn-group btn-group-sm">
                        <button
                          type="button"
                          className="btn btn-outline-secondary"
                          onClick={() => setSelectedTasks(availableTasks.map(t => t.id))}
                        >
                          Select All
                        </button>
                        <button
                          type="button"
                          className="btn btn-outline-secondary"
                          onClick={() => setSelectedTasks([])}
                        >
                          Clear All
                        </button>
                      </div>
                    )}
                  </div>
                  {loadingTasks ? (
                    <div className="text-center py-2">
                      <div className="spinner-border spinner-border-sm" role="status">
                        <span className="visually-hidden">Loading tasks...</span>
                      </div>
                    </div>
                  ) : availableTasks.length > 0 ? (
                    <div className="border rounded p-3 max-height-200 overflow-auto">
                      {availableTasks.map((task) => (
                        <div key={task.id} className="form-check mb-2">
                          <input
                            className="form-check-input"
                            type="checkbox"
                            value={task.id}
                            id={`task-${task.id}`}
                            checked={selectedTasks.includes(task.id)}
                            onChange={(e) => {
                              if (e.target.checked) {
                                setSelectedTasks([...selectedTasks, task.id]);
                              } else {
                                setSelectedTasks(selectedTasks.filter(id => id !== task.id));
                              }
                            }}
                          />
                          <label className="form-check-label" htmlFor={`task-${task.id}`}>
                            <span className="fw-medium">{task.title}</span>
                            <span className="badge bg-secondary ms-2 small">{task.status}</span>
                          </label>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <small className="text-muted">No tasks available</small>
                  )}
                </div>

                {selectedTasks.length > 0 && (
                  <div className="alert alert-light">
                    <div className="small text-muted">
                      <i className="bi bi-list-task me-1"></i>
                      Working on {selectedTasks.length} task{selectedTasks.length > 1 ? 's' : ''}:
                    </div>
                    <ul className="small mb-0 mt-1">
                      {selectedTasks.map(taskId => {
                        const task = availableTasks.find(t => t.id === taskId);
                        return (
                          <li key={taskId} className="text-muted">
                            {task?.title}
                          </li>
                        );
                      })}
                    </ul>
                  </div>
                )}
              </div>
              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={handleTaskLinkCancel}
                >
                  Cancel
                </button>
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={handleTaskLinkConfirm}
                  disabled={loadingTasks}
                >
                  <i className="bi bi-link me-2"></i>
                  {selectedTasks.length > 0 ? `Link ${selectedTasks.length} Task${selectedTasks.length > 1 ? 's' : ''}` : 'Link Tasks'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Session Replace Confirmation Modal */}
      {showSessionReplaceModal && pendingSessionData && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.7)', zIndex: 1055 }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content">
              <div className="modal-header border-0">
                <h5 className="modal-title text-warning">
                  <i className="bi bi-exclamation-triangle-fill me-2"></i>
                  Replace Active Session?
                </h5>
                <button
                  type="button"
                  className="btn-close"
                  onClick={cancelReplaceSession}
                  aria-label="Close"
                ></button>
              </div>
              <div className="modal-body">
                <div className="mb-3">
                  <p className="mb-2">
                    You currently have an active <strong>{currentSession?.type?.toLowerCase()?.replace('_', ' ')} session</strong> running.
                  </p>
                  <p className="mb-3">
                    Creating a new <strong>{pendingSessionData?.type?.toLowerCase()?.replace('_', ' ')} session</strong> will stop the current one.
                  </p>
                  <div className="alert alert-info">
                    <small>
                      <i className="bi bi-info-circle me-1"></i>
                      Your current progress will be lost if you continue.
                    </small>
                  </div>
                </div>
              </div>
              <div className="modal-footer border-0">
                <button
                  type="button"
                  className="btn btn-outline-secondary"
                  onClick={cancelReplaceSession}
                >
                  <i className="bi bi-x-circle me-2"></i>
                  Keep Current Session
                </button>
                <button
                  type="button"
                  className="btn btn-warning"
                  onClick={confirmReplaceSession}
                  disabled={loading}
                >
                  {loading ? (
                    <>
                      <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                      Creating...
                    </>
                  ) : (
                    <>
                      <i className="bi bi-arrow-right-circle me-2"></i>
                      Replace Session
                    </>
                  )}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Pomodoro;