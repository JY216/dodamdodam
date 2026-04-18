package dev.yeonlog.dodamdodam.mappers;

import dev.yeonlog.dodamdodam.entities.JournalEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JournalMapper {
    List<JournalEntity> findAllByUserId(@Param("userId") String userId);

    JournalEntity findById(@Param("id") Long id, @Param("userId") String userId);

    void insert(JournalEntity journal);

    void deleteById(@Param("id") Long id, @Param("userId") String userId);

    Integer findGoal(@Param("userId") String userId);

    void upsertGoal(@Param("userId") String userId, @Param("goalCount") int goalCount);

    int countDoneByUserId(@Param("userId") String userId);
}
