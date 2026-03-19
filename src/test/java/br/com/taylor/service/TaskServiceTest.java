package br.com.taylor.service;

import br.com.taylor.entity.Task;
import br.com.taylor.enums.TaskStatus;
import br.com.taylor.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

        assertEquals("Status cannot be empty", actual.getMessage());
    }

    //UPDATE
    @Test
    @DisplayName("ATUALIZAR TAREFA OK!")
    void deveAtualizarTarefaComSucesso(){
        //given
        Long idExistente = 1L;
        Task tarefaSalva = new Task(1,"descricao",TaskStatus.TODO);
//        Task dadosAtualizados = new Task(1,"descricao",TaskStatus.DONE);

//        when(repository.findById(idExistente)).thenReturn(tarefaSalva);
        given(repository.findById(idExistente)).willReturn(tarefaSalva);
        given(repository.update(idExistente, tarefaSalva)).willReturn(true);

        //when
//        var actual = service.update(idExistente,dadosAtualizados);
        var actual = service.update(idExistente, new Task(1, "descricao", TaskStatus.DONE));

        //then
        assertEquals(TaskStatus.DONE,actual.getStatus());
        assertEquals(TaskStatus.DONE,tarefaSalva.getStatus());
        then(repository).should().update(eq(idExistente),eq(tarefaSalva));
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

        assertEquals("Description cannot be empty", actual.getMessage());
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








}
