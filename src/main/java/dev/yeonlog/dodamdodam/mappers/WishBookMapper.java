package dev.yeonlog.dodamdodam.mappers;

import dev.yeonlog.dodamdodam.entities.WishBookEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WishBookMapper {
    // 희망 도서 신청
    int insertWishBook(WishBookEntity wishBook);

    // 유저 - 내 신청 목록
    List<WishBookEntity> selectByUserId(@Param("userId") String userId);

    // 관리자 - 전체 신청 목록
    List<WishBookEntity> selectAllWishBooks();

    // 관리자 - 상태 변경
    void updateStatus(@Param("id") long id, @Param("status") String status);

    List<WishBookEntity> selectAllWishBooksWithPage(@Param("pageSize") int pageSize, @Param("offset") int offset);
    int countAllWishBooks();
}
