package dev.yeonlog.dodamdodam.mappers;

import dev.yeonlog.dodamdodam.entities.BookEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookLikeMapper {
    void insertLike(@Param("userId") String userId,
                    @Param("bookId") Long bookId);

    void deleteLike(@Param("userId") String userId,
                    @Param("bookId") Long bookId);

    int countLike(@Param("userId") String userId,
                  @Param("bookId") Long bookId);

    List<Long> selectLikedBookIds(@Param("userId") String userId);

    List<BookEntity> selectLikedBooks(@Param("userId") String userId);
}
