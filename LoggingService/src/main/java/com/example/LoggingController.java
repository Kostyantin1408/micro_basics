package com.example;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class LoggingController {

    ConcurrentHashMap<String,String> loggingMap = new ConcurrentHashMap<>();

    @GetMapping("/logging_service")
    public ResponseEntity<String> getLoggingService() {
        return ResponseEntity.ok(loggingMap.values().toString());
    }

    @PostMapping("/logging_service")
    public ResponseEntity<String> postLoggingService(@RequestBody Map<String, String> requestBody) {
        String message =  requestBody.get("message");
        String uuid = requestBody.get("uuid");
        loggingMap.put(uuid,message);
        return ResponseEntity.ok("Successfuly saved");
    }
}
