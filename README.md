1. Objetivo do Projeto:
   
O objetivo principal é desenvolver um sistema de gerenciamento de tarefas (to-do list) que inicialmente foi implementado como uma aplicação monolítica simples. 
Na segunda etapa do projeto, o sistema será transformado em uma arquitetura de 
microsserviços, com os seguintes objetivos: 

  • Dividir responsabilidades: Separar a lógica de negócios (backend) e a interface de usuário (frontend);
  • Facilitar escalabilidade e manutenção: Garantir que o sistema possa crescer de forma modular e que os serviços possam ser atualizados individualmente; 
  • Demonstrar boas práticas de desenvolvimento: Implementar microsserviços RESTful, comunicação entre serviços e integração de um banco de dados.

-----------------------------------------------------------------------------------------------###-----------------------------------------

2. Características do Repositório Monolítico 
• Contexto: 
O projeto inicial é uma aplicação Java monolítica que roda no console da IDE. Ele gerencia tarefas básicas com funcionalidades de CRUD (Criar, Ler, Atualizar e 
Deletar).

Funcionalidades principais: 
• Adicionar tarefas;
• Listar todas as tarefas;
• Atualizar o status das tarefas (pendente/concluído);
• Remover tarefas.

Problemas no modelo atual: 
• Forte acoplamento: Toda a lógica está centralizada em um único repositório, dificultando adições ou alterações futuras;
• Sem interface de usuário amigável: O sistema é baseado no console e não possui uma interface visual;
• Escalabilidade limitada: Novas funcionalidades aumentariam a complexidade do código e dificultariam a manutenção. 

-----------------------------------------------------------------------------------------------###-----------------------------------------

3. Ferramentas e Tecnologias Utilizadas

Na Parte 1 (Monolito): 
• Linguagem: Java
• IDE: Spring Tool Suite (STS)
• Banco de Dados: Sem persistência (dados apenas em memória)

Na Parte 2 (Microsserviços): 
Backend: 
• Spring Boot;
• Banco de Dados H2 (persistência em memória);
• Exposição de APIs RESTful para gerenciar tarefas. 

Frontend (Interface de Usuário): 
• Spring Boot com Thymeleaf para gerar páginas HTML dinâmicas;
• Comunicação com o backend via REST (JSON). 

-----------------------------------------------------------------------------------------------###-----------------------------------------

4. Processo de Análise e Extração 
Durante a transição para microsserviços, identificamos as seguintes partes importantes para separação: 

    1. Serviço Backend (Tarefas): 
    • Responsável por toda a lógica de negócios (CRUD de tarefas); 
    • Persistência no banco de dados H2;
    • Exposição de APIs RESTful para comunicação com outros serviços.
    
    2. Serviço UI (Interface de Usuário): 
    • Um serviço separado para apresentar uma interface web simples;
    • Responsável por renderizar páginas HTML com listas de tarefas e formulários para interação do usuário; 
    • Faz requisições HTTP para o backend.

-----------------------------------------------------------------------------------------------###-----------------------------------------

5. Proposta de Arquitetura Futura 
A nova arquitetura será baseada em dois microsserviços independentes que se comunicam via APIs RESTful.

    Descrição dos Serviços: 
    1. Microsserviço Backend (Tarefas): 
    • Responsável por gerenciar as tarefas e armazená-las no banco H2;
    • Oferece endpoints REST para adicionar, listar, atualizar e remover tarefas.
    
    2. Microsserviço UI (Interface de Usuário): 
    • Responsável pela interação com o usuário por meio de páginas web;
    • Utiliza Spring Boot com Thymeleaf para renderização de páginas;
    • Consome os serviços REST do backend.

Esboço da Arquitetura: 
Cliente (Navegador) 
↕ 
UI Service (Spring Boot + Thymeleaf) 
↕ 
Backend Service (Spring Boot API RESTful) 
↕ 
Banco de Dados H2 

-----------------------------------------------------------------------------------------------###-----------------------------------------

6. Desafios e Pontos de Atenção 
    • Gerenciamento de comunicação entre serviços: 
      • Garantir que o serviço de UI consiga acessar os endpoints do backend corretamente.
    
    • Estado dos dados: 
      • Na atual arquitetura, o banco H2 é em memória, o que significa que os dados serão perdidos ao reiniciar a aplicação. Será necessário considerar melhorias no futuro, como a adoção de um banco de dados persistente 
    (ex.: MySQL). 
    
    • Desenvolvimento de uma UI funcional: 
      • Criar uma interface amigável, garantindo que os dados sejam exibidos de forma clara e que os formulários enviem as requisições corretas ao backend. 


-----------------------------------------------------------------------------------------------###-----------------------------------------

  
7. Próximos Passos 
    1. Configurar o microsserviço Backend: 
      • Criar endpoints REST para as operações de CRUD de tarefas.
    
    2. Criar o microsserviço UI: 
      • Implementar páginas HTML simples usando Thymeleaf para listar, adicionar, atualizar e remover tarefas.
    
    3. Integração entre os serviços: 
      • Garantir que o serviço de UI consuma corretamente as APIs REST do backend.
    
    4. Testar a aplicação completa: 
      • Testar o fluxo completo, desde a interação do usuário no frontend até o gerenciamento das tarefas no backend.

-----------------------------------------------------------------------------------------------###-----------------------------------------
