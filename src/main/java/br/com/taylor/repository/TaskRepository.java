package br.com.taylor.repository;

import br.com.taylor.entity.Task;
import java.util.List;

public interface TaskRepository {
    List<Task> findAll();
    Task findById(Long id);
    Task save(Task task);
    boolean update(Long id, Task updateTask);
    boolean delete(Long id);
}