package dev.yeonlog.dodamdodam.mappers;

import dev.yeonlog.dodamdodam.dtos.UserAdminDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserAdminMapper {
    List<UserAdminDto> findAll(@Param("name") String name, @Param("birth") String birth, @Param("mobile") String mobile, @Param("offset") int offset, @Param("limit") int limit);

    int countAll(@Param("name") String name, @Param("birth") String birth, @Param("mobile") String mobile);
}
