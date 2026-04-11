package dev.yeonlog.dodamdodam.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yeonlog.dodamdodam.entities.*;
import dev.yeonlog.dodamdodam.mappers.*;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private final EventMapper eventMapper;
    private final NoticeMapper noticeMapper;

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String adminPage(@RequestParam(defaultValue = "dashboard") String menu,
                            @RequestParam(required = false) Long id,
                            @RequestParam(defaultValue = "1") int page,
                            @RequestParam(required = false) String searchKeyword,
                            @RequestParam(required = false) String statusKeyword,
                            @RequestParam(required = false) String loanKeyword,
                            Model model) {
        model.addAttribute("currentMenu", menu);
        model.addAttribute("searchKeyword", searchKeyword);
        model.addAttribute("statusKeyword", statusKeyword);
        model.addAttribute("loanKeyword", loanKeyword);
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
                if (statusKeyword != null && !statusKeyword.trim().isEmpty()) {
                    List<BookEntity> books = bookMapper.searchBooksWithStatus(statusKeyword.trim(), pageSize, offset);
                    int totalCount = bookMapper.countBooksWithStatus(statusKeyword.trim());
                    PageVo pageVo = new PageVo(page, totalCount, pageSize, 5);
                    model.addAttribute("books", books);
                    model.addAttribute("pageVo", pageVo);
                } else {
                    List<BookEntity> books = bookMapper.selectAllBooksWithStatusPage(pageSize, offset);
                    int totalCount = bookMapper.countAllBooksWithStatus();
                    PageVo pageVo = new PageVo(page, totalCount, pageSize, 5);
                    model.addAttribute("books", books);
                    model.addAttribute("pageVo", pageVo);
                }
            }
            case "book-copies" -> {
                model.addAttribute("book", bookMapper.selectById(id));
                model.addAttribute("copies", bookMapper.selectCopiesByBookId(id));
            }
            case "circulation-process" -> {
                if (loanKeyword != null && !loanKeyword.trim().isEmpty()) {
                    List<LoanEntity> loans = loanMapper.searchLoans(loanKeyword.trim(), pageSize, offset);
                    int totalCount = loanMapper.countSearchLoans(loanKeyword.trim());
                    PageVo pageVo = new PageVo(page, totalCount, pageSize, 5);
                    model.addAttribute("loans", loans);
                    model.addAttribute("pageVo", pageVo);
                } else {
                    List<LoanEntity> loans = loanMapper.selectAllLoansWithPage(pageSize, offset);
                    int totalCount = loanMapper.countAllLoans();
                    PageVo pageVo = new PageVo(page, totalCount, pageSize, 5);
                    model.addAttribute("loans", loans);
                    model.addAttribute("pageVo", pageVo);
                }
            }
            case "circulation-status" -> {
                if (loanKeyword != null && !loanKeyword.trim().isEmpty()) {
                    List<LoanEntity> loans = loanMapper.searchLoans(loanKeyword.trim(), pageSize, offset);
                    int totalCount = loanMapper.countSearchLoans(loanKeyword.trim());
                    loans = loans.stream().filter(l -> l.getStatus().equals("LOANED")).toList();
                    loans.forEach(loan -> {
                        if (loan.getDueDate() != null) {
                            long diff = java.time.temporal.ChronoUnit.DAYS.between(
                                    java.time.LocalDate.now(), loan.getDueDate());
                            loan.setDday(diff);
                        }
                    });
                    PageVo pageVo = new PageVo(page, totalCount, pageSize, 5);
                    model.addAttribute("loans", loans);
                    model.addAttribute("pageVo", pageVo);
                } else {
                    List<LoanEntity> loans = loanMapper.selectLoanedWithPage(pageSize, offset);
                    int totalCount = loanMapper.countLoanedLoans();
                    loans.forEach(loan -> {
                        if (loan.getDueDate() != null) {
                            long diff = java.time.temporal.ChronoUnit.DAYS.between(
                                    java.time.LocalDate.now(), loan.getDueDate());
                            loan.setDday(diff);
                        }
                    });
                    PageVo pageVo = new PageVo(page, totalCount, pageSize, 5);
                    model.addAttribute("loans", loans);
                    model.addAttribute("pageVo", pageVo);
                }
            }
            case "circulation-history" -> {
                if (loanKeyword != null && !loanKeyword.trim().isEmpty()) {
                    List<LoanEntity> loans = loanMapper.searchLoans(loanKeyword.trim(), pageSize, offset);
                    int totalCount = loanMapper.countSearchLoans(loanKeyword.trim());
                    PageVo pageVo = new PageVo(page, totalCount, pageSize, 5);
                    model.addAttribute("loans", loans);
                    model.addAttribute("pageVo", pageVo);
                } else {
                    List<LoanEntity> loans = loanMapper.selectAllLoansWithPage(pageSize, offset);
                    int totalCount = loanMapper.countAllLoans();
                    PageVo pageVo = new PageVo(page, totalCount, pageSize, 5);
                    model.addAttribute("loans", loans);
                    model.addAttribute("pageVo", pageVo);
                }
            }
            case "wish-book-list" -> {
                List<WishBookEntity> wishBooks = wishBookMapper.selectAllWishBooksWithPage(pageSize, offset);
                int totalCount = wishBookMapper.countAllWishBooks();
                PageVo pageVo = new PageVo(page, totalCount, pageSize, 5);
                model.addAttribute("wishBooks", wishBooks);
                model.addAttribute("pageVo", pageVo);
            }
            case "book-edit" -> {
                BookEntity book = bookMapper.selectById(id);
                book.setCategoryIds(bookMapper.selectCategoryIdsByBookId(id));
                model.addAttribute("book", book);
                model.addAttribute("categories", bookMapper.selectAllCategories());
            }
            case "event-register" -> {}
            case "event-manage" ->
                    model.addAttribute("events", eventMapper.selectAllEvents());
            case "notice-register" -> {}
            case "notice-manage" ->
                    model.addAttribute("notices", noticeMapper.selectAllNotices());
            case "notice-edit" ->
                    model.addAttribute("notice", noticeMapper.selectById(id));
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
    public String registerBook(BookEntity book,
                               @RequestParam(value = "categoryIds", required = false) List<Integer> categoryIds,
                               RedirectAttributes redirectAttributes) {
        try {
            book.setAdminRegistered(true);
            bookMapper.insertBook(book);

            // 카테고리 여러개 insert
            if (categoryIds != null) {
                for (int categoryId : categoryIds) {
                    bookMapper.insertBookCategory(book.getId(), categoryId);
                }
            }

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

    // 행사 등록
    @RequestMapping(value = "/events/register", method = RequestMethod.POST)
    public String registerEvent(EventEntity event,
                                @RequestParam(required = false) MultipartFile posterFile,
                                RedirectAttributes redirectAttributes) throws Exception {
        if (posterFile != null && !posterFile.isEmpty()) {
            String uploadPath = "C:/upload/dodamdodam/uploads/events/";
            File dir = new File(uploadPath);
            if (!dir.exists()) dir.mkdirs();

            // 확장자만 가져오고 파일명은 UUID로만 저장
            String originalFilename = posterFile.getOriginalFilename();
            String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID() + ext; // 한글 파일명 제거

            File dest = new File(uploadPath + fileName);
            posterFile.transferTo(dest);

            event.setPosterImage("/uploads/events/" + fileName);
        }

        eventMapper.insertEvent(event);
        redirectAttributes.addFlashAttribute("successMsg", "행사가 등록됐어요!");
        return "redirect:/admin?menu=event-manage";
    }

    // 행사 삭제
    @RequestMapping(value = "/events/delete", method = RequestMethod.POST)
    public String deleteEvent(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        eventMapper.deleteEvent(id);
        redirectAttributes.addFlashAttribute("successMsg", "행사가 삭제됐어요!");
        return "redirect:/admin?menu=event-manage";
    }

    // 공지 등록
    @RequestMapping(value = "/notices/register", method = RequestMethod.POST)
    public String registerNotice(NoticeEntity notice,
                                 @RequestParam(required = false) String isPinned,
                                 RedirectAttributes redirectAttributes) {
        notice.setPinned(isPinned != null);
        noticeMapper.insertNotice(notice);
        redirectAttributes.addFlashAttribute("successMsg", "공지가 등록됐어요!");
        return "redirect:/admin?menu=notice-manage";
    }

    // 공지 삭제
    @RequestMapping(value = "/notices/delete", method = RequestMethod.POST)
    public String deleteNotice(@RequestParam Long id,
                               RedirectAttributes redirectAttributes) {
        noticeMapper.deleteNotice(id);
        redirectAttributes.addFlashAttribute("successMsg", "공지가 삭제됐어요!");
        return "redirect:/admin?menu=notice-manage";
    }

    // 공지 수정
    @RequestMapping(value = "/notices/update", method = RequestMethod.POST)
    public String updateNotice(NoticeEntity notice,
                               @RequestParam(required = false) String isPinned,
                               RedirectAttributes redirectAttributes) {
        notice.setPinned(isPinned != null);
        noticeMapper.updateNotice(notice);
        redirectAttributes.addFlashAttribute("successMsg", "공지가 수정됐어요!");
        return "redirect:/admin?menu=notice-manage";
    }
}
