package dev.yeonlog.dodamdodam.mappers;

import dev.yeonlog.dodamdodam.entities.NoticeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoticeMapper {
    void insertNotice(NoticeEntity notice);

    List<NoticeEntity> selectAllNotices();
    NoticeEntity selectById(@Param("id") Long id);

    void updateNotice(NoticeEntity notice);

    void deleteNotice(@Param("id") Long id);

    List<NoticeEntity> selectPinnedNotices();
}
