package sopt.thingkerton.domain.dev.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sopt.thingkerton.domain.diary.repository.DiaryRepository;
import sopt.thingkerton.domain.quest.entity.DailyQuestSet;
import sopt.thingkerton.domain.quest.repository.DailyQuestSetRepository;
import sopt.thingkerton.domain.quest.repository.QuestRepository;
import sopt.thingkerton.domain.quest.repository.QuestTextRepository;

/**
 * 개발·QA용 데이터 초기화. {@code app.dev.reset-enabled=true}일 때만 API가 노출된다.
 */
@Service
@RequiredArgsConstructor
public class DevResetService {

    private final DailyQuestSetRepository dailyQuestSetRepository;
    private final QuestRepository questRepository;
    private final QuestTextRepository questTextRepository;
    private final DiaryRepository diaryRepository;
    private final Clock clock;

    /**
     * 오늘(Asia/Seoul) 묶음과 그 퀘스트·번역·일기를 삭제한다. 묶음이 없으면 아무것도 하지 않는다.
     * 다른 날짜의 일기(중복 추천 제외 목록)와 S3 사진은 건드리지 않는다.
     *
     * @return 삭제한 퀘스트 수
     */
    @Transactional
    public int resetToday(String userKey) {
        LocalDate today = LocalDate.now(clock);
        return dailyQuestSetRepository.findByUserKeyAndQuestDate(userKey, today)
                .map(this::deleteSet)
                .orElse(0);
    }

    private int deleteSet(DailyQuestSet set) {
        List<Long> questIds = questRepository.findIdsByDailySetId(set.getId());
        if (!questIds.isEmpty()) {
            diaryRepository.deleteAllByQuestIdIn(questIds);
            questTextRepository.deleteAllByQuestIdIn(questIds);
            questRepository.deleteAllByIdIn(questIds);
        }
        dailyQuestSetRepository.deleteById(set.getId());
        dailyQuestSetRepository.flush();
        return questIds.size();
    }
}
