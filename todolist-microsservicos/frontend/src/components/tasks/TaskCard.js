import React from "react";
import { STATUS_MAP } from "../../utils/Constants";

function TaskCard({ task, onDeleteClick, onEditClick }) {
  return (
    <div
      className="card mb-3 shadow-sm"
      style={{ cursor: "pointer" }}
      onClick={onEditClick} // se clicar no card, chama onEditClick
    >
      <div
        className="card-body"
        style={{ transition: "background-color 0.2s" }}
        onMouseEnter={(e) => {
          e.currentTarget.style.backgroundColor = "rgba(0,0,0,0.05)";
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.backgroundColor = "";
        }}
      >
        {/* Título e botão Delete */}
        <div className="d-flex justify-content-between">
          <h5 className="card-title mb-1">{task.title}</h5>
          <button
            className="btn btn-sm btn-danger"
            onClick={(e) => {
              e.stopPropagation();
              onDeleteClick();
            }}
          >
            Delete
          </button>
        </div>

        {/* Badge de prioridade (criticidade) */}
        {task.priority && (
          <span
            className={`badge text-bg-${
              task.priority === "CRITICAL"
                ? "danger"
                : task.priority === "HIGH"
                ? "warning text-dark"
                : task.priority === "MEDIUM"
                ? "info text-dark"
                : "secondary"
            } mb-2`}
          >
            {task.priority}
          </span>
        )}

        {/* Descrição */}
        {task.description && (
          <p className="card-text mb-1">{task.description}</p>
        )}

        {/* DueDate */}
        {task.dueDate && (
          <p className="card-text mb-1">
            <small className="text-muted">Due: {task.dueDate}</small>
          </p>
        )}

        {/* Status */}
        <p className="card-text">
          <small className="text-muted">
            Status: {STATUS_MAP[task.status] || task.status}
          </small>
        </p>
      </div>
    </div>
  );
}

export default TaskCard;