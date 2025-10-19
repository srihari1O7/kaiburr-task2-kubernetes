package com.kaiburr.task1.repository;

import com.kaiburr.task1.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository 
public interface TaskRepository extends MongoRepository<Task, String> {
    
    @Query("{'name': {$regex : ?0, $options: 'i'}}")
    List<Task> findByNameRegex(String name);
}