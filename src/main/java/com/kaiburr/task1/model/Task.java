package com.kaiburr.task1.model;

import lombok.Data; // From Lombok, gives us @Getter, @Setter, @ToString
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Data 
@Document(collection = "tasks") 
public class Task {

    @Id
    private String id; 

    private String name;
    private String description;
    private String command; 
    private String framework;
    private String assignedTo;

    @CreatedDate 
    private LocalDateTime createdAt;
}