package com.ai.testdata.aigen.service;

import com.ai.testdata.aigen.model.GenerateRequest;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DataGeneratorService {

    private final OpenAiService openAiService;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final int BATCH_SIZE = 20;

    public DataGeneratorService(@Value("${openai.api.key}") String apiKey) {
        this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(45));
    }

    public Map<String, Object> generateData(GenerateRequest request) {
        try {
            List<Map<String, Object>> allData = new ArrayList<>();
            Set<String> seenRows = new HashSet<>();
            int remaining = request.getRows();

            Random rnd = new Random();

            while (allData.size() < request.getRows()) {
                int currentBatchSize = Math.min(BATCH_SIZE, remaining);

                // Add slight randomness to the prompt to encourage variety
                String variation = "VariationSeed_" + rnd.nextInt(10000);

                List<Map<String, Object>> batchData = generateBatch(
                        currentBatchSize,
                        request.getFields(),
                        request.getAdvancedInstructions(),
                        variation
                );

                // Deduplicate against previously generated rows
                List<Map<String, Object>> uniqueBatch = new ArrayList<>();
                for (Map<String, Object> row : batchData) {
                    String hash = row.toString(); // simple row string for uniqueness
                    if (!seenRows.contains(hash)) {
                        uniqueBatch.add(row);
                        seenRows.add(hash);
                    }
                }

                allData.addAll(uniqueBatch);
                remaining = request.getRows() - allData.size();

                // Safety: if batch adds nothing new, slightly perturb and continue
                if (uniqueBatch.isEmpty() && remaining > 0) {
                    for (Map<String, Object> row : batchData) {
                        Map<String, Object> clone = new HashMap<>(row);
                        // Add small perturbation to string fields
                        for (String key : clone.keySet()) {
                            Object val = clone.get(key);
                            if (val instanceof String str) {
                                clone.put(key, str + "_" + rnd.nextInt(1000));
                            }
                        }
                        allData.add(clone);
                        remaining--;
                        if (remaining <= 0) break;
                    }
                }
            }

            return Map.of("rows", allData.size(), "data", allData);

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "Failed to generate data: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> generateBatch(int numRows, List<String> fields, String advancedInstructions, String variationSeed) throws Exception {
        StringBuilder prompt = new StringBuilder();
        prompt.append(String.format(
                "Generate %d rows of realistic JSON test data for fields: %s. ",
                numRows,
                String.join(", ", fields)
        ));
        prompt.append("Respond ONLY with a valid JSON array (starting with [ and ending with ]). ");
        prompt.append("The array must contain EXACTLY " + numRows + " objects. ");
        prompt.append("Do NOT summarize, skip items, or add explanations. ");
        prompt.append("Each object must include all requested fields. ");
        prompt.append("Ensure variation from previous data batches. Seed: ").append(variationSeed).append(". ");

        if (advancedInstructions != null && !advancedInstructions.isBlank()) {
            prompt.append("Additionally, follow these instructions: ").append(advancedInstructions.trim());
        }

        ChatCompletionRequest chatRequest = ChatCompletionRequest.builder()
                .model("gpt-4o-mini")
                .messages(List.of(new ChatMessage("user", prompt.toString())))
                .temperature(0.4)  // slightly higher for more diversity
                .maxTokens(4000)
                .build();

        String response = openAiService.createChatCompletion(chatRequest)
                .getChoices().get(0).getMessage().getContent();

        response = cleanGPTResponse(response);

        Object parsed = mapper.readValue(response, Object.class);
        List<Map<String, Object>> data;

        if (parsed instanceof List) {
            data = (List<Map<String, Object>>) parsed;
        } else if (parsed instanceof Map) {
            data = List.of((Map<String, Object>) parsed);
        } else {
            throw new RuntimeException("Unexpected JSON format from GPT: " + parsed.getClass().getName());
        }

        return data;
    }

    private String cleanGPTResponse(String response) {
        if (response == null) return "";

        response = response.strip();

        if (response.startsWith("```")) {
            int firstNewline = response.indexOf('\n');
            int lastFence = response.lastIndexOf("```");
            if (firstNewline > 0 && lastFence > firstNewline) {
                response = response.substring(firstNewline + 1, lastFence).trim();
            }
        }

        response = response.replaceAll("^`+|`+$", "");

        Pattern jsonPattern = Pattern.compile("(\\{.*\\}|\\[.*\\])", Pattern.DOTALL);
        Matcher matcher = jsonPattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return response.trim();
    }
}
