package br.com.taylor.integration;

import br.com.taylor.entity.Task;
import br.com.taylor.enums.TaskStatus;
import br.com.taylor.repository.JdbcTaskRepository;
import br.com.taylor.repository.TaskRepository;
import br.com.taylor.service.TaskService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TaskServiceIntegrationTest {

    private Connection connection;
    private TaskRepository repository;
    private TaskService service;

    private static final int BATCH_SIZE = 100;
    private static final int SLEEP_MS = 10;

    @BeforeAll
    void setupDatabase() throws Exception {
        System.out.println("🔧 Configurando banco de teste em memória...");

        // 1. Criar banco SQLite em memória
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");

        // 2. Criar tabela tasks
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    description TEXT NOT NULL,
                    status TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
        }

        System.out.println("✅ Banco configurado!");

        // 3. Criar proxi que NÃO fecha a conexão após cada operação
        Connection nonClosingConnection = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) ->{
                    if (method.getName().equals("close")) {
                        System.out.println("🚫 Ignorando close() na conexão de teste!");
                        return null; // Ignora a chamada de close()
                    }
                        return method.invoke(connection, args);
                   }
        );

        // 4. Criar repositório e serviço usando o proxi
        repository = new JdbcTaskRepository(() -> nonClosingConnection);
        service = new TaskService(repository);
    }

    @AfterEach
    void cleanDatabase() throws Exception {
        // Limpar dados entre testes
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM tasks");
        }
    }

    @AfterAll
    void closeDatabase() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("🔒 Banco fechado!");
        }
    }

    private void criarTarefasComStatusVariados() {
        Map<String, TaskStatus> dados = Map.of(
                "Tarefa 1", TaskStatus.TODO,
                "Tarefa 2", TaskStatus.IN_PROGRESS,
                "Tarefa 3", TaskStatus.DONE,
                "Tarefa 4", TaskStatus.TODO,
                "Tarefa 5", TaskStatus.IN_PROGRESS,
                "Tarefa 6", TaskStatus.DONE
        );

        dados.forEach((desc, status) ->
                service.create(new Task(0, desc, status))
        );
    }

    // Teste de fluxo completo: criar tarefa e buscar por ID
    @Test
    void deveCriarEBuscarTarefaComFluxoCompleto() {
        // GIVEN
        Task novaTarefa = new Task(0, "Meu primeiro teste de integração", TaskStatus.TODO);

        // WHEN
        Task criada = service.create(novaTarefa);
        Task encontrada = service.findById((long) criada.getId());

        // THEN
        assertNotNull(encontrada);
        assertEquals("Meu primeiro teste de integração", encontrada.getDescription());
        assertEquals(TaskStatus.TODO, encontrada.getStatus());
        assertTrue(criada.getId() > 0);

        System.out.println("✅ Tarefa criada e encontrada com sucesso: " + encontrada);
    }

    // Teste de fluxo completo: criar tarefa, atualizar status e verificar atualização
    @Test
    void deveAtualizarStatusDaTarefa() {
        //given
        Task criada = service.create(new Task(0, "Tarefa para atualizar", TaskStatus.TODO));

        //when
        criada.setStatus(TaskStatus.DONE);
        Task atualizada = service.update((long) criada.getId(), criada);

        //then
        assertEquals(TaskStatus.DONE, criada.getStatus());
        assertEquals(criada.getStatus(), atualizada.getStatus());
        assertEquals(TaskStatus.DONE, service.findById((long) criada.getId()).getStatus());
        assertEquals("Tarefa para atualizar", atualizada.getDescription());
    }

    // Teste de fluxo completo: criar múltiplas tarefas e verificar contagem e integridade dos dados
    @Test
    void deveInserirMultiplasTarefasEContarCorretametne() {
        //given
        List<String> descriptions = List.of("Tarefa 1", "Tarefa 2", "Tarefa 3");
        for (String desc : descriptions) {
            service.create(new Task(0, desc, TaskStatus.TODO));
        }

        //when
        List<Task> todas = service.findAll();

        //then
        // Compara quantidade de tasks
        assertEquals(descriptions.size(), todas.size());
        // Valida integridade das descrições
        List<String> descricoesRetornadas = todas.stream()
                .map(Task::getDescription)
                .sorted()
                .collect(Collectors.toList());
        assertEquals(
                descriptions.stream().sorted().collect(Collectors.toList()),
                descricoesRetornadas,
                "Deve conter todas as descrições");

        List<String> ordenacao = todas.stream()
                .sorted(Comparator.comparing(Task::getCreatedAt).reversed())
                .map(Task::getDescription)
                .toList();
        assertTrue(ordenacao.containsAll(descricoesRetornadas),
                "Deve conter todas as descrições");
    }

    // Cria uma tarefa, e atualiza a sua descrição
    @Test
    void deveAtualizarDescricaoDaTarefa() {
        //given
        Task criada = service.create(new Task(0, "Nova Task",  TaskStatus.TODO));

        //when
        criada.setDescription("Nova Task Atualizada");
        Task atualizada = service.update((long) criada.getId(), criada);

        //then
        assertEquals(criada.getId(), service.findById((long) criada.getId()).getId());
        assertEquals("Nova Task Atualizada", atualizada.getDescription());
        assertEquals(criada.getDescription(), atualizada.getDescription());
        assertEquals(service.findById((long) criada.getId()).getDescription(), atualizada.getDescription());
    }

    // Teste de fluxo completo: Cria objeto Task, e deleta, valida delete e não persistência em banco.
    @Test
    void deveDeletarTarefa() {
        //given
        Task criada = service.create(new Task(0,"To Delete", TaskStatus.TODO));

        //when
        boolean deletou = service.delete((long) criada.getId());

        //then
        assertTrue(deletou);

        //Validar que a tarefa não existe mais (lança exceção)
        RuntimeException excecao = assertThrows(
                RuntimeException.class,
                () -> service.findById((long) criada.getId())
        );

        assertTrue(
                excecao.getMessage().contains("Task not found"),
                "Mensagem deve indicar que a tarefa não foi encontrada"
        );
    }

    // Teste de fluxo completo: Deve gerar IDs únicos.
    @Test
    void deveGerarIDsSequenciaisAutomaticamente() {
        //given + when
        List<String> tasksDescription = List.of("Tarefa 1", "Tarefa 2", "Tarefa 3");

        for (String desc :  tasksDescription) {
            service.create(new Task (0, desc, TaskStatus.TODO));
        }
        //when
        List<Task> todas = service.findAll();

        //then
        // Validar quantidade
        assertEquals(tasksDescription.size(), todas.size());
        // Validar 'IDs' únicos e não nulos
        Set<Integer> checkIDs = todas.stream()
                .map(Task::getId)
                .collect(Collectors.toSet());
        assertEquals(todas.size(), checkIDs.size(), "Todos os IDs devem ser ÚNICOS.");
        assertTrue(checkIDs.stream().allMatch(id -> id > 0), "IDs deve ser positivo.");
        // Valida que todas as descrições estão presentes
        // Alternativa 1
        assertEquals(
                tasksDescription.stream().sorted().collect(Collectors.toList()),
                todas.stream()
                        .sorted(Comparator.comparing(Task::getDescription))
                        .map(Task::getDescription)
                        .collect(Collectors.toList()),
                "Todas as descrições deve estar presentes"
        );
        // Alternativa 2
        Set<String> compareDescriptions = todas.stream()
                .map(Task::getDescription)
                .collect(Collectors.toSet());
        assertTrue(
                compareDescriptions.containsAll(tasksDescription),
                "Todas as descrições devem estar presentes"
        );
    }

    // Teste de fluxo completo: Deve criar Uma task, e verificar se está gerando Timestamps automáticamente
    @Test
    void devePreencherTimestampsAutomaticamente() {
        //given
        LocalDateTime before = LocalDateTime.now();

        //when
        Task novaTask = service.create(new Task(0, "Task Criada", TaskStatus.TODO));
        LocalDateTime after = LocalDateTime.now();

        //then
        assertNotNull(novaTask.getCreatedAt());
        assertNotNull(novaTask.getUpdatedAt());

        //Valida que novaTask está dentro do intervalo before/after
        assertTrue(
                !novaTask.getCreatedAt().isBefore(before) &&
                !novaTask.getUpdatedAt().isAfter(after),
                "created_at deve estar entre before e after da operação"
        );

        // Valida que created_at e updated_at são iguais na criação inicial (margem de 1 seg)
        assertTrue(
                Duration.between(novaTask.getCreatedAt(), novaTask.getUpdatedAt()).getSeconds() <= 1,
                "crated_at e updated_at devem ser praticamente iguais na criação"
        );
    }

    // Teste de fluxo completo: Criação, atualização de task, verifica Timestamp updated_at
    @Test
    void deveAtualizarTimestampNaAtualizacao() {
        //given
        Task taskInicial = service.create(new Task(0, "Task criada", TaskStatus.TODO));
        LocalDateTime createdAtInicial = taskInicial.getCreatedAt()
                .truncatedTo(ChronoUnit.MILLIS);
        LocalDateTime updatedAtBefore = taskInicial.getUpdatedAt();

        // Wait 10ms to guarantee difference between created and updated after update task
        try { Thread.sleep(SLEEP_MS); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        };

        //when
        taskInicial.setDescription("Task modificada");
        Task taskAtualizada = service.update((long) taskInicial.getId(), taskInicial);
        LocalDateTime createdAtAtualizada = taskAtualizada.getCreatedAt()
                .truncatedTo(ChronoUnit.MILLIS);

        //then
        assertEquals(
                createdAtInicial,
                createdAtAtualizada,
                "created_at não deve ser modificado por atualizações na Task"
        );
        assertTrue(
                taskAtualizada.getUpdatedAt().isAfter(updatedAtBefore),
                "updated_at deve ser posterior ao atualiza"
        );
    }

    // Teste de fluxo completo: Buscar Task por Status
    @Test
    void deveBuscarTarefasPorStatus() {
        //given
        // (2 TODO / 2 IN_PROGRESS / 2 DONE)
        criarTarefasComStatusVariados();

        //when
        List<Task> todoTasks = service.findByStatus(List.of(TaskStatus.TODO));
        List<Task> inProgressTasks = service.findByStatus(List.of(TaskStatus.IN_PROGRESS));
        List<Task> doneTasks = service.findByStatus(List.of(TaskStatus.DONE));

        //then
        assertEquals(2, todoTasks.size(), "Deve conter duas tarefas TODO");
        assertEquals(2, inProgressTasks.size(), "Deve conter duas tarefas IN_PROGRESS");
        assertEquals(2, doneTasks.size(), "Deve conter duas tarefas DONE");
        //
        assertTrue(todoTasks.stream().
                allMatch(t -> t.getStatus() == TaskStatus.TODO),
                "Todas as tasks devem ter status TODO");
        assertTrue(inProgressTasks.stream().
                allMatch(t -> t.getStatus() == TaskStatus.IN_PROGRESS),
                "Todas as tasks devem ter status IN_PROGRESS");
        assertTrue(doneTasks.stream().
                allMatch(t -> t.getStatus() == TaskStatus.DONE),
                "Todas as tasks devem ter status DONE");
    }

    // Deve lançar (IllegalArgumentException) ao tentar criar tarefa com descrição vazia.
    @Test
    void deveLancarErroAoCriarTarefaComDescricaoVazia() {
        //given
        Task noDescriptionTask = new Task(0, "", TaskStatus.TODO);

        //when + then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(noDescriptionTask),
                "service.create() não deve aceitar Task com descrição (vazia/null)"
        );

        // checa se o banco foi afetado
        List<Task> anyTask = service.findAll();
        assertTrue(anyTask.isEmpty());
    }

    // Deve lançar RuntimeException ao tentar deletar task com status DONE
    @Test
    void deveLancarErroAoDeletarTarefaConcluida() {
        //given
        Task doneTask = service.create(new Task(0, "Done task", TaskStatus.DONE));

        //when + then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.delete((long) doneTask.getId()),
                "Deve lançar exceção, ao tentar deletar task com status DONE"
        );

        //then: Validar que é a exceção CORRETA (não "Task not found")
        assertTrue(
                exception.getMessage().toLowerCase().contains("done"),
                "Mensagem deve mencionar 'done': " + exception.getMessage()
        );

        List<Task> allTasks = service.findAll();
        //Validar que tarefa específica ainda existe
        assertTrue(allTasks.stream().anyMatch(t -> t.getId() == doneTask.getId()),
                "Tarefa DONE deve permanecer no banco após tentativa de delete");

        //then: banco de dados intacto
        assertFalse(allTasks.isEmpty(), "Não deve deletar tasks com Status DONE");
    }

    // Validar atualização de Timestamp updated_at após atualização da task
    @Test
    void deveAtualizarTimestampAposUpdate() {
        //GIVEN
        Task taskInicial = service.create(new Task(0, "Task Criada", TaskStatus.IN_PROGRESS));

        //Normalizar ara milissegundos
        LocalDateTime createdAtAntes = taskInicial.getCreatedAt().truncatedTo(ChronoUnit.MILLIS);
        LocalDateTime updatedAtAntes = taskInicial.getUpdatedAt().truncatedTo(ChronoUnit.MILLIS);

        //Garantir diferença nos timestamps
        try {Thread.sleep(SLEEP_MS);} catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        //WHEN: Atualizar task
        taskInicial.setDescription("Task Atualizada");
        Task taskAtualizada = service.update((long) taskInicial.getId(), taskInicial);

        //Normalizar timestamps retornados
        LocalDateTime createdAtAtualizada = taskAtualizada.getCreatedAt().truncatedTo(ChronoUnit.MILLIS);
        LocalDateTime updatedAtAtualizada = taskAtualizada.getUpdatedAt().truncatedTo(ChronoUnit.MILLIS);

        //THEN: Validações precisas

        // 1. created_at NÃO deve mudar
        assertEquals(
                createdAtAntes,
                createdAtAtualizada,
                            "created_at deve permanecer inalterado após update"
        );

        // 2. updated_at DEVE ser posterior ao anterior
        assertTrue(
                updatedAtAtualizada.isAfter(updatedAtAntes),
                String.format(
                        "updated_at deve ser posterior (antes %s, depois %s)",
                updatedAtAntes, updatedAtAtualizada
                )
        );
    }

    // Deve Lançar erro ao buscar por ‘ID’ inexistente
    @Test
    void deveLancarErroAoBuscarIDInexistente() {
        // GIVEN
        Long idInexistente = -1L;

        // WHEN + THEN
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> service.findById(idInexistente),
                "Deve lançar erro ao buscar ID inexistente"
        );

        // Validar MENSAGEM específica
        String message = exception.getMessage();
        assertTrue(
                message.contains("Task not found: " + idInexistente),
                "Mensagem deve indicar 'Task not found: " + exception.getMessage()
        );

        // Validar que NÃO é NullPointerException (bug)
        assertFalse(
                exception instanceof NullPointerException,
                "Não deve ser NullPointerException (indica bug no serviço)"
        );
    }

    // Validar ORDEM de criação.

    @Test
    void deveManterOrdemDeCriacao() {
        //GIVEN
        criarTarefasComStatusVariados();

        //WHEN
        List<Task> allTasks = service.findAll();

        //THEN
        boolean estaOrdenado = IntStream.range(0, allTasks.size() -1)
                .allMatch(i ->
                        allTasks.get(i).getId() < allTasks.get(i + 1).getId());
        assertTrue(
                estaOrdenado,
                "IDs devem estar em ordem crescente (refletindo ordem de criação)"
                );
        // Show IDs for debug
        System.out.println("IDs: " + allTasks.stream()
                .map(Task::getId)
                .toList()
        );
    }

    @ParameterizedTest
    @CsvSource({
            "V1, V2, TODO, TODO",
            "V1, V1, TODO, IN_PROGRESS",
            "V1, V2, TODO, TODO"
    })
    void devePermitirAtualizacaoIndividual(
            String descAntes, String descDepois,
            TaskStatus statusAntes, TaskStatus statusDepois
    ){
        // GIVEN
        Task task = service.create(new Task(0, descAntes, statusAntes));

        // WHEN
        task.setDescription(descDepois);
        task.setStatus(statusDepois);
        service.update((long) task.getId(), task);

        // THEN
        Task atual = service.findById((long) task.getId());
        assertAll("Validar atualização",
                () -> assertEquals(descDepois, atual.getDescription()),
                () -> assertEquals(statusDepois, atual.getStatus())
        );
    }

    @Test
    void deveIniciarSemTasks() {
        // GIVEN + WHEN
        List<Task> allTasks = service.findAll();
        // THEN
        assertTrue(allTasks.isEmpty());
        assertEquals(0, allTasks.size());
    }

    @Test
    void deveInserirCemTarefasRapidamente() {
        // GIVEN
        int quantidade = BATCH_SIZE;
        long inicio =  System.currentTimeMillis();

        // WHEN
        for (int i = 0; i < quantidade; i++) {
            service.create(new Task(0, "Task " + i, TaskStatus.IN_PROGRESS));
        }
        long fim = System.currentTimeMillis();

        // THEN
        List<Task> allTasks = service.findAll();
        assertEquals(quantidade, allTasks.size());

        long tempoTotal = fim - inicio;
        System.out.println("Tempo total: " + tempoTotal + "ms");
        assertTrue(tempoTotal < 5000, "Inserção demorou mais de 5 segundos: " + tempoTotal + "ms");
    }





    @ParameterizedTest
    @MethodSource("dadosDescricaoInvalida")
    void deveRejeitarDescricaoInvalida2(String descricaoInvalida) {
        // WHEN + THEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(new Task(0,descricaoInvalida, TaskStatus.TODO))
        );

        // Validar mensagem
        String message = exception.getMessage().toLowerCase();
        assertTrue(
                message.contains("description")
                        && (message.contains("empty")
                        || message.contains("blank")),
                "Mensagem deve mencionar 'description' e 'empty/blank': " + message
        );
    }
    static Stream<String> dadosDescricaoInvalida() {
        return Stream.of("","   ","\t","\n");
    }


    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"}) // vazio, espaços, tab, quebra de linha
    void deveRejeitarDescricaoInvalida(String descricaoRuim) {
        // Roda 4 vezes, uma para cada valor acima
        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(new Task(0, descricaoRuim, TaskStatus.TODO)),
                "Descrição '" + descricaoRuim + "' deve ser rejeitada"
        );
    }
}















