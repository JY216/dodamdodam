package dev.yeonlog.dodamdodam.services.GeminiServices;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatbotService {
    @Value("${gemini.api.key}")
    private String apiKey;
    @Value("${gemini.api.url}")
    private String apiUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    public String chat(String userMessage) {
        String fullUrl = apiUrl + "?key=" + apiKey;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 1. 시스템 지침 정의
        Map<String, Object> systemInstruction = Map.of(
                "parts", List.of(Map.of("text", "당신은 '도담도담 도서관'의 전문 사서 AI입니다. 당신의 역할은 사용자의 도서 관련 질문에만 친절하게 답변하는 것입니다.[제약 사항]1. 사용자가 책, 독서, 도서관, 문학 등 도서와 직접적으로 관련된 질문을 할 때만 답변하세요.2. 만약 사용자의 질문이 도서와 관련이 없다면, 반드시 정중하게 거절하세요.3. 거절 메시지는 다음과 같이 출력하세요: \"죄송합니다, 저는 도서와 관련된 내용만 답변할 수 있어요. 책이나 도서관 이용에 대해 궁금한 점이 있으신가요?\"4. 절대로 다른 주제(날씨, 정치, 일상 대화 등)에 대해 답변하지 마세요."))
        );

        // 2. 요청 본문 구성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("system_instruction", systemInstruction);
        requestBody.put("contents", List.of(
                Map.of("parts", List.of(Map.of("text", userMessage)))
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(fullUrl, entity, Map.class);
            List<Map> candidates = (List<Map>) response.getBody().get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return "응답을 받지 못했습니다. 다시 시도해주세요.";
            }
            Map content = (Map) candidates.get(0).get("content");
            List<Map> parts = (List<Map>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            return "오류가 발생했습니다: " + e.getMessage();
        }
    }
}
