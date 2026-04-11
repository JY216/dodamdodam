package dev.yeonlog.dodamdodam.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserStatusMapper {
    String findStatusByUserId(@Param("userId") String userId);

    void updateStatus(@Param("userId") String userId, @Param("status") String status);
}
