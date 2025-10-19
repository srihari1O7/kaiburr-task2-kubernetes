package com.kaiburr.task1.repository;

import com.kaiburr.task1.model.TaskExecution;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskExecutionRepository extends MongoRepository<TaskExecution, String> {
    
}