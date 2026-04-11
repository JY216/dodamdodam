package dev.yeonlog.dodamdodam.mappers;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DashboardMapper {
    // 회원
    int countAllUsers();
    int countTodayUsers();
    int countMonthlyUsers();
    int countSuspendedUsers();

    // 도서
    int countAllBooks();
    int countNewBooks();

    // 대출
    int countActiveLoans();

    // 행사
    int countActiveEvents();
}
