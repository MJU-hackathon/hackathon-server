package sopt.thingkerton.domain.diary.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sopt.thingkerton.domain.diary.code.DiaryErrorCode;
import sopt.thingkerton.domain.diary.entity.Diary;
import sopt.thingkerton.domain.diary.repository.DiaryRepository;
import sopt.thingkerton.domain.quest.code.QuestErrorCode;
import sopt.thingkerton.domain.quest.entity.Quest;
import sopt.thingkerton.domain.quest.entity.QuestStatus;
import sopt.thingkerton.domain.quest.service.QuestService;
import sopt.thingkerton.global.exception.BusinessException;

/**
 * 일기 생성과 퀘스트 RECORDED 전환을 한 트랜잭션으로 처리한다.
 */
@Service
@RequiredArgsConstructor
public class DiaryWriter {

    private final DiaryRepository diaryRepository;
    private final QuestService questService;
    private final Clock clock;

    @Transactional
    public Diary save(String userKey, DiaryCreateCommand command, String photoKey, String titleSnapshot) {
        Quest quest = questService.getOwnedQuest(userKey, command.questId());
        checkRecordable(quest, LocalDate.now(clock), command.draftStartedAt());
        quest.record(LocalDateTime.now(clock));
        return diaryRepository.saveAndFlush(Diary.create(userKey, quest, command.draftId(), titleSnapshot, photoKey,
                command.outcome(), command.memo(), command.draftStartedAt()));
    }

    /**
     * 이미 기록됐으면 {@code 409 ALREADY_RECORDED}, 패스됐거나 자정 예외에 해당하지 않는 종료 퀘스트면
     * {@code 409 QUEST_NOT_AVAILABLE}.
     */
    public static void checkRecordable(Quest quest, LocalDate today, LocalDateTime draftStartedAt) {
        if (quest.getStatus() == QuestStatus.RECORDED) {
            throw new BusinessException(DiaryErrorCode.ALREADY_RECORDED);
        }
        if (!quest.isRecordable(today, draftStartedAt)) {
            throw new BusinessException(QuestErrorCode.QUEST_NOT_AVAILABLE);
        }
    }
}
