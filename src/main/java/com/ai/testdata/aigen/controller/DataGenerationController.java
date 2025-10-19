package com.ai.testdata.aigen.controller;

import com.ai.testdata.aigen.model.GenerateRequest;
import com.ai.testdata.aigen.service.DataGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DataGenerationController {

    private final DataGeneratorService dataGeneratorService;

    @Autowired
    public DataGenerationController(DataGeneratorService dataGeneratorService) {
        this.dataGeneratorService = dataGeneratorService;
    }

    @PostMapping("/generate")
    public Map<String, Object> generateData(@RequestBody GenerateRequest request) {
        return dataGeneratorService.generateData(request);
    }
}