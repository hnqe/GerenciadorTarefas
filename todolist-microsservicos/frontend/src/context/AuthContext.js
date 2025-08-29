import React, { createContext, useState, useEffect } from "react";

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const storedToken = localStorage.getItem("jwt");
    if (storedToken && storedToken !== "null" && storedToken !== "undefined") {
      // Verificar se o token ainda é válido
      try {
        const tokenPayload = JSON.parse(atob(storedToken.split('.')[1]));
        const currentTime = Date.now() / 1000;
        
        if (tokenPayload.exp && tokenPayload.exp > currentTime) {
          setToken(storedToken);
        } else {
          // Token expirado
          localStorage.removeItem("jwt");
          setToken(null);
        }
      } catch (error) {
        // Token inválido
        localStorage.removeItem("jwt");
        setToken(null);
      }
    }
    setIsLoading(false);
  }, []);

  const login = (newToken) => {
    localStorage.setItem("jwt", newToken);
    setToken(newToken);
  };  

  const logout = () => {
    localStorage.removeItem("jwt");
    setToken(null);
  };

  return (
    <AuthContext.Provider value={{ token, login, logout, isLoading }}>
      {children}
    </AuthContext.Provider>
  );
};