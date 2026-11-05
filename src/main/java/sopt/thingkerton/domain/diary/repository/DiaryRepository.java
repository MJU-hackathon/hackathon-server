package sopt.thingkerton.domain.diary.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sopt.thingkerton.domain.diary.entity.Diary;
import sopt.thingkerton.domain.quest.entity.PlaceActivityKey;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    @Query("select d from Diary d join fetch d.quest where d.draftId = :draftId")
    Optional<Diary> findWithQuestByDraftId(@Param("draftId") String draftId);

    @Query("select d from Diary d join fetch d.quest where d.userKey = :userKey order by d.createdAt desc, d.id desc")
    List<Diary> findAllWithQuestByUserKey(@Param("userKey") String userKey);

    /**
     * 사용자가 실제로 경험한(EXPERIENCED) 장소+활동 조합. 다음 퀘스트 생성에서 제외한다.
     */
    @Query("""
            select distinct new sopt.thingkerton.domain.quest.entity.PlaceActivityKey(d.placeKey, d.activityKey)
            from Diary d
            where d.userKey = :userKey
              and d.outcome = sopt.thingkerton.domain.diary.entity.Outcome.EXPERIENCED
            """)
    List<PlaceActivityKey> findExperiencedKeys(@Param("userKey") String userKey);
}
