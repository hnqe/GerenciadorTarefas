import React, { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import api from "../services/taskService";

const Header = () => {
  const [username, setUsername] = useState("");
  const [role, setRole] = useState("");
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    fetchUserInfo();
  }, []);

  const fetchUserInfo = async () => {
    try {
      const response = await api.get("/home/user-info");
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
    } catch (error) {
      console.error("Error fetching user info in Header:", error);
      setUsername("Guest");
      setRole("ROLE_USER");
    }
  };


  const handleLogout = () => {
    localStorage.removeItem("jwt");
    localStorage.removeItem("username");
    localStorage.removeItem("theme");
    
    navigate("/login");
  };


  return (
    <header className="modern-header shadow-sm">
      <div className="container-fluid">
        <nav className="navbar navbar-expand-lg py-4">
          <div className="d-flex align-items-center justify-content-between w-100">
            
            {/* Left Side: Navigation */}
            <div className="d-flex align-items-center gap-3">
              {location.pathname !== "/home" && (
                <button
                  className="btn btn-outline-primary d-flex align-items-center px-3 py-2"
                  onClick={() => navigate("/home")}
                  style={{ fontSize: '0.95rem', fontWeight: '500' }}
                >
                  <i className="bi bi-house-fill me-2" style={{ fontSize: '1.1rem' }}></i>
                  Dashboard
                </button>
              )}
              
              {location.pathname !== "/tasks" && (
                <button
                  className="btn btn-outline-secondary d-flex align-items-center px-3 py-2"
                  onClick={() => navigate("/tasks")}
                  style={{ fontSize: '0.95rem', fontWeight: '500' }}
                >
                  <i className="bi bi-list-task me-2" style={{ fontSize: '1.1rem' }}></i>
                  Tasks
                </button>
              )}

              {/* Pomodoro Button estilizado */}
              {location.pathname !== "/pomodoro" && (
                <button
                  className="btn d-flex align-items-center px-4 py-2"
                  onClick={() => navigate("/pomodoro")}
                  style={{
                    background: 'var(--gradient-danger)',
                    color: 'white',
                    border: 'none',
                    borderRadius: '25px',
                    fontSize: '0.95rem',
                    fontWeight: '600',
                    boxShadow: '0 4px 15px rgba(220, 38, 38, 0.3)',
                    transition: 'all 0.3s ease'
                  }}
                  onMouseEnter={(e) => {
                    e.target.style.transform = 'translateY(-2px)';
                    e.target.style.boxShadow = '0 6px 20px rgba(220, 38, 38, 0.4)';
                  }}
                  onMouseLeave={(e) => {
                    e.target.style.transform = 'translateY(0)';
                    e.target.style.boxShadow = '0 4px 15px rgba(220, 38, 38, 0.3)';
                  }}
                >
                  <i className="bi bi-stopwatch me-2" style={{ fontSize: '1.1rem' }}></i>
                  Pomodoro
                </button>
              )}
            </div>

            {/* Center: Title Premium */}
            <div className="position-absolute start-50 translate-middle-x">
              <div className="d-flex align-items-center position-relative">
                <h1 className="mb-0 fw-bold header-title position-relative" style={{ 
                  fontSize: '2.5rem',
                  fontFamily: 'Poppins, sans-serif',
                  background: 'linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%)',
                  WebkitBackgroundClip: 'text',
                  WebkitTextFillColor: 'transparent',
                  backgroundClip: 'text',
                  letterSpacing: '0.05em',
                  textShadow: '0 2px 10px rgba(102, 126, 234, 0.3)',
                  filter: 'drop-shadow(0 2px 4px rgba(102, 126, 234, 0.2))',
                  transform: 'scaleX(1.1)'
                }}>
                  Todo<span style={{ 
                    fontWeight: '300', 
                    fontStyle: 'italic',
                    background: 'linear-gradient(135deg, #ff6b6b 0%, #ffa500 100%)',
                    WebkitBackgroundClip: 'text',
                    WebkitTextFillColor: 'transparent',
                    backgroundClip: 'text'
                  }}>List</span>
                </h1>
                
                {/* Decorative elements */}
                <div className="position-absolute" style={{
                  top: '-10px',
                  right: '-25px',
                  width: '10px',
                  height: '10px',
                  background: 'linear-gradient(135deg, #ff6b6b, #ffa500)',
                  borderRadius: '50%',
                  animation: 'pulse 2s infinite'
                }}></div>
                
                <div className="position-absolute" style={{
                  bottom: '-8px',
                  left: '-18px',
                  width: '8px',
                  height: '8px',
                  background: 'linear-gradient(135deg, #667eea, #764ba2)',
                  borderRadius: '50%',
                  animation: 'pulse 2s infinite 0.5s'
                }}></div>
              </div>
            </div>

            {/* Right Side: User Actions */}
            <div className="d-flex align-items-center">

              {/* User Menu melhorado */}
              <div className="dropdown">
                <button
                  className="btn btn-outline-primary d-flex align-items-center dropdown-toggle px-3 py-2"
                  type="button"
                  data-bs-toggle="dropdown"
                  aria-expanded="false"
                  style={{ 
                    borderRadius: '12px',
                    fontSize: '0.95rem',
                    fontWeight: '500',
                    minWidth: '140px'
                  }}
                >
                  <div className="me-2">
                    <i 
                      className={`bi ${role === "ROLE_ADMIN" ? 'bi-shield-check' : 'bi-person-circle'}`}
                      style={{ fontSize: '1.2rem' }}
                    ></i>
                  </div>
                  <div className="text-start">
                    <div className="fw-bold header-username" style={{ fontSize: '0.95rem' }}>{username}</div>
                  </div>
                </button>
                
                <ul className="dropdown-menu dropdown-menu-end shadow">
                  <li>
                    <span className="dropdown-header">
                      <i className="bi bi-person me-2"></i>
                      Account
                    </span>
                  </li>
                  
                  {role === "ROLE_ADMIN" && (
                    <li>
                      <button
                        className="dropdown-item"
                        onClick={() => navigate("/admin-dashboard")}
                      >
                        <i className="bi bi-gear me-2 text-warning"></i>
                        Admin Dashboard
                      </button>
                    </li>
                  )}
                  
                  
                  <li><hr className="dropdown-divider" /></li>
                  
                  <li>
                    <button className="dropdown-item text-danger" onClick={handleLogout}>
                      <i className="bi bi-box-arrow-right me-2"></i>
                      Sign Out
                    </button>
                  </li>
                </ul>
              </div>

            </div>
          </div>
        </nav>
      </div>
    </header>
  );
};

export default Header;