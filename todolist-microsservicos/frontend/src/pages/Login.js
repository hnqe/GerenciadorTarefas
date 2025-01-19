import axios from "axios";
import React, { useContext, useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthContext } from "../context/AuthContext";

const Login = () => {
  const { login } = useContext(AuthContext);
  const [credentials, setCredentials] = useState({ username: "", password: "" });
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");
    try {
      const response = await axios.post("http://localhost:8080/api/auth/login", {
        username: credentials.username,
        password: credentials.password,
      });
      const { token } = response.data;
      login(token); // Salva no contexto e no localStorage
      navigate("/home");
    } catch (error) {
      if (error.response && error.response.status === 401) {
        setError("Invalid username or password.");
      } else {
        setError("Unexpected error occurred. Please try again.");
      }
    }
  };

  return (
    <div className="d-flex flex-column min-vh-100 justify-content-center align-items-center bg-light">
      <div className="card shadow-sm" style={{ width: "420px", maxWidth: "90%" }}>
        <div className="card-body p-4">
          {/* Nome da aplicação no topo */}
          <h3 className="text-center fw-bold mb-3" style={{ fontFamily: "Georgia, serif" }}>
            TO DO
          </h3>

          <h4 className="card-title text-center mb-4">Login</h4>

          {error && <div className="alert alert-danger">{error}</div>}

          <form onSubmit={handleLogin}>
            <div className="mb-3">
              <label htmlFor="login-username" className="form-label">
                Username
              </label>
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

            <div className="mb-3">
              <label htmlFor="login-password" className="form-label">
                Password
              </label>
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

            <div className="d-grid">
              <button type="submit" className="btn btn-primary fw-bold">
                Sign In
              </button>
            </div>
          </form>

          <div className="mt-3 text-center">
            <span>Don't have an account?</span>{" "}
            <button
              className="btn btn-link p-0"
              onClick={() => navigate("/register")}
            >
              Register here
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;