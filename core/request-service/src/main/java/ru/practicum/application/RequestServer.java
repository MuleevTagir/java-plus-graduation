package ru.practicum.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = {"ru.practicum"})
public class RequestServer {
    public static void main(String[] args) {
        SpringApplication.run(RequestServer.class, args);
    }
}
