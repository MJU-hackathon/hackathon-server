package sopt.thingkerton.domain.quest.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sopt.thingkerton.domain.quest.entity.Quest;
import sopt.thingkerton.domain.quest.entity.QuestStatus;

public interface QuestRepository extends JpaRepository<Quest, Long> {

    @Query("select q from Quest q join fetch q.dailySet where q.id = :id")
    Optional<Quest> findWithSetById(@Param("id") Long id);

    @Query("select q from Quest q join fetch q.dailySet where q.dailySet.id = :setId and q.status = :status order by q.id")
    List<Quest> findAllWithSetByDailySetIdAndStatus(@Param("setId") Long setId, @Param("status") QuestStatus status);

    long countByDailySetIdAndStatus(Long dailySetId, QuestStatus status);

    @Query("select q.id from Quest q where q.dailySet.id = :setId")
    List<Long> findIdsByDailySetId(@Param("setId") Long setId);

    @Modifying(clearAutomatically = true)
    @Query("delete from Quest q where q.id in :ids")
    void deleteAllByIdIn(@Param("ids") Collection<Long> ids);
}
