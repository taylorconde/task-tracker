package br.com.taylor.service;

import br.com.taylor.entity.Task;
import br.com.taylor.enums.TaskStatus;
import br.com.taylor.repository.TaskRepository;

import java.time.LocalDateTime;
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
            throw new IllegalArgumentException("Status cannot be null");
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

        Task toUpdate = new Task(
                existing.getId(),
                existing.getDescription(),
                existing.getStatus(),
                existing.getCreatedAt(),
                existing.getUpdatedAt()
        );

        if (newData.getDescription() != null)
            if (!newData.getDescription().isBlank())
                toUpdate.setDescription(newData.getDescription());
            else throw new IllegalArgumentException("Description cannot be empty or blank");

        if (newData.getStatus() != null) toUpdate.setStatus(newData.getStatus());

        toUpdate = new Task(
                toUpdate.getId(),
                toUpdate.getDescription(),
                toUpdate.getStatus(),
                toUpdate.getCreatedAt(),
                LocalDateTime.now()
        );


        if (!repository.update(id, toUpdate)) throw new RuntimeException("Falha ao atualizar tarefa com ID: " + id);

        existing.setDescription(toUpdate.getDescription());
        existing.setStatus(toUpdate.getStatus());
        existing.setUpdatedAt(toUpdate.getUpdatedAt());

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