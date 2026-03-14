package dev.yeonlog.dodamdodam.mappers;

import dev.yeonlog.dodamdodam.entities.EmailTokenEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EmailTokenMapper {
    int insert(@Param(value = "emailToken")EmailTokenEntity emailToken);

    EmailTokenEntity select(@Param(value = "email") String email,
                            @Param(value = "code") String code,
                            @Param(value = "salt") String salt);

    int update(@Param(value = "emailToken") EmailTokenEntity emailToken);
}
