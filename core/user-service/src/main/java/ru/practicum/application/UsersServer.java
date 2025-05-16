package ru.practicum.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class UsersServer {
    public static void main(String[] args) {
        SpringApplication.run(UsersServer.class, args);
    }
}
