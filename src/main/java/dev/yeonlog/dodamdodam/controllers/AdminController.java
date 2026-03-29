package dev.yeonlog.dodamdodam.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yeonlog.dodamdodam.entities.BookCopyEntity;
import dev.yeonlog.dodamdodam.entities.BookEntity;
import dev.yeonlog.dodamdodam.entities.LoanEntity;
import dev.yeonlog.dodamdodam.entities.WishBookEntity;
import dev.yeonlog.dodamdodam.mappers.BookMapper;
import dev.yeonlog.dodamdodam.mappers.LoanMapper;
import dev.yeonlog.dodamdodam.mappers.WishBookMapper;
import dev.yeonlog.dodamdodam.vos.PageVo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/admin")
@RequiredArgsConstructor
public class AdminController {

    private final BookMapper bookMapper;
    private final WishBookMapper wishBookMapper;
    @Value("${kakao.api.key}")
    private String kakaoApiKey;
    @Value("${aladin.api.key}")
    private String aladinApiKey;
    private final LoanMapper loanMapper;

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String adminPage(@RequestParam(defaultValue = "dashboard") String menu,
                            @RequestParam(required = false) Long id,
                            @RequestParam(defaultValue = "1") int page,
                            @RequestParam(required = false) String searchKeyword,
                            Model model) {
        model.addAttribute("currentMenu", menu);
        model.addAttribute("searchKeyword", searchKeyword);
        int pageSize = 10;
        int offset = (page - 1) * pageSize;

        switch (menu) {
            case "book-register" ->
                    model.addAttribute("categories", bookMapper.selectAllCategories());
            case "book-list" -> {
                if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                    // 검색어 있을 때
                    List<BookEntity> books = bookMapper.searchBooks(searchKeyword.trim(), "all", pageSize, offset);
                    int totalCount = bookMapper.countSearchBooks(searchKeyword.trim(), "all");
                    PageVo pageVo = new PageVo(page, totalCount, pageSize, 5);
                    model.addAttribute("books", books);
                    model.addAttribute("pageVo", pageVo);
                } else {
                    // 검색어 없을 때
                    List<BookEntity> books = bookMapper.selectAllBooksWithPage(pageSize, offset);
                    int totalCount = bookMapper.countAllBooks();
                    PageVo pageVo = new PageVo(page, totalCount, pageSize, 5);
                    model.addAttribute("books", books);
                    model.addAttribute("pageVo", pageVo);
                }
            }
            case "book-status" -> {
                List<BookEntity> books = bookMapper.selectAllBooksWithStatusPage(pageSize, offset);
                int totalCount = bookMapper.countAllBooksWithStatus();
                PageVo pageVo = new PageVo(page, totalCount, pageSize, 5);
                model.addAttribute("books", books);
                model.addAttribute("pageVo", pageVo);
            }
            case "book-copies" -> {
                model.addAttribute("book", bookMapper.selectById(id));
                model.addAttribute("copies", bookMapper.selectCopiesByBookId(id));
            }
            case "circulation-process" ->
                    model.addAttribute("loans", loanMapper.selectAllLoans());
            case "circulation-status" -> {
                List<LoanEntity> loans = loanMapper.selectAllLoans();
                loans.forEach(loan -> {
                    if (loan.getDueDate() != null && loan.getStatus().equals("LOANED")) {
                        long diff = java.time.temporal.ChronoUnit.DAYS.between(
                                java.time.LocalDate.now(), loan.getDueDate()
                        );
                        loan.setDday(diff);
                    }
                });
                model.addAttribute("loans", loans);
            }
            case "circulation-history" ->
                    model.addAttribute("loans", loanMapper.selectAllLoans());
            case "wish-book-list" -> {
                List<WishBookEntity> wishBooks = wishBookMapper.selectAllWishBooksWithPage(pageSize, offset);
                int totalCount = wishBookMapper.countAllWishBooks();
                PageVo pageVo = new PageVo(page, totalCount, pageSize, 5);
                model.addAttribute("wishBooks", wishBooks);
                model.addAttribute("pageVo", pageVo);
            }
            case "book-edit" -> {
                model.addAttribute("book", bookMapper.selectById(id));
                model.addAttribute("categories", bookMapper.selectAllCategories());
            }
        }
        return "admin/admin-page";
    }


    @RequestMapping(value = "/books/search-isbn", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> searchByIsbn(@RequestParam String isbn) throws Exception {
        // 1차: 카카오 API
        Map<String, Object> kakaoResult = searchByIsbnFromKakao(isbn);
        if (kakaoResult != null) {
            return ResponseEntity.ok(kakaoResult);
        }

        // 2차: 알라딘 API
        Map<String, Object> aladinResult = searchByIsbnFromAladin(isbn);
        if (aladinResult != null) {
            return ResponseEntity.ok(aladinResult);
        }

        return ResponseEntity.ok(Map.of("result", "FAIL"));
    }

    private Map<String, Object> searchByIsbnFromKakao(String isbn) throws Exception {
        String url = "https://dapi.kakao.com/v3/search/book?query=" + isbn + "&target=isbn";
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "KakaoAK " + kakaoApiKey);

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();

        JsonNode root = new ObjectMapper().readTree(sb.toString());
        JsonNode documents = root.path("documents");

        if (documents.isEmpty()) return null;

        JsonNode book = documents.get(0);
        String publishDate = book.path("datetime").asText();
        if (publishDate.length() >= 10) publishDate = publishDate.substring(0, 10);

        return Map.of(
                "result", "SUCCESS",
                "title", book.path("title").asText(),
                "author", book.path("authors").size() > 0 ? book.path("authors").get(0).asText() : "",
                "publisher", book.path("publisher").asText(),
                "publishDate", publishDate,
                "coverImage", book.path("thumbnail").asText()
        );
    }

    private Map<String, Object> searchByIsbnFromAladin(String isbn) throws Exception {
        String url = "http://www.aladin.co.kr/ttb/api/ItemLookUp.aspx"
                + "?ttbkey=" + aladinApiKey
                + "&itemIdType=ISBN13"
                + "&ItemId=" + isbn
                + "&output=js"
                + "&Version=20131101"
                + "&Cover=Big";

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();

        JsonNode root = new ObjectMapper().readTree(sb.toString());
        JsonNode items = root.path("item");

        if (items.isEmpty()) return null;

        JsonNode book = items.get(0);
        String publishDate = book.path("pubDate").asText(); // yyyy-mm-dd 형식

        return Map.of(
                "result", "SUCCESS",
                "title", book.path("title").asText(),
                "author", book.path("author").asText().replaceAll("\\(지은이\\)", "").trim(),
                "publisher", book.path("publisher").asText(),
                "publishDate", publishDate,
                "coverImage", book.path("cover").asText()
        );
    }

    @RequestMapping(value = "/books/register", method = RequestMethod.POST)
    public String registerBook(BookEntity book, RedirectAttributes redirectAttributes) {

        // 수량만큼 book_copies insert
        try {
            // 도서 insert
            book.setAdminRegistered(true);
            bookMapper.insertBook(book);

            for (int i = 0; i < book.getTotalQuantity(); i++) {
                BookCopyEntity copy = BookCopyEntity.builder()
                        .bookId(book.getId())
                        .status("AVAILABLE")
                        .build();
                bookMapper.insertBookCopy(copy);
            }
            redirectAttributes.addFlashAttribute("successMsg", "도서가 등록되었어요!");
        } catch (DuplicateKeyException e) {
            redirectAttributes.addFlashAttribute("errorMsg", "이미 등록된 ISBN이에요! 수량 추가는 도서 상태 관리에서 해주세요.");
            return "redirect:/admin?menu=book-register";
        }

        return "redirect:/admin?menu=book-list";
    }

    @RequestMapping(value = "/books/add-copy", method = RequestMethod.POST)
    public String addCopy(@RequestParam long bookId,
                          @RequestParam int totalQuantity,
                          RedirectAttributes redirectAttributes) {
        bookMapper.addBookCopy(bookId);
        bookMapper.updateTotalQuantity(bookId, totalQuantity + 1);
        redirectAttributes.addFlashAttribute("successMsg", "수량이 1권 추가됐어요!");
        return "redirect:/admin?menu=book-status";
    }

    @RequestMapping(value = "/books/update-copy-status", method = RequestMethod.POST)
    public String updateCopyStatus(@RequestParam long copyId,
                                   @RequestParam String status,
                                   @RequestParam long bookId,
                                   RedirectAttributes redirectAttributes) {
        bookMapper.updateCopyStatus(copyId, status);
        redirectAttributes.addFlashAttribute("successMsg", "사본 상태가 변경됐어요!");
        return "redirect:/admin?menu=book-copies&id=" + bookId;
    }

    // 대출 승인
    @RequestMapping(value = "/loans/approve", method = RequestMethod.POST)
    public String approveLoan(@RequestParam long loanId,
                              @RequestParam long copyId,
                              RedirectAttributes redirectAttributes) {
        loanMapper.approveLoan(loanId);
        bookMapper.updateCopyStatus(copyId, "LOANED");
        redirectAttributes.addFlashAttribute("successMsg", "대출 승인됐어요!");
        return "redirect:/admin?menu=circulation-process";
    }

    // 반납 처리
    @RequestMapping(value = "/loans/return", method = RequestMethod.POST)
    public String returnLoan(@RequestParam long loanId,
                             @RequestParam long copyId,
                             RedirectAttributes redirectAttributes) {
        loanMapper.returnLoan(loanId);
        bookMapper.updateCopyStatus(copyId, "AVAILABLE");
        redirectAttributes.addFlashAttribute("successMsg", "반납 처리됐어요!");
        return "redirect:/admin?menu=circulation-process";
    }

    @RequestMapping(value = "/wish-books/status", method = RequestMethod.POST)
    public String updateWishBookStatus(@RequestParam long id,
                                       @RequestParam String status,
                                       RedirectAttributes redirectAttributes) {
        wishBookMapper.updateStatus(id, status);
        redirectAttributes.addFlashAttribute("successMsg", "상태가 변경됐어요!");
        return "redirect:/admin?menu=wish-book-list";
    }

    @RequestMapping(value = "/books/update", method = RequestMethod.POST)
    public String updateBook(BookEntity book,
                             @RequestParam(required = false) Integer categoryId,
                             RedirectAttributes redirectAttributes) {
        bookMapper.updateBook(book);

        // 카테고리 수정
        if (categoryId != null && categoryId > 0) {
            bookMapper.deleteBookCategories(book.getId());
            bookMapper.insertBookCategory(book.getId(), categoryId);
        }

        // 수량 변경 시 book_copies 동기화
        BookEntity existing = bookMapper.selectById(book.getId());
        int currentCopies = existing.getTotalQuantity();
        int newQuantity = book.getTotalQuantity();

        if (newQuantity > currentCopies) {
            for (int i = 0; i < newQuantity - currentCopies; i++) {
                bookMapper.addBookCopy(book.getId());
            }
        }

        redirectAttributes.addFlashAttribute("successMsg", "도서 정보가 수정됐어요!");
        return "redirect:/admin?menu=book-list";
    }
}
