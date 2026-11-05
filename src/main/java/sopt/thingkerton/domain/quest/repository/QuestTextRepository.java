package sopt.thingkerton.domain.quest.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sopt.thingkerton.domain.quest.entity.QuestText;
import sopt.thingkerton.domain.quest.entity.QuestTextId;

public interface QuestTextRepository extends JpaRepository<QuestText, QuestTextId> {

    List<QuestText> findAllByQuestIdIn(Collection<Long> questIds);

    @Modifying(clearAutomatically = true)
    @Query("delete from QuestText t where t.questId in :questIds")
    void deleteAllByQuestIdIn(@Param("questIds") Collection<Long> questIds);
}
