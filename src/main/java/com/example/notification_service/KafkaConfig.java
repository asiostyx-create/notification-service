package com.example.notification_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class KafkaConfig {

    @Autowired
    Environment environment;


}
