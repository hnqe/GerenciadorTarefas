package com.todo.service;


import java.util.List;

import org.springframework.stereotype.Service;

import com.todo.model.Tarefa;
import com.todo.repository.TarefaRepository;

@Service
public class TarefaService {

    private final TarefaRepository tarefaRepository;

    public TarefaService(TarefaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    public Tarefa salvar(Tarefa tarefa) {
        return tarefaRepository.save(tarefa);
    }

    public List<Tarefa> listarTodas() {
        return tarefaRepository.findAll();
    }

    public Tarefa atualizar(Long id, Tarefa novaTarefa) {
        return tarefaRepository.findById(id).map(tarefa -> {
            tarefa.setDescricao(novaTarefa.getDescricao());
            tarefa.setConcluida(novaTarefa.isConcluida());
            return tarefaRepository.save(tarefa);
        }).orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));
    }

    public void excluir(Long id) {
        tarefaRepository.deleteById(id);
    }
}

