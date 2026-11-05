package sopt.thingkerton.domain.quest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sopt.thingkerton.domain.quest.entity.QuestText;
import sopt.thingkerton.domain.quest.entity.QuestTextId;

public interface QuestTextRepository extends JpaRepository<QuestText, QuestTextId> {
}
