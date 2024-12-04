package com.todo;

import com.todo.service.GerenciadorDeTarefas;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        GerenciadorDeTarefas gerenciador = new GerenciadorDeTarefas();

        while (true) {
            System.out.println("\n=== Menu To-Do ===");
            System.out.println("1. Adicionar Tarefa");
            System.out.println("2. Listar Tarefas");
            System.out.println("3. Atualizar Tarefa");
            System.out.println("4. Remover Tarefa");
            System.out.println("5. Sair");
            System.out.print("Escolha uma opção: ");

            int opcao = scanner.nextInt();
            scanner.nextLine(); // Consumir a quebra de linha

            switch (opcao) {
                case 1 -> {
                    System.out.print("Digite a descrição da tarefa: ");
                    String descricao = scanner.nextLine();
                    gerenciador.adicionarTarefa(descricao);
                }
                case 2 -> {
                    System.out.println("Listando tarefas...");
                    gerenciador.listarTarefas();
                }
                case 3 -> {
                    System.out.print("Digite o ID da tarefa a ser atualizada: ");
                    int id = scanner.nextInt();
                    gerenciador.atualizarTarefa(id);
                }
                case 4 -> {
                    System.out.print("Digite o ID da tarefa a ser removida: ");
                    int id = scanner.nextInt();
                    gerenciador.removerTarefa(id);
                }
                case 5 -> {
                    System.out.println("Saindo... Até mais!");
                    scanner.close();
                    System.exit(0);
                }
                default -> System.out.println("Opção inválida! Tente novamente.");
            }
        }
    }
}
