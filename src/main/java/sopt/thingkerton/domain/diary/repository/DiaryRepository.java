package sopt.thingkerton.domain.diary.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sopt.thingkerton.domain.diary.entity.Diary;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    Optional<Diary> findByDraftId(String draftId);
}
