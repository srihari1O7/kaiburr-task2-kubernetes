package com.kaiburr.task1.service;

import com.kaiburr.task1.dto.TaskRequest;
import com.kaiburr.task1.model.Task;
import com.kaiburr.task1.model.TaskExecution;

import java.util.List;


public interface TaskService {

  
    Task createTask(TaskRequest taskRequest);

  
    List<Task> getAllTasks();

   
    Task getTaskById(String id);

    
    void deleteTask(String id);

    
    List<Task> findTasksByName(String name);

    
    TaskExecution executeTask(String id);
}