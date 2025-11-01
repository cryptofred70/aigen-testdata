package com.ai.testdata.aigen.controller;

import com.ai.testdata.aigen.model.GenerateRequest;
import com.ai.testdata.aigen.service.DataGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DataGenerationController {

    @Value("${app.free.max-rows}")
    private int MAX_FREE_ROWS;

    @Value("${app.pro.max-rows}")
    private int MAX_PRO_ROWS;

    private final DataGeneratorService dataGeneratorService;

    @Autowired
    public DataGenerationController(DataGeneratorService dataGeneratorService) {
        this.dataGeneratorService = dataGeneratorService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateData(@RequestBody GenerateRequest request, @RequestHeader(value = "X-User-Tier", defaultValue = "free") String userTier) {
        int maxRows = userTier.equalsIgnoreCase("pro") ? MAX_PRO_ROWS : MAX_FREE_ROWS;
        if (request.getRows() > maxRows) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Free users can only generate up to " + maxRows + " rows. Upgrade to Pro for higher limits!"
            ));
        }
        return ResponseEntity.ok(dataGeneratorService.generateData(request));
    }
}