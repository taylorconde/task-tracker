package br.com.taylor.entity;

import br.com.taylor.enums.TaskStatus;

import java.time.LocalDateTime;

import static br.com.taylor.utils.JsonUtils.escape;


public class Task {

    private int id;
    private String description;
    private TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public Task(int id ,String description, TaskStatus status){
        this.id = id;
        this.description = description;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public int getId(){return id;}
    public String getDescription(){return description;}
    public TaskStatus getStatus(){return status;}

    public void setDescription(String description){
        this.description = description;
    }
    public void setStatus(TaskStatus status){
        this.status = status;
    }

    public void touch(){
        this.updatedAt = LocalDateTime.now();
    }

    public String toJson() {
        return String.format(
                "{" +
                        "\"id\":%d," +
                        "\"description\":\"%s\"," +
                        "\"status\":\"%s\"," +
                        "\"createdAt\":\"%s\"," +
                        "\"updatedAt\":\"%s\"" +
                        "}",
                id,
                escape(description),
                status,
                createdAt,
                updatedAt
        );
    }


    @Override
    public String toString(){
        return String.format(
                "ID:[%d] | Status:(%s) | Description: %s | CreatedAt: %s | UpdatedAt: %s",
                id,
                status,
                description,
                createdAt,
                updatedAt
        );
    }
}