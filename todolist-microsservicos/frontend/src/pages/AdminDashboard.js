import React, { useEffect, useState } from "react";
import Header from "../components/Header";
import Footer from "../components/Footer";
import { getAdminDashboard } from "../services/authService";

const AdminDashboard = () => {
  const [adminMessage, setAdminMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    getAdminDashboard()
      .then((respData) => setAdminMessage(respData))
      .catch((err) => {
        console.error("Error fetching admin dashboard:", err);
        setError("You are not authorized or an error occurred.");
      });
  }, []);

  return (
    <div className="d-flex flex-column min-vh-100">
      <Header />
      <div className="container mt-5 flex-grow-1">
        <h2>Admin Dashboard</h2>
        {error && <div className="alert alert-danger">{error}</div>}
        {adminMessage && <p>{adminMessage}</p>}
      </div>
      <Footer />
    </div>
  );
};

export default AdminDashboard;