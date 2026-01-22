package br.com.taylor.repository;

import br.com.taylor.entity.Task;
import br.com.taylor.enums.TaskStatus;

import java.util.List;

public interface TaskRepository {
    List<Task> findAll();
    Task findById(Long id);
    List<Task> findByStatus(List<TaskStatus> status);
    Task save(Task task);
    boolean update(Long id, Task updateTask);
    boolean delete(Long id);
}