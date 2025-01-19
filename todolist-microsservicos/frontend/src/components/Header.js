import React, { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import api from "../services/taskService";

const Header = () => {
  const [username, setUsername] = useState("");
  const [role, setRole] = useState("");
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    api.get("/home/user-info")
      .then((response) => {
        const fetchedUsername = response.data.username || "Guest";
        const roleFromApi = response.data.role || "USER";

        // Garante o prefixo "ROLE_"
        if (roleFromApi === "ADMIN") {
          setRole("ROLE_ADMIN");
        } else if (!roleFromApi.startsWith("ROLE_")) {
          setRole("ROLE_" + roleFromApi);
        } else {
          setRole(roleFromApi);
        }

        setUsername(fetchedUsername);
      })
      .catch((error) => {
        console.error("Error fetching user info in Header:", error);
        setUsername("Guest");
        setRole("ROLE_USER");
      });
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("jwt");
    localStorage.removeItem("username");
    navigate("/login");
  };

  return (
    <header className="bg-light border-bottom p-3">
      <div className="container d-flex align-items-center justify-content-between">
        {/* Se não estiver na /home, mostra o botão Home */}
        {location.pathname !== "/home" && (
          <button
            className="btn btn-info me-3"
            onClick={() => navigate("/home")}
          >
            HOME
          </button>
        )}

        {/* Título principal: TO DO */}
        <h1
          className="m-0"
          style={{
            fontSize: "2rem",
            fontFamily: "Georgia, serif",
            fontWeight: "bold",
          }}
        >
          TO DO
        </h1>

        <div className="d-flex align-items-center">
          <span className="me-3">
            Hello, <strong>{username}</strong>
          </span>

          {/* Se for ADMIN, mostra botão "Admin" */}
          {role === "ROLE_ADMIN" && (
            <button
              className="btn btn-warning me-3"
              onClick={() => navigate("/admin-dashboard")}
            >
              ADMIN
            </button>
          )}

          <button className="btn btn-danger" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </div>
    </header>
  );
};

export default Header;