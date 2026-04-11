package dev.yeonlog.dodamdodam.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yeonlog.dodamdodam.dtos.LoanedBookDto;
import dev.yeonlog.dodamdodam.mappers.LoanHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiRecommendService {
    private final LoanHistoryMapper loanHistoryMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=";

    @Value("${aladin.api.key}")
    private String aladinApiKey;


    // 로그인 + 대출 이력 있는 유저 개인 추천
    public String getPersonalRecommendation(String userId) throws Exception {
        List<LoanedBookDto> history = loanHistoryMapper.findLoanedBooksByUserId(userId);

        if (history.size() < 5) {
            // TODO 10권 적정이긴 한데 5권... 일단 회의를 하기
            return getGeneralRecommendation();
        }

        StringBuilder bookList = new StringBuilder();
        for (LoanedBookDto book : history) {
            bookList.append("- ").append(book.getTitle())
                    .append(" / ").append(book.getAuthor()).append("\n");
        }

        long seed = System.currentTimeMillis();

        String prompt = """
                당신은 '도담 매니저'라는 도서관 사서 AI입니다. [seed:%d]
                아래는 사용자가 최근에 대출한 도서 목록입니다:
                
                %s
                
                이 사용자의 취향을 분석하여 읽으면 좋을 도서 3권을 추천해 주세요.
                반드시 아래 JSON 형식으로만 응답하고, 다른 텍스트는 절대 포함하지 마세요.
                [
                    {"title": "도서 제목", "author": "저자명", "reason": "추천 이유 한 줄"},
                    {"title": "도서 제목", "author": "저자명", "reason": "추천 이유 한 줄"},
                    {"title": "도서 제목", "author": "저자명", "reason": "추천 이유 한 줄"}
                ]
                """.formatted(seed, bookList);

        String result = callGemini(prompt);

        JsonNode books = objectMapper.readTree(result);
        com.fasterxml.jackson.databind.node.ArrayNode enriched =
                objectMapper.createArrayNode();

        for (JsonNode book : books) {
            com.fasterxml.jackson.databind.node.ObjectNode node =
                    objectMapper.createObjectNode();
            node.put("title",    book.path("title").asText());
            node.put("author",   book.path("author").asText());
            node.put("reason",   book.path("reason").asText());
            String coverUrl = getCoverImage(book.path("title").asText());
            node.put("coverUrl", coverUrl != null ? coverUrl : "");
            enriched.add(node);
        }
        return objectMapper.writeValueAsString(enriched);
    }

    // 비로그인 혹은 대출 이력 없는 일반 추천
    public String getGeneralRecommendation() throws Exception {
        String[] genres = {"소설", "에세이", "인문학", "과학", "자기계발", "역사", "경제", "심리학", "철학", "환경"};
        String g1 = genres[(int)(Math.random() * genres.length)];
        String g2 = genres[(int)(Math.random() * genres.length)];
        long seed = System.currentTimeMillis();

        String prompt = """
        당신은 '도담 매니저'라는 도서관 사서 AI입니다. [seed:%d]
        오늘의 추천 테마는 '%s'와 '%s'입니다.
        절대로 이전에 추천한 책을 반복하지 마세요.
        누구나 재미있게 읽을 수 있는 완전히 새로운 도서 3권을 추천해주세요.
        반드시 아래 JSON 형식으로만 응답하고, 다른 텍스트는 절대 포함하지 마세요.
        
        [
          {"title": "도서 제목", "author": "저자명", "reason": "추천 이유 한 줄"},
          {"title": "도서 제목", "author": "저자명", "reason": "추천 이유 한 줄"},
          {"title": "도서 제목", "author": "저자명", "reason": "추천 이유 한 줄"}
        ]
        """.formatted(seed, g1, g2);

        String result = callGemini(prompt);

        JsonNode books = objectMapper.readTree(result);
        com.fasterxml.jackson.databind.node.ArrayNode enriched =
                objectMapper.createArrayNode();

        for (JsonNode book : books) {
            com.fasterxml.jackson.databind.node.ObjectNode node =
                    objectMapper.createObjectNode();
            node.put("title",    book.path("title").asText());
            node.put("author",   book.path("author").asText());
            node.put("reason",   book.path("reason").asText());
            String coverUrl = getCoverImage(book.path("title").asText());
            node.put("coverUrl", coverUrl != null ? coverUrl : "");
            enriched.add(node);
        }
        return objectMapper.writeValueAsString(enriched);
    }

    // Gemini API 호출

    private String callGemini(String prompt) throws Exception {
        String requestBody = """
                {
                  "contents": [
                    {
                      "parts": [
                        {"text": "%s"}
                      ]
                    }
                  ]
                }
                """.formatted(prompt.replace("\"", "\\\"").replace("\n", "\\n"));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_URL + geminiApiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode root = objectMapper.readTree(response.body());
        String text = root
                .path("candidates").get(0)
                .path("content")
                .path("parts").get(0)
                .path("text").asText();

        // ```json ... ``` 펜스 제거
        return text.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
    }

    private String getCoverImage(String title) {
        try {
            String encodedTitle = java.net.URLEncoder.encode(title, "UTF-8");
            String url = "https://www.aladin.co.kr/ttb/api/ItemSearch.aspx"
                    + "?ttbkey=" + aladinApiKey
                    + "&Query=" + encodedTitle
                    + "&SearchTarget=Book"
                    + "&MaxResults=1"
                    + "&output=js"
                    + "&Version=20131101";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode items = root.path("item");

            if (!items.isEmpty()) {
                String cover = items.get(0).path("cover").asText();
                return cover.replace("http://", "https://");  // ← 추가
            }
        } catch (Exception e) {
            // 표지 못 가져와도 무시 (이니셜 카드로 대체)
        }
        return null;
    }

}
