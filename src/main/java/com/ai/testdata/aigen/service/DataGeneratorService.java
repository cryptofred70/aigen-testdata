package com.ai.testdata.aigen.service;

import com.ai.testdata.aigen.model.GenerateRequest;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DataGeneratorService {

    private final OpenAiService openAiService;
    private final ObjectMapper mapper = new ObjectMapper();

    public DataGeneratorService(@Value("${openai.api.key}") String apiKey) {
        this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(30));
    }

    public Map<String, Object> generateData(GenerateRequest request) {
        try {
            // Build GPT prompt
            StringBuilder prompt = new StringBuilder();
            prompt.append(String.format(
                    "Generate %d rows of realistic JSON test data for fields: %s. ",
                    request.getRows(),
                    String.join(", ", request.getFields())
            ));
            prompt.append("Respond ONLY with a valid JSON array. ");

            // Advanced instructions (optional)
            if (request.getAdvancedInstructions() != null && !request.getAdvancedInstructions().isBlank()) {
                prompt.append("Additionally, follow these instructions: ")
                        .append(request.getAdvancedInstructions().trim())
                        .append(" ");
            } else {
                // Default hint: GPT can repeat values naturally
                prompt.append("It is OK for some fields to repeat naturally across rows. ");
            }

            prompt.append("Avoid forcing all values to be unique.");

            // Send request to GPT
            ChatCompletionRequest chatRequest = ChatCompletionRequest.builder()
                    .model("gpt-4o-mini")
                    .messages(List.of(new ChatMessage("user", prompt.toString())))
                    .temperature(0.4)
                    .maxTokens(1200)
                    .build();

            String response = openAiService.createChatCompletion(chatRequest)
                    .getChoices().get(0).getMessage().getContent();

            // Robust cleanup
            response = cleanGPTResponse(response);

            // Parse JSON
            List<Map<String, Object>> data = mapper.readValue(response, List.class);

            return Map.of("rows", data.size(), "data", data);

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "Failed to generate data: " + e.getMessage());
        }
    }

    // Helper method
    private String cleanGPTResponse(String response) {
        response = response.strip();
        if (response.startsWith("```")) {
            response = response.substring(response.indexOf('\n') + 1, response.lastIndexOf("```")).trim();
        }
        response = response.replaceAll("^`+|`+$", "");

        Pattern jsonPattern = Pattern.compile("(\\{.*\\}|\\[.*\\])", Pattern.DOTALL);
        Matcher matcher = jsonPattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new RuntimeException("No JSON object/array found in GPT response: " + response);
        }
    }
}
