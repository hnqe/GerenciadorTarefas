package com.todo.service;

import com.todo.model.Tarefa;
import java.util.ArrayList;
import java.util.List;

public class GerenciadorDeTarefas {
    private List<Tarefa> tarefas = new ArrayList<>();
    private int proximoId = 1;

    // Adicionar uma nova tarefa
    public void adicionarTarefa(String descricao) {
        tarefas.add(new Tarefa(proximoId++, descricao));
        System.out.println("Tarefa adicionada com sucesso!");
    }

    // Listar todas as tarefas
    public void listarTarefas() {
        if (tarefas.isEmpty()) {
            System.out.println("Nenhuma tarefa cadastrada.");
        } else {
            System.out.println("Tarefas:");
            for (Tarefa tarefa : tarefas) {
                System.out.println(tarefa);
            }
        }
    }

    // Atualizar uma tarefa (marcar como concluída)
    public void atualizarTarefa(int id) {
        for (Tarefa tarefa : tarefas) {
            if (tarefa.getId() == id) {
                tarefa.setConcluida(!tarefa.isConcluida());
                System.out.println("Status da tarefa atualizado!");
                return;
            }
        }
        System.out.println("Tarefa não encontrada.");
    }

    // Remover uma tarefa
    public void removerTarefa(int id) {
        tarefas.removeIf(tarefa -> tarefa.getId() == id);
        System.out.println("Tarefa removida (se existia).");
    }
}
