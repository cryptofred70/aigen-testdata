package com.ai.testdata.aigen.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Value("${app.free.max-rows}")
    private int freeMaxRows;

    @Value("${app.pro.max-rows}")
    private int proMaxRows;

    @Value("${app.pro.price-eur}")
    private double proPriceEur;

    @Value("${app.pro.checkout-url}")
    private String proCheckoutUrl;

    @GetMapping
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("FREE_MAX_ROWS", freeMaxRows);
        config.put("PRO_MAX_ROWS", proMaxRows);
        config.put("PRO_PRICE_EUR", proPriceEur);
        config.put("PRO_CHECKOUT_URL", proCheckoutUrl);
        return config;
    }
}
