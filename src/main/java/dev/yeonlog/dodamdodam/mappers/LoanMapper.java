package dev.yeonlog.dodamdodam.mappers;

import dev.yeonlog.dodamdodam.entities.LoanEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LoanMapper {

    // 예약 신청
    int insertLoan(@Param("loan") LoanEntity loan);

    // 관리자 - 전체 대출 목록 조회
    List<LoanEntity> selectAllLoans();

    // 관리자 - 대출 승인
    int approveLoan(@Param("id") long id);

    // 관리자 - 반납 처리
    int returnLoan(@Param("id") long id);

    // 유저 - 내 대출 목록
    List<LoanEntity> selectLoansByUserId(@Param("userId") String userId);

    // 대출 단건 조회
    LoanEntity selectById(@Param("id") long id);

    void cancelLoan(@Param("id") long id);

    int countPendingOrActiveLoan(@Param("userId") String userId, @Param("bookId") long bookId );

    List<LoanEntity> searchLoans(@Param("keyword") String keyword,
                                 @Param("pageSize") int pageSize,
                                 @Param("offset") int offset);

    int countSearchLoans(@Param("keyword") String keyword);

    List<LoanEntity> selectAllLoansWithPage(@Param("pageSize") int pageSize, @Param("offset") int offset);

    int countAllLoans();

    List<LoanEntity> selectLoanedWithPage(@Param("pageSize") int pageSize, @Param("offset") int offset);

    int countLoanedLoans();
}
