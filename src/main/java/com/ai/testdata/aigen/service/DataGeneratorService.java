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
            // 🧠 Build prompt
            String userPrompt = String.format(
                    "Generate %d rows of realistic JSON test data for fields: %s. " +
                            "Respond ONLY with a valid JSON array. Do not include markdown or explanations.",
                    request.getRows(),
                    String.join(", ", request.getFields())
            );

            // 🧩 Build chat request
            ChatCompletionRequest chatRequest = ChatCompletionRequest.builder()
                    .model("gpt-4o-mini")
                    .messages(List.of(new ChatMessage("user", userPrompt)))
                    .temperature(0.4)
                    .maxTokens(1200)
                    .build();

            // 🧾 Get response
            String response = openAiService.createChatCompletion(chatRequest)
                    .getChoices().get(0).getMessage().getContent();

            // 🧹 Clean up backticks or code fences
            response = response.strip();
            if (response.startsWith("```")) {
                response = response.substring(response.indexOf('\n') + 1, response.lastIndexOf("```")).trim();
            }
            response = response.replaceAll("^`+|`+$", "");

            // 🕵️ Extract JSON object or array
            Pattern jsonPattern = Pattern.compile("(\\{.*\\}|\\[.*\\])", Pattern.DOTALL);
            Matcher matcher = jsonPattern.matcher(response);
            if (matcher.find()) {
                response = matcher.group(1);
            } else {
                throw new RuntimeException("No JSON object/array found in GPT response: " + response);
            }

            // ✅ Parse JSON safely
            List<Map<String, Object>> data = mapper.readValue(response, List.class);

            return Map.of(
                    "rows", data.size(),
                    "data", data
            );

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                    "error", "Failed to generate data: " + e.getMessage()
            );
        }
    }
}
