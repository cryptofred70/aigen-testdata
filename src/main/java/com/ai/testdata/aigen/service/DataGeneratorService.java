package com.ai.testdata.aigen.service;

import com.ai.testdata.aigen.model.GenerateRequest;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DataGeneratorService {

    public Map<String, Object> generateData(GenerateRequest request) {
        List<Map<String, Object>> generated = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < request.getRows(); i++) {
            Map<String, Object> row = new HashMap<>();
            int seed = random.nextInt(1000); // same seed for all fields in this row

            for (String field : request.getFields()) {
                row.put(field, generateRandomValue(field, random, seed));
            }
            generated.add(row);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("data", generated);
        response.put("rows", generated.size());
        return response;
    }

    private Object generateRandomValue(String field, Random random, int seed) {
        field = field.toLowerCase();

        if (field.contains("name")) return "User" + seed;
        if (field.contains("email")) return "user" + seed + "@example.com";
        if (field.contains("age")) return 18 + random.nextInt(50);
        if (field.contains("city")) return List.of("Paris", "London", "Berlin", "Madrid").get(random.nextInt(4));

        return "Value" + seed;
    }
}
