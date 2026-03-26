package dev.yeonlog.dodamdodam.mappers;

import dev.yeonlog.dodamdodam.entities.BookCopyEntity;
import dev.yeonlog.dodamdodam.entities.BookEntity;
import dev.yeonlog.dodamdodam.entities.CategoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookMapper {
    // 카테고리 전체 조회
    List<CategoryEntity> selectAllCategories();

    // 도서 등록
    int insertBook(BookEntity book);

    // 카테고리 매핑 insert
    void insertBookCategory(@Param("bookId") long bookId, @Param("categoryId") int categoryId);

    // 도서 사본 등록 (수량 만큼 반복 insert)
    int insertBookCopy(BookCopyEntity bookCopy);

    // 도서 목록
    List<BookEntity> selectAllBooks();

    // 도서 상태 관리용
    List<BookEntity> selectAllBooksWithStatus();

    void addBookCopy(@Param("bookId") long bookId);

    void updateTotalQuantity(@Param("id") long id, @Param("quantity") int quantity);

    List<BookCopyEntity> selectCopiesByBookId(@Param("bookId") long bookId);

    BookEntity selectById(@Param("id") long id);

    void updateCopyStatus(@Param("id") long id, @Param("status") String status);

    List<BookEntity> searchBooks(@Param("keyword") String keyword,
                                 @Param("type") String type,
                                 @Param("pageSize") int pageSize,
                                 @Param("offset") int offset);

    BookCopyEntity selectAvailableCopy(@Param("bookId") long bookId);

    int countSearchBooks(@Param("keyword") String keyword, @Param("type") String type);

    List<BookEntity> searchBooksByCategory(int categoryId, int pageSize, int offset);
    int countBooksByCategory(int categoryId);

    int countAllBooks();

    List<BookEntity> selectAllBooksWithPage(@Param("pageSize") int pageSize,
                                            @Param("offset") int offset);

    void updateCoverImage(@Param("id") Long id, @Param("coverImage") String coverImage);

    List<BookEntity> selectNewBooks(@Param("categoryId") Integer categoryId,
                                    @Param("month") Integer month,
                                    @Param("week") Integer week,
                                    @Param("pageSize") int pageSize,
                                    @Param("offset") int offset);

    int countNewBooks(@Param("categoryId") Integer categoryId,
                      @Param("month") Integer month,
                      @Param("week") Integer week);
}
