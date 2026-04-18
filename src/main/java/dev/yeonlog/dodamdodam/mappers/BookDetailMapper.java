package dev.yeonlog.dodamdodam.mappers;

import dev.yeonlog.dodamdodam.dtos.BookDetailDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BookDetailMapper {
    BookDetailDto findById(@Param("id") Long id);
    int countTotal(@Param("bookId") Long bookId);
    int countLoaned(@Param("bookId") Long bookId);
    int countAvailable(@Param("bookId") Long bookId);
    boolean isLiked(@Param("bookId") Long bookId, @Param("userId") String userId);
}
