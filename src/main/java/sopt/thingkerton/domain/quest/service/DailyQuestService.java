package sopt.thingkerton.domain.quest.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import sopt.thingkerton.domain.diary.repository.DiaryRepository;
import sopt.thingkerton.domain.profile.entity.AppLocale;
import sopt.thingkerton.domain.profile.entity.Interest;
import sopt.thingkerton.domain.profile.entity.Profile;
import sopt.thingkerton.domain.profile.service.ProfileService;
import sopt.thingkerton.domain.quest.code.QuestErrorCode;
import sopt.thingkerton.domain.quest.entity.DailyQuestSet;
import sopt.thingkerton.domain.quest.entity.PlaceActivityKey;
import sopt.thingkerton.domain.quest.entity.Quest;
import sopt.thingkerton.domain.quest.entity.QuestStatus;
import sopt.thingkerton.domain.quest.entity.QuestText;
import sopt.thingkerton.domain.quest.repository.DailyQuestSetRepository;
import sopt.thingkerton.domain.quest.repository.QuestRepository;
import sopt.thingkerton.global.exception.BusinessException;
import sopt.thingkerton.global.infra.ai.AiTimeLimiter;

/**
 * 오늘의 퀘스트. 사용자·날짜(Asia/Seoul)당 묶음 1개, 퀘스트 3개이며 보충하지 않는다.
 * <p>
 * LLM 호출을 트랜잭션 밖에서 하기 위해 클래스에 트랜잭션을 걸지 않는다.
 * 저장은 {@link DailyQuestWriter}, 조회는 Repository 트랜잭션으로 처리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyQuestService {

    private final ProfileService profileService;
    private final DailyQuestSetRepository dailyQuestSetRepository;
    private final QuestRepository questRepository;
    private final DiaryRepository diaryRepository;
    private final QuestGenerator questGenerator;
    private final DemoQuestProvider demoQuestProvider;
    private final DailyQuestWriter dailyQuestWriter;
    private final QuestTextService questTextService;
    private final QuestImageResolver questImageResolver;
    private final AiTimeLimiter aiTimeLimiter;
    private final QuestProperties questProperties;
    private final QuestGeocoder questGeocoder;
    private final QuestImageAttacher questImageAttacher;
    private final Clock clock;

    /**
     * 오늘 묶음이 없으면 요청 좌표와 현재 관심사로 생성하고, 있으면 좌표·관심사가 달라도 기존 묶음의 남은 퀘스트를 반환한다.
     */
    public List<LocalizedQuest> ensure(String userKey, BigDecimal latitude, BigDecimal longitude,
                                       AppLocale requestedLocale) {
        Profile profile = profileService.getProfile(userKey);
        AppLocale locale = requestedLocale != null ? requestedLocale : profile.getLocale();
        LocalDate today = LocalDate.now(clock);

        if (dailyQuestSetRepository.findByUserKeyAndQuestDate(userKey, today).isEmpty()) {
            createDailySet(profile, today, latitude, longitude, locale);
        }
        return getRemaining(findTodaySet(userKey, today), today, locale);
    }

    /**
     * 오늘 묶음의 남은(AVAILABLE) 퀘스트. 묶음이 없으면 {@code 404 DAILY_SET_NOT_FOUND}, 다 처리했으면 빈 목록.
     */
    public List<LocalizedQuest> getToday(String userKey, AppLocale requestedLocale) {
        AppLocale locale = profileService.resolveLocale(userKey, requestedLocale);
        LocalDate today = LocalDate.now(clock);
        return getRemaining(findTodaySet(userKey, today), today, locale);
    }

    private void createDailySet(Profile profile, LocalDate today, BigDecimal latitude, BigDecimal longitude,
                                AppLocale locale) {
        long startedAt = System.nanoTime();
        List<Interest> interests = profile.getInterests().stream().sorted().toList();
        Set<PlaceActivityKey> excludedKeys = new HashSet<>(diaryRepository.findExperiencedKeys(profile.getUserKey()));

        List<GeneratedQuest> quests = generate(
                new QuestGenerationCommand(latitude, longitude, interests, excludedKeys, locale));
        boolean isFallback = quests == null;
        AppLocale textLocale = locale;
        if (isFallback) {
            quests = demoQuestProvider.getDemoQuests();
            textLocale = DemoQuestProvider.DEMO_LOCALE;
        }
        // 대표 이미지 AI 생성과 장소 주소 → 좌표 변환은 서로 무관하므로 동시에 실행한다 (응답 시간 = 둘 중 긴 쪽)
        // 퀘스트 생성 후 마감까지 남은 시간을 쓴다 (각 상한을 넘지 않음). 생성이 빨리 끝나면 이미지에 시간을 더 준다
        // 이미지: 데모 퀘스트는 생성하지 않음, 실패하면 카테고리 기본 이미지 / 좌표: 실패하면 좌표 없이 저장
        Duration remaining = questProperties.ensureDeadline().minusNanos(System.nanoTime() - startedAt);
        Duration imageTimeout = capped(questProperties.imageGenerationTimeout(), remaining);
        CompletableFuture<List<GeneratedQuest>> withImages = isFallback
                ? CompletableFuture.completedFuture(quests)
                : questImageAttacher.attachImagesAsync(quests, imageTimeout);
        List<GeneratedQuest> withCoordinates = questGeocoder.attachCoordinates(quests,
                capped(questProperties.geocodingTimeout(), remaining));
        quests = mergeImageRefs(withCoordinates, withImages.join());

        try {
            dailyQuestWriter.save(profile.getUserKey(), today, latitude, longitude, interests, isFallback, quests,
                    textLocale);
        } catch (DataIntegrityViolationException exception) {
            // 동시 요청으로 다른 요청이 먼저 오늘 묶음을 만들었다. 그 묶음을 사용한다.
            log.info("Daily quest set already created concurrently. userKey={}", profile.getUserKey());
        }
    }

    /**
     * @return {@code min(limit, remaining)}. 마감이 지났으면 0
     */
    private static Duration capped(Duration limit, Duration remaining) {
        if (remaining.isNegative()) {
            return Duration.ZERO;
        }
        return limit.compareTo(remaining) <= 0 ? limit : remaining;
    }

    /**
     * 좌표를 붙인 목록에 이미지 참조를 합친다. 두 목록은 같은 퀘스트를 같은 순서로 담고 있다.
     */
    private List<GeneratedQuest> mergeImageRefs(List<GeneratedQuest> withCoordinates, List<GeneratedQuest> withImages) {
        return IntStream.range(0, withCoordinates.size())
                .mapToObj(i -> withCoordinates.get(i).withImageRef(withImages.get(i).imageRef()))
                .toList();
    }

    /**
     * @return 제외 조합을 걸러낸 퀘스트 3개. 생성 실패·시간 초과·개수 부족이면 null (데모로 대체)
     */
    private List<GeneratedQuest> generate(QuestGenerationCommand command) {
        long startedAt = System.currentTimeMillis();
        try {
            List<GeneratedQuest> quests = aiTimeLimiter.call(() -> questGenerator.generate(command),
                            questProperties.generationTimeout()).stream()
                    .filter(quest -> !command.excludedKeys().contains(quest.key()))
                    .toList();
            // 제한 시간(QUEST_GENERATION_TIMEOUT) 조정용
            log.info("Quest generation took {}ms", System.currentTimeMillis() - startedAt);
            if (quests.size() != QuestGenerator.QUEST_COUNT) {
                log.warn("Quest generation returned {} usable quests. Falling back to demo.", quests.size());
                return null;
            }
            return quests;
        } catch (RuntimeException exception) {
            log.warn("Quest generation failed after {}ms. Falling back to demo.",
                    System.currentTimeMillis() - startedAt, exception);
            return null;
        }
    }

    private DailyQuestSet findTodaySet(String userKey, LocalDate today) {
        return dailyQuestSetRepository.findByUserKeyAndQuestDate(userKey, today)
                .orElseThrow(() -> new BusinessException(QuestErrorCode.DAILY_SET_NOT_FOUND));
    }

    private List<LocalizedQuest> getRemaining(DailyQuestSet set, LocalDate today, AppLocale locale) {
        List<Quest> quests = questRepository.findAllWithSetByDailySetIdAndStatus(set.getId(), QuestStatus.AVAILABLE);
        Map<Long, QuestText> texts = questTextService.getTexts(quests, locale);
        return quests.stream()
                .map(quest -> new LocalizedQuest(quest, texts.get(quest.getId()),
                        questImageResolver.toUrl(quest.getImageRef()), quest.currentStatus(today)))
                .toList();
    }
}
