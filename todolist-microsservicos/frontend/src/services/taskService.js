import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8081/api", // URL base do TaskService
  });
  
  // Adiciona o token JWT no cabeçalho
  api.interceptors.request.use(
    (config) => {
      const token = localStorage.getItem("jwt");
      console.log("Adding token to request headers:", token); // Verificar token no cabeçalho
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    (error) => Promise.reject(error)
  );  
  
export default api;
  