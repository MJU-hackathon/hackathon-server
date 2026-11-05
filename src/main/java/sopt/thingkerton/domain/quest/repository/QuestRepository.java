package sopt.thingkerton.domain.quest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sopt.thingkerton.domain.quest.entity.Quest;

public interface QuestRepository extends JpaRepository<Quest, Long> {
}
