package br.com.taylor.service;

import br.com.taylor.entity.Task;
import br.com.taylor.enums.TaskStatus;
import br.com.taylor.repository.JdbcTaskRepository;
import br.com.taylor.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;

public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public Task create(Task task) {

        if(task.getDescription().isBlank()){
            throw new IllegalArgumentException("Description cannot be empty");
        }
        if(task.getStatus() == null){
            throw new IllegalArgumentException("Status cannot be empty");
        }
        return repository.save(task);
    }

    public Task findById(Long id) {
        Task found = repository.findById(id);
        if (found == null) throw new RuntimeException("Task not found: " + id);
        return found;
    }

    public List<Task> findAll() {
        return repository.findAll();
    }

    public Task update(Long id, Task newData){

        Task existing = this.findById(id);

        if (newData.getDescription() != null)
            if (!newData.getDescription().isBlank())
                existing.setDescription(newData.getDescription());
            else throw new IllegalArgumentException("Description cannot be empty");

        if (newData.getStatus() != null) existing.setStatus(newData.getStatus());

        existing.touch();

        if (!repository.update(id, existing)) throw new RuntimeException();

        return existing;
    }

    public boolean delete(Long id) {
        Task found = this.findById(id);
        if (found.getStatus() == TaskStatus.DONE) throw new RuntimeException("Task is already done: " + id);
        return repository.delete(id);
    }

    public List<Task> findByStatus(List<TaskStatus> status) {
        return repository.findByStatus(status);
    }
}