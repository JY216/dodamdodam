package dev.yeonlog.dodamdodam.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yeonlog.dodamdodam.entities.BookEntity;
import dev.yeonlog.dodamdodam.mappers.BookMapper;
import dev.yeonlog.dodamdodam.vos.PageVo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final BookMapper bookMapper;

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String search(@RequestParam(required = false) String keyword,
                         @RequestParam(defaultValue = "all") String type,
                         @RequestParam(defaultValue = "1") int page,
                         Model model) throws Exception {

        model.addAttribute("keyword", keyword);
        model.addAttribute("type", type);

        if (keyword != null && !keyword.trim().isEmpty()) {
            int pageSize = 10;

            // 1. DB 검색 (상단 고정 - 페이지 상관없이 항상 표시)
            List<BookEntity> dbBooks = bookMapper.searchBooks(
                    keyword.trim(), type, 100, 0); // 전체 조회

            // 2. 카카오 API 페이지네이션 검색
            List<BookEntity> kakaoBooks = searchKakao(keyword.trim(), type, page, pageSize);
            int kakaoTotalCount = getKakaoTotalCount(keyword.trim(), type);

            // 3. DB ISBN 목록
            List<String> dbIsbns = dbBooks.stream()
                    .map(BookEntity::getIsbn)
                    .toList();

            // 4. 카카오 결과 중 DB에 없는 것만
            List<BookEntity> filteredKakao = kakaoBooks.stream()
                    .filter(b -> !dbIsbns.contains(b.getIsbn()))
                    .toList();

            // 5. DB 결과 + 카카오 결과 합치기
            List<BookEntity> allBooks = new ArrayList<>(dbBooks);
            allBooks.addAll(filteredKakao);

            // 6. 카카오 기준 페이지네이션
            PageVo pageVo = new PageVo(page, kakaoTotalCount, pageSize, 5);

            model.addAttribute("books", allBooks);
            model.addAttribute("pageVo", pageVo);
        }

        return "search/book-search";
    }

    private List<BookEntity> searchKakao(String keyword, String type,
                                         int page, int size) throws Exception {
        String target = switch (type) {
            case "title" -> "&target=title";
            case "author" -> "&target=person";
            case "isbn" -> "&target=isbn";
            default -> "";
        };

        String url = "https://dapi.kakao.com/v3/search/book?query="
                + java.net.URLEncoder.encode(keyword, "UTF-8")
                + "&size=" + size
                + "&page=" + page
                + target;

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "KakaoAK " + kakaoApiKey);

        BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(sb.toString());
        JsonNode documents = root.path("documents");

        List<BookEntity> books = new ArrayList<>();
        for (JsonNode doc : documents) {
            BookEntity book = new BookEntity();
            book.setTitle(doc.path("title").asText());
            book.setAuthor(doc.path("authors").size() > 0 ?
                    doc.path("authors").get(0).asText() : "");
            book.setPublisher(doc.path("publisher").asText());
            book.setIsbn(doc.path("isbn").asText().split(" ").length > 1 ?
                    doc.path("isbn").asText().split(" ")[1] :
                    doc.path("isbn").asText());
            book.setCoverImage(doc.path("thumbnail").asText());
            book.setAvailableQuantity(-1);
            books.add(book);
        }
        return books;
    }

    private int getKakaoTotalCount(String keyword, String type) throws Exception {
        String target = switch (type) {
            case "title" -> "&target=title";
            case "author" -> "&target=person";
            case "isbn" -> "&target=isbn";
            default -> "";
        };

        String url = "https://dapi.kakao.com/v3/search/book?query="
                + java.net.URLEncoder.encode(keyword, "UTF-8")
                + "&size=1"
                + target;

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "KakaoAK " + kakaoApiKey);

        BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(sb.toString());

        // 카카오 API 최대 50페이지 * 10개 = 500개
        int total = root.path("meta").path("total_count").asInt();
        return Math.min(total, 500);
    }
}