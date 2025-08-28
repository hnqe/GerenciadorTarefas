import React, { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import Header from "../components/Header";
import Footer from "../components/Footer";
import api from "../services/taskService";
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
  const location = useLocation();
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isInitialLoad, setIsInitialLoad] = useState(true);
  const [newTask, setNewTask] = useState({
    title: "",
    description: "",
    dueDate: "",
    status: "PENDENTE",
    priority: "LOW",
  });
  const [editTask, setEditTask] = useState(null);
  const [taskToDelete, setTaskToDelete] = useState(null);
  const [filterPriority, setFilterPriority] = useState("ALL");
  const [searchTerm, setSearchTerm] = useState("");

  // Carrega tasks ao montar
  useEffect(() => {
    fetchTasks();
    // Após o primeiro carregamento, marca que não é mais inicial
    const timer = setTimeout(() => setIsInitialLoad(false), 1000);
    return () => clearTimeout(timer);
  }, []);

  // Listener para atualizações de tasks de outras páginas (ex: Pomodoro)
  useEffect(() => {
    const handleStorageChange = (event) => {
      if (event.key === 'taskUpdate') {
        try {
          const taskUpdate = JSON.parse(event.newValue || '{}');
          console.log('🔄 Task update received from Pomodoro:', taskUpdate);
          
          if (taskUpdate.type === 'TASK_COMPLETED' || taskUpdate.type === 'TASK_STATUS_UPDATED') {
            // Update the specific task in current state
            setTasks(prevTasks => 
              prevTasks.map(task => 
                task.id === taskUpdate.taskId 
                  ? { ...task, status: taskUpdate.newStatus }
                  : task
              )
            );
            
            const actionLabel = taskUpdate.type === 'TASK_COMPLETED' ? 'completed' : 'updated';
            console.log(`✅ Task ${taskUpdate.taskId} ${actionLabel} as ${taskUpdate.newStatus} in Tasks page`);
          }
        } catch (error) {
          console.error('Error parsing task update:', error);
        }
      }
    };

    // Listen for storage changes from other tabs/components
    window.addEventListener('storage', handleStorageChange);
    
    // Also listen for custom events in same tab
    const handleCustomEvent = (event) => {
      if (event.detail && (event.detail.type === 'TASK_COMPLETED' || event.detail.type === 'TASK_STATUS_UPDATED')) {
        const taskUpdate = event.detail;
        setTasks(prevTasks => 
          prevTasks.map(task => 
            task.id === taskUpdate.taskId 
              ? { ...task, status: taskUpdate.newStatus }
              : task
          )
        );
        const actionLabel = taskUpdate.type === 'TASK_COMPLETED' ? 'completed' : 'updated';
        console.log(`✅ Task ${taskUpdate.taskId} ${actionLabel} via custom event`);
      }
    };
    
    window.addEventListener('taskUpdate', handleCustomEvent);
    
    return () => {
      window.removeEventListener('storage', handleStorageChange);
      window.removeEventListener('taskUpdate', handleCustomEvent);
    };
  }, []);

  // Detecta navegação para a página tasks após carregamento inicial
  useEffect(() => {
    if (!isInitialLoad && location.pathname === '/tasks') {
      // Só recarrega se for uma navegação real (não apenas mudança de estado)
      const timer = setTimeout(() => {
        // Se não há tasks ou são muito poucos, vale a pena recarregar
        if (tasks.length === 0) {
          fetchTasks(true); // Força mostrar notificação
        } else {
          // Se já tem tasks, só mostra notificação sem recarregar
          console.log(`Tasks updated: Found ${tasks.length} tasks`);
        }
      }, 100);
      return () => clearTimeout(timer);
    }
  }, [location.pathname, isInitialLoad]);

  // Busca todas as tasks do back
  const fetchTasks = async () => {
    setLoading(true);
    try {
      const response = await api.get("/tasks");
      setTasks(response.data);
      // Simple console log instead of notification
      if (!isInitialLoad) {
        console.log(`Tasks updated: Found ${response.data.length} tasks`);
      }
    } catch (error) {
      console.error("Error fetching tasks:", error);
    } finally {
      setLoading(false);
    }
  };

  // Cria nova task com notificação
  const handleCreateTask = async (e) => {
    e.preventDefault();
    try {
      const response = await api.post("/tasks", newTask);
      const createdTask = response.data;
      
      
      fetchTasks();
      setNewTask({
        title: "",
        description: "",
        dueDate: "",
        status: "PENDENTE",
        priority: "LOW",
      });
      closeBootstrapModal("addTaskModal");
      
      console.log(`Task created: ${createdTask.title}`);
    } catch (error) {
      console.error("Error creating task:", error);
    }
  };

  // Edita task com detecção de mudanças
  const handleEditTask = async (e) => {
    e.preventDefault();
    if (!editTask) return;
    
    try {
      const originalTask = tasks.find(t => t.id === editTask.id);
      const changes = {};
      
      // Detectar mudanças
      if (originalTask.title !== editTask.title) changes.title = editTask.title;
      if (originalTask.description !== editTask.description) changes.description = editTask.description;
      if (originalTask.dueDate !== editTask.dueDate) changes.dueDate = editTask.dueDate;
      if (originalTask.priority !== editTask.priority) changes.priority = editTask.priority;
      if (originalTask.status !== editTask.status) changes.status = editTask.status;
      
      await api.put(`/tasks/edit/${editTask.id}`, editTask);
      
      
      fetchTasks();
      setEditTask(null);
      closeBootstrapModal("editTaskModal");
      
      console.log(`Task updated: ${editTask.title}`);
    } catch (error) {
      console.error("Error editing task:", error);
    }
  };

  // Ao clicar no card, chama esta função
  const openEditModal = (task) => {
    setEditTask(task);
    const modalEl = document.getElementById("editTaskModal");
    if (modalEl) {
      const modalInstance =
        window.bootstrap.Modal.getInstance(modalEl) ||
        new window.bootstrap.Modal(modalEl);
      modalInstance.show();
    }
  };

  // Deletar com confirmação
  const handleDeleteTask = async () => {
    if (!taskToDelete) return;
    try {
      await api.delete(`/tasks/delete/${taskToDelete.id}`);
      fetchTasks();
      setTaskToDelete(null);
      closeBootstrapModal("deleteConfirmModal");
      
      console.log(`Task deleted: ${taskToDelete.title}`);
    } catch (error) {
      console.error("Error deleting task:", error);
    }
  };

  // Drag & Drop melhorado
  const handleDragStart = (e, task) => {
    e.dataTransfer.setData("taskId", task.id);
    e.dataTransfer.setData("oldStatus", task.status);
    
    // Adicionar efeito visual
    e.target.style.opacity = "0.5";
  };

  const handleDragEnd = (e) => {
    e.target.style.opacity = "1";
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    e.dataTransfer.dropEffect = "move";
  };

  const handleDrop = async (e, newStatus) => {
    e.preventDefault();
    const taskId = e.dataTransfer.getData("taskId");
    const oldStatus = e.dataTransfer.getData("oldStatus");
    
    if (!taskId || oldStatus === newStatus) return;

    const taskToUpdate = tasks.find((t) => t.id === taskId);
    if (!taskToUpdate) return;

    // OTIMIZAÇÃO: Atualizar estado local imediatamente (UI responsiva)
    const updatedTask = { ...taskToUpdate, status: newStatus };
    setTasks(prevTasks => 
      prevTasks.map(task => 
        task.id === taskId ? updatedTask : task
      )
    );

    try {
      // Sync com servidor em background
      await api.put(`/tasks/edit/${taskId}`, updatedTask);
      
      
      // Não precisa mais do fetchTasks() - estado já foi atualizado!
      
      const statusLabels = {
        'PENDENTE': 'Pending',
        'EM_ANDAMENTO': 'In Progress', 
        'CONCLUIDO': 'Completed'
      };
      
      console.log(`Task moved: ${taskToUpdate.title} to ${statusLabels[newStatus]}`);
    } catch (err) {
      console.error("Error moving task:", err);
    }
  };

  // Filtrar tarefas
  const filteredTasks = tasks.filter(task => {
    const matchesPriority = filterPriority === "ALL" || task.priority === filterPriority;
    const matchesSearch = task.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         task.description?.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesPriority && matchesSearch;
  });

  // Estatísticas das tarefas
  const taskStats = {
    total: tasks.length,
    pending: tasks.filter(t => t.status === 'PENDENTE').length,
    inProgress: tasks.filter(t => t.status === 'EM_ANDAMENTO').length,
    completed: tasks.filter(t => t.status === 'CONCLUIDO').length
  };

  // Colunas do Kanban
  const columns = [
    { 
      value: "PENDENTE", 
      label: "Pending", 
      color: "warning",
      icon: "bi-clock",
      count: taskStats.pending 
    },
    { 
      value: "EM_ANDAMENTO", 
      label: "In Progress", 
      color: "info",
      icon: "bi-arrow-repeat",
      count: taskStats.inProgress 
    },
    { 
      value: "CONCLUIDO", 
      label: "Completed", 
      color: "success",
      icon: "bi-check-circle",
      count: taskStats.completed 
    },
  ];

  if (loading) {
    return (
      <div className="d-flex flex-column min-vh-100">
        <Header />
        <div className="container mt-5 flex-grow-1 text-center">
          <div className="spinner-border text-primary" style={{ width: '3rem', height: '3rem' }}>
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="mt-3 text-muted">Loading your tasks...</p>
        </div>
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
            <div className="d-flex justify-content-between align-items-center flex-wrap">
              <div>
                <h2 className="mb-1 fw-bold dashboard-title">My Tasks Dashboard</h2>
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
              
              <button
                className="btn btn-gradient text-white d-flex align-items-center pulse"
                data-bs-toggle="modal"
                data-bs-target="#addTaskModal"
              >
                <i className="bi bi-plus-circle me-2"></i>
                Add New Task
              </button>
            </div>
          </div>
        </div>

        {/* Task Statistics */}
        <div className="row mb-4">
          <div className="col-md-3 col-sm-6 mb-3">
            <div className="modern-card p-3 text-center">
              <div className="text-primary fs-2 mb-2">
                <i className="bi bi-list-task"></i>
              </div>
              <h3 className="mb-1">{taskStats.total}</h3>
              <p className="text-muted mb-0 small">Total Tasks</p>
            </div>
          </div>
          <div className="col-md-3 col-sm-6 mb-3">
            <div className="modern-card p-3 text-center">
              <div className="text-warning fs-2 mb-2">
                <i className="bi bi-clock"></i>
              </div>
              <h3 className="mb-1">{taskStats.pending}</h3>
              <p className="text-muted mb-0 small">Pending</p>
            </div>
          </div>
          <div className="col-md-3 col-sm-6 mb-3">
            <div className="modern-card p-3 text-center">
              <div className="text-info fs-2 mb-2">
                <i className="bi bi-arrow-repeat"></i>
              </div>
              <h3 className="mb-1">{taskStats.inProgress}</h3>
              <p className="text-muted mb-0 small">In Progress</p>
            </div>
          </div>
          <div className="col-md-3 col-sm-6 mb-3">
            <div className="modern-card p-3 text-center">
              <div className="text-success fs-2 mb-2">
                <i className="bi bi-check-circle"></i>
              </div>
              <h3 className="mb-1">{taskStats.completed}</h3>
              <p className="text-muted mb-0 small">Completed</p>
            </div>
          </div>
        </div>

        {/* Filters */}
        <div className="row mb-4">
          <div className="col-md-6">
            <div className="input-group">
              <span className="input-group-text">
                <i className="bi bi-search"></i>
              </span>
              <input
                type="text"
                className="form-control"
                placeholder="Search tasks..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
          </div>
          <div className="col-md-6">
            <select
              className="form-select"
              value={filterPriority}
              onChange={(e) => setFilterPriority(e.target.value)}
            >
              <option value="ALL">All Priorities</option>
              <option value="LOW">Low Priority</option>
              <option value="MEDIUM">Medium Priority</option>
              <option value="HIGH">High Priority</option>
              <option value="CRITICAL">Critical Priority</option>
            </select>
          </div>
        </div>

        {/* Kanban Board */}
        <div className="row g-4">
          {columns.map((col) => (
            <TaskColumn
              key={col.value}
              title={col.label}
              columnKey={col.value}
              tasks={filteredTasks}
              color={col.color}
              icon={col.icon}
              count={col.count}
              onEditTask={openEditModal}
              onDeleteTask={(task) => setTaskToDelete(task)}
              onDragStart={handleDragStart}
              onDragEnd={handleDragEnd}
              onDragOver={handleDragOver}
              onDrop={handleDrop}
            />
          ))}
        </div>

        {/* Empty State */}
        {tasks.length === 0 && (
          <div className="text-center mt-5 pt-5">
            <i className="bi bi-clipboard-x display-1 text-muted"></i>
            <h3 className="mt-3 text-muted">No tasks yet</h3>
            <p className="text-muted mb-4">Create your first task to get started!</p>
            <button
              className="btn btn-gradient text-white"
              data-bs-toggle="modal"
              data-bs-target="#addTaskModal"
            >
              <i className="bi bi-plus-circle me-2"></i>
              Create First Task
            </button>
          </div>
        )}
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