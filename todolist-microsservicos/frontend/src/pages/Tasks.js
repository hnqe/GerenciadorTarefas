import React, { useState, useEffect } from "react";
import Header from "../components/Header";
import Footer from "../components/Footer";
import api from "../services/taskService"; // Axios p/ TaskService
import TaskColumn from "../components/tasks/TaskColumn";
import AddTaskModal from "../components/tasks/AddTaskModal";
import EditTaskModal from "../components/tasks/EditTaskModal";
import DeleteConfirmModal from "../components/tasks/DeleteConfirmModal";

// Função auxiliar p/ fechar modal via Bootstrap
function closeBootstrapModal(modalId) {
  const modalElement = document.getElementById(modalId);
  if (!modalElement) return;
  const modalInstance =
    window.bootstrap.Modal.getInstance(modalElement) ||
    new window.bootstrap.Modal(modalElement);
  modalInstance.hide();
}

// Interceptor p/ enviar token (caso não tenha feito antes)
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("jwt");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

const Tasks = () => {
  const [tasks, setTasks] = useState([]);
  const [newTask, setNewTask] = useState({
    title: "",
    description: "",
    dueDate: "",
    status: "PENDENTE",
    priority: "LOW",
  });
  const [editTask, setEditTask] = useState(null);
  const [taskToDelete, setTaskToDelete] = useState(null);

  // Carrega tasks ao montar
  useEffect(() => {
    fetchTasks();
  }, []);

  // Busca todas as tasks do back
  const fetchTasks = async () => {
    try {
      const response = await api.get("/tasks");
      setTasks(response.data);
    } catch (error) {
      console.error("Error fetching tasks:", error);
    }
  };

  // Cria nova task
  const handleCreateTask = async (e) => {
    e.preventDefault();
    try {
      await api.post("/tasks", newTask);
      fetchTasks();
      setNewTask({
        title: "",
        description: "",
        dueDate: "",
        status: "PENDENTE",
        priority: "LOW",
      });
      closeBootstrapModal("addTaskModal");
    } catch (error) {
      console.error("Error creating task:", error);
    }
  };

  // Edita task
  const handleEditTask = async (e) => {
    e.preventDefault();
    if (!editTask) return;
    try {
      await api.put(`/tasks/edit/${editTask.id}`, editTask);
      fetchTasks();
      setEditTask(null);
      closeBootstrapModal("editTaskModal");
    } catch (error) {
      console.error("Error editing task:", error);
    }
  };

  // Ao clicar no card, chama esta função
  const openEditModal = (task) => {
    // Seta a task que vamos editar
    setEditTask(task);

    // Força a abertura do modal via Bootstrap
    const modalEl = document.getElementById("editTaskModal");
    if (modalEl) {
      const modalInstance =
        window.bootstrap.Modal.getInstance(modalEl) ||
        new window.bootstrap.Modal(modalEl);
      modalInstance.show();
    }
  };

  // Deletar
  const handleDeleteTask = async () => {
    if (!taskToDelete) return;
    try {
      await api.delete(`/tasks/delete/${taskToDelete.id}`);
      fetchTasks();
      setTaskToDelete(null);
      closeBootstrapModal("deleteConfirmModal");
    } catch (error) {
      console.error("Error deleting task:", error);
    }
  };

  // Drag & Drop
  const handleDragStart = (e, task) => {
    e.dataTransfer.setData("taskId", task.id);
    e.dataTransfer.setData("oldStatus", task.status);
  };
  const handleDragOver = (e) => e.preventDefault();
  const handleDrop = async (e, newStatus) => {
    e.preventDefault();
    const taskId = e.dataTransfer.getData("taskId");
    const oldStatus = e.dataTransfer.getData("oldStatus");
    if (!taskId || oldStatus === newStatus) return;

    const taskToUpdate = tasks.find((t) => t.id === taskId);
    if (!taskToUpdate) return;

    try {
      await api.put(`/tasks/edit/${taskId}`, {
        ...taskToUpdate,
        status: newStatus,
      });
      fetchTasks();
    } catch (err) {
      console.error("Error moving task:", err);
    }
  };

  // Colunas do Kanban
  const columns = [
    { value: "PENDENTE", label: "Pending" },
    { value: "EM_ANDAMENTO", label: "In Progress" },
    { value: "CONCLUIDO", label: "Done" },
  ];

  return (
    <div className="d-flex flex-column min-vh-100">
      <Header />

      <div className="container mt-4 flex-grow-1">
        {/* Botão p/ abrir modal de criar */}
        <button
          className="btn btn-primary mb-4"
          data-bs-toggle="modal"
          data-bs-target="#addTaskModal"
        >
          + Add New Task
        </button>

        {/* As colunas do Kanban */}
        <div className="row g-4">
          {columns.map((col) => (
            <TaskColumn
              key={col.value}
              title={col.label}
              columnKey={col.value}
              tasks={tasks}
              onEditTask={openEditModal}   // aqui passamos a ref p/ abrir modal
              onDeleteTask={(task) => setTaskToDelete(task)}
              onDragStart={handleDragStart}
              onDragOver={handleDragOver}
              onDrop={handleDrop}
            />
          ))}
        </div>
      </div>

      {/* MODALS */}
      <AddTaskModal
        newTask={newTask}
        setNewTask={setNewTask}
        handleCreateTask={handleCreateTask}
      />

      <EditTaskModal
        editTask={editTask}
        setEditTask={setEditTask}
        handleEditTask={handleEditTask}
      />

      <DeleteConfirmModal
        taskToDelete={taskToDelete}
        setTaskToDelete={setTaskToDelete}
        handleDeleteTask={handleDeleteTask}
      />

      <Footer />
    </div>
  );
};

export default Tasks;