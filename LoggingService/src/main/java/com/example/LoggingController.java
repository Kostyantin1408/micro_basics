package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

@RestController
public class LoggingController {
    private final HazelcastInstance hazelcastInstance;
    private final IMap<String, String> loggingMap;
    private final IMap<String, Boolean> cacheMap;

    @Autowired
    public LoggingController(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
        this.loggingMap = hazelcastInstance.getMap("loggingMap");
        this.cacheMap = hazelcastInstance.getMap("cacheMap");
    }

    @GetMapping("/logging_service")
    public ResponseEntity<String> getLoggingService(@RequestParam String uuid) {
        if (!cacheMap.containsKey(uuid)) {
            cacheMap.put(uuid, true);
            return ResponseEntity.ok(loggingMap.values().toString());
        }
        return ResponseEntity.status(500).body("Such uuid is already present in db!" +
                " (Which means that you have already made that request)");
    }

    @PostMapping("/logging_service")
    public ResponseEntity<String> postLoggingService(@RequestBody Map<String, String> requestBody) {
        String address = hazelcastInstance.getCluster().getLocalMember().getAddress().toString();
        String message = requestBody.get("message");
        String uuid = requestBody.get("uuid");
        System.out.println("Got message: " + message + " on address: " + address);
        if (!loggingMap.containsKey(uuid)) {
            loggingMap.put(uuid,message);
            return ResponseEntity.ok("Successfuly saved");
        }
        return ResponseEntity.status(500).body("Such uuid is already present in db!" +
                " (Which means that you have already made that request)");
    }
}
