package dev.yeonlog.dodamdodam.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yeonlog.dodamdodam.entities.UserEntity;
import dev.yeonlog.dodamdodam.entities.WishBookEntity;
import dev.yeonlog.dodamdodam.mappers.UserMapper;
import dev.yeonlog.dodamdodam.mappers.WishBookMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WishBookController {

    private final WishBookMapper wishBookMapper;
    private final UserMapper userMapper;

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    // 희망 도서 신청 페이지
    @RequestMapping(value = "/wish-book", method = RequestMethod.GET)
    public String wishBookPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";

        String userId = userDetails.getUsername();
        UserEntity user = userMapper.selectByUserId(userId);

        String phone = user.getMobileFirst() + " - "
                + user.getMobileSecond() + " - "
                + user.getMobileThird();

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        model.addAttribute("userName", user.getName());
        model.addAttribute("userPhone", phone);
        model.addAttribute("today", today);

        return "wish-book/wish-book";
    }

    // 희망 도서 신청 처리
    @RequestMapping(value = "/wish-book", method = RequestMethod.POST)
    public String submitWishBook(@AuthenticationPrincipal UserDetails userDetails,
                                 WishBookEntity wishBook,
                                 RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";

        wishBook.setUserId(userDetails.getUsername());
        wishBookMapper.insertWishBook(wishBook);

        redirectAttributes.addFlashAttribute("successMsg", "희망 도서 신청이 완료 됐어요!");
        return "redirect:/wish-book";

    }

    // 도서 검색 (카카오 API)
    @RequestMapping(value = "/wish-book/search", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> searchBook(@RequestParam String keyword) throws Exception {
        String url = "https://dapi.kakao.com/v3/search/book?query="
                + java.net.URLEncoder.encode(keyword, "UTF-8");

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

        if (documents.isEmpty()) {
            return ResponseEntity.ok(Map.of("result", "FAIL"));
        }

        List<Map<String, Object>> books = new ArrayList<>();
        for (JsonNode book : documents) {
            String publishDate = book.path("datetime").asText();
            String publishYear = publishDate.length() >= 4 ? publishDate.substring(0, 4) : "";

            books.add(Map.of(
                    "title",       book.path("title").asText(),
                    "author",      book.path("authors").size() > 0 ? book.path("authors").get(0).asText() : "",
                    "publisher",   book.path("publisher").asText(),
                    "publishYear", publishYear,
                    "isbn",        book.path("isbn").asText(),
                    "price",       book.path("price").asInt()
            ));
        }
        return ResponseEntity.ok(Map.of("result", "SUCCESS", "books", books));
    }
}
