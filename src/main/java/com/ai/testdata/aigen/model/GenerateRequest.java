package com.ai.testdata.aigen.model;

import java.util.List;

public class GenerateRequest {
    private List<String> fields;
    private int rows = 5;

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }
}