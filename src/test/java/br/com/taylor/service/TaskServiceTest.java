package br.com.taylor.service;

import br.com.taylor.entity.Task;
import br.com.taylor.enums.TaskStatus;
import br.com.taylor.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

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





}