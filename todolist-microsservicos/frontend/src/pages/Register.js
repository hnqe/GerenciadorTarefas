import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { register } from "../services/authService";

const Register = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [success, setSuccess] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();
    setError("");
    try {
      const responseMsg = await register(username, password);
      setSuccess(responseMsg);
      setTimeout(() => navigate("/login"), 2000);
    } catch (err) {
      setError(
        err.response?.data || "Registration failed. Username might already be taken."
      );
    }
  };

  return (
    <div className="d-flex flex-column min-vh-100 justify-content-center align-items-center bg-light">
      <div className="card shadow-sm" style={{ width: "420px", maxWidth: "90%" }}>
        <div className="card-body p-4">
          <h3 className="text-center fw-bold mb-3" style={{ fontFamily: "Georgia, serif" }}>
            TO DO
          </h3>
          <h4 className="card-title text-center mb-4">Register</h4>

          {success && <div className="alert alert-success">{success}</div>}
          {error && <div className="alert alert-danger">{error}</div>}

          <form onSubmit={handleRegister}>
            <div className="mb-3">
              <label htmlFor="reg-username" className="form-label">
                Username
              </label>
              <input
                type="text"
                id="reg-username"
                className="form-control"
                placeholder="Choose a username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
                minLength={3}
                maxLength={20}
              />
            </div>

            <div className="mb-3">
              <label htmlFor="reg-password" className="form-label">
                Password
              </label>
              <input
                type="password"
                id="reg-password"
                className="form-control"
                placeholder="Choose a password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                minLength={6}
              />
            </div>

            <div className="d-grid">
              <button type="submit" className="btn btn-primary fw-bold">
                Create Account
              </button>
            </div>
          </form>

          <p className="mt-3 text-center">
            Already have an account?{" "}
            <button
              className="btn btn-link p-0"
              onClick={() => navigate("/login")}
            >
              Login here
            </button>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Register;