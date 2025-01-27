package com.example;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoggingController {
    @GetMapping("/logging_service")
    public String getHandler() {
        return "Hello pidar";
    }
}
