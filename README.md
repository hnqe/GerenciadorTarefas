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

##### API Externa Integrada
A aplicação utiliza a API pública FavQs para obter citações motivacionais, exibidas na página inicial do usuário autenticado.

Endpoint consumido: GET /qotd (quote of the day).
A integração é realizada no backend por meio de Spring WebClient, e o conteúdo é retornado para o frontend.

#### **Estrutura Atual do Projeto Monolítico**

O projeto é uma aplicação monolítica onde todas as funcionalidades (backend, frontend e banco de dados) estão centralizadas em um único repositório.

---

### 3. Configuração e Execução do Projeto Monolítico

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

---

### 4. Evolução para Microsserviços

Para evoluir o projeto, dividimos a aplicação em três grandes partes:

#### **Microsserviços Realizados**

1. **Auth-Service**:
   - Responsável pela autenticação e emissão de tokens JWT.
   - Define usuários, roles (ADMIN, USER), validações de login, etc.
   - Proverá endpoint de /admin/dashboard para simular privilégio de administrador.
   - Persistência de dados no banco de dados PostgreSQL.

2. **Task-Service**:
   - Responsável pelo CRUD de tarefas (Create, Read, Update, Delete).
   - Restringe acessos via token JWT fornecido pelo Auth-Service.
   - Possui integração com o Postgres para persistência das tarefas.
   - Retorna dados em formato JSON para ser consumido por qualquer frontend.
   - Integração externa: também é responsável por buscar citações na API FavQs para exibição na página inicial. A aplicação utiliza a API pública FavQs para obter citações motivacionais, exibidas na página inicial do usuário autenticado.
   Endpoint consumido: GET /qotd (quote of the day).
   A integração é realizada no backend por meio de Spring WebClient, e o conteúdo é retornado para o frontend.

3. **Frontend (React)**:
   - Responsável pela interface do usuário.
   - Consome as APIs do Auth-Service (login, registro) e do Task-Service (tarefas).
   - Exibe listas de tarefas e tela de admin (se o usuário tiver ROLE_ADMIN).

Essa arquitetura microsserviços permite que cada um seja executado em porta e ambiente separado:

 - Auth-Service em localhost:8080
 - Task-Service em localhost:8081
 - Frontend em localhost:3000

Eles se comunicam via HTTP (REST).

---

### 5. Configuração e Execução do Projeto Microsserviços

A seguir, descrevemos passo a passo para rodar cada um dos serviços separadamente:

#### 5.1 Auth-Service

1. Pré-requisitos
   - Java 17+
   - Banco de dados PostgreSQL configurado
   - Maven instalado

2. Configuração no application.properties
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/auth_service
   spring.datasource.username=postgres
   spring.datasource.password=123
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   jwt.secret=yourSecretKeyMustBeLongerAndMoreSecureInRealApplication
   ```

3. Passos para execução
   - Clonar/baixar o projeto do auth-service.
   - No diretório do auth-service, rodar:

     ```properties
     mvn clean install
     mvn spring-boot:run
     ```
   - O Auth-Service subirá em http://localhost:8080.

4. Testar endpoints
   - Registro: POST /api/auth/register
   - Login: POST /api/auth/login
   - Admin: GET /admin/dashboard (precisa de ROLE_ADMIN)
   - Validate: GET /api/auth/validate-token?token=...

#### 5.2 Task-Service

1. Pré-requisitos
   - Java 17+
   - Outro banco PostgreSQL (ou a mesma instância com DB diferente)
   - Maven instalado

2. Configuração no application.properties
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/task_service
   spring.datasource.username=postgres
   spring.datasource.password=123
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true

   server.port=8081

   auth-service.url=http://localhost:8080
   jwt.secret=yourSecretKeyMustBeLongerAndMoreSecureInRealApplication
   ```

3. Passos para execução
   - Clonar/baixar o projeto do task-service.
   - No diretório do task-service, rodar:

     ```properties
     mvn clean install
     mvn spring-boot:run
     ```
   - O Task-Service subirá em http://localhost:8081.

4. Testar endpoints
   - Listar tarefas: GET /api/tasks (requer token JWT).
   - Criar tarefa: POST /api/tasks (body em JSON, requer token).
   - Editar: PUT /api/tasks/edit/{id}
   - Deletar: DELETE /api/tasks/delete/{id}
   - Página de “home” do Task-Service (citações / tasks do dia): GET /api/home/user-info / GET /api/home/tasks/today

#### 5.3 Task-Service

1. Pré-requisitos
   - Node.js e npm (ou yarn) instalados.

2. Instalação
   ```properties
   cd frontend
   npm install
   ```

3. Configurações
   - Arquivo src/services/taskService.js aponta para http://localhost:8081/api (para o Task-Service).
   - Arquivo src/services/authService.js aponta para http://localhost:8080/api/auth (para o Auth-Service).

4. Execução 

   ```properties
   npm start
   ```

   Por padrão, estará em http://localhost:3000.

5. Fluxo 
  - Abra o navegador em http://localhost:3000.
  - Cadastre usuário (Register) ou faça login (Login).
  - Navegue para /home (terá a citação do dia + resumo das tasks).
  - Clique em “Go to Tasks” para abrir o kanban (Pendente, In Progress, Done).
  - Se logar com o usuário admin (senha admin123), verá um botão “Admin” no Header que acessa /admin-dashboard.

---

### 6. Como Funciona a Comunicação

- O Frontend obtém token JWT no Auth-Service (http://localhost:8080).
- O token é armazenado no localStorage.
- Em cada requisição ao Task-Service (http://localhost:8081), o frontend inclui Authorization: Bearer <token>.
- O Task-Service, ao receber, valida o token localmente (decodifica JWT) e opcionalmente chama GET /api/auth/validate-token - no Auth-Service para confirmar se está válido.
- Se for válido, libera o acesso.

---

### 7. Configuração Geral e Observações

- Cada serviço (Auth-Service e Task-Service) possui seu próprio banco e configurações.
- Para rodar localmente, garanta que PostgreSQL esteja rodando e cada DB (auth_service, task_service) esteja criado.
- Lembre-se de usar Java 17 (ou superior) para compatibilidade com o Spring Boot 3.x.
- Caso deseje personalizar portas ou URLs, altere em application.properties e no frontend correspondente.

---

### 8. Como Testar o Fluxo Completo

1. Inicie o Auth-Service (mvn spring-boot:run na pasta do auth-service).
2. Inicie o Task-Service (mvn spring-boot:run na pasta do task-service).
3. Inicie o Frontend React (npm start).
4. Acesse http://localhost:3000.
5. Registre um usuário ou use o admin pré-criado (admin / admin123).
6. Faça login, observe o token salvo no localStorage.
7. Vá para Home => verá citação + lista de tarefas do dia.
8. Vá para Tasks => kanban onde pode criar, arrastar (drag & drop) e editar tarefas.
9. Se estiver logado como admin, verá o botão “Admin” => /admin-dashboard (que chama http://localhost:8080/admin/dashboard).

---

### 9. Conclusão

Este projeto demonstra:

- Arquitetura de Microsserviços: cada contexto (autenticação, gerenciamento de tarefas, frontend) é isolado.
- JWT: autenticação centralizada no Auth-Service, validada localmente pelo Task-Service.
- React para o frontend desacoplado.
- Integração de bancos de dados PostgreSQL independentes para cada serviço.
- A arquitetura de microsserviços possibilita manutenção e escalabilidade simplificadas. Cada serviço pode evoluir, escalar ou até mesmo ser migrado para tecnologias diferentes sem impactar o resto da aplicação, contanto que as interfaces REST sejam mantidas.