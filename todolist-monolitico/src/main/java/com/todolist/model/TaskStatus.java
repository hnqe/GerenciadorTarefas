package com.todolist.model;

import lombok.Getter;

@Getter
public enum TaskStatus {
    PENDENTE("Pendente"),
    EM_ANDAMENTO("Em Andamento"),
    CONCLUIDO("Concluído");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

}
