# 📋 Sistema Gerenciador de Tarefas

Um sistema completo de gerenciamento de tarefas desenvolvido com **Spring Boot** e **PostgreSQL**, disponível em **duas arquiteturas**: **Monolítica** e **Microsserviços**. Inclui funcionalidades de autenticação, CRUD de tarefas, timer Pomodoro e dashboard administrativo.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-green.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.2.0-blue.svg)](https://reactjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)

## 🎯 Duas Arquiteturas Disponíveis

Este repositório contém **duas implementações** do mesmo sistema de gerenciamento de tarefas:

### 1. 🏢 **Versão Monolítica** (`todolist-monolitico/`)
- **Uma única aplicação Spring Boot** com interface web Thymeleaf
- **Banco único** PostgreSQL
- **Tudo integrado**: autenticação, tarefas e interface no mesmo projeto
- **Ideal para**: aprendizado, prototipagem rápida e projetos pequenos

### 2. 🔧 **Versão Microsserviços** (`todolist-microsservicos/`)
- **4 aplicações separadas**: Auth-Service, Task-Service, Pomodoro-Service e Frontend React
- **Bancos independentes** para cada microsserviço
- **APIs REST** com comunicação via HTTP
- **Ideal para**: escalabilidade, manutenibilidade e sistemas complexos

---

## 🏗️ Arquitetura Monolítica

A versão monolítica implementa todas as funcionalidades em uma única aplicação Spring Boot:

```
┌─────────────────────────────────────────────────────┐
│                APLICAÇÃO MONOLÍTICA                 │
│                     :8080                           │
├─────────────────────────────────────────────────────┤
│ • Autenticação JWT                                  │
│ • CRUD de Tarefas                                   │
│ • Interface Web (Thymeleaf)                        │
│ • Dashboard Administrativo                          │
│ • API Externa (FavQs)                              │
│ • Controllers + Services + Repositories            │
└─────────────────────────────────────────────────────┘
                            │
                ┌─────────────────────┐
                │    PostgreSQL       │
                │    todolist         │
                │    :5432            │
                └─────────────────────┘
```

### ✨ **Funcionalidades da Versão Monolítica:**
- 🔐 Sistema completo de login/registro
- 📝 CRUD de tarefas com interface web
- 🎨 Interface com **Thymeleaf** e **Bootstrap**
- 📊 Dashboard com estatísticas
- 💬 Citações motivacionais da API FavQs
- 👨‍💼 Painel administrativo
- 🔒 Segurança com Spring Security e JWT

## 🏗️ Arquitetura de Microsserviços

O projeto implementa uma **arquitetura de microsserviços** com 4 componentes principais:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Auth-Service  │    │  Task-Service   │    │ Pomodoro-Service │
│   (React)       │    │   :8080         │    │   :8081         │    │   :8082          │
│   :3000         │    │                 │    │                 │    │                  │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ • Interface Web │    │ • Autenticação  │    │ • CRUD Tarefas  │    │ • Timer Pomodoro │
│ • Dashboard     │────│ • JWT Tokens    │    │ • Kanban Board  │    │ • Sessões        │
│ • Kanban Board  │    │ • Admin Panel   │    │ • API Externa   │    │ • Configurações  │
│ • Timer Pomodoro│    │ • User Mgmt     │    │ • Task Stats    │    │ • Histórico      │
└─────────────────┘    └─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │                       │
         └───────────────────────┼───────────────────────┼───────────────────────┘
                                 │                       │
                    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
                    │  PostgreSQL     │    │  PostgreSQL     │    │  PostgreSQL     │
                    │  auth_service   │    │  task_service   │    │ pomodoro_service │
                    │  :5433          │    │  :5434          │    │  :5435          │
                    └─────────────────┘    └─────────────────┘    └─────────────────┘
```

## ✨ Funcionalidades Principais

### 🔐 **Autenticação e Autorização**
- ✅ Sistema completo de registro e login
- ✅ Autenticação JWT com refresh tokens
- ✅ Controle de acesso baseado em roles (USER/ADMIN)
- ✅ Dashboard administrativo com estatísticas

### 📝 **Gerenciamento de Tarefas**
- ✅ CRUD completo de tarefas
- ✅ Sistema Kanban com drag & drop (Pendente → Em Andamento → Concluído)
- ✅ Prioridades e datas de vencimento
- ✅ Filtros e busca avançada
- ✅ Estatísticas de produtividade

### 🍅 **Timer Pomodoro Integrado**
- ✅ Sessões de foco, pausa curta e pausa longa
- ✅ Configurações personalizáveis de tempo
- ✅ Vinculação de tarefas às sessões
- ✅ Histórico de sessões e estatísticas
- ✅ Controles de play/pause/stop
- ✅ Notificações sonoras

### 📊 **Dashboard e Relatórios**
- ✅ Página inicial com resumo do dia
- ✅ Citações motivacionais (API externa)
- ✅ Gráficos de produtividade
- ✅ Painel administrativo com métricas

## 🚀 Instalação e Configuração

### Pré-requisitos
- **Java 17+** ([Download](https://openjdk.org/))
- **PostgreSQL 15+** ([Download](https://www.postgresql.org/))
- **Maven 3.8+** (ou usar o wrapper incluído)
- **Node.js 18+** e **npm** (apenas para microsserviços) ([Download](https://nodejs.org/))
- **Docker** e **Docker Compose** (opcional, recomendado para microsserviços)

---

## 🏢 Como Executar a Versão Monolítica

### 1️⃣ Configurar Banco de Dados
```sql
-- No PostgreSQL, criar o banco:
CREATE DATABASE todolist;
```

### 2️⃣ Configurar Aplicação
```bash
# 1. Clone o repositório
git clone https://github.com/hnqe/GerenciadorTarefas.git
cd GerenciadorTarefas/todolist-monolitico

# 2. Configure application.properties (se necessário)
# Arquivo: src/main/resources/application.properties
```

### 3️⃣ Executar Aplicação
```bash
# Compilar e executar
./mvnw clean install
./mvnw spring-boot:run

# Ou usando Maven local
mvn clean install
mvn spring-boot:run
```

### 4️⃣ Acessar Aplicação
- **URL:** http://localhost:8080
- **Login Admin:** `admin` / `admin123`
- **Páginas:**
  - `/login` - Login
  - `/register` - Registro
  - `/home` - Dashboard principal
  - `/tasks` - Gerenciamento de tarefas

### ⚙️ Configuração do Monólito
```properties
# application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/todolist
spring.datasource.username=postgres
spring.datasource.password=123
spring.jpa.hibernate.ddl-auto=update
```

---

## 🔧 Como Executar os Microsserviços

### Opção 1: Docker Compose (Recomendado)

```bash
# 1. Clone o repositório
git clone https://github.com/hnqe/GerenciadorTarefas.git
cd GerenciadorTarefas/todolist-microsservicos

# 2. Suba todos os serviços
docker-compose up -d

# 3. Instale dependências do frontend
cd frontend
npm install

# 4. Inicie o frontend
npm start
```

### Opção 2: Instalação Manual

#### 1️⃣ Configurar Bancos de Dados
```sql
-- No PostgreSQL, criar os bancos:
CREATE DATABASE auth_service;
CREATE DATABASE task_service;
CREATE DATABASE pomodoro_service;
```

#### 2️⃣ Auth-Service
```bash
cd auth-service
./mvnw clean install
./mvnw spring-boot:run
# Rodará em http://localhost:8080
```

#### 3️⃣ Task-Service
```bash
cd task-service
./mvnw clean install
./mvnw spring-boot:run
# Rodará em http://localhost:8081
```

#### 4️⃣ Pomodoro-Service
```bash
cd pomodoro-service
./mvnw clean install
./mvnw spring-boot:run
# Rodará em http://localhost:8082
```

#### 5️⃣ Frontend
```bash
cd frontend
npm install
npm start
# Rodará em http://localhost:3000
```

## 🔧 Configuração dos Serviços

### Auth-Service (Port 8080)
```properties
# application.properties
server.port=8080
spring.datasource.url=jdbc:postgresql://localhost:5433/auth_service
spring.datasource.username=postgres
spring.datasource.password=123
jwt.secret=yourSecretKeyMustBeLongerAndMoreSecureInRealApplication
```

### Task-Service (Port 8081)
```properties
# application.properties
server.port=8081
spring.datasource.url=jdbc:postgresql://localhost:5434/task_service
spring.datasource.username=postgres
spring.datasource.password=123
auth-service.url=http://localhost:8080
```

### Pomodoro-Service (Port 8082)
```properties
# application.properties
server.port=8082
spring.datasource.url=jdbc:postgresql://localhost:5435/pomodoro_service
spring.datasource.username=postgres
spring.datasource.password=123
jwt.secret=yourSecretKeyMustBeLongerAndMoreSecureInRealApplication
```

## 📚 API Documentation

### 🔐 Auth-Service Endpoints
| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| POST | `/api/auth/register` | Registrar usuário | ❌ |
| POST | `/api/auth/login` | Login usuário | ❌ |
| GET | `/api/auth/validate-token` | Validar token JWT | ❌ |
| GET | `/admin/dashboard` | Dashboard admin | ✅ ADMIN |
| GET | `/admin/users` | Listar usuários | ✅ ADMIN |

### 📝 Task-Service Endpoints
| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| GET | `/api/tasks` | Listar tarefas | ✅ USER |
| POST | `/api/tasks` | Criar tarefa | ✅ USER |
| GET | `/api/tasks/{id}` | Buscar tarefa | ✅ USER |
| PUT | `/api/tasks/edit/{id}` | Editar tarefa | ✅ USER |
| DELETE | `/api/tasks/delete/{id}` | Deletar tarefa | ✅ USER |
| GET | `/api/home/user-info` | Info do usuário | ✅ USER |
| GET | `/api/home/tasks/today` | Tarefas do dia | ✅ USER |

### 🍅 Pomodoro-Service Endpoints
| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| GET | `/api/pomodoro/health` | Health check | ❌ |
| POST | `/api/pomodoro/sessions` | Criar sessão | ✅ USER |
| POST | `/api/pomodoro/sessions/{id}/start` | Iniciar sessão | ✅ USER |
| POST | `/api/pomodoro/sessions/{id}/pause` | Pausar sessão | ✅ USER |
| POST | `/api/pomodoro/sessions/{id}/stop` | Parar sessão | ✅ USER |
| POST | `/api/pomodoro/sessions/{id}/complete` | Completar sessão | ✅ USER |
| GET | `/api/pomodoro/sessions` | Listar sessões | ✅ USER |
| GET | `/api/pomodoro/sessions/current` | Sessão atual | ✅ USER |
| GET | `/api/pomodoro/settings` | Config. usuário | ✅ USER |
| PUT | `/api/pomodoro/settings` | Atualizar config. | ✅ USER |

## 🧪 Testes

### Executar Testes Unitários
```bash
# Auth-Service
cd auth-service
./mvnw test

# Task-Service  
cd task-service
./mvnw test

# Pomodoro-Service
cd pomodoro-service
./mvnw test
```

### Executar Testes de Integração
```bash
# Docker-based (recomendado)
cd auth-service && ./run-tests.bat
cd task-service && ./run-tests.bat
cd pomodoro-service && ./run-tests.bat

# Todos os testes
./run-integration-tests.bat
```

## 🎯 Como Usar

### 1. Acesso Inicial
- URL: **http://localhost:3000**
- **Admin:** `admin` / `admin123`
- **Usuário:** Registre uma nova conta

### 2. Fluxo de Trabalho
1. **Faça login** na aplicação
2. **Crie tarefas** na seção "Tasks"
3. **Use o Kanban** para organizar (arrastar e soltar)
4. **Inicie sessões Pomodoro** para focar nas tarefas
5. **Monitore estatísticas** no dashboard

### 3. Funcionalidades do Pomodoro
1. **Criar sessão** → vincular tarefa(s)
2. **Configurar tempo** (25min foco, 5min pausa)
3. **Iniciar timer** → tarefa muda para "Em Andamento"
4. **Pausar/Retomar** conforme necessário
5. **Completar** para salvar estatísticas

## 🏭 Tecnologias Utilizadas

### Backend
- **Spring Boot 3.4.1** - Framework principal
- **Spring Security** - Autenticação e autorização
- **Spring Data JPA** - Persistência de dados
- **PostgreSQL** - Banco de dados
- **JWT (jjwt 0.11.5)** - Tokens de autenticação
- **Maven** - Gerenciamento de dependências
- **Docker** - Containerização

### Frontend
- **React 18.2.0** - Interface de usuário
- **React Router** - Navegação
- **Axios** - Cliente HTTP
- **Bootstrap/CSS** - Estilização
- **Context API** - Gerenciamento de estado

### DevOps & Qualidade
- **JUnit 5** - Testes unitários
- **Testcontainers** - Testes de integração
- **Docker Compose** - Orquestração de contêineres
- **GitHub Actions** - CI/CD (configurável)

## ⚖️ Comparação: Monólito vs Microsserviços

| Aspecto | 🏢 Monolítico | 🔧 Microsserviços |
|---------|---------------|-------------------|
| **Complexidade** | ✅ Simples para iniciar | ❗ Maior complexidade inicial |
| **Desenvolvimento** | ✅ Desenvolvimento rápido | ❗ Coordenação entre equipes |
| **Deploy** | ✅ Um único artefato | ❗ Múltiplos deploys |
| **Escalabilidade** | ❗ Escala tudo junto | ✅ Escala partes específicas |
| **Tecnologias** | ❗ Stack unificado | ✅ Tecnologias por serviço |
| **Manutenção** | ❗ Impacto em toda aplicação | ✅ Mudanças isoladas |
| **Debugging** | ✅ Fácil debug local | ❗ Debug distribuído |
| **Performance** | ✅ Chamadas locais | ❗ Latência de rede |
| **Banco de Dados** | ❗ Acoplamento forte | ✅ Dados independentes |
| **Ideal para** | Projetos pequenos/médios | Sistemas grandes/equipes |

### 🎯 **Quando usar cada arquitetura?**

#### 🏢 **Use o Monólito quando:**
- ✅ Equipe pequena (1-5 desenvolvedores)
- ✅ Projeto em fase inicial/prototipagem
- ✅ Domínio do negócio bem definido
- ✅ Requisitos de performance críticos
- ✅ Simplicidade é prioridade

#### 🔧 **Use Microsserviços quando:**
- ✅ Equipe grande (múltiplos times)
- ✅ Diferentes tecnologias por contexto
- ✅ Escalabilidade independente necessária
- ✅ Deploy independente é crucial
- ✅ Domínios bem separados

---

## 📁 Estrutura do Projeto

```
GerenciadorTarefas/
│
├── 🏢 todolist-monolitico/          # VERSÃO MONOLÍTICA
│   ├── src/main/java/com/todolist/
│   │   ├── ToDoListApplication.java  # Classe principal
│   │   ├── config/                   # Configurações (Security, JWT)
│   │   ├── controller/              # Controllers web
│   │   │   ├── AuthController.java  # Autenticação
│   │   │   ├── HomeController.java  # Página inicial
│   │   │   ├── TaskPageController.java # Tarefas
│   │   │   └── AdminController.java # Admin
│   │   ├── model/                   # Entidades JPA
│   │   ├── repository/             # Repositórios
│   │   └── service/                # Serviços de negócio
│   ├── src/main/resources/
│   │   ├── application.properties  # Configurações
│   │   └── templates/              # Templates Thymeleaf
│   │       ├── home.html           # Dashboard principal
│   │       ├── login.html          # Tela de login
│   │       ├── register.html       # Registro
│   │       └── tasks.html          # Gerenciamento de tarefas
│   ├── src/test/java/              # Testes unitários
│   └── pom.xml                     # Dependências Maven
│
├── 🔧 todolist-microsservicos/      # VERSÃO MICROSSERVIÇOS
│   ├── auth-service/               # Microsserviço de autenticação
│   │   ├── src/main/java/         # Código fonte
│   │   ├── src/test/java/         # Testes unitários e integração
│   │   ├── Dockerfile             # Container da aplicação
│   │   ├── Dockerfile.test        # Container para testes
│   │   ├── run-tests.bat         # Script de teste Windows
│   │   └── pom.xml               # Dependências Maven
│   │
│   ├── task-service/              # Microsserviço de tarefas
│   │   ├── src/main/java/        # Código fonte
│   │   ├── src/test/java/        # Testes
│   │   └── [estrutura similar]
│   │
│   ├── pomodoro-service/         # Microsserviço Pomodoro
│   │   ├── src/main/java/        # Código fonte
│   │   ├── src/test/java/        # Testes
│   │   └── [estrutura similar]
│   │
│   ├── frontend/                 # Aplicação React
│   │   ├── src/
│   │   │   ├── components/       # Componentes React
│   │   │   ├── pages/           # Páginas da aplicação
│   │   │   ├── services/        # Clientes API
│   │   │   └── context/         # Contextos React
│   │   ├── public/              # Arquivos estáticos
│   │   └── package.json         # Dependências npm
│   │
│   ├── docker-compose.yml       # Orquestração completa
│   ├── run-integration-tests.bat # Testes end-to-end
│   └── .gitignore               # Arquivos ignorados
│
└── README.md                    # Este arquivo
```