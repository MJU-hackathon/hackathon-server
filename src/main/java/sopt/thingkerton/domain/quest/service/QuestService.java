package sopt.thingkerton.domain.quest.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sopt.thingkerton.domain.profile.entity.AppLocale;
import sopt.thingkerton.domain.profile.service.ProfileService;
import sopt.thingkerton.domain.quest.code.QuestErrorCode;
import sopt.thingkerton.domain.quest.entity.Quest;
import sopt.thingkerton.domain.quest.entity.QuestStatus;
import sopt.thingkerton.domain.quest.entity.QuestText;
import sopt.thingkerton.domain.quest.repository.QuestRepository;
import sopt.thingkerton.global.exception.BusinessException;

/**
 * 퀘스트 단건 조회·패스. 본인 묶음의 퀘스트만 다룰 수 있다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestService {

    private final QuestRepository questRepository;
    private final ProfileService profileService;
    private final QuestTextService questTextService;
    private final QuestImageResolver questImageResolver;
    private final Clock clock;

    /**
     * 번역이 필요할 수 있어(LLM 호출) 트랜잭션 밖에서 실행한다.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public LocalizedQuest getDetail(String userKey, Long questId, AppLocale requestedLocale) {
        Quest quest = getOwnedQuest(userKey, questId);
        AppLocale locale = profileService.resolveLocale(userKey, requestedLocale);
        QuestText text = questTextService.getTexts(List.of(quest), locale).get(quest.getId());
        return new LocalizedQuest(quest, text, questImageResolver.toUrl(quest.getImageRef()),
                quest.currentStatus(LocalDate.now(clock)));
    }

    /**
     * 상태를 SKIPPED로 바꾼다. 새 퀘스트를 보충하지 않고 일기도 만들지 않는다.
     */
    @Transactional
    public PassResult pass(String userKey, Long questId) {
        Quest quest = getOwnedQuest(userKey, questId);
        if (!quest.isAvailable(LocalDate.now(clock))) {
            throw new BusinessException(QuestErrorCode.QUEST_NOT_AVAILABLE);
        }
        quest.skip(LocalDateTime.now(clock));
        long remaining = questRepository.countByDailySetIdAndStatus(quest.getDailySet().getId(), QuestStatus.AVAILABLE);
        return new PassResult(quest, remaining);
    }

    /**
     * 묶음까지 로딩한 본인 퀘스트. 없거나 다른 사용자 것이면 {@code 404 QUEST_NOT_FOUND}.
     */
    public Quest getOwnedQuest(String userKey, Long questId) {
        return questRepository.findWithSetById(questId)
                .filter(quest -> quest.getDailySet().isOwnedBy(userKey))
                .orElseThrow(() -> new BusinessException(QuestErrorCode.QUEST_NOT_FOUND));
    }
}
