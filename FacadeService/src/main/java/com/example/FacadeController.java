package com.example;


import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.orbitz.consul.KeyValueClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import com.orbitz.consul.model.catalog.CatalogService;
import com.orbitz.consul.Consul;
import org.springframework.web.client.RestTemplate;

import java.util.*;


@RestController
public class FacadeController {

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
        Consul consul = Consul.builder().build();
        List<CatalogService> loggingServices = consul.catalogClient().getService("logging-server").getResponse();
        if (loggingServices == null || loggingServices.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("No logging service available");
        }
        Random random = new Random();

        String loggingServiceResponse = null;
        int index = random.nextInt(loggingServices.size());
        for (int i = 0; i < loggingServices.size(); ++i) {
            CatalogService service = loggingServices.get((index + i) % loggingServices.size());
            String serviceUrl = "http://" + service.getAddress() + ":" + service.getServicePort() + "/logging_service";
            loggingServiceResponse = fetchExternalData(serviceUrl, uuid, "logging");
            if (loggingServiceResponse != null) {
                break;
            }
        }
        if (loggingServiceResponse == null) {
            return ResponseEntity.status(500).body("Error fetching data from logging service.");
        }

        List<CatalogService> messageServices = consul.catalogClient().getService("message-server").getResponse();
        if (messageServices == null || messageServices.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("No message service available");
        }

        String messageServiceResponse = null;
        int indexMessage = random.nextInt(messageServices.size());

        for (int i = 0; i < messageServices.size(); ++i) {
            CatalogService service = messageServices.get((indexMessage + i) % messageServices.size());
            String serviceUrl = "http://" + service.getAddress() + ":" + service.getServicePort() + "/message_service";
            messageServiceResponse = fetchExternalData(serviceUrl, uuid, "message");

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

        Consul consul = Consul.builder().build();
        KeyValueClient kvClient = consul.keyValueClient();
        Optional<String> queue_name = kvClient.getValueAsString("queue-name");



        String uuid = UUID.randomUUID().toString();
        Map<String, String> requestJSON = new HashMap<>();
        String message = requestBody.get("message");
        requestJSON.put("uuid", uuid);
        requestJSON.put("message", message);
        IQueue<String> queue;
        queue = queue_name.<IQueue<String>>map(hazelcastInstance::getQueue).orElseGet(() -> hazelcastInstance.getQueue("queue"));
        queue.add(message);
        try {

            Random random = new Random();
            List<CatalogService> loggingServices = consul.catalogClient().getService("logging-server").getResponse();
            if (loggingServices == null || loggingServices.isEmpty()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("No logging service available");
            }
            ResponseEntity<String> response = null;

            int index = random.nextInt(loggingServices.size());
            for (int i = 0; i < loggingServices.size(); ++i) {
                CatalogService service = loggingServices.get((index + i) % loggingServices.size());
                String serviceUrl = "http://" + service.getAddress() + ":" + service.getServicePort() + "/logging_service";
                System.out.println(serviceUrl);
                response = restTemplate.postForEntity(
                        serviceUrl,
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
