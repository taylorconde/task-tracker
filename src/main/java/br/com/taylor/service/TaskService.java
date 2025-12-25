package br.com.taylor.service;

import br.com.taylor.entity.Task;
import br.com.taylor.enums.TaskStatus;
import br.com.taylor.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;

public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public Task create(Task task) {
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

        if (newData.getDescription() != null) existing.setDescription(newData.getDescription());
        if (newData.getStatus() != null) existing.setStatus(newData.getStatus());

        existing.touch();

        if (!repository.update(id, existing)) throw new RuntimeException();

        return existing;
    }

    public boolean delete(Long id) {
        Task found = repository.findById(id);
        if (found == null) throw new RuntimeException("Task not found: " + id);

        return repository.delete(id);
    }

    public List<Task> findByStatus(TaskStatus status) {
        List<Task> allTasks = findAll();
        List<Task> filteredTasks = new ArrayList<>();

        for(Task task : allTasks){
            if(task.getStatus() == status) filteredTasks.add(task);
        }
        return filteredTasks;
    }
}