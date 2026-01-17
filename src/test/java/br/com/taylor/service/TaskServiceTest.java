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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository repository;

    @InjectMocks
    private TaskService service;

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
        Task expected = new Task(id,descricao,status);

        //WHEN & THEN
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> {
            service.create(expected);
        });

        // Verificacao adicional
        assertEquals("Description cannot be empty", actual.getMessage());
        }

    @Test
    void deveLancarErroQuandoStatusVazia(){
        //GIVEN
        int id = 1;
        String description = "descricao";
        Task expected = new Task(id, description, null);

        //WHEN & THEN
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> {
            service.create(expected);
        });

        assertEquals("Status cannot be empty", actual.getMessage());
    }








}