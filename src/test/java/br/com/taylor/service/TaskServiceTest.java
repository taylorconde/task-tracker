package br.com.taylor.service;

import br.com.taylor.entity.Task;
import br.com.taylor.enums.TaskStatus;
import br.com.taylor.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private static final int TASK_LOOP_COUNT = 10;

    @Mock
    private TaskRepository repository;

    @InjectMocks
    private TaskService service;

    //CREATE
    @Test
    void deveCriarTarefaComSucesso(){
        //GIVEN
        int id = 1;
        String descricao = "descricao";
        TaskStatus status = TaskStatus.TODO;
        Task expected = new Task(id,descricao,status);

        when(repository.save(expected)).thenReturn(expected);

        //WHEN
        var actual = service.create(expected);

        //THEN
        verify(repository).save(expected);
        assertNotNull(actual);
        assertEquals(expected,actual);
    }

    @Test
    void deveLancarErroQuandoDescricaoVazia(){
        //GIVEN
        int id = 1;
        TaskStatus status = TaskStatus.DONE;
        String descricao = "";
        Task tarefaInvalida = new Task(id,descricao,status);

        //WHEN & THEN
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> {
            service.create(tarefaInvalida);
        });

        // Verificacao adicional
        assertEquals("Description cannot be empty", actual.getMessage());
        }

    @Test
    void deveLancarErroQuandoStatusVazia(){
        //GIVEN
        int id = 1;
        String description = "descricao";
        Task tarefaInvalida = new Task(id, description, null);

        //WHEN & THEN
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> {
            service.create(tarefaInvalida);
        });

        assertEquals("Status cannot be null", actual.getMessage());
    }

    //UPDATE
    @Test
    @DisplayName("ATUALIZAR TAREFA OK!")
    void deveAtualizarTarefaComSucesso(){
        //given
        Long idExistente = 1L;
        Task tarefaSalva = new Task(1,"descricao",TaskStatus.TODO);

        given(repository.findById(anyLong())).willReturn(tarefaSalva);
        given(repository.update(anyLong(), any(Task.class))).willReturn(true);

        //when
        Task actual = service.update(idExistente, new Task(1, "descricao", TaskStatus.DONE));

        //then
        assertEquals(TaskStatus.DONE,actual.getStatus());
        assertEquals(TaskStatus.DONE,tarefaSalva.getStatus());
        then(repository).should(times(1)).findById(anyLong());
        then(repository).should().update(eq(idExistente), any(Task.class));
    }


    @Test
    void deveLancarErroAoAtualizarTarefaInexistente() {
        //GIVEN
        Long idInexistente = 99L;
        Task tarefa = new Task(0, "descricao", TaskStatus.DONE);

        when(repository.findById(idInexistente)).thenReturn(null);
        //WHEN & THEN
        RuntimeException actual = assertThrows(RuntimeException.class, () ->
                service.update(idInexistente, tarefa));

        assertEquals("Task not found: " + idInexistente,actual.getMessage());
    }

    @Test
    void deveLancarErroAoAtualizarTarefaComDescricaoVazia() {
        //given
        Long idExistente = 1L;
        Task tarefaExistente = new Task(1, "description", TaskStatus.IN_PROGRESS);


        given(repository.findById(idExistente)).willReturn(tarefaExistente);

        //when
        RuntimeException actual = assertThrows(IllegalArgumentException.class, () ->
                service.update(idExistente, new Task(1, "", TaskStatus.DONE)));

        assertEquals("Description cannot be empty or blank", actual.getMessage());
    }

    // DELETE
    @Test
    void deveLancarErroAoTentarDeletarTarefaConclulida() {
        //given
        Long idExistente = 1L;
        Task tarefaExistente = new Task(1, "descricao", TaskStatus.DONE);

        given(repository.findById(idExistente)).willReturn(tarefaExistente);

        //when
        RuntimeException actual = assertThrows(RuntimeException.class, () ->
                service.delete(idExistente));

        //then

        assertEquals("Task is already done: " + idExistente,actual.getMessage());
        then(repository).should(never()).delete(anyLong());
        then(repository).should().findById(idExistente);
    }

    @Test
    void deveDeletarTarefaComSucesso(){
        //given
        Long idExistente = 1L;
        Task tarefaExistente = new Task(1, "descricao", TaskStatus.IN_PROGRESS);

        given(repository.findById(idExistente)).willReturn(tarefaExistente);
        given(repository.delete(idExistente)).willReturn(true);
        //when
        var actual = service.delete(idExistente);

         //then
        assertTrue(actual);
        then(repository).should().delete(idExistente);
    }

    //FIND BY STATUS
    @Test
    void deveRetornarListaVaziaQuandoNaoEncontrarTarefas(){
        //given
        List<TaskStatus> status = List.of(TaskStatus.DONE);

        given(repository.findByStatus(status)).willReturn(new ArrayList<>());
        //when
        List<Task> actual = service.findByStatus(status);

        //then
        assertTrue(actual.isEmpty());
    }

    @Test
    void deveRetornarListaQuandoEncontrarTarefas(){
        //given
        List<TaskStatus> status = List.of(TaskStatus.DONE, TaskStatus.TODO);
        List<Task> tarefasConcluidas = new ArrayList<>();
        for(int i = 0; i < TASK_LOOP_COUNT; i++){
            tarefasConcluidas.add(new Task(i, "description:  " + i, TaskStatus.DONE));
            tarefasConcluidas.add(new Task(i, "description:  " + i, TaskStatus.TODO));
        }

        given(repository.findByStatus(status)).willReturn(tarefasConcluidas);
        //when
        List<Task> actual = service.findByStatus(status);

        //then
        assertEquals(tarefasConcluidas, actual);
        then(repository).should(never()).delete(any());
        then(repository).should(never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar erro ANTES para descrição vazia SEM chamar repository")
    void deveLancarErroParaDescricaoVaziaSemChamarRepository() {
        //GIVEN
        Task tarefa = new Task(0, "", TaskStatus.DONE);

        //WHEN + THEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(tarefa),
                "Não deve permitir tasks com descrição vazia."
                );
        // THEN: Verificar mensagem
        assertTrue(exception.getMessage().toLowerCase().contains("empty"),
                "Mensagem deve indicar campo vazio: " + exception.getMessage()
                );

        // THEN: Verificar que repository NÃO foi chamado
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("TaskService DEVE chamar repository.findById() corretamente")
    void deveChamarRepositoryFindById() {
        // GIVEN
        Long idLong = 1L;
        Task  tarefa = new Task(1, "descricao", TaskStatus.DONE);
        given(repository.findById(idLong)).willReturn(tarefa);

        //WHEN
        Task actual = service.findById(idLong);

        // THEN: Validar conteúdo
        assertEquals(tarefa.getId(), actual.getId(),"ID deve corresponder");
        assertEquals(tarefa.getDescription(), actual.getDescription(), "Descrição deve corresponder");
        assertEquals(tarefa.getStatus(), actual.getStatus(),  "Status deve corresponder");

        // Validar interação
        verify(repository, times(1)).findById(idLong);
    }

    @Test
    @DisplayName("Service DEVE buscar task antiga antes de atualizar")
    void deveBuscarTaskAntigaAntesDeAtualizar() {
        //GIVEN
        Long idLong = 1L;
        Task tarefa = new Task(1, "old Task", TaskStatus.TODO);
        given(repository.findById(idLong)).willReturn(tarefa);
        given(repository.update(anyLong(), any(Task.class))).willReturn(true);

        //WHEN
        tarefa.setDescription("New Description");
        service.update(idLong, tarefa);

        //THEN: Verificar ORDEN das chamadas
        InOrder inOrder = inOrder(repository);
        inOrder.verify(repository).findById(eq(idLong));
        inOrder.verify(repository).update(eq(idLong), any());

        // Validar que update() recebeu a task modificada
        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(repository).update(eq(idLong), captor.capture());

        Task taskAtualizada = captor.getValue();
        assertEquals("New Description", taskAtualizada.getDescription(),
                "Descrição atualizada deve ser persistida");
    }

    @Test
    @DisplayName("NÃO deve deletar task com status DONE")
    void naoDeveDeletarTaskComStatusDone() {
        // GIVEN
        Long idLong = 1L;
        Task tarefa = new Task(1, "descricao", TaskStatus.DONE);
        given(repository.findById(idLong)).willReturn(tarefa);

        // WHEN + THEN
        RuntimeException exception =  assertThrows(
                RuntimeException.class,
                () -> service.delete(idLong),
                "Não deve deletar task com status DONE"
                );
        // Validar mensagem específica (protege contra falsos positivos)
        String msg = exception.getMessage().toLowerCase();
        assertTrue(
                msg.toLowerCase().contains("done") || msg.toLowerCase().contains("concluída"),
                "Mensagem deve indicar status DONE: "+ exception.getMessage()
        );

        // Não é nullPointerException (bug!)
        assertFalse(
                exception instanceof NullPointerException,
                "Não deve ser NullPointerException (indica bug no serviço)"
        );

        verify(repository, never()).delete(anyLong());
        verify(repository).findById(eq(idLong));
    }

    @Test
    @DisplayName("repository.save() deve ser chamado sempre que uma task é criada ")
    void repositoryDeveSerChamadoSempreQueUmaTaskForCriada() {
        // GIVEN
        AtomicInteger counter = new AtomicInteger(0);
        given(repository.save(any(Task.class))).willAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            // Simular preenchimento de ID pelo banco
            if (task.getId() == 0) {
                task.setId(counter.getAndIncrement());
            }
            return task;
        });

        // WHEN
        List<Task> tasks = List.of(
                new Task(0, "Task1", TaskStatus.TODO),
                new Task(0, "Task2", TaskStatus.TODO),
                new Task(0, "Task3", TaskStatus.TODO),
                new Task(0, "Task4", TaskStatus.TODO),
                new Task(0, "Task5", TaskStatus.TODO)
        );

        for (Task task : tasks) {
            service.create(task);
        }

        // THEN
        verify(repository, times(tasks.size())).save(any());
    }

    @Test
    @DisplayName("deveRetornarTodasAsTarefasQuandoExistirem")
    void deveRetornarTodasAsTarefasQuandoExistirem() {
        // GIVEN
        List<Task> allTasks = IntStream.rangeClosed(1, TASK_LOOP_COUNT)
                        .mapToObj(i -> new Task(i, "Taks" + i, TaskStatus.TODO))
                        .collect(Collectors.toList());
        given(repository.findAll()).willReturn(allTasks);

        // WHEN
        List<Task> result = service.findAll();

        // THEN
        assertNotNull(result, "Lista não deve ser nula");
        assertEquals(TASK_LOOP_COUNT, result.size(), "Tamanho deve corresponder");

        // Verificação essencial
        verify(repository).findAll();
    }

    @Test
    @DisplayName("deveRetornarListaVaziaQuandoNaoExistiremTarefas")
    void deveRetornarListaVaziaQuandoNaoExistiremTarefas() {
        // GIVEN
        given(repository.findAll()).willReturn(Collections.emptyList());

        // WHEN
        List<Task> result = service.findAll();

        // THEN
        assertTrue(result.isEmpty(), "Lista deve estar vazia quando não há tarefas");
        verify(repository).findAll();
    }

    @Test
    @DisplayName("deveLancarErroQuandoRepositoryUpdateRetornarFalse")
    void deveLancarErroQuandoRepositoryUpdateRetornarFalse() {
       //GIVEN
        Long idLong = 1L;
        Task taskExistente = new Task(1, "descricao original", TaskStatus.TODO);

        given(repository.findById(eq(idLong))).willReturn(taskExistente);
        given(repository.update(eq(idLong),any(Task.class))).willReturn(false);

        //WHEN THEN
        Task taskModificada = new  Task(1, "descricao modificada", TaskStatus.DONE);
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.update(idLong, taskModificada),
                "Deve lançar exceção quando update falha"
        );

        //THEN
        assertTrue(
                exception.getMessage().toLowerCase().contains("falha") ||
                        exception.getMessage().toLowerCase().contains("atualizar"),
                "Mensagem deve indicar falha na atualização: " + exception.getMessage()
        );

        InOrder inOrder = inOrder(repository);
        inOrder.verify(repository).findById(eq(idLong));
        inOrder.verify(repository).update(eq(idLong), any(Task.class));

    }

    @Test
    @DisplayName("deveAtualizarApenasStatusQuandoDescricaoForNull")
    void deveAtualizarApenasStatusQuandoDescricaoForNull() {
        // GIVEN
        Long idLong = 1L;
        Task originalTask = new Task(1, "Original Description", TaskStatus.TODO);

        LocalDateTime before = originalTask.getUpdatedAt().truncatedTo(ChronoUnit.MILLIS);

        given(repository.findById(eq(idLong))).willReturn(originalTask);
        given(repository.update(eq(idLong),any(Task.class))).willReturn(true);

        Task updatedTask = new Task(1, null, TaskStatus.DONE);

        // WHEN
        Task result = service.update(idLong, updatedTask);
        LocalDateTime after = originalTask.getUpdatedAt().truncatedTo(ChronoUnit.MILLIS);

        // THEN

        assertEquals(TaskStatus.DONE, originalTask.getStatus(),
                "Status deve ser atualizado para DONE");
        assertEquals(TaskStatus.DONE, result.getStatus());

        assertEquals("Original Description", originalTask.getDescription(),
                "Descrição não deve ser alterada quando null");
        assertEquals("Original Description", result.getDescription());

        assertTrue(
                !after.isBefore(before),
                String.format(
                        "Timestamp não deve retroceder (before: %s, after: %s, diferença: %dms)",
                        before, after, ChronoUnit.MILLIS.between(before, after)
                )
        );

        verify(repository).update(eq(idLong), argThat(task ->
                task.getStatus() == TaskStatus.DONE &&
                "Original Description".equals(task.getDescription())
        ));
    }

    @Test
    @DisplayName("deveAtualizarApenasDescricaoQuandoStatusForNull")
    void deveAtualizarApenasDescricaoQuandoStatusForNull() {
        // GIVEN
        Long idLong = 1L;
        Task  originalTask = new Task(1, "Original Description", TaskStatus.TODO);

        LocalDateTime before = originalTask.getUpdatedAt().truncatedTo(ChronoUnit.MILLIS);

        given(repository.findById(eq(idLong))).willReturn(originalTask);
        given(repository.update(eq(idLong),any(Task.class))).willReturn(true);

        Task  updatedTask = new Task(1, "New Description", null);
        // WHEN
        Task result = service.update(idLong, updatedTask);
        LocalDateTime after = originalTask.getUpdatedAt().truncatedTo(ChronoUnit.MILLIS);

        // THEN
        assertEquals(TaskStatus.TODO, originalTask.getStatus(),
                "Status não deve ser alterada, quando null");
        assertEquals(TaskStatus.TODO, result.getStatus());

        assertEquals("New Description", originalTask.getDescription(),
                "Descrição deve ser alterada.");
        assertEquals("New Description", result.getDescription());

        assertTrue(
                !after.isBefore(before),
                String.format(
                        "Timestamp não deve retroceder (before: %s, after: %s, diferença: %dms)",
                        before, after, ChronoUnit.MILLIS.between(before, after)
                )
        );
        verify(repository).update(eq(idLong), argThat(task ->
                task.getStatus() == TaskStatus.TODO &&
                task.getDescription().equals("New Description")
        ));
    }

    @Test
    @DisplayName("naoDeveAtualizarDescricaoQuandoDescricaoForVazia")
    void naoDeveAtualizarDescricaoQuandoDescricaoForVazia() {
        // GIVEN
        Long idLong = 1L;
        Task originalTask = new Task(1,"description", TaskStatus.TODO);
        LocalDateTime  before = originalTask.getUpdatedAt().truncatedTo(ChronoUnit.MILLIS);

        given(repository.findById(eq(idLong))).willReturn(originalTask);

        Task wrongTask = new Task(1,"", TaskStatus.TODO);
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.update(idLong, wrongTask),
                "Deve lançar exceção para descrição vazia"
        );
        LocalDateTime after = originalTask.getUpdatedAt().truncatedTo(ChronoUnit.MILLIS);
        // THEN

        assertTrue(
                exception.getMessage().toLowerCase().contains("empty"),
                "Mensagem deve conter 'empty': " + exception.getMessage()
        );
        verify(repository, never()).update(eq(idLong), any(Task.class));
        assertEquals("description", originalTask.getDescription(),
                "Descrição original não deve ser alterada");
        assertEquals(before, after,
                "Timestamp não deve ser atualizado em caso de erro");
    }

    @Test
    @DisplayName("deveLancarErroQuandoDescricaoForApenasEspacosEmBranco")
    void deveLancarErroQuandoDescricaoForApenasEspacosEmBranco() {
        // GIVEN
        Long idLong = 1L;
        Task originalTask = new Task(1, "original Description", TaskStatus.TODO);
        LocalDateTime before = originalTask.getUpdatedAt().truncatedTo(ChronoUnit.MILLIS);
        given(repository.findById(eq(idLong))).willReturn(originalTask);

        Task wrongTask = new Task(1, " ",  TaskStatus.TODO);

        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.update(idLong, wrongTask),
                "Deve lançar erro ao tentar atualizar taks com descrição em branco"
        );
        LocalDateTime after = originalTask.getUpdatedAt().truncatedTo(ChronoUnit.MILLIS);

        // THEN
        assertTrue(
                exception.getMessage().toLowerCase().contains("empty") ||
                exception.getMessage().toLowerCase().contains("blank"),
                "mensagem deve conter 'empty' ou 'blank': " + exception.getMessage()
        );
        assertEquals(before,after,
                "Timestamp não deve ser atualizado em caso de erro"
        );
        assertEquals("original Description", originalTask.getDescription(),
                "Descrição original não deve ser alterada");
        verify(repository, never()).update(eq(idLong), any(Task.class));
    }

    @Test
    @DisplayName("deveBuscarTarefaPorIdComSucesso")
    void deveBuscarTarefaPorIdComSucesso() {
        // GIVEN
        Long idLong = 1L;
        Task task = new Task(1,"tarefa", TaskStatus.DONE);
        given(repository.findById(eq(idLong))).willReturn(task);

        // WHEN
        Task taskFound = service.findById(idLong);

        // THEN
        assertNotNull(taskFound,"Tarefa deve ser encontrada");
        assertAll(
                "Valisar campos da tarefa",
                () -> assertEquals(task.getId(), taskFound.getId(), "Id deve ser igual"),
                () -> assertEquals(task.getDescription(),taskFound.getDescription(), "Descrição deve ser igual"),
                () -> assertEquals(task.getStatus(), taskFound.getStatus(), "Status deve ser o mesmo.")
        );
        verify(repository).findById(eq(idLong));
    }

    @Test
    @DisplayName("deveLancarErroQuandoBuscarPorIdInexistente")
    void deveLancarErroQuandoBuscarPorIdInexistente() {
        // GIVEN
        Long idLong = -1L;
        given(repository.findById(eq(idLong))).willReturn(null);

        // WHEN
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.findById(idLong),
                "Deve lançar RuntimeException quando ID não existe"
        );

        // THEN
        assertTrue(exception.getMessage().toLowerCase().contains("found") ||
                exception.getMessage().toLowerCase().contains("not"),
                "Mensagem deve conter 'not' ou 'found': "  + exception.getMessage()
        );
        assertFalse(exception instanceof NullPointerException,
                "Não deve ser NullPointerException — deve ser RuntimeException explícita");
        verify(repository).findById(eq(idLong));
    }

    @Test
    @DisplayName("deveBuscarTarefasPorStatusComSucesso")
    void deveBuscarTarefasPorStatusComSucesso() {
        // GIVEN
        List<TaskStatus> specificStatuses = List.of(
                TaskStatus.TODO,
                TaskStatus.DONE,
                TaskStatus.IN_PROGRESS,
                TaskStatus.TODO
        );
        List<Task> tasks = List.of(
                new Task(0,"Task",TaskStatus.TODO),
                new Task(0,"Task",TaskStatus.TODO),
                new Task(0,"Task",TaskStatus.TODO),
                new Task(0,"Task",TaskStatus.IN_PROGRESS),
                new Task(0,"Task",TaskStatus.DONE),
                new Task(0,"Task",TaskStatus.DONE)
        );
        given(repository.findByStatus(argThat(list ->
                list.containsAll(List.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS,TaskStatus.DONE))
        ))).willReturn(tasks);

        // WHEN
        List<Task> foundTasks = service.findByStatus(specificStatuses);

        // THEN
        assertNotNull(foundTasks, "Tarefa deve ser encontrada");
        assertTrue(foundTasks.stream().allMatch(t ->
                                specificStatuses.contains(t.getStatus())
        ));
        verify(repository).findByStatus(specificStatuses);
    }

    @Test
    @DisplayName("deveLancarErroQuandoStatusForNullNoCreate")
    void deveLancarErroQuandoStatusForNullNoCreate() {
        // GIVEN
        Task invalida = new Task(1, "Task invalida", null);

        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(invalida),
                "Deve lançar exceção IllegalArgumentException para status null "
        );

        // THEN
        String message = exception.getMessage().toLowerCase();
        assertTrue(
                message.contains("null") || message.contains("status"),
                "Mensagem deve indicar problema com status: " + message
        );

        verify(repository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("deveChamarTouchAoAtualizarTarefa")
    void deveChamarTouchAoAtualizarTarefa() {
        // GIVEN
        Long idLong = 1L;
        Task original = new Task(1,"tarefa", TaskStatus.TODO);

        LocalDateTime before = original.getUpdatedAt().truncatedTo(ChronoUnit.MILLIS);

        given(repository.findById(anyLong())).willReturn(original);
        given(repository.update(anyLong(),any(Task.class))).willReturn(true);

        Task updated = new Task(1,"tarefa atualizada", TaskStatus.DONE);
        service.update(idLong, updated);

        // WHEN
        LocalDateTime after = original.getUpdatedAt().truncatedTo(ChronoUnit.MILLIS);

        // THEN
        InOrder inOrder = inOrder(repository);
        inOrder.verify(repository).findById(anyLong());
        inOrder.verify(repository).update(anyLong(),any(Task.class));

        assertAll(
                () -> assertEquals("tarefa atualizada", original.getDescription()),
                () -> assertTrue(
                        !after.isBefore(before),
                        String.format(
                                "Timestamp não deve retroceder (before: %s, after: %s, diferença: %dms)",
                                before, after, ChronoUnit.MILLIS.between(before, after)
                        )
                )
        );
        verify(repository).findById(anyLong());
    }

    @Test
    @DisplayName("naoDeveDeletarTarefaQuandoRepositoryDeleteRetornarFalse")
    void naoDeveDeletarTarefaQuandoRepositoryDeleteRetornarFalse() {
        // GIVEN
        Long idLong = 1L;
        Task toDeleteTask = new  Task(1,"tarefa", TaskStatus.TODO);
        given(repository.findById(anyLong())).willReturn(toDeleteTask);
        given(repository.delete(anyLong())).willReturn(false);

        // WHEN
        Boolean result =  service.delete(idLong);

        // THEN
        assertFalse(result, "Deve retornar false quando delete() falha");
        verify(repository).findById(anyLong());
        verify(repository).delete(anyLong());
    }

    @Test
    @DisplayName("deveVerificarStatusAntesDeDeletar")
    void deveVerificarStatusAntesDeDeletar() {
        // GIVEN
        Long idLong = 1L;
        Task doneTask = new Task(1,"tarefa", TaskStatus.DONE);

        given(repository.findById(anyLong())).willReturn(doneTask);

        // WHEN + THEN
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.delete(idLong),
                "Deve lançar RuntimeException, ao tentar deletar task com status DONE"
        );

        // THEN
        String message = exception.getMessage().toLowerCase();
        assertTrue(
                message.contains("done") ||
                        message.contains("already"),
                "Mensagem deve indicar status DONE: " + message
        );

        verify(repository, never()).delete(anyLong());
        verify(repository).findById(anyLong());

        InOrder inOrder = inOrder(repository);
        inOrder.verify(repository).findById(anyLong());
    }

    @Test
    @DisplayName("devePermitirAtualizacaoParcialApenasComStatus")
    void devePermitirAtualizacaoParcialApenasComStatus() {
        // GIVEN
        Long idLong = 1L;
        Task originalTask = new Task(1,"tarefa", TaskStatus.TODO);

        String description = originalTask.getDescription();
        LocalDateTime before = originalTask.getUpdatedAt().truncatedTo(ChronoUnit.MILLIS);

        given(repository.findById(anyLong())).willReturn(originalTask);
        given(repository.update(anyLong(),any(Task.class))).willReturn(true);

        // WHEN
        Task updated = service.update(idLong, new Task(1,null, TaskStatus.IN_PROGRESS));
        LocalDateTime after = originalTask.getUpdatedAt().truncatedTo(ChronoUnit.MILLIS);

        // THEN
        assertAll("Validar atualização parcial",
                () -> assertEquals("tarefa", originalTask.getDescription(),
                        "Descrição não deve mudar quando null"),
                () -> assertEquals(TaskStatus.IN_PROGRESS, originalTask.getStatus(),
                        "Status deve ser atualizado"),
                () -> assertTrue(
                        !after.isBefore(before),
                        String.format(
                                "Timestamp não deve retroceder (before: %s, after: %s, diferença: %dms)",
                                before, after, ChronoUnit.MILLIS.between(before, after)
                        )
                )
        );
        verify(repository).findById(anyLong());
        verify(repository).update(anyLong(), argThat(task ->
                description.equals(originalTask.getDescription()) &&
                        task.getStatus().equals(TaskStatus.IN_PROGRESS) &&
                        task.getUpdatedAt() != null
        ));
    }

    @Test
    @DisplayName("deveManterDadosOriginaisQuandoAtualizacaoFalhar")
    void deveManterDadosOriginaisQuandoAtualizacaoFalhar() {
        // GIVEN
        Long idLong = 1L;
        Task originalTask = new Task(1,"tarefa", TaskStatus.IN_PROGRESS);

        LocalDateTime before = originalTask.getUpdatedAt().truncatedTo(ChronoUnit.MILLIS);
        given(repository.findById(anyLong())).willReturn(originalTask);
        given(repository.update(anyLong(),any(Task.class))).willReturn(false);

        // WHEN
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.update(idLong,new Task(1,"tarefa", TaskStatus.DONE)),
                "Deve lançar RuntimeException, quando houver falha no banco"
        );

        LocalDateTime after = originalTask.getUpdatedAt().truncatedTo(ChronoUnit.MILLIS);

        // THEN
        assertAll(
                () ->  assertEquals("tarefa", originalTask.getDescription(),
                        "Descrição deve permanecer a mesma em falha no repository"),
                () ->  assertEquals(TaskStatus.IN_PROGRESS, originalTask.getStatus(),
                        "Status NÃO deve ser atualizado, em falha no repository"),
                () ->  assertTrue(before.isEqual(after),
                        "Timestamp NÃO deve ser atualizado, em caso de falha na atualização"),
                () -> assertTrue(
                        exception.getMessage().toLowerCase().contains("falha") ||
                                exception.getMessage().toLowerCase().contains("tarefa"),
                        "exceção deve conter 'falha', ou  'tarefa': " + exception.getMessage()
                )
        );
        verify(repository).findById(anyLong());
    }
}