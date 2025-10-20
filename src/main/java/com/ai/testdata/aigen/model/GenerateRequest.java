package com.ai.testdata.aigen.model;

import java.util.List;

public class GenerateRequest {
    private List<String> fields;
    private Integer rows;
    private String advancedInstructions; // optional free-text instructions

    // Getters & Setters
    public List<String> getFields() { return fields; }
    public void setFields(List<String> fields) { this.fields = fields; }

    public Integer getRows() { return rows; }
    public void setRows(Integer rows) { this.rows = rows; }

    public String getAdvancedInstructions() { return advancedInstructions; }
    public void setAdvancedInstructions(String advancedInstructions) { this.advancedInstructions = advancedInstructions; }
}