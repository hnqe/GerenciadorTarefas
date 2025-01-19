import React from "react";
import { PRIORITIES } from "../../utils/Constants";

function EditTaskModal({ editTask, setEditTask, handleEditTask }) {
  return (
    <div
      className="modal fade"
      id="editTaskModal"
      tabIndex="-1"
      aria-labelledby="editTaskModalLabel"
      aria-hidden="true"
    >
      <div className="modal-dialog">
        <form onSubmit={handleEditTask}>
          <div className="modal-content">
            <div className="modal-header">
              <h5 className="modal-title" id="editTaskModalLabel">
                Edit Task
              </h5>
              <button type="button" className="btn-close" data-bs-dismiss="modal" />
            </div>
            <div className="modal-body">
              <input
                type="text"
                placeholder="Title"
                className="form-control mb-3"
                value={editTask?.title || ""}
                onChange={(e) =>
                  setEditTask({ ...editTask, title: e.target.value })
                }
                required
              />
              <textarea
                placeholder="Description"
                className="form-control mb-3"
                value={editTask?.description || ""}
                onChange={(e) =>
                  setEditTask({ ...editTask, description: e.target.value })
                }
              />
              <input
                type="date"
                className="form-control mb-3"
                value={editTask?.dueDate || ""}
                onChange={(e) =>
                  setEditTask({ ...editTask, dueDate: e.target.value })
                }
              />
              <select
                className="form-select mb-3"
                value={editTask?.status || "PENDENTE"}
                onChange={(e) =>
                  setEditTask({ ...editTask, status: e.target.value })
                }
              >
                <option value="PENDENTE">Pending</option>
                <option value="EM_ANDAMENTO">In Progress</option>
                <option value="CONCLUIDO">Done</option>
              </select>
              <select
                className="form-select"
                value={editTask?.priority || "LOW"}
                onChange={(e) =>
                  setEditTask({ ...editTask, priority: e.target.value })
                }
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
                Save Changes
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}

export default EditTaskModal;
