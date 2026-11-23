package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {

        // Elindítja a különálló backend szervert
        SpringApplication.run(BackendApplication.class, args);

    }
}
