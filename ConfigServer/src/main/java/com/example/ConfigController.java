package com.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ConfigController {

    @Value("${message.service.urls}")
    private String[] messageServiceUrl;

    @Value("${logging.service.urls}")
    private String[] loggingServiceUrls;


    @GetMapping("/config_service")
    public ResponseEntity<Map<String, Object>> getLoggingService() {
        Map<String, Object> response = new HashMap<>();
        response.put("messageServiceUrls", messageServiceUrl);
        response.put("loggingServiceUrls", Arrays.asList(loggingServiceUrls));
        return ResponseEntity.ok(response);
    }
}
