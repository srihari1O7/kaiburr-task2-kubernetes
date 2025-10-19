package com.kaiburr.task1.controller;

import com.kaiburr.task1.dto.TaskRequest;
import com.kaiburr.task1.dto.TaskResponse;
import com.kaiburr.task1.model.Task;
import com.kaiburr.task1.model.TaskExecution;
import com.kaiburr.task1.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController 
@RequestMapping("/tasks") 
public class TaskController {

    private final TaskService taskService;

   
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest taskRequest) {
        Task createdTask = taskService.createTask(taskRequest);
        // We convert the Task model to a TaskResponse DTO before sending
        return new ResponseEntity<>(TaskResponse.from(createdTask), HttpStatus.CREATED);
    }

   
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        // Convert the whole list of Tasks to a list of TaskResponses
        List<TaskResponse> taskResponses = tasks.stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(taskResponses);
    }

   
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable String id) {
        Task task = taskService.getTaskById(id); // Service throws 404 if not found
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @GetMapping("/search")
    public ResponseEntity<List<TaskResponse>> findTasksByName(@RequestParam String name) {
        List<Task> tasks = taskService.findTasksByName(name);
        List<TaskResponse> taskResponses = tasks.stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(taskResponses);
    }

    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id); // Service throws 404 if not found
        return ResponseEntity.noContent().build(); // Standard 204 No Content response
    }

    
    @PutMapping("/{id}/execute")
    public ResponseEntity<TaskExecution> executeTask(@PathVariable String id) {
        TaskExecution executionResult = taskService.executeTask(id);
        return ResponseEntity.ok(executionResult);
    }
}