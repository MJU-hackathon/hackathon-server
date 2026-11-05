package sopt.thingkerton.domain.quest.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sopt.thingkerton.domain.profile.entity.AppLocale;
import sopt.thingkerton.domain.profile.entity.Interest;
import sopt.thingkerton.domain.quest.entity.DailyQuestSet;
import sopt.thingkerton.domain.quest.entity.Quest;
import sopt.thingkerton.domain.quest.entity.QuestText;
import sopt.thingkerton.domain.quest.repository.DailyQuestSetRepository;
import sopt.thingkerton.domain.quest.repository.QuestRepository;
import sopt.thingkerton.domain.quest.repository.QuestTextRepository;

/**
 * 오늘 묶음과 퀘스트·설명을 한 트랜잭션으로 저장한다.
 * <p>
 * 같은 사용자·날짜 묶음이 이미 있으면 {@code UNIQUE(user_key, quest_date)} 위반으로
 * {@link org.springframework.dao.DataIntegrityViolationException}이 발생하고 전체가 롤백된다.
 */
@Service
@RequiredArgsConstructor
public class DailyQuestWriter {

    private final DailyQuestSetRepository dailyQuestSetRepository;
    private final QuestRepository questRepository;
    private final QuestTextRepository questTextRepository;
    private final QuestImageResolver questImageResolver;
    private final Clock clock;

    @Transactional
    public DailyQuestSet save(String userKey, LocalDate questDate, BigDecimal latitude, BigDecimal longitude,
                              List<Interest> interests, boolean isFallback, List<GeneratedQuest> quests,
                              AppLocale textLocale) {
        DailyQuestSet set = dailyQuestSetRepository.saveAndFlush(
                DailyQuestSet.create(userKey, questDate, latitude, longitude, interests, isFallback));
        LocalDateTime now = LocalDateTime.now(clock);
        for (GeneratedQuest generated : quests) {
            Quest quest = questRepository.save(Quest.create(set,
                    generated.key().placeKey(),
                    generated.key().activityKey(),
                    generated.category(),
                    generated.estimatedMinutes(),
                    generated.placeName(),
                    questImageResolver.resolveRef(generated.category(), generated.key().activityKey()),
                    now));
            questTextRepository.save(QuestText.create(quest.getId(), textLocale, generated.title(),
                    generated.reason(), generated.description(), generated.steps()));
        }
        return set;
    }
}
