package com.kaiburr.task1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;


@Data 
@AllArgsConstructor 
public class ErrorResponse {
    
    private int statusCode;
    private String message;
    private String details;
    private LocalDateTime timestamp;

}