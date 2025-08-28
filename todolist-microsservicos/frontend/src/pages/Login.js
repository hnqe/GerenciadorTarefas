import axios from "axios";
import React, { useContext, useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthContext } from "../context/AuthContext";

const Login = () => {
  const { login } = useContext(AuthContext);
  const [credentials, setCredentials] = useState({ username: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    
    try {
      const response = await axios.post("http://localhost:8080/api/auth/login", {
        username: credentials.username,
        password: credentials.password,
      });
      
      const { token } = response.data;
      login(token);
      
      
      navigate("/home");
    } catch (error) {
      if (error.response && error.response.status === 401) {
        setError("Invalid username or password.");
      } else {
        setError("Unexpected error occurred. Please try again.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container gradient-bg">
      <div className="position-absolute top-0 end-0 m-3">
      </div>
      
      <div className="auth-card glass-card animate__animated animate__fadeInUp">
        {/* Logo e título */}
        <div className="text-center mb-4">
          <div className="mb-3">
            <i className="bi bi-check-circle-fill display-4" style={{ color: 'var(--primary)' }}></i>
          </div>
          <h1 className="fw-bold mb-2" style={{ 
            fontFamily: 'Poppins, sans-serif',
            fontSize: '2.5rem'
          }}>
            TodoList
          </h1>
          <p className="mb-0">Manage your tasks efficiently</p>
        </div>

        <h4 className="text-center mb-4">
          <i className="bi bi-person-circle me-2"></i>
          Sign In
        </h4>

        {error && (
          <div className="alert alert-danger animate__animated animate__shake">
            <i className="bi bi-exclamation-triangle me-2"></i>
            {error}
          </div>
        )}

        <form onSubmit={handleLogin}>
          <div className="mb-3">
            <label htmlFor="login-username" className="form-label">
              <i className="bi bi-person me-2"></i>
              Username
            </label>
            <div className="input-group">
              <span className="input-group-text">
                <i className="bi bi-person"></i>
              </span>
              <input
                type="text"
                id="login-username"
                className="form-control"
                placeholder="Enter your username"
                value={credentials.username}
                onChange={(e) =>
                  setCredentials({ ...credentials, username: e.target.value })
                }
                required
              />
            </div>
          </div>

          <div className="mb-4">
            <label htmlFor="login-password" className="form-label">
              <i className="bi bi-lock me-2"></i>
              Password
            </label>
            <div className="input-group">
              <span className="input-group-text">
                <i className="bi bi-lock"></i>
              </span>
              <input
                type="password"
                id="login-password"
                className="form-control"
                placeholder="Enter your password"
                value={credentials.password}
                onChange={(e) =>
                  setCredentials({ ...credentials, password: e.target.value })
                }
                required
              />
            </div>
          </div>

          <div className="d-grid mb-3">
            <button 
              type="submit" 
              className="btn btn-gradient text-white fw-bold py-3"
              disabled={loading}
            >
              {loading ? (
                <>
                  <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                  Signing In...
                </>
              ) : (
                <>
                  <i className="bi bi-box-arrow-in-right me-2"></i>
                  Sign In
                </>
              )}
            </button>
          </div>
        </form>

        <div className="text-center">
          <p className="mb-2">Don't have an account?</p>
          <button
            className="btn btn-outline-light fw-bold"
            onClick={() => navigate("/register")}
          >
            <i className="bi bi-person-plus me-2"></i>
            Create Account
          </button>
        </div>

      </div>
    </div>
  );
};

export default Login;