import React from "react";
import { PRIORITIES } from "../../utils/Constants";

function AddTaskModal({ newTask, setNewTask, handleCreateTask }) {
  return (
    <div
      className="modal fade"
      id="addTaskModal"
      tabIndex="-1"
      aria-labelledby="addTaskModalLabel"
      aria-hidden="true"
    >
      <div className="modal-dialog">
        <form onSubmit={handleCreateTask}>
          <div className="modal-content">
            <div className="modal-header">
              <h5 className="modal-title" id="addTaskModalLabel">
                Add New Task
              </h5>
              <button
                type="button"
                className="btn-close"
                data-bs-dismiss="modal"
              />
            </div>
            <div className="modal-body">
              <input
                type="text"
                placeholder="Title"
                className="form-control mb-3"
                value={newTask.title}
                onChange={(e) => setNewTask({ ...newTask, title: e.target.value })}
                required
              />
              <textarea
                placeholder="Description"
                className="form-control mb-3"
                value={newTask.description}
                onChange={(e) => setNewTask({ ...newTask, description: e.target.value })}
              />
              <input
                type="date"
                className="form-control mb-3"
                value={newTask.dueDate}
                onChange={(e) => setNewTask({ ...newTask, dueDate: e.target.value })}
              />
              <select
                className="form-select mb-3"
                value={newTask.status}
                onChange={(e) => setNewTask({ ...newTask, status: e.target.value })}
              >
                <option value="PENDENTE">Pending</option>
                <option value="EM_ANDAMENTO">In Progress</option>
                <option value="CONCLUIDO">Done</option>
              </select>
              <select
                className="form-select"
                value={newTask.priority}
                onChange={(e) => setNewTask({ ...newTask, priority: e.target.value })}
              >
                {PRIORITIES.map((p) => (
                  <option key={p.value} value={p.value}>
                    {p.label}
                  </option>
                ))}
              </select>
            </div>
            <div className="modal-footer">
              <button type="submit" className="btn btn-primary">
                Save
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}

export default AddTaskModal;
