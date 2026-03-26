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
import java.util.List;

@Controller
@RequiredArgsConstructor
public class NewBooksController {

    private final BookMapper bookMapper;

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @RequestMapping(value = "/new-books", method = RequestMethod.GET)
    public String getNewBooks(@RequestParam(required = false) Integer category,
                              @RequestParam(required = false) Integer month,
                              @RequestParam(required = false) Integer week,
                              @RequestParam(defaultValue = "1") int page,
                              Model model) {

        int pageSize = 10;
        int offset = (page - 1) * pageSize;

        List<BookEntity> books = bookMapper.selectNewBooks(category, month, week, pageSize, offset);
        fillCoverImages(books);
        int totalCount = bookMapper.countNewBooks(category, month, week);

        PageVo pageVo = new PageVo(page, totalCount, pageSize, 5);

        model.addAttribute("books", books);
        model.addAttribute("pageVo", pageVo);
        model.addAttribute("category", category);
        model.addAttribute("month", month);
        model.addAttribute("week", week);

        return "new-books/new-books";
    }

    private void fillCoverImages(List<BookEntity> books) {
        for (BookEntity book : books) {
            if (book.getCoverImage() == null || book.getCoverImage().isEmpty()) {
                try {
                    String thumbnail = fetchThumbnail(
                            "https://dapi.kakao.com/v3/search/book?query="
                                    + java.net.URLEncoder.encode(book.getIsbn(), "UTF-8")
                                    + "&target=isbn&size=1");

                    if (thumbnail.isEmpty() && book.getTitle() != null) {
                        thumbnail = fetchThumbnail(
                                "https://dapi.kakao.com/v3/search/book?query="
                                        + java.net.URLEncoder.encode(book.getTitle(), "UTF-8")
                                        + "&target=title&size=1");
                    }

                    if (!thumbnail.isEmpty()) {
                        book.setCoverImage(thumbnail);
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    private String fetchThumbnail(String url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "KakaoAK " + kakaoApiKey);

        BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();

        JsonNode root = new ObjectMapper().readTree(sb.toString());
        JsonNode documents = root.path("documents");
        if (documents.size() > 0) {
            return documents.get(0).path("thumbnail").asText();
        }
        return "";
    }
}