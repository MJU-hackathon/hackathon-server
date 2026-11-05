package sopt.thingkerton.domain.diary.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import sopt.thingkerton.domain.diary.code.DiaryErrorCode;
import sopt.thingkerton.domain.diary.entity.Diary;
import sopt.thingkerton.domain.diary.entity.Outcome;
import sopt.thingkerton.domain.diary.repository.DiaryRepository;
import sopt.thingkerton.domain.profile.entity.AppLocale;
import sopt.thingkerton.domain.profile.service.ProfileService;
import sopt.thingkerton.domain.quest.entity.Quest;
import sopt.thingkerton.domain.quest.entity.QuestText;
import sopt.thingkerton.domain.quest.service.QuestService;
import sopt.thingkerton.domain.quest.service.QuestTextService;
import sopt.thingkerton.global.exception.BusinessException;
import sopt.thingkerton.global.infra.s3.S3Uploader;
import sopt.thingkerton.global.infra.s3.UploadedFile;

/**
 * 일기 저장·목록. 사진 업로드와 번역(LLM)을 트랜잭션 밖에서 하기 위해 클래스에 트랜잭션을 걸지 않는다.
 * 저장은 {@link DiaryWriter}가 한 트랜잭션으로 처리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryService {

    static final Set<String> PHOTO_TYPES = Set.of("image/jpeg", "image/png", "image/heic", "image/heif");
    private static final long MAX_PHOTO_BYTES = 10L * 1024 * 1024;
    private static final String PHOTO_DIR = "diaries/";

    private final DiaryRepository diaryRepository;
    private final DiaryWriter diaryWriter;
    private final QuestService questService;
    private final QuestTextService questTextService;
    private final ProfileService profileService;
    private final S3Uploader s3Uploader;
    private final Clock clock;

    /**
     * 일기를 저장하고 퀘스트를 RECORDED로 바꾼다. 같은 draftId로 다시 오면 새로 만들지 않고 기존 일기를 반환한다.
     */
    public LocalizedDiary create(String userKey, DiaryCreateCommand command) {
        Optional<Diary> duplicate = diaryRepository.findWithQuestByDraftId(command.draftId());
        if (duplicate.isPresent()) {
            return existing(userKey, duplicate.get());
        }
        validateMemo(command.memo());
        validatePhoto(command);

        Quest quest = questService.getOwnedQuest(userKey, command.questId());
        DiaryWriter.checkRecordable(quest, LocalDate.now(clock), command.draftStartedAt());
        String title = titleOf(userKey, quest);

        UploadedFile photo = s3Uploader.uploadImage(command.photo(), PHOTO_DIR + userKey, PHOTO_TYPES);
        try {
            Diary diary = diaryWriter.save(userKey, command, photo.key(), title);
            return new LocalizedDiary(diary, title, photo.url());
        } catch (DataIntegrityViolationException exception) {
            // 같은 draftId 동시 제출, 또는 다른 draftId로 같은 퀘스트가 먼저 기록됨
            s3Uploader.delete(photo.key());
            return diaryRepository.findWithQuestByDraftId(command.draftId())
                    .map(diary -> existing(userKey, diary))
                    .orElseThrow(() -> new BusinessException(DiaryErrorCode.ALREADY_RECORDED));
        } catch (RuntimeException exception) {
            s3Uploader.delete(photo.key());
            throw exception;
        }
    }

    /**
     * 작성 시각 최신순 일기와 경험 결과별 개수. 제목은 요청 언어(없으면 프로필 언어)로, 메모는 원문 그대로.
     */
    public DiaryListResult getDiaries(String userKey, AppLocale requestedLocale) {
        AppLocale locale = profileService.resolveLocale(userKey, requestedLocale);
        List<Diary> diaries = diaryRepository.findAllWithQuestByUserKey(userKey);
        Map<Long, QuestText> texts = questTextService.getTexts(
                diaries.stream().map(Diary::getQuest).toList(), locale);

        List<LocalizedDiary> items = diaries.stream()
                .map(diary -> {
                    QuestText text = texts.get(diary.getQuest().getId());
                    String title = text != null ? text.getTitle() : diary.getTitleSnapshot();
                    return new LocalizedDiary(diary, title, s3Uploader.getUrl(diary.getPhotoKey()));
                })
                .toList();
        long experienced = diaries.stream().filter(diary -> diary.getOutcome() == Outcome.EXPERIENCED).count();
        return new DiaryListResult(items, experienced, diaries.size() - experienced);
    }

    private LocalizedDiary existing(String userKey, Diary diary) {
        if (!diary.getUserKey().equals(userKey)) {
            throw new BusinessException(DiaryErrorCode.ALREADY_RECORDED);
        }
        return new LocalizedDiary(diary, diary.getTitleSnapshot(), s3Uploader.getUrl(diary.getPhotoKey()));
    }

    private String titleOf(String userKey, Quest quest) {
        AppLocale locale = profileService.resolveLocale(userKey, null);
        QuestText text = questTextService.getTexts(List.of(quest), locale).get(quest.getId());
        return text != null ? text.getTitle() : quest.getPlaceName();
    }

    private void validateMemo(String memo) {
        if (memo != null && memo.codePointCount(0, memo.length()) > Diary.MEMO_MAX_LENGTH) {
            throw new BusinessException(DiaryErrorCode.MEMO_TOO_LONG);
        }
    }

    private void validatePhoto(DiaryCreateCommand command) {
        String contentType = command.photo().getContentType();
        if (contentType == null || !PHOTO_TYPES.contains(contentType) || command.photo().getSize() > MAX_PHOTO_BYTES) {
            throw new BusinessException(DiaryErrorCode.INVALID_PHOTO);
        }
    }
}
