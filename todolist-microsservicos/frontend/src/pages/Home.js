import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/taskService";
import Header from "../components/Header";
import Footer from "../components/Footer";

import {
  STATUS_MAP,
  COLUMN_STYLES,
  priorityOrder
} from "../utils/Constants";

// Função para mapear cor do status (usando COLUMN_STYLES)
const getStatusStyle = (status) => {
  return COLUMN_STYLES[status] || {
    background: "#f8f9fa",
    borderColor: "#ccc",
  };
};

// Função para estilo do badge de prioridade
const getPriorityBadgeClass = (priority) => {
  switch (priority) {
    case "CRITICAL":
      return "badge bg-danger";
    case "HIGH":
      return "badge bg-warning text-dark";
    case "MEDIUM":
      return "badge bg-info text-dark";
    default:
      return "badge bg-secondary";
  }
};

const Home = () => {
  const [username, setUsername] = useState("");
  const [idea, setIdea] = useState("Loading idea...");
  const [tasks, setTasks] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    // Busca info do usuário e frase inspiradora
    api.get("/home/user-info")
      .then((response) => {
        setUsername(response.data.username || "Guest");
        setIdea(response.data.idea);
      })
      .catch((error) => {
        console.error("Error fetching user info:", error);
      });

    // Tarefas de hoje
    api.get("/home/tasks/today")
      .then((response) => {
        setTasks(response.data);
      })
      .catch((error) => {
        console.error("Error fetching tasks:", error);
      });
  }, []);

  // Ordena localmente as tarefas por prioridade (CRITICAL > HIGH > MEDIUM > LOW)
  const sortedTasks = [...tasks].sort(
    (a, b) => priorityOrder[b.priority] - priorityOrder[a.priority]
  );

  // Cálculo de progresso (qtd concluídas vs total)
  const totalTasks = sortedTasks.length;
  const doneTasks = sortedTasks.filter((t) => t.status === "CONCLUIDO").length;
  const progressPercentage = totalTasks > 0
    ? Math.round((doneTasks / totalTasks) * 100)
    : 0;

  return (
    <div className="d-flex flex-column min-vh-100">
      <Header />

      <div className="container mt-4 flex-grow-1">
        {/* Título e Inspiração */}
        <div className="text-center mb-4">
          <h2 className="fw-bold">Welcome, {username}!</h2>
          
            {/* Botão para navegar para Tasks (maior) */}
            <button
              className="btn btn-primary btn-lg"
              onClick={() => navigate("/tasks")}
            >
              GO TO TASKS
            </button>
          <p className="lead mb-0 mt-3">Here's something to inspire you today:</p>
        </div>

        {/* Card da Quote */}
        <div className="row justify-content-center mb-5">
          <div className="col-md-8">
            <div className="card shadow-sm">
              <div className="card-body">
                <blockquote className="blockquote mb-0">
                  <p className="fs-4" style={{ fontStyle: "italic" }}>
                    {idea}
                  </p>
                  <footer className="blockquote-footer mt-3">
                    Your daily inspiration
                  </footer>
                </blockquote>
              </div>
            </div>
          </div>
        </div>

        {/* Barra de progresso, se tiver tasks */}
        {totalTasks > 0 && (
          <div className="row justify-content-center mb-4">
            <div className="col-md-8">
              <div className="d-flex justify-content-between mb-2">
                <span>You have {totalTasks} tasks today</span>
                <span>{doneTasks} done</span>
              </div>
              <div className="progress" style={{ height: "1.5rem" }}>
                <div
                  className="progress-bar progress-bar-striped progress-bar-animated"
                  role="progressbar"
                  style={{ width: `${progressPercentage}%` }}
                  aria-valuenow={progressPercentage}
                  aria-valuemin="0"
                  aria-valuemax="100"
                >
                  {progressPercentage}%
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Tarefas de hoje em "cartões coloridos" */}
        <div className="row justify-content-center">
          <div className="col-md-8">
            <h3 className="mb-3">Your Tasks for Today</h3>
            {sortedTasks.length > 0 ? (
              <div className="row row-cols-1 row-cols-md-2 g-3 mb-4">
                {sortedTasks.map((task) => {
                  const style = getStatusStyle(task.status);
                  return (
                    <div className="col" key={task.id}>
                      <div
                        className="card h-100 shadow-sm"
                        style={{
                          backgroundColor: style.background,
                          borderLeft: `7px solid ${style.borderColor}`
                        }}
                      >
                        <div className="card-body d-flex flex-column justify-content-between">
                          <div>
                            {/* Título + prioridade */}
                            <div className="d-flex align-items-center justify-content-between mb-2">
                              <h5 className="card-title mb-0">{task.title}</h5>
                              {task.priority && (
                                <span className={getPriorityBadgeClass(task.priority)}>
                                  {task.priority}
                                </span>
                              )}
                            </div>

                            {/* Status */}
                            <p className="mb-1" style={{ fontWeight: 500 }}>
                              Status: <span>{STATUS_MAP[task.status]}</span>
                            </p>

                            {/* Se quiser exibir dueDate */}
                            {task.dueDate && (
                              <p className="text-muted mb-1" style={{ fontSize: "0.9rem" }}>
                                Due: {task.dueDate}
                              </p>
                            )}
                          </div>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            ) : (
              <div className="alert alert-secondary" role="alert">
                No tasks for today. Take some time to relax!
              </div>
            )}
          </div>
        </div>
      </div>

      <Footer />
    </div>
  );
};

export default Home;