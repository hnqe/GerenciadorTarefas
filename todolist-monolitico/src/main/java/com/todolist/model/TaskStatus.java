package com.todolist.model;

public enum TaskStatus {
    PENDENTE("Pendente"),
    EM_ANDAMENTO("Em Andamento"),
    CONCLUIDO("Concluído");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

}
