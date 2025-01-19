import React from "react";
import TaskCard from "./TaskCard"; 
import { priorityOrder, COLUMN_STYLES } from "../../utils/Constants";

function TaskColumn({ 
  title, 
  columnKey, 
  tasks, 
  onEditTask, 
  onDeleteTask, 
  onDragStart, 
  onDragOver, 
  onDrop 
}) {
  // Filtra e ORDENA as tasks pela priority
  const filteredTasks = tasks
    .filter((t) => t.status === columnKey)
    .sort((a, b) => priorityOrder[b.priority] - priorityOrder[a.priority]);

  const style = COLUMN_STYLES[columnKey] || {
    background: "#f8f9fa",
    borderColor: "#ccc"
  };

  return (
    <div
      className="col-md-4"
      onDragOver={onDragOver}
      onDrop={(e) => onDrop(e, columnKey)}
    >
      <div
        className="p-3 border rounded"
        style={{
          backgroundColor: style.background,
          borderColor: style.borderColor,
          minHeight: "500px",
        }}
      >
        <h2 className="text-center">{title}</h2>
        <div style={{ marginTop: "1rem" }}>
          {filteredTasks.length === 0 && (
            <p className="text-muted fst-italic">No tasks here</p>
          )}
          {filteredTasks.map((task) => (
            <div
              key={task.id}
              draggable
              onDragStart={(e) => onDragStart(e, task)}
            >
              <TaskCard
                task={task}
                // Quando clica no card, chama onEditTask(task)
                onEditClick={() => onEditTask(task)}
                onDeleteClick={() => onDeleteTask(task)}
              />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default TaskColumn;