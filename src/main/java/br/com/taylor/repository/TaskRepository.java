package br.com.taylor.repository;

import br.com.taylor.entity.Task;
import br.com.taylor.enums.TaskStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import java.util.List;

public class TaskRepository {

    private static final String FILE_PATH = "tasks.json";

    public List<Task> loadTasks(){
        return Collections.emptyList();
    }

    public void saveTasks(List<Task> tasks){}

    public List<Task> listTask(){
        return null;
    }

    public List<Task> listTaskByStatus(TaskStatus status) {
        return null;
    }
}