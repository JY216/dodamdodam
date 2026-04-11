package dev.yeonlog.dodamdodam.mappers;

import dev.yeonlog.dodamdodam.dtos.UserEventDto;
import dev.yeonlog.dodamdodam.dtos.UserLoanDto;
import dev.yeonlog.dodamdodam.dtos.UserOverdueDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserActivityMapper {
    List<UserLoanDto> findLoansByUserId(@Param("userId") String userId);
    UserOverdueDto findOverdueByUserId(@Param("userId") String userId);
    List<UserEventDto> findEventsByUserId(@Param("userId") String userId);
}
