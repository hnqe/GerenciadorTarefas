import React from "react";

function DeleteConfirmModal({ taskToDelete, setTaskToDelete, handleDeleteTask }) {
  return (
    <div
      className="modal fade"
      id="deleteConfirmModal"
      tabIndex="-1"
      aria-labelledby="deleteConfirmModalLabel"
      aria-hidden="true"
    >
      <div className="modal-dialog modal-dialog-centered modal-sm">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title" id="deleteConfirmModalLabel">
              Confirm Delete
            </h5>
            <button type="button" className="btn-close" data-bs-dismiss="modal" />
          </div>
          <div className="modal-body">
            Are you sure you want to delete the task{" "}
            <strong>{taskToDelete?.title}</strong>?
          </div>
          <div className="modal-footer">
            <button
              className="btn btn-secondary"
              data-bs-dismiss="modal"
              onClick={() => setTaskToDelete(null)}
            >
              Cancel
            </button>
            <button className="btn btn-danger" onClick={handleDeleteTask}>
              Yes, Delete
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default DeleteConfirmModal;
