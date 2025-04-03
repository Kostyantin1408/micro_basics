package com.example;


import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;


@RestController
public class FacadeController {

    @Value("${config.service.url}")
    private String configServiceUrl;

    @Value("${retries}")
    private int maxAttempts;

    @Value("${delay.milliseconds}")
    private long delayMillis;

    private final HazelcastInstance hazelcastInstance;
    private final RestTemplate restTemplate;

    @Autowired
    public FacadeController(HazelcastInstance hazelcastInstance, RestTemplate restTemplate) {
        this.hazelcastInstance = hazelcastInstance;
        this.restTemplate = restTemplate;
    }

    private String fetchExternalData(String url, String uuid, String serviceName) {
        int attempt = 0;

        while (attempt < maxAttempts) {
            try {
                String fullUrl = url + "?uuid=" + uuid;
                ResponseEntity<String> response = restTemplate.getForEntity(fullUrl, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    System.out.println(response.getBody());
                    return response.getBody();
                } else {
                    System.err.println("Failed to fetch data from " + serviceName + ": " + response.getStatusCode());
                }
            } catch (Exception e) {
                System.err.println("Error fetching data from " + serviceName + ": " + e.getMessage());
            }
            attempt++;
            if (attempt < maxAttempts) {
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return "NO RESPONSE FROM " + serviceName;
    }

    @GetMapping("/facade_service")
    public ResponseEntity<?> getHandler() {
        String uuid = UUID.randomUUID().toString();
        Map configResponse = restTemplate.getForEntity(configServiceUrl, Map.class).getBody();

        if (configResponse == null) {
            return ResponseEntity.status(500).body("Configuration response is null.");
        }

        Random random = new Random();
        List<String> loggingUrls = (List<String>) configResponse.get("loggingServiceUrls");

        String loggingServiceResponse = null;
        int index = random.nextInt(loggingUrls.size());
        for (int i = 0; i < loggingUrls.size(); ++i) {
            loggingServiceResponse = fetchExternalData(loggingUrls.get((index + i) % 
                    loggingUrls.size()), uuid, "logging");
            if (loggingServiceResponse != null) {
                break;
            }
        }
        if (loggingServiceResponse == null) {
            return ResponseEntity.status(500).body("Error fetching data from logging service.");
        }

        List<String> messageURLs = (List<String>) configResponse.get("messageServiceUrls");
        String messageServiceResponse = null;
        int indexMessage = random.nextInt(messageURLs.size());
        for (int i = 0; i < messageURLs.size(); ++i) {
            messageServiceResponse = fetchExternalData(messageURLs.get((indexMessage + i) %
                    messageURLs.size()), uuid, "message");
            if (messageServiceResponse != null) {
                break;
            }
        }
        if (messageServiceResponse == null) {
            return ResponseEntity.status(500).body("Error fetching data from message service.");
        }

        return ResponseEntity.ok(loggingServiceResponse +
                ": " + messageServiceResponse + "\n");
    }

    @PostMapping("/facade_service")
    public ResponseEntity<String> postHandler(@RequestBody Map<String, String> requestBody) {
        String uuid = UUID.randomUUID().toString();
        Map<String, String> requestJSON = new HashMap<>();
        String message = requestBody.get("message");
        requestJSON.put("uuid", uuid);
        requestJSON.put("message", message);
        IQueue<String> queue = hazelcastInstance.getQueue("queue");
        queue.add(message);
        try {
            Map configResponse = restTemplate.getForEntity(configServiceUrl, Map.class).getBody();

            if (configResponse == null) {
                return ResponseEntity.status(500).body("Configuration response is null.");
            }

            Random random = new Random();
            List<String> loggingUrls = (List<String>) configResponse.get("loggingServiceUrls");
            ResponseEntity<String> response = null;

            int index = random.nextInt(loggingUrls.size());
            for (int i = 0; i < loggingUrls.size(); ++i) {
                response = restTemplate.postForEntity(
                        loggingUrls.get((index + i) % loggingUrls.size()),
                        requestJSON, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    break;
                }
            }
            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok("Saved message successfully.\n");
            } else {
                return ResponseEntity.status(response.getStatusCode()).body("Failed to save message.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error saving message: " + e.getMessage());
        }
    }
}
