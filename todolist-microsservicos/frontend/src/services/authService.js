// src/services/authService.js

import axios from "axios";

// URL base onde seu auth-service roda
// Se quiser deixar configurável via variável de ambiente, use process.env.REACT_APP_AUTH_URL
const API_URL = "http://localhost:8080";

/**
 * Faz login chamando POST /api/auth/login (JSON).
 * Retorna { token } se sucesso.
 */
export const login = async (username, password) => {
  const response = await axios.post(`${API_URL}/api/auth/login`, {
    username,
    password,
  });
  return response.data; // { token: 'xxx' }
};

/**
 * Faz registro chamando POST /api/auth/register (JSON).
 * Retorna mensagem ou dados de sucesso se sucesso.
 */
export const register = async (username, password) => {
  const response = await axios.post(`${API_URL}/api/auth/register`, {
    username,
    password,
  });
  return response.data; // ex: "Registration successful!"
};

/**
 * Chama GET /admin/dashboard com o token do localStorage (se houver).
 * Retorna a string do painel de admin.
 */
export const getAdminDashboard = async () => {
  const token = localStorage.getItem("jwt");
  const headers = token
    ? { Authorization: `Bearer ${token}` }
    : {};

  const response = await axios.get(`${API_URL}/admin/dashboard`, { headers });
  return response.data; // ex: "Bem-vindo ao painel do administrador!"
};