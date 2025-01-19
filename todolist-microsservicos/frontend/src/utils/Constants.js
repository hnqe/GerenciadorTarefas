export const STATUS_MAP = {
  PENDENTE: "Pending",
  EM_ANDAMENTO: "In Progress",
  CONCLUIDO: "Done",
};

export const COLUMN_STYLES = {
  PENDENTE: {
    background: "#FFF8CD",
    borderColor: "#FFD400",
  },
  EM_ANDAMENTO: {
    background: "#D1ECF1",
    borderColor: "#17A2B8",
  },
  CONCLUIDO: {
    background: "#D4EDDA",
    borderColor: "#28A745",
  },
};

export const PRIORITIES = [
  { value: "LOW", label: "Low" },
  { value: "MEDIUM", label: "Medium" },
  { value: "HIGH", label: "High" },
  { value: "CRITICAL", label: "Critical" },
];

// Para ordenação
export const priorityOrder = {
  CRITICAL: 4,
  HIGH: 3,
  MEDIUM: 2,
  LOW: 1,
};