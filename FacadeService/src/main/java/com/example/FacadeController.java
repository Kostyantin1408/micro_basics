package com.example;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@RestController
public class FacadeController {

    private final RestTemplate restTemplate;

    @Autowired
    public FacadeController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private String fetchExternalData(String url, String serviceName) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                System.err.println("Failed to fetch data from " + serviceName + ": " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Error fetching data from " + serviceName + ": " + e.getMessage());
        }
        return null;
    }

    @GetMapping("/facade_service")
    public ResponseEntity<?> getHandler() {
        Map<String, Object> response = new HashMap<>();

        String loggingURL = "http://localhost:1235/logging_service";
        String messageURL = "http://localhost:1236/message_service";

        String loggingServiceResponse = fetchExternalData(loggingURL, "logging");
        if (loggingServiceResponse == null) {
            return ResponseEntity.status(500).body("Error fetching data from logging service.");
        }
        response.put("loggingServiceResponse", loggingServiceResponse);

        String messageServiceResponse = fetchExternalData(messageURL, "message");
        if (messageServiceResponse == null) {
            return ResponseEntity.status(500).body("Error fetching data from message service.");
        }
        response.put("messageServiceResponse", messageServiceResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/facade_service")
    public ResponseEntity<String> postHandler(@RequestBody Map<String, String> requestBody) {
        String loggingURL = "http://localhost:1235/logging_service";
        String uuid = UUID.randomUUID().toString();
        Map<String, String> requestJSON = new HashMap<>();
        requestJSON.put("uuid", uuid);
        requestJSON.put("message", requestBody.get("message"));
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(loggingURL, requestJSON, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok("Saved message successfully.");
            } else {
                return ResponseEntity.status(response.getStatusCode()).body("Failed to save message.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error saving message: " + e.getMessage());
        }
    }
}
