package br.com.taylor.entity;

import br.com.taylor.enums.TaskStatus;

public class Task {

    private int id;
    private String description;
    private TaskStatus status;

    public Task(int id ,String description, TaskStatus status){
        this.id = id;
        this.description = description;
        this.status = status;
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

    @Override
    public String toString(){
        return String.format(
                "ID:[%d] %nStatus:(%s) %nDescription: %s",
                id,
                status,
                description
        );
    }
}