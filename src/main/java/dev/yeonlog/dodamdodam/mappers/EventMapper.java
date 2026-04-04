package dev.yeonlog.dodamdodam.mappers;

import dev.yeonlog.dodamdodam.entities.EventApplicationEntity;
import dev.yeonlog.dodamdodam.entities.EventEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EventMapper {
    // 행사 등록
    void insertEvent(EventEntity event);

    // 행사 목록
    List<EventEntity> selectAllEvents();

    // 행사 상세
    EventEntity selectEventById(@Param("id") Long id);

    // 행사 수정
    void updateEvent(EventEntity event);

    // 행사 삭제
    void deleteEvent(@Param("id") Long id);

    // 행사 신청
    void insertApplication(EventApplicationEntity application);

    // 신청 취소
    void deleteApplication(@Param("eventId") Long eventId,
                           @Param("userId") String userId);

    // 신청 여부 확인
    int countApplication(@Param("eventId") Long eventId,
                         @Param("userId") String userId);

    // 행사별 신청자 수
    int countApplicationsByEventId(@Param("eventId") Long eventId);

    // 행사별 신청자 목록 (관리자용)
    List<EventApplicationEntity> selectApplicationsByEventId(@Param("eventId") Long eventId);

    List<EventApplicationEntity> selectApplicationsByUserId(@Param("userId") String userId);

}
