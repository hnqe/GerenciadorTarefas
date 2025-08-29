export const STATUS_MAP = {
  TODO: "Pending",
  IN_PROGRESS: "In Progress",
  COMPLETED: "Done",
};

export const COLUMN_STYLES = {
  TODO: {
    background: "#FFF8CD",
    borderColor: "#FFD400",
  },
  IN_PROGRESS: {
    background: "#D1ECF1",
    borderColor: "#17A2B8",
  },
  COMPLETED: {
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