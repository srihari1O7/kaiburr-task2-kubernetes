package com.kaiburr.task1.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Data
@Builder 
@Document(collection = "task_executions")
public class TaskExecution {

    @Id
    private String id;
    
    private String taskId; 
    private String commandRun;
    private String output;
    private boolean success;

    @CreatedDate
    private LocalDateTime executedAt;
}