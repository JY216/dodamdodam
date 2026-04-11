package dev.yeonlog.dodamdodam.mappers;

import dev.yeonlog.dodamdodam.dtos.LoanedBookDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LoanHistoryMapper {
    List<LoanedBookDto> findLoanedBooksByUserId(@Param("userId") String userId);
}
