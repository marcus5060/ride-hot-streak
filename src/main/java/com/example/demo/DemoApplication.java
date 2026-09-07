package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @RestController
    static class HomeController {

        @GetMapping("/")
        public String home() {
            return "Spring Boot is running \u2014 built with Maven in a GitHub Codespaces devcontainer, with Claude Code available in the terminal.";
        }
    }
}
