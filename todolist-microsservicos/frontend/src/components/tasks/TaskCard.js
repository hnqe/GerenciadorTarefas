import React from "react";
import { STATUS_MAP } from "../../utils/Constants";

function TaskCard({ task, onDeleteClick, onEditClick }) {
  return (
    <div
      className="task-card"
      style={{ cursor: "pointer" }}
      onClick={onEditClick}
    >
      {/* Título e botão Delete */}
      <div className="d-flex justify-content-between align-items-start mb-2">
        <h5 className="mb-0 fw-semibold task-title">{task.title}</h5>
        <button
          className="btn btn-sm"
          style={{
            background: 'var(--danger)',
            color: 'var(--text-inverse)',
            border: 'none'
          }}
          onClick={(e) => {
            e.stopPropagation();
            onDeleteClick();
          }}
          data-bs-toggle="modal"
          data-bs-target="#deleteConfirmModal"
        >
          <i className="bi bi-trash"></i>
        </button>
      </div>

      {/* Badge de prioridade (criticidade) */}
      {task.priority && (
        <span
          className={`priority-badge mb-2 ${
            task.priority === "CRITICAL"
              ? "priority-critical"
              : task.priority === "HIGH"
              ? "priority-high"
              : task.priority === "MEDIUM"
              ? "priority-medium"
              : "priority-low"
          }`}
        >
          {task.priority}
        </span>
      )}

      {/* Descrição */}
      {task.description && (
        <p className="mb-2 task-description">{task.description}</p>
      )}

      {/* DueDate */}
      {task.dueDate && (
        <p className="mb-2">
          <small className="task-due-date">
            <i className="bi bi-calendar-event me-1"></i>
            Due: {task.dueDate}
          </small>
        </p>
      )}

      {/* Status */}
      <div className="d-flex align-items-center">
        <span className={`status-badge status-${task.status?.toLowerCase()}`}>
          {STATUS_MAP[task.status] || task.status}
        </span>
      </div>
    </div>
  );
}

export default TaskCard;