package com.example;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


@RestController
public class FacadeController {

    private final RestTemplate restTemplate;

    @Autowired
    public FacadeController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/facade_service")
    public ResponseEntity<String> getHandler() {
        String loggingURL = "http://localhost:1235/logging_service";
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(loggingURL, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                System.out.println(responseBody);
                return ResponseEntity.ok("Received from external service: " + responseBody);
            } else {
                return ResponseEntity.status(response.getStatusCode())
                        .body("Failed to fetch data from external service.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching data: " + e.getMessage());
        }
    }
}
