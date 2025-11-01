package com.ai.testdata.aigen.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@RestController
@RequestMapping("/api/license")
public class LicenseController {

    @Value("${lemon.api.key}")
    private String apiKey;

    private static final String API_URL = "https://api.lemonsqueezy.com/v1/licenses/activate";

    @Autowired
    private ObjectMapper mapper;

    @PostMapping("/verify")
    public ResponseEntity<String> verifyLicense(@RequestBody Map<String, String> payload) {
        String licenseKey = payload.get("licenseKey");

        Map<String, String> requestPayload = Map.of(
                "license_key", licenseKey,
                "instance_name", "local-pc"
        );
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestPayload)))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return ResponseEntity.ok(response.body());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error verifying license: " + e.getMessage());
        }
    }
}
