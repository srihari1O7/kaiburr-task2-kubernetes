package com.kaiburr.task1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

// This one annotation combines three: @Configuration, @EnableAutoConfiguration, @ComponentScan
@SpringBootApplication 
// This annotation enables MongoDB's automatic auditing features (like @CreatedDate)
@EnableMongoAuditing 
public class Task1Application {

    // This is the main method that starts the whole application
    public static void main(String[] args) {
        SpringApplication.run(Task1Application.class, args);
    }
}