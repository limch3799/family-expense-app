package com.example.d105.domain.report.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class SsafyAiService {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // RestTemplate과 ObjectMapper를 스프링 빈으로 주입받음
    public SsafyAiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String callSsafyGpt(String prompt) {
        return callSsafyGpt(prompt, "Answer in Korean");
    }

    public String callSsafyGpt(String userPrompt, String systemPrompt) {
        return callSsafyGptWithOptions(userPrompt, systemPrompt, "gpt-4.1-mini", 0.3, 4096);
    }

    // 옵션을 세밀하게 지정할 수 있는 메서드
    public String callSsafyGptWithOptions(String userPrompt, String systemPrompt,
                                          String model, double temperature, int maxTokens) {
        try {
            String url = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    "max_tokens", maxTokens,
                    "temperature", temperature
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return extractContentFromResponse(response.getBody());
            } else {
                throw new IllegalStateException("API 호출 실패: " + response.getStatusCode() + " - " + response.getBody());
            }

        } catch (Exception e) {
            System.err.println("SSAFY API 호출 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("SSAFY AI API 호출 실패", e);
        }
    }

    private String extractContentFromResponse(String responseBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JsonNode choices = jsonNode.get("choices");

            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).get("message");
                if (message != null && message.has("content")) {
                    return message.get("content").asText();
                }
            }

            throw new IllegalStateException("응답에서 content를 찾을 수 없습니다: " + responseBody);

        } catch (Exception e) {
            System.err.println("응답 파싱 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("응답 파싱 실패", e);
        }
    }
}
