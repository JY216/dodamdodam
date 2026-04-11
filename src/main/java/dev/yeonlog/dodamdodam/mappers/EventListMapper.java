package dev.yeonlog.dodamdodam.mappers;

import dev.yeonlog.dodamdodam.dtos.EventApplicantDto;
import dev.yeonlog.dodamdodam.dtos.EventListDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EventListMapper {
    List<EventListDto> findAll(@Param("offset") int offset, @Param(("limit")) int limit);

    int countAll();

    List<EventApplicantDto> findApplicantsByEventId(@Param("eventId") Long eventId);
}
