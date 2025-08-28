import React, { useEffect, useState } from "react";
import Header from "../components/Header";
import Footer from "../components/Footer";
import { getAdminStats, getAllUsers } from "../services/authService";
import { getTaskStats } from "../services/taskService";

const AdminDashboard = () => {
  const [userStats, setUserStats] = useState(null);
  const [taskStats, setTaskStats] = useState(null);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchAdminData = async () => {
      try {
        // Fetch all admin data in parallel
        const [userStatsData, taskStatsData, usersData] = await Promise.all([
          getAdminStats(),
          getTaskStats(),
          getAllUsers()
        ]);

        setUserStats(userStatsData);
        setTaskStats(taskStatsData);
        setUsers(usersData);
      } catch (err) {
        console.error("Error fetching admin data:", err);
        setError("You are not authorized or an error occurred.");
      } finally {
        setLoading(false);
      }
    };

    fetchAdminData();
  }, []);

  if (loading) {
    return (
      <div className="d-flex flex-column min-vh-100">
        <Header />
        <div className="container mt-5 flex-grow-1 text-center">
          <div className="spinner-border text-primary" style={{ width: '3rem', height: '3rem' }}>
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="mt-3 text-muted">Loading admin dashboard...</p>
        </div>
        <Footer />
      </div>
    );
  }

  return (
    <div className="d-flex flex-column min-vh-100 fade-in">
      <Header />
      
      <div className="container mt-4 flex-grow-1">
        {/* Dashboard Header */}
        <div className="row mb-4">
          <div className="col">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <h2 className="mb-1 fw-bold dashboard-title">
                  <i className="bi bi-shield-check me-2 text-primary"></i>
                  Admin Dashboard
                </h2>
                <p className="text-muted mb-0">
                  <i className="bi bi-calendar-date me-1"></i>
                  {new Date().toLocaleDateString('en-US', { 
                    weekday: 'long', 
                    year: 'numeric', 
                    month: 'long', 
                    day: 'numeric' 
                  })}
                </p>
              </div>
            </div>
          </div>
        </div>

        {error && (
          <div className="alert alert-danger">
            <i className="bi bi-exclamation-triangle-fill me-2"></i>
            {error}
          </div>
        )}

        {/* User Statistics */}
        {userStats && (
          <div className="row mb-4">
            <div className="col">
              <h4 className="mb-3">
                <i className="bi bi-people me-2"></i>
                User Statistics
              </h4>
            </div>
          </div>
        )}

        <div className="row mb-4">
          {userStats && (
            <>
              <div className="col-md-4 col-sm-6 mb-3">
                <div className="modern-card p-3 text-center">
                  <div className="text-primary fs-2 mb-2">
                    <i className="bi bi-people-fill"></i>
                  </div>
                  <h3 className="mb-1">{userStats.totalUsers}</h3>
                  <p className="text-muted mb-0 small">Total Users</p>
                </div>
              </div>
              <div className="col-md-4 col-sm-6 mb-3">
                <div className="modern-card p-3 text-center">
                  <div className="text-warning fs-2 mb-2">
                    <i className="bi bi-person-fill"></i>
                  </div>
                  <h3 className="mb-1">{userStats.totalRegularUsers}</h3>
                  <p className="text-muted mb-0 small">Regular Users</p>
                </div>
              </div>
              <div className="col-md-4 col-sm-6 mb-3">
                <div className="modern-card p-3 text-center">
                  <div className="text-danger fs-2 mb-2">
                    <i className="bi bi-shield-fill"></i>
                  </div>
                  <h3 className="mb-1">{userStats.totalAdmins}</h3>
                  <p className="text-muted mb-0 small">Admins</p>
                </div>
              </div>
            </>
          )}
        </div>

        {/* Task Statistics */}
        {taskStats && (
          <div className="row mb-4">
            <div className="col">
              <h4 className="mb-3">
                <i className="bi bi-list-task me-2"></i>
                Task Statistics
              </h4>
            </div>
          </div>
        )}

        <div className="row mb-4">
          {taskStats && (
            <>
              <div className="col-md-3 col-sm-6 mb-3">
                <div className="modern-card p-3 text-center">
                  <div className="text-info fs-2 mb-2">
                    <i className="bi bi-clipboard-data"></i>
                  </div>
                  <h3 className="mb-1">{taskStats.totalTasks}</h3>
                  <p className="text-muted mb-0 small">Total Tasks</p>
                </div>
              </div>
              <div className="col-md-3 col-sm-6 mb-3">
                <div className="modern-card p-3 text-center">
                  <div className="text-warning fs-2 mb-2">
                    <i className="bi bi-clock"></i>
                  </div>
                  <h3 className="mb-1">{taskStats.pendingTasks}</h3>
                  <p className="text-muted mb-0 small">Pending</p>
                </div>
              </div>
              <div className="col-md-3 col-sm-6 mb-3">
                <div className="modern-card p-3 text-center">
                  <div className="text-primary fs-2 mb-2">
                    <i className="bi bi-arrow-repeat"></i>
                  </div>
                  <h3 className="mb-1">{taskStats.inProgressTasks}</h3>
                  <p className="text-muted mb-0 small">In Progress</p>
                </div>
              </div>
              <div className="col-md-3 col-sm-6 mb-3">
                <div className="modern-card p-3 text-center">
                  <div className="text-success fs-2 mb-2">
                    <i className="bi bi-check-circle"></i>
                  </div>
                  <h3 className="mb-1">{taskStats.completedTasks}</h3>
                  <p className="text-muted mb-0 small">Completed</p>
                </div>
              </div>
            </>
          )}
        </div>

        {/* User List */}
        {users.length > 0 && (
          <div className="row">
            <div className="col">
              <div className="modern-card">
                <div className="card-header bg-transparent border-0 p-4">
                  <h4 className="mb-0">
                    <i className="bi bi-people me-2"></i>
                    User Management
                  </h4>
                </div>
                <div className="card-body p-4">
                  <div className="table-responsive">
                    <table className="table table-hover">
                      <thead>
                        <tr>
                          <th scope="col">
                            <i className="bi bi-person me-1"></i>
                            Username
                          </th>
                          <th scope="col">
                            <i className="bi bi-shield me-1"></i>
                            Role
                          </th>
                          <th scope="col">
                            <i className="bi bi-key me-1"></i>
                            ID
                          </th>
                        </tr>
                      </thead>
                      <tbody>
                        {users.map((user) => (
                          <tr key={user.id}>
                            <td>
                              <div className="d-flex align-items-center">
                                <div className="avatar-sm me-2">
                                  <i className="bi bi-person-circle fs-4"></i>
                                </div>
                                <span className="fw-medium">{user.username}</span>
                              </div>
                            </td>
                            <td>
                              <span className={`badge ${user.role === 'ADMIN' ? 'bg-danger' : 'bg-primary'}`}>
                                <i className={`bi ${user.role === 'ADMIN' ? 'bi-shield-fill' : 'bi-person-fill'} me-1`}></i>
                                {user.role}
                              </span>
                            </td>
                            <td>
                              <code className="text-muted small">{user.id}</code>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      <Footer />
    </div>
  );
};

export default AdminDashboard;