# 📋 Task Tracker API

Uma API RESTful para gerenciamento de tarefas (CRUD), desenvolvida com Java puro para fins de estudo sobre o funcionamento interno do protocolo HTTP e arquitetura de software sem dependência de frameworks robustos.

## 🚀 Tecnologias Utilizadas

Este projeto foi construído utilizando apenas as bibliotecas padrão do **Java (JDK)**, sem frameworks externos como Spring ou Hibernate.

* **Java 17+**: Linguagem principal.
* **com.sun.net.httpserver**: Servidor HTTP nativo do Java para gerenciar requisições e rotas.
* **Maven**: Gerenciamento de dependências e build.
* **JUnit 5** (Opcional/Previsto): Para testes unitários.
## 🏗️ Estrutura do Projeto

O projeto segue a estrutura padrão do Maven e uma arquitetura em camadas (Layered Architecture) para garantir a separação de responsabilidades:

```text
.
├── pom.xml                 # Configuração de dependências e build do Maven
└── src
    └── main
        └── java
            └── br.com.taylor
                ├── application
                │   └── Main.java           # Ponto de entrada (Inicia o servidor)
                ├── controller
                │   └── TaskController.java # Recebe requisições HTTP e valida dados
                ├── entity
                │   └── Task.java           # Modelo de dados (Objeto Task)
                ├── enums
                │   └── TaskStatus.java     # Estados da tarefa (TODO, DONE, etc.)
                ├── repository
                │   └── TaskRepository.java # Persistência de dados (em memória)
                ├── serializer
                │   └── TaskSerializer.java # Converte JSON <-> Objeto Java
                ├── service
                │   └── TaskService.java    # Regras de negócio
                └── utils
                    └── JsonUtils.java      # Utilitários gerais
