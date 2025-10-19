package com.kaiburr.task1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class TaskRequest {

    
    @NotBlank(message = "Task name cannot be blank")
    @Size(min = 3, max = 100, message = "Task name must be between 3 and 100 characters")
    private String name;

    private String description;

    @NotBlank(message = "Command cannot be blank")
    private String command;
    
    private String framework;
    private String assignedTo;
}