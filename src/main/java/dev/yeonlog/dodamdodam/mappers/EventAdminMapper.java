package dev.yeonlog.dodamdodam.mappers;

import dev.yeonlog.dodamdodam.dtos.EventAdminDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EventAdminMapper {
    List<EventAdminDto> findAll(@Param("offset") int offset, @Param("limit") int limit);

    int countAll();

    void updateStatus(@Param("id") Long id, @Param("status") String status);

    void deleteById(@Param("id") Long id);


}
