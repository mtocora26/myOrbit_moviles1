package com.app.MyOrbit.tasks;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByUserIdOrderByIdAsc(String userId);
}