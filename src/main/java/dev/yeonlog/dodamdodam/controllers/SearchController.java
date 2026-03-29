package dev.yeonlog.dodamdodam.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yeonlog.dodamdodam.entities.BookEntity;
import dev.yeonlog.dodamdodam.mappers.BookLikeMapper;
import dev.yeonlog.dodamdodam.mappers.BookMapper;
import dev.yeonlog.dodamdodam.vos.PageVo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

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
    private final BookLikeMapper bookLikeMapper;

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @Value("${aladin.api.key}")
    private String aladinApiKey;

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String search(@RequestParam(required = false) String keyword,
                         @RequestParam(defaultValue = "all") String type,
                         @RequestParam(defaultValue = "0") int categoryId,
                         @RequestParam(defaultValue = "1") int page,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model) throws Exception {

        model.addAttribute("keyword", keyword);
        model.addAttribute("type", type);
        model.addAttribute("categoryId", categoryId);

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasCategory = categoryId > 0;

        // 찜 목록 추가
        if (userDetails != null) {
            List<Long> likedBookIds = bookLikeMapper.selectLikedBookIds(userDetails.getUsername());
            model.addAttribute("likedBookIds", likedBookIds);
        }

        if (hasKeyword) {
            int pageSize = 10;

            // 1. DB 검색
            List<BookEntity> dbBooks = bookMapper.searchBooks(keyword.trim(), type, 100, 0);
            fillCoverImages(dbBooks);

            // 2. 카카오 API 검색
            List<BookEntity> kakaoBooks = searchKakao(keyword.trim(), type, page, pageSize);
            int kakaoTotalCount = getKakaoTotalCount(keyword.trim(), type);

            // 3. 알라딘 API 검색
            List<BookEntity> aladinBooks = searchAladin(keyword.trim(), type, page, pageSize);

            // 4. DB ISBN 목록
            List<String> dbIsbns = dbBooks.stream().map(BookEntity::getIsbn).toList();
            List<String> kakaoIsbns = kakaoBooks.stream().map(BookEntity::getIsbn).toList();

            // 5. 카카오에 없는 것만 필터
            List<BookEntity> filteredKakao = kakaoBooks.stream()
                    .filter(b -> !dbIsbns.contains(b.getIsbn()))
                    .toList();

            // 6. 알라딘에서 DB에도 카카오에도 없는 것만
            List<BookEntity> filteredAladin = aladinBooks.stream()
                    .filter(b -> !dbIsbns.contains(b.getIsbn()) && !kakaoIsbns.contains(b.getIsbn()))
                    .toList();

            // 7. 합치기: DB + 카카오 + 알라딘
            List<BookEntity> allBooks = new ArrayList<>(dbBooks);
            allBooks.addAll(filteredKakao);
            allBooks.addAll(filteredAladin);

            PageVo pageVo = new PageVo(page, kakaoTotalCount, pageSize, 5);
            model.addAttribute("books", allBooks);
            model.addAttribute("pageVo", pageVo);

        } else if (hasCategory) {
            int pageSize = 10;
            int offset = (page - 1) * pageSize;

            List<BookEntity> books = bookMapper.searchBooksByCategory(categoryId, pageSize, offset);
            fillCoverImages(books);
            int totalCount = bookMapper.countBooksByCategory(categoryId);

            PageVo pageVo = new PageVo(page, totalCount, pageSize, 5);
            model.addAttribute("books", books);
            model.addAttribute("pageVo", pageVo);
        }

        return "search/book-search";
    }

    private List<BookEntity> searchAladin(String keyword, String type, int page, int size) throws Exception {
        String searchType = switch (type) {
            case "title" -> "Title";
            case "author" -> "Author";
            case "isbn" -> "ISBN";
            default -> "Keyword";
        };

        String url = "http://www.aladin.co.kr/ttb/api/ItemSearch.aspx"
                + "?ttbkey=" + aladinApiKey
                + "&Query=" + java.net.URLEncoder.encode(keyword, "UTF-8")
                + "&QueryType=" + searchType
                + "&MaxResults=" + size
                + "&start=" + page
                + "&SearchTarget=Book"
                + "&output=js"
                + "&Version=20131101"
                + "&Cover=Big";

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");

        BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(sb.toString());
        JsonNode items = root.path("item");

        List<BookEntity> books = new ArrayList<>();
        for (JsonNode item : items) {
            String isbn = item.path("isbn13").asText();
            if (isbn.isEmpty()) isbn = item.path("isbn").asText();
            if (isbn.isEmpty()) continue;

            BookEntity book = new BookEntity();
            book.setTitle(item.path("title").asText());
            book.setAuthor(item.path("author").asText().replaceAll("\\(지은이\\)", "").trim());
            book.setPublisher(item.path("publisher").asText());
            book.setIsbn(isbn);
            book.setCoverImage(item.path("cover").asText());
            book.setAvailableQuantity(-1);
            books.add(book);
        }
        return books;
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

    @RequestMapping(value = "/book-detail/{id}", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String bookDetail(@PathVariable Long id, Model model) {
        return null;
    }

}