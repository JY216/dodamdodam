package dev.yeonlog.dodamdodam.controllers;

import dev.yeonlog.dodamdodam.entities.BookCopyEntity;
import dev.yeonlog.dodamdodam.entities.BookEntity;
import dev.yeonlog.dodamdodam.mappers.BookMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class BookEnsureController {

    private final BookMapper bookMapper;

    // 예약/찜 전에 DB에 책이 없으면 자동 저장하고 book_id 반환
    @RequestMapping(value = "/books/ensure", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> ensureBook(
            @RequestParam String isbn,
            @RequestParam String title,
            @RequestParam String author,
            @RequestParam String publisher,
            @RequestParam(required = false) String publishDate,
            @RequestParam(required = false) String coverImage) {

        // 이미 DB에 있으면 바로 id 반환
        Long existingId = bookMapper.findBookIdByIsbn(isbn);
        if (existingId != null) {
            return ResponseEntity.ok(Map.of("bookId", existingId));
        }

        // DB에 없으면 자동 저장
        BookEntity book = new BookEntity();
        book.setIsbn(isbn);
        book.setTitle(title);
        book.setAuthor(author);
        book.setPublisher(publisher);
        book.setPublishDate(publishDate != null && !publishDate.isEmpty() ? java.time.LocalDate.parse(publishDate) : null);
        book.setCoverImage(coverImage);
        book.setTotalQuantity(1);

        bookMapper.insertBookAuto(book);

        // book_copies도 1개 자동 생성
        BookCopyEntity copy = BookCopyEntity.builder()
                .bookId(book.getId())
                .status("AVAILABLE")
                .build();
        bookMapper.insertBookCopy(copy);

        return ResponseEntity.ok(Map.of("bookId", book.getId()));
    }

}
