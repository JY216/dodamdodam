package dev.yeonlog.dodamdodam.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yeonlog.dodamdodam.dtos.BookDetailDto;
import dev.yeonlog.dodamdodam.mappers.BookDetailMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class BookDetailController {

    private final BookDetailMapper bookDetailMapper;
    private final ObjectMapper objectMapper;

    @Value("${aladin.api.key}")
    private String aladinApiKey;

    @RequestMapping(value = "/books/{id}", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String bookDetail(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) throws Exception {

        BookDetailDto book = bookDetailMapper.findById(id);
        if (book == null) return "redirect:/search";

        int totalCopies   = bookDetailMapper.countTotal(id);
        int loanedCopies  = bookDetailMapper.countLoaned(id);
        int availableCopies = bookDetailMapper.countAvailable(id);
        boolean canReserve  = availableCopies == 0 && loanedCopies > 0;

        // 좋아요 여부
        boolean isLiked = false;
        if (userDetails != null) {
            isLiked = bookDetailMapper.isLiked(id, userDetails.getUsername());
        }

        // 알라딘 API로 책 소개 + 페이지 수 가져오기
        String description = "";
        String pages = "";
        if (book.getIsbn() != null && !book.getIsbn().isBlank()) {
            try {
                Map<String, String> extra = fetchBookExtra(book.getIsbn());
                description = extra.getOrDefault("description", "");
                pages       = extra.getOrDefault("pages", "");
            } catch (Exception ignored) {}
        }

        model.addAttribute("book",            book);
        model.addAttribute("totalCopies",     totalCopies);
        model.addAttribute("loanedCopies",    loanedCopies);
        model.addAttribute("availableCopies", availableCopies);
        model.addAttribute("canReserve",      canReserve);
        model.addAttribute("isLiked",         isLiked);
        model.addAttribute("description",     description);
        model.addAttribute("pages",           pages);
        model.addAttribute("isLoggedIn",      userDetails != null);

        return "search/book-detail";
    }

    private Map<String, String> fetchBookExtra(String isbn) throws Exception {
        String url = "http://www.aladin.co.kr/ttb/api/ItemLookUp.aspx"
                + "?ttbkey=" + aladinApiKey
                + "&itemIdType=ISBN13"
                + "&ItemId=" + isbn
                + "&output=js"
                + "&Version=20131101"
                + "&OptResult=description,subInfo";

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");

        BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();

        JsonNode root  = objectMapper.readTree(sb.toString());
        JsonNode items = root.path("item");

        Map<String, String> result = new java.util.HashMap<>();
        if (!items.isEmpty()) {
            JsonNode item = items.get(0);
            result.put("description", item.path("description").asText(""));
            int pages = item.path("subInfo").path("itemPage").asInt(0);
            result.put("pages", pages > 0 ? pages + "p" : "");
        }
        return result;
    }
}