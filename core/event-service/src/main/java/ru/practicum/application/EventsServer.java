package ru.practicum.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = {"ru.practicum"})
public class EventsServer {
    public static void main(String[] args) {
        SpringApplication.run(EventsServer.class, args);
    }
}
