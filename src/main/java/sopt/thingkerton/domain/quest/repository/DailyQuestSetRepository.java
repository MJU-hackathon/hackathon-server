package sopt.thingkerton.domain.quest.repository;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sopt.thingkerton.domain.quest.entity.DailyQuestSet;

public interface DailyQuestSetRepository extends JpaRepository<DailyQuestSet, Long> {

    Optional<DailyQuestSet> findByUserKeyAndQuestDate(String userKey, LocalDate questDate);
}
