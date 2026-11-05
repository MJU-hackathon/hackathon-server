package sopt.thingkerton.domain.quest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.support.TransactionTemplate;
import sopt.thingkerton.domain.diary.entity.Diary;
import sopt.thingkerton.domain.diary.entity.Outcome;
import sopt.thingkerton.domain.diary.repository.DiaryRepository;
import sopt.thingkerton.domain.profile.entity.Interest;
import sopt.thingkerton.domain.quest.entity.DailyQuestSet;
import sopt.thingkerton.domain.quest.entity.PlaceActivityKey;
import sopt.thingkerton.domain.quest.entity.Quest;
import sopt.thingkerton.domain.quest.repository.DailyQuestSetRepository;
import sopt.thingkerton.domain.quest.repository.QuestRepository;
import sopt.thingkerton.support.FakeAiConfig;
import sopt.thingkerton.support.FakeQuestGenerator;
import sopt.thingkerton.support.FakeQuestTranslator;
import sopt.thingkerton.support.FixedClockConfig;
import sopt.thingkerton.support.MutableClock;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({FixedClockConfig.class, FakeAiConfig.class})
class QuestApiTest {

    private static final LocalDate TODAY = FixedClockConfig.NOW.toLocalDate();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private FakeQuestGenerator generator;
    @Autowired
    private FakeQuestTranslator translator;
    @Autowired
    private MutableClock clock;
    @Autowired
    private DailyQuestSetRepository dailyQuestSetRepository;
    @Autowired
    private QuestRepository questRepository;
    @Autowired
    private DiaryRepository diaryRepository;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @AfterEach
    void tearDown() {
        generator.reset();
        translator.reset();
        clock.reset();
    }

    @Test
    void 첫_ensure는_3개를_만들고_같은_날_재호출은_같은_퀘스트를_반환한다() throws Exception {
        String userKey = newUserWithProfile("ko");

        List<Integer> first = questIds(ensure(userKey, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ENSURE_DAILY_QUESTS"))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].title").value(org.hamcrest.Matchers.startsWith("[ko]")))
                .andExpect(jsonPath("$.data[0].imageUrl").value("https://cdn.example.com/quest-images/food.jpg")));

        // 다른 좌표로 재호출해도 새로 만들지 않는다
        List<Integer> second = questIds(mockMvc.perform(post("/api/daily-quests/ensure")
                        .header("X-User-Key", userKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"latitude\":37.0,\"longitude\":127.0}"))
                .andExpect(status().isOk()));

        assertThat(second).isEqualTo(first);
        assertThat(generator.commands()).hasSize(1);
        assertThat(generator.commands().getFirst().interests()).containsExactly(Interest.KPOP, Interest.FOOD);
    }

    @Test
    void 프로필이_없으면_ensure는_404_PROFILE_NOT_FOUND() throws Exception {
        ensure(UUID.randomUUID().toString(), null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROFILE_NOT_FOUND"));
    }

    @Test
    void 좌표_범위를_벗어나면_400() throws Exception {
        String userKey = newUserWithProfile("ko");
        mockMvc.perform(post("/api/daily-quests/ensure")
                        .header("X-User-Key", userKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"latitude\":91,\"longitude\":126.9}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void 동시에_ensure를_2번_호출해도_묶음은_1개만_생성된다() throws Exception {
        String userKey = newUserWithProfile("ko");
        generator.setDelayMillis(300);
        CountDownLatch start = new CountDownLatch(1);
        Callable<List<Integer>> call = () -> {
            start.await();
            return questIds(ensure(userKey, null).andExpect(status().isOk()));
        };

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<List<Integer>> a = executor.submit(call);
            Future<List<Integer>> b = executor.submit(call);
            start.countDown();

            assertThat(a.get()).hasSize(3).isEqualTo(b.get());
        } finally {
            executor.shutdown();
        }
        assertThat(dailyQuestSetRepository.findByUserKeyAndQuestDate(userKey, TODAY)).isPresent();
        assertThat(dailyQuestSetRepository.findAll().stream()
                .filter(set -> set.getUserKey().equals(userKey))).hasSize(1);
    }

    @Test
    void 패스하면_today에서_빠지고_보충하지_않는다() throws Exception {
        String userKey = newUserWithProfile("ko");
        List<Integer> ids = questIds(ensure(userKey, null));

        pass(userKey, ids.get(0))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PASS_QUEST"))
                .andExpect(jsonPath("$.data.status").value("SKIPPED"))
                .andExpect(jsonPath("$.data.remainingCount").value(2));

        List<Integer> today = questIds(today(userKey, null).andExpect(status().isOk()));
        assertThat(today).containsExactly(ids.get(1), ids.get(2));

        // 재호출해도 보충 없음, 이미 패스한 퀘스트는 409
        assertThat(questIds(ensure(userKey, null))).hasSize(2);
        pass(userKey, ids.get(0))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("QUEST_NOT_AVAILABLE"));
        mockMvc.perform(get("/api/quests/{id}", ids.get(0)).header("X-User-Key", userKey))
                .andExpect(jsonPath("$.data.status").value("SKIPPED"));
    }

    @Test
    void 모두_처리하면_today는_빈_배열_묶음이_없으면_404() throws Exception {
        String userKey = newUserWithProfile("ko");
        today(userKey, null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DAILY_SET_NOT_FOUND"));

        for (Integer id : questIds(ensure(userKey, null))) {
            pass(userKey, id).andExpect(status().isOk());
        }

        today(userKey, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void locale을_바꾸면_같은_questId가_새_언어로_나오고_번역은_캐시된다() throws Exception {
        String userKey = newUserWithProfile("ko");
        List<Integer> ids = questIds(ensure(userKey, null));

        List<Integer> enIds = questIds(today(userKey, "en")
                .andExpect(jsonPath("$.data[0].title").value(org.hamcrest.Matchers.startsWith("[en]")))
                .andExpect(jsonPath("$.data[0].reason").value("[en] reason")));
        today(userKey, "en").andExpect(status().isOk());

        assertThat(enIds).isEqualTo(ids);
        assertThat(translator.calls()).isEqualTo(1);
        assertThat(generator.commands()).hasSize(1);

        mockMvc.perform(get("/api/quests/{id}", ids.get(0)).header("X-User-Key", userKey).param("locale", "vi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_QUEST_DETAIL"))
                .andExpect(jsonPath("$.data.locale").value("vi"))
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.data.steps.length()").value(3))
                .andExpect(jsonPath("$.data.steps[0]").value("[vi] step1"))
                .andExpect(jsonPath("$.data.description").value("[vi] description"));
    }

    @Test
    void 지원하지_않는_locale은_400() throws Exception {
        String userKey = newUserWithProfile("ko");
        ensure(userKey, null);
        today(userKey, "fr")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_LOCALE"));
    }

    @Test
    void 다른_사용자의_퀘스트는_404() throws Exception {
        String owner = newUserWithProfile("ko");
        String other = newUserWithProfile("ko");
        Integer questId = questIds(ensure(owner, null)).getFirst();

        mockMvc.perform(get("/api/quests/{id}", questId).header("X-User-Key", other))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("QUEST_NOT_FOUND"));
        pass(other, questId)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("QUEST_NOT_FOUND"));
    }

    @Test
    void 다음_날이_되면_전날_퀘스트는_EXPIRED이고_패스할_수_없다() throws Exception {
        String userKey = newUserWithProfile("ko");
        Integer questId = questIds(ensure(userKey, null)).getFirst();

        clock.set(FixedClockConfig.NOW.plusDays(1).withHour(0).withMinute(0));

        mockMvc.perform(get("/api/quests/{id}", questId).header("X-User-Key", userKey))
                .andExpect(jsonPath("$.data.status").value("EXPIRED"));
        pass(userKey, questId)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("QUEST_NOT_AVAILABLE"));
        today(userKey, null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DAILY_SET_NOT_FOUND"));
    }

    @Test
    void 다음_날_생성_시_EXPERIENCED_조합만_제외_목록에_들어간다() throws Exception {
        String userKey = newUserWithProfile("ko");
        List<Integer> ids = questIds(ensure(userKey, null));
        PlaceActivityKey experienced = recordDiary(userKey, ids.get(0), "draft-" + UUID.randomUUID(), Outcome.EXPERIENCED);
        PlaceActivityKey notExperienced = recordDiary(userKey, ids.get(1), "draft-" + UUID.randomUUID(),
                Outcome.NOT_EXPERIENCED);

        clock.set(FixedClockConfig.NOW.plusDays(1));
        ensure(userKey, null).andExpect(status().isOk());

        assertThat(generator.commands()).hasSize(2);
        assertThat(generator.commands().get(1).excludedKeys())
                .contains(experienced)
                .doesNotContain(notExperienced);
    }

    @Test
    void 생성_결과에_제외_조합이_섞이면_데모로_대체한다() throws Exception {
        String userKey = newUserWithProfile("ko");
        List<Integer> ids = questIds(ensure(userKey, null));
        PlaceActivityKey experienced = recordDiary(userKey, ids.get(0), "draft-" + UUID.randomUUID(), Outcome.EXPERIENCED);

        generator.setFixedResult(List.of(
                FakeQuestGenerator.quest(experienced.placeKey(), Interest.FOOD, null),
                FakeQuestGenerator.quest("fresh-b", Interest.KPOP, null),
                FakeQuestGenerator.quest("fresh-c", Interest.GAME, null)));
        clock.set(FixedClockConfig.NOW.plusDays(1));
        ensure(userKey, null).andExpect(jsonPath("$.data.length()").value(3));

        DailyQuestSet set = dailyQuestSetRepository.findByUserKeyAndQuestDate(userKey, TODAY.plusDays(1)).orElseThrow();
        assertThat(set.isFallback()).isTrue();
    }

    @Test
    void LLM이_실패하면_데모_퀘스트_3개로_대체하고_is_fallback이_true() throws Exception {
        String userKey = newUserWithProfile("en");
        generator.setFailing(true);

        ensure(userKey, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].placeName").value("영천시장"));

        DailyQuestSet set = dailyQuestSetRepository.findByUserKeyAndQuestDate(userKey, TODAY).orElseThrow();
        assertThat(set.isFallback()).isTrue();
    }

    @Test
    void 생성이_제한_시간을_넘기면_기다리지_않고_데모_퀘스트로_대체한다() throws Exception {
        String userKey = newUserWithProfile("ko");
        generator.setDelayMillis(3_000);

        long start = System.currentTimeMillis();
        ensure(userKey, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].placeName").value("영천시장"));
        long elapsed = System.currentTimeMillis() - start;

        assertThat(elapsed).isLessThan(2_500);
        assertThat(dailyQuestSetRepository.findByUserKeyAndQuestDate(userKey, TODAY).orElseThrow().isFallback())
                .isTrue();
    }

    @Test
    void 번역이_제한_시간을_넘기면_원문을_반환하고_다음_요청에서_다시_번역한다() throws Exception {
        String userKey = newUserWithProfile("ko");
        ensure(userKey, null);
        translator.setDelayMillis(3_000);

        today(userKey, "en")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value(org.hamcrest.Matchers.startsWith("[ko]")));

        translator.setDelayMillis(0);
        today(userKey, "en")
                .andExpect(jsonPath("$.data[0].title").value(org.hamcrest.Matchers.startsWith("[en]")));
        assertThat(translator.calls()).isEqualTo(2);
    }

    private PlaceActivityKey recordDiary(String userKey, Integer questId, String draftId, Outcome outcome) {
        return transactionTemplate.execute(status -> {
            Quest quest = questRepository.findWithSetById(questId.longValue()).orElseThrow();
            quest.record(LocalDateTime.now(clock));
            diaryRepository.save(Diary.create(userKey, quest, draftId, "title", "diaries/x.jpg", outcome, null,
                    LocalDateTime.now(clock)));
            return new PlaceActivityKey(quest.getPlaceKey(), quest.getActivityKey());
        });
    }

    private String newUserWithProfile(String locale) throws Exception {
        String userKey = UUID.randomUUID().toString();
        mockMvc.perform(put("/api/profile")
                        .header("X-User-Key", userKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname":"하나","locale":"%s","birthDate":"2003-04-12",
                                 "stayStartDate":"2026-03-01","stayEndDate":"2027-02-28","interests":["KPOP","FOOD"]}
                                """.formatted(locale)))
                .andExpect(status().isOk());
        return userKey;
    }

    private ResultActions ensure(String userKey, String locale) throws Exception {
        String localeField = locale == null ? "" : ",\"locale\":\"" + locale + "\"";
        return mockMvc.perform(post("/api/daily-quests/ensure")
                .header("X-User-Key", userKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"latitude\":37.5718,\"longitude\":126.9620" + localeField + "}"));
    }

    private ResultActions today(String userKey, String locale) throws Exception {
        var request = get("/api/daily-quests/today").header("X-User-Key", userKey);
        if (locale != null) {
            request.param("locale", locale);
        }
        return mockMvc.perform(request);
    }

    private ResultActions pass(String userKey, Integer questId) throws Exception {
        return mockMvc.perform(post("/api/quests/{id}/pass", questId).header("X-User-Key", userKey));
    }

    private List<Integer> questIds(ResultActions actions) throws Exception {
        String body = actions.andReturn().getResponse().getContentAsString();
        return new ArrayList<>(JsonPath.read(body, "$.data[*].questId"));
    }
}
