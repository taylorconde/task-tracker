package br.com.taylor.repository;

import br.com.taylor.entity.Task;
import br.com.taylor.enums.TaskStatus;
import br.com.taylor.serializer.TaskSerializer;
import br.com.taylor.utils.JsonUtils;

import java.util.List;

public class JsonTaskRepository implements TaskRepository{

    private final String filePath;

    public JsonTaskRepository(String filePath) {
        this.filePath = filePath;
    }

    // Read all
    public List<Task> findAll(){
        String json = JsonUtils.readFile(filePath);
        return TaskSerializer.fromJson(json);
    }

    // Find by ID
    public Task findById(Long id) {
        return findAll().stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElse(null);
    }

    //todo
    @Override
    public List<Task> findByStatus(List<TaskStatus> status) {
        return List.of();
    }

    // Create new Task
    public Task save(Task task) {
        List<Task> tasks = findAll();

        int nextId = tasks.stream()
                .mapToInt(Task::getId)
                .max()
                .orElse(0) + 1;

        Task newTask = new Task(nextId, task.getDescription(), task.getStatus());
        tasks.add(newTask);

        JsonUtils.writeFile(filePath, TaskSerializer.toJson(tasks));
        return newTask;
    }

    // Update a Task
    public boolean update(Long id, Task updateTask) {
        List<Task> tasks = findAll();

        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId() == id) {
                tasks.set(i, updateTask);
                JsonUtils.writeFile(filePath, TaskSerializer.toJson(tasks));
                return true;
            }
        }
        return false;
    }

    // Exclusion
    public boolean delete(Long id) {
        List<Task> tasks = findAll();
        int before = tasks.size();

        tasks.removeIf(t -> t.getId() == id);
        int after = tasks.size();

        if (after < before) {
            JsonUtils.writeFile(filePath, TaskSerializer.toJson(tasks));
            return true;
        }

        return false;
    }
}