package com.example;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class LoggingController {

    ConcurrentHashMap<String,String> loggingMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, Boolean> cacheMap = new ConcurrentHashMap<>();

    @GetMapping("/logging_service")
    public ResponseEntity<String> getLoggingService(@RequestParam String uuid) {
        System.out.println(uuid);
        if (!cacheMap.contains(uuid)) {
            cacheMap.put(uuid, true);
            return ResponseEntity.ok(loggingMap.values().toString());
        }
        return ResponseEntity.status(500).body("Such uuid is already present in db!" +
                " (Which means that you have already made that request)");
    }

    @PostMapping("/logging_service")
    public ResponseEntity<String> postLoggingService(@RequestBody Map<String, String> requestBody) {
        String message =  requestBody.get("message");
        String uuid = requestBody.get("uuid");
        if (!loggingMap.contains(uuid)) {
            loggingMap.put(uuid,message);
            return ResponseEntity.ok("Successfuly saved");
        }
        return ResponseEntity.status(500).body("Such uuid is already present in db!" +
                " (Which means that you have already made that request)");
    }

}
