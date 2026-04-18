package dev.yeonlog.dodamdodam.mappers;

import dev.yeonlog.dodamdodam.entities.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    int insert (UserEntity user);

    UserEntity selectByUserId(@Param("userId") String userId);

    int countByEmail(@Param("email") String email); // 이메일 중복 체크용

    UserEntity selectByEmail(@Param("email") String email);

    void updatePassword(@Param("userId") String userId, @Param("password") String password);

    void updateProfile(UserEntity user);
}
