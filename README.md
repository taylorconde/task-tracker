# 📋 Task Tracker API

[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

Uma API RESTful completa para gerenciamento de tarefas (CRUD), desenvolvida com **Java puro** para fins de estudo sobre o funcionamento interno do protocolo HTTP e arquitetura de software **sem dependência de frameworks robustos** como Spring ou Hibernate.

## 📖 Sobre o Projeto

Este projeto foi criado como um exercício de aprendizado para entender profundamente:
- Como funciona o protocolo HTTP em baixo nível
- Construção de APIs REST sem frameworks
- Arquitetura em camadas (Layered Architecture)
- **Múltiplas estratégias de persistência** (SQLite, PostgreSQL, JSON)
- Padrões de projeto (Repository, Factory, Strategy)
- Serialização/Deserialização manual de JSON
- Testes automatizados (unitários, de integração e E2E)
- Interface web interativa com Kanban board drag-and-drop

## 🚀 Tecnologias Utilizadas

### Backend (Java)
- **Java 21**: Linguagem de programação principal
- **com.sun.net.httpserver**: Servidor HTTP nativo do JDK
- **Maven**: Gerenciador de dependências e build
- **JUnit 5**: Framework para testes
- **JDBC**: API para conexão com bancos de dados

### Banco de Dados
- **SQLite**: Banco de dados local (arquivo `task.db`)
- **PostgreSQL**: Suporte para banco relacional robusto
- **JSON**: Persistência em arquivo JSON como alternativa

### Frontend
- **HTML5/CSS3**: Interface web responsiva
- **JavaScript**: Lógica do cliente e manipulação DOM
- **Kanban Board**: Interface drag-and-drop para gerenciar tarefas
- **Dark/Light Mode**: Alternância de tema claro/escuro

### Testes
- **JUnit 5**: Testes unitários e de integração da camada de serviço
- **Playwright**: Testes E2E automatizados (TypeScript)

### DevOps
- **Docker & Docker Compose**: Containerização da aplicação
- **GitHub Actions**: CI/CD para testes automatizados

## 🏗️ Arquitetura do Projeto

O projeto segue uma arquitetura em camadas bem definida com suporte a múltiplos tipos de persistência:

```
task-tracker/
├── src/
│   ├── main/
│   │   └── java/br/com/taylor/
│   │       ├── application/
│   │       │   └── Main.java                    # Ponto de entrada
│   │       ├── controller/
│   │       │   └── TaskController.java          # Controlador REST
│   │       ├── entity/
│   │       │   └── Task.java                    # Entidade Task
│   │       ├── enums/
│   │       │   └── TaskStatus.java              # Status (TODO, IN_PROGRESS, DONE)
│   │       ├── infra/
│   │       │   ├── ConnectionFactory.java       # Factory para conexões
│   │       │   ├── DatabaseConfig.java          # Config de banco abstrata
│   │       │   ├── PostgresDatabaseConfig.java  # Config PostgreSQL
│   │       │   └── SQLiteDatabaseSetup.java     # Setup SQLite
│   │       ├── repository/
│   │       │   ├── TaskRepository.java          # Interface do repositório
│   │       │   ├── JdbcTaskRepository.java      # Impl. com JDBC (SQL)
│   │       │   └── JsonTaskRepository.java      # Impl. com JSON (arquivo)
│   │       ├── serializer/
│   │       │   └── TaskSerializer.java          # Conversão JSON ↔ Java
│   │       ├── server/
│   │       │   └── TaskHttpServer.java          # Servidor HTTP
│   │       ├── service/
│   │       │   └── TaskService.java             # Lógica de negócio
│   │       └── utils/
│   │           ├── JdbcUtils.java               # Utilitários JDBC
│   │           └── JsonUtils.java               # Utilitários JSON
│   └── test/
│       └── java/br/com/taylor/
│           ├── integration/                     # Testes de integração
│           │   └── TaskServiceIntegrationTest.java
│           └── service/
│               └── TaskServiceTest.java         # Testes unitários
├── public/
│   ├── index.html                               # Interface web
│   ├── css/
│   │   ├── base.css
│   │   ├── kanban.css                           # Estilo Kanban board
│   │   └── variables.css
│   └── js/
│       └── script.js                            # Lógica frontend
├── tests/
│   └── tasktracer.spec.ts                       # Testes E2E (Playwright)
├── data/
│   ├── task.db                                  # Banco SQLite
│   └── task.json                                # Persistência JSON
├── docker-compose.yml                           # Orquestração Docker
├── pom.xml                                      # Configuração Maven
└── package.json                                 # Dependências Node.js
```

### Camadas e Responsabilidades

| Camada | Responsabilidade |
|--------|------------------|
| **Application** | Ponto de entrada e inicialização do servidor |
| **Server** | Gerencia o servidor HTTP e roteamento |
| **Controller** | Recebe requisições HTTP, valida entrada e retorna respostas |
| **Service** | Contém regras de negócio e orquestra operações |
| **Repository** | Interface de persistência com múltiplas implementações (JDBC/JSON) |
| **Infra** | Configuração de infraestrutura e conexões com banco de dados |
| **Entity** | Define estrutura dos dados (modelo de domínio) |
| **Serializer** | Converte objetos Java ↔ JSON |
| **Utils** | Funções auxiliares reutilizáveis |

### Padrões de Projeto Implementados

- **Repository Pattern**: Abstração da camada de persistência
- **Factory Pattern**: `ConnectionFactory` para criação de conexões
- **Strategy Pattern**: Múltiplas implementações de `TaskRepository`
- **Layered Architecture**: Separação clara de responsabilidades

## ✨ Funcionalidades Principais

### Interface Web Interativa
- 🎨 **Dark/Light Mode**: Alternância entre tema claro e escuro
- 📋 **Kanban Board**: Visualização em colunas (A Fazer, Em Progresso, Concluído)
- 🖱️ **Drag and Drop**: Arraste e solte tarefas entre as colunas
- 📱 **Responsivo**: Interface adaptável para diferentes tamanhos de tela

### API RESTful Completa
- ✅ **Create**: Criar novas tarefas
- 📖 **Read**: Listar todas as tarefas ou buscar por ID
- ✏️ **Update**: Atualizar informações de tarefas existentes
- ❌ **Delete**: Remover tarefas
- 🔄 **Status Management**: Transição de status (TODO → IN_PROGRESS → DONE)

## 📋 Funcionalidades (CRUD)

A API suporta todas as operações CRUD:

### ✅ Create (Criar)
```http
POST /tasks
Content-Type: application/json

{
  "description": "Estudar Java",
  "status": "TODO"
}
```

### 📖 Read (Ler)
```http
# Listar todas as tarefas
GET /tasks

# Buscar tarefa específica
GET /tasks/{id}
```

### ✏️ Update (Atualizar)
```http
PUT /tasks/{id}
Content-Type: application/json

{
  "description": "Estudar Streams e Lambda",
  "status": "IN_PROGRESS"
}
```

### ❌ Delete (Deletar)
```http
DELETE /tasks/{id}
```

## 🎯 Status das Tarefas

As tarefas podem ter os seguintes status:

| Status | Descrição |
|--------|-----------|
| `TODO` | Tarefa criada, aguardando início |
| `IN_PROGRESS` | Tarefa em andamento |
| `DONE` | Tarefa concluída |

## 💾 Opções de Persistência

O projeto implementa **múltiplas estratégias de persistência** através do padrão Repository:

### Configure via `.env`:

- `DB_TYPE=auto` - **Recomendado**: Tenta PostgreSQL, faz fallback para SQLite automaticamente
- `DB_TYPE=PostgreSQL` - Força uso do PostgreSQL (falha se indisponível)  
- `DB_TYPE=SQLite` - Força uso do SQLite

### 1. SQLite (Recomendado para desenvolvimento)
- Banco de dados local em arquivo (`data/task.db`)
- Não requer instalação de servidor
- Ideal para testes e desenvolvimento rápido
- Implementação: `JdbcTaskRepository` + `SQLiteDatabaseSetup`

### 2. PostgreSQL (Produção)
- Banco de dados relacional robusto
- Configurável via Docker Compose
- Ideal para ambientes de produção
- Implementação: `JdbcTaskRepository` + `PostgresDatabaseConfig`

### 3. JSON (Persistência simples)
- Armazenamento em arquivo JSON (`data/task.json`)
- Sem dependência de banco de dados
- Útil para prototipagem rápida
- Implementação: `JsonTaskRepository`

A escolha da persistência é feita através da `ConnectionFactory`, permitindo trocar facilmente entre as implementações.

## 🚦 Como Executar

### Pré-requisitos
- Java JDK 21 ou superior
- Maven 3.8+
- Docker e Docker Compose (opcional)

### Opção 1: Executar Localmente

1. **Clone o repositório:**
```bash
git clone https://github.com/taylorconde/task-tracker.git
cd task-tracker
```

2. **Compile o projeto:**
```bash
mvn clean compile
```

3. **Execute a aplicação:**
```bash
mvn exec:java
```

4. **Acesse a API:**
```
http://localhost:8080
```

### Opção 2: Executar com Docker

1. **Construa e execute os containers:**
```bash
docker-compose up --build
```

2. **Acesse a aplicação:**
```
http://localhost:8080
```

## 🧪 Testes

O projeto inclui testes automatizados em três camadas:

### Testes Unitários (JUnit 5)
```bash
mvn test
```

### Testes de Integração (JUnit 5)
```bash
mvn test
```

### Testes E2E (Playwright)
```bash
npm install
npm test
```

Os testes cobrem:
- ✅ Criação de tarefas
- ✅ Listagem de tarefas
- ✅ Atualização de tarefas
- ✅ Exclusão de tarefas
- ✅ Validação de entrada
- ✅ Tratamento de erros
- ✅ Integração com banco de dados (PostgreSQL/SQLite)

## 🔄 Changelog / Histórico de Mudanças

### v2.0.0 - Interface Drag and Drop + Dark Mode
- ✨ **Nova feature**: Interface web completa com suporte a drag-and-drop
- 🎨 **Dark/Light Mode**: Alternância de tema claro/escuro
- 📋 **Kanban Board**: Visualização em colunas com reorganização visual
- 🔧 Melhorias significativas na experiência do usuário (UX)

### v1.5.0 - Testes Automatizados
- ✅ Implementação de testes E2E com Playwright
- 🧪 Cobertura de testes unitários ampliada
- 🤖 CI/CD com GitHub Actions

### v1.0.0 - Versão Inicial
- 🎉 CRUD completo de tarefas
- 🌐 API RESTful funcional
- 💾 Persistência em SQLite/PostgreSQL/JSON
- 📝 Serialização JSON manual

## 🔮 Melhorias Futuras

- [ ] Sincronização de dados entre PostgreSQL e SQLite
- [ ] Endpoint `GET /api/status` para mostrar banco ativo
- [ ] Interface visual para indicar qual banco está em uso
- [ ] Sistema de backup automático

## 📊 Estrutura de Dados

### Objeto Task (JSON)
```json
{
  "id": 1,
  "description": "Estudar Java",
  "status": "TODO",
  "createdAt": "2025-02-14T10:30:00",
  "updatedAt": "2025-02-14T10:30:00"
}
```

## 🎓 Aprendizados e Conceitos Aplicados

Este projeto demonstra conhecimento em:

- ✅ **Programação Orientada a Objetos (POO)**: Encapsulamento, herança, polimorfismo, interfaces
- ✅ **Clean Code**: Código legível, manutenível e bem organizado
- ✅ **Design Patterns**: Repository Pattern, Factory Pattern, Strategy Pattern
- ✅ **REST API**: Princípios RESTful, verbos HTTP, status codes
- ✅ **Arquitetura em Camadas**: Separação de responsabilidades (Controller → Service → Repository)
- ✅ **Persistência de Dados**: JDBC, SQLite, PostgreSQL, JSON
- ✅ **Testes Automatizados**: TDD, testes unitários (JUnit), testes de integração e E2E (Playwright)
- ✅ **Versionamento**: Git Flow, branches, pull requests, conventional commits
- ✅ **DevOps**: Docker, Docker Compose, CI/CD, GitHub Actions
- ✅ **Frontend**: HTML5, CSS3, JavaScript, manipulação DOM, drag-and-drop

## 📝 Endpoints da API

| Método | Endpoint | Descrição | Status Code |
|--------|----------|-----------|-------------|
| GET | `/tasks` | Lista todas as tarefas | 200 OK |
| GET | `/tasks/{id}` | Busca tarefa por ID | 200 OK / 404 Not Found |
| POST | `/tasks` | Cria nova tarefa | 201 Created |
| PUT | `/tasks/{id}` | Atualiza tarefa | 200 OK / 404 Not Found |
| DELETE | `/tasks/{id}` | Remove tarefa | 204 No Content / 404 Not Found |

## 🤝 Como Contribuir

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feat/nova-feature`)
3. Commit suas mudanças (`git commit -m 'feat: adiciona nova feature'`)
4. Push para a branch (`git push origin feat/nova-feature`)
5. Abra um Pull Request

### Padrão de Commits
Seguimos o [Conventional Commits](https://www.conventionalcommits.org/):
- `feat:` Nova funcionalidade
- `fix:` Correção de bug
- `docs:` Alteração em documentação
- `test:` Adição ou modificação de testes
- `refactor:` Refatoração de código

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 👨‍💻 Autor

**Taylor Conde**
- GitHub: [@taylorconde](https://github.com/taylorconde)
- LinkedIn: [John Taylor](https://www.linkedin.com/in/john-taylor-verified/)

## 🙏 Agradecimentos

Projeto desenvolvido como parte dos estudos em desenvolvimento backend com Java, focando em fundamentos e boas práticas de engenharia de software.

---

⭐ Se este projeto te ajudou de alguma forma, considere dar uma estrela no repositório!

## 📸 Screenshots

### Light Mode
*Interface em modo claro com Kanban board drag-and-drop*
<img width="806" height="607" alt="image" src="https://github.com/user-attachments/assets/5e5a1be4-9c22-4a87-a654-56e2bf45d4d0" />


### Dark Mode
*Interface em modo escuro - alternância com um clique*
<img width="799" height="641" alt="image" src="https://github.com/user-attachments/assets/f611a54c-057b-46b2-bdef-85782602a15a" />