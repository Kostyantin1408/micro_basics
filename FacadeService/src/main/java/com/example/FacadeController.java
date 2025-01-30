package com.example;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${logging.service.url}")
    private String loggingURL;

    @Value("${message.service.url}")
    private String messageURL;

    @Value("${retries}")
    private int maxAttempts;

    @Value("${delay.milliseconds}")
    private long delayMillis;


    @Autowired
    public FacadeController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private String fetchExternalData(String url, String uuid, String serviceName) {
        int attempt = 0;

        while (attempt < maxAttempts) {
            try {
                String fullUrl = url + "?uuid=" + uuid;
                ResponseEntity<String> response = restTemplate.getForEntity(fullUrl, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
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
        System.out.println(uuid);
        String loggingServiceResponse = fetchExternalData(loggingURL, uuid, "logging");
        if (loggingServiceResponse == null) {
            return ResponseEntity.status(500).body("Error fetching data from logging service.");
        }

        String messageServiceResponse = fetchExternalData(messageURL, uuid, "message");
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
        requestJSON.put("uuid", uuid);
        requestJSON.put("message", requestBody.get("message"));
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(loggingURL, requestJSON, String.class);
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
