package br.com.taylor.service;

import br.com.taylor.entity.Task;
import br.com.taylor.enums.TaskStatus;
import br.com.taylor.repository.TaskRepository;

import java.util.List;

public class TaskService {

    private final TaskRepository repository;

    public TaskService() {
        this.repository = new TaskRepository();
    }

    public Task crateTask(String description, TaskStatus status) {
        return null;
    }

    public Task updateTask(int id, String description, TaskStatus status) {
        return null;
    }

    public Task updateStatus(int id, TaskStatus status) {
        return null;
    }

    public boolean deleteTask(int id) {
        return false;
    }

    public List<Task> listAll() {
        return null;
    }

    public List<Task> listByStatus(TaskStatus status) {
        return null;
    }
}
