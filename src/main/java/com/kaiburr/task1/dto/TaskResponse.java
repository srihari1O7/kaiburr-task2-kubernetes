package com.kaiburr.task1.dto;

import com.kaiburr.task1.model.Task;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class TaskResponse {
    
    private String id;
    private String name;
    private String description;
    private String command;
    private String framework;
    private String assignedTo;
    private LocalDateTime createdAt;

    
    public static TaskResponse from(Task task) {
        TaskResponse dto = new TaskResponse();
        dto.setId(task.getId());
        dto.setName(task.getName());
        dto.setDescription(task.getDescription());
        dto.setCommand(task.getCommand());
        dto.setFramework(task.getFramework());
        dto.setAssignedTo(task.getAssignedTo());
        dto.setCreatedAt(task.getCreatedAt());
        return dto;
    }
}