import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8081/api", // URL base do TaskService
  });
  
  // Adiciona o token JWT no cabeçalho
  api.interceptors.request.use(
    (config) => {
      const token = localStorage.getItem("jwt");
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    (error) => Promise.reject(error)
  );  

// Admin function for task statistics
export const getTaskStats = async () => {
  const response = await api.get("/tasks/admin/stats");
  return response.data;
};
  
export default api;
  