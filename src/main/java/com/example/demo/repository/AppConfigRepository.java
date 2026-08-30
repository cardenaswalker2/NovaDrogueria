package com.example.demo.repository;

import com.example.demo.model.AppConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AppConfigRepository extends MongoRepository<AppConfig, String> {
}
