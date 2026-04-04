package dev.yeonlog.dodamdodam.mappers;

import dev.yeonlog.dodamdodam.entities.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    int insert (UserEntity user);

    UserEntity selectByUserId(@Param("userId") String userId);

    UserEntity selectByEmail(@Param("email") String email);

    void updatePassword(@Param("userId") String userId, @Param("password") String password);
}
