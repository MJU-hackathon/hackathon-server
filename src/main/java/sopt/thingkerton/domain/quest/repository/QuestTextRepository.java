package sopt.thingkerton.domain.quest.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import sopt.thingkerton.domain.quest.entity.QuestText;
import sopt.thingkerton.domain.quest.entity.QuestTextId;

public interface QuestTextRepository extends JpaRepository<QuestText, QuestTextId> {

    List<QuestText> findAllByQuestIdIn(Collection<Long> questIds);
}
