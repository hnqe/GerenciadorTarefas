import axios from "axios";

const API_BASE_URL = "http://localhost:8082/api/pomodoro";

// Create axios instance with auth token
const createAuthenticatedRequest = () => {
  const token = localStorage.getItem("jwt") || localStorage.getItem("token");
  return axios.create({
    baseURL: API_BASE_URL,
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  });
};

const pomodoroService = {
  // Session Management
  createSession: async (sessionData) => {
    const api = createAuthenticatedRequest();
    const response = await api.post("/sessions", sessionData);
    return response.data;
  },

  startSession: async (sessionId) => {
    const api = createAuthenticatedRequest();
    const response = await api.post(`/sessions/${sessionId}/start`);
    return response.data;
  },

  pauseSession: async (sessionId) => {
    const api = createAuthenticatedRequest();
    const response = await api.post(`/sessions/${sessionId}/pause`);
    return response.data;
  },

  completeSession: async (sessionId, notes = "") => {
    const api = createAuthenticatedRequest();
    const response = await api.post(`/sessions/${sessionId}/complete`, {
      notes: notes,
    });
    return response.data;
  },

  stopSession: async (sessionId, notes = "") => {
    const api = createAuthenticatedRequest();
    const response = await api.post(`/sessions/${sessionId}/stop`, {
      notes: notes,
    });
    return response.data;
  },

  getCurrentSession: async () => {
    const api = createAuthenticatedRequest();
    try {
      // Get all sessions and find the active one (RUNNING or PAUSED)
      const response = await api.get("/sessions");
      const sessions = response.data;
      
      // Find the most recent active session (RUNNING or PAUSED)
      // Sort by createdAt descending to get the newest session first
      const activeSessions = sessions.filter(session => 
        session.status === 'RUNNING' || session.status === 'PAUSED'
      ).sort((a, b) => new Date(b.createdAt || b.startTime) - new Date(a.createdAt || a.startTime));
      
      const activeSession = activeSessions[0];
      
      return activeSession || null;
    } catch (error) {
      console.error('Error getting current session:', error);
      return null;
    }
  },

  getUserSessions: async () => {
    const api = createAuthenticatedRequest();
    const response = await api.get("/sessions");
    return response.data;
  },

  getSessionsByTask: async (taskId) => {
    const api = createAuthenticatedRequest();
    const response = await api.get(`/sessions/task/${taskId}`);
    return response.data;
  },

  getSessionById: async (sessionId) => {
    const api = createAuthenticatedRequest();
    const response = await api.get(`/sessions/${sessionId}`);
    return response.data;
  },

  // User Settings
  getUserSettings: async () => {
    const api = createAuthenticatedRequest();
    const response = await api.get("/settings");
    return response.data;
  },

  updateUserSettings: async (settings) => {
    const api = createAuthenticatedRequest();
    const response = await api.put("/settings", settings);
    return response.data;
  },

  resetUserSettings: async () => {
    const api = createAuthenticatedRequest();
    const response = await api.post("/settings/reset");
    return response.data;
  },

  // Health Check
  healthCheck: async () => {
    const response = await axios.get(`${API_BASE_URL}/health`);
    return response.data;
  },
};

export default pomodoroService;