---

### **README**

---

### 1. Objetivo do Projeto

O objetivo principal é desenvolver um sistema de gerenciamento de tarefas (**to-do list**) que inicialmente foi implementado como uma aplicação monolítica simples. 

Futuramente, o sistema será transformado em uma arquitetura de **microsserviços** para:
- **Dividir responsabilidades**: Separar a lógica de negócios e a interface de usuário.
- **Facilitar escalabilidade e manutenção**: Tornar o sistema modular e permitir atualizações independentes.
- **Demonstrar boas práticas**: Implementar serviços RESTful, comunicação entre microsserviços e integração com bancos de dados.

---

### 2. Descrição do Projeto Monolítico Atual

#### **Tecnologias Utilizadas**

- **Backend**:
  - Spring Boot
  - Spring Security
  - Spring Data JPA
  - PostgreSQL (persistência de dados)
  - JWT (JSON Web Tokens) para autenticação
  - WebClient para integração com APIs externas (ex.: FavQs para citações motivacionais)
- **Frontend**:
  - Thymeleaf (renderização de páginas HTML no backend)
  - Bootstrap (estilização)

#### **Funcionalidades do Sistema**

- **Autenticação e autorização**:
  - Login e registro de usuários.
  - Controle de permissões (usuário comum e administrador).
- **Gerenciamento de tarefas**:
  - Adicionar, listar, editar e excluir tarefas.
  - Atualizar status das tarefas (pendente, em andamento, concluído).
- **Página inicial personalizada**:
  - Saudação com o nome do usuário autenticado.
  - Exibição de uma citação motivacional obtida via API externa.
  - Resumo das tarefas do dia.

#### **Estrutura Atual do Projeto Monolítico**

O projeto é uma aplicação monolítica onde todas as funcionalidades (backend, frontend e banco de dados) estão centralizadas em um único repositório.

---

### 3. Evolução para Microsserviços (Planejado)

Com o objetivo de melhorar a modularidade e escalabilidade, está prevista a transição para uma arquitetura baseada em microsserviços. 

#### **Microsserviços Planejados**

1. **Microsserviço de Gerenciamento de Tarefas (Backend)**:
   - Exposição de APIs RESTful para:
     - Criar, listar, editar e excluir tarefas.
     - Gerenciar status das tarefas.
   - Persistência de dados no banco de dados PostgreSQL.

2. **Microsserviço de Autenticação (opcional)**:
   - Responsável por autenticação e emissão de tokens JWT.

3. **Microsserviço de Interface de Usuário (Frontend)**:
   - Implementação de uma interface desacoplada utilizando um framework moderno como React ou Angular.
   - Consome as APIs REST do backend.

---

### 4. Configuração e Execução do Projeto Monolítico

#### **Pré-requisitos**
1. **Java 17** ou superior instalado.
2. **PostgreSQL** configurado e rodando.

#### **Configuração do Banco de Dados**
Certifique-se de configurar o banco de dados PostgreSQL com as seguintes credenciais no arquivo `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/todolist
spring.datasource.username=postgres
spring.datasource.password=123
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

#### **Passos para Execução**
1. Clone o repositório.
2. Compile o projeto com Maven:
   ```
   mvn clean install
   ```
3. Execute a aplicação:
   ```
   mvn spring-boot:run
   ```
4. Acesse no navegador:
   - Página de login: `http://localhost:8080/login`
   - Página inicial (após login): `http://localhost:8080/home`
   - Gerenciamento de tarefas: `http://localhost:8080/tasks`