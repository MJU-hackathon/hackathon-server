package sopt.thingkerton.domain.dev;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import sopt.thingkerton.domain.quest.entity.Quest;
import sopt.thingkerton.domain.quest.repository.DailyQuestSetRepository;
import sopt.thingkerton.domain.quest.repository.QuestRepository;
import sopt.thingkerton.domain.quest.repository.QuestTextRepository;
import sopt.thingkerton.support.FakeAiConfig;
import sopt.thingkerton.support.FakeGeocodingPort;
import sopt.thingkerton.support.FakeQuestGenerator;
import sopt.thingkerton.support.FakeQuestTranslator;
import sopt.thingkerton.support.FixedClockConfig;
import sopt.thingkerton.support.MutableClock;

@SpringBootTest(properties = "app.dev.reset-enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({FixedClockConfig.class, FakeAiConfig.class})
class DevResetApiTest {

    private static final LocalDate TODAY = FixedClockConfig.NOW.toLocalDate();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private FakeQuestGenerator generator;
    @Autowired
    private FakeQuestTranslator translator;
    @Autowired
    private FakeGeocodingPort geocodingPort;
    @Autowired
    private MutableClock clock;
    @Autowired
    private DailyQuestSetRepository dailyQuestSetRepository;
    @Autowired
    private QuestRepository questRepository;
    @Autowired
    private QuestTextRepository questTextRepository;
    @Autowired
    private DiaryRepository diaryRepository;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @AfterEach
    void tearDown() {
        generator.reset();
        translator.reset();
        geocodingPort.reset();
        clock.reset();
    }

    @Test
    void 초기화하면_오늘_묶음이_지워지고_ensure가_새로_생성한다() throws Exception {
        String userKey = newUserWithProfile();
        List<Integer> first = questIds(ensure(userKey));
        // 다른 언어 번역과 일기까지 있는 상태에서 지운다
        mockMvc.perform(post("/api/daily-quests/ensure").header("X-User-Key", userKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"latitude\":37.5718,\"longitude\":126.9620,\"locale\":\"en\"}"));
        recordDiary(userKey, first.getFirst());

        reset(userKey)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("RESET_TODAY_QUESTS"))
                .andExpect(jsonPath("$.data.deletedQuestCount").value(3));

        List<Long> firstIds = first.stream().map(Integer::longValue).toList();
        assertThat(dailyQuestSetRepository.findByUserKeyAndQuestDate(userKey, TODAY)).isEmpty();
        assertThat(questRepository.findAllById(firstIds)).isEmpty();
        assertThat(questTextRepository.findAllByQuestIdIn(firstIds)).isEmpty();
        assertThat(diaryRepository.findAllWithQuestByUserKey(userKey)).isEmpty();
        mockMvc.perform(get("/api/daily-quests/today").header("X-User-Key", userKey))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DAILY_SET_NOT_FOUND"));

        List<Integer> second = questIds(ensure(userKey).andExpect(status().isOk()));
        assertThat(second).hasSize(3).doesNotContainAnyElementsOf(first);
        assertThat(generator.commands()).hasSize(2);
    }

    @Test
    void 오늘_묶음이_없어도_200이고_0개를_삭제한다() throws Exception {
        reset(UUID.randomUUID().toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deletedQuestCount").value(0));
    }

    @Test
    void 다른_날짜와_다른_사용자의_묶음은_지우지_않는다() throws Exception {
        String userKey = newUserWithProfile();
        String other = newUserWithProfile();
        clock.set(FixedClockConfig.NOW.minusDays(1));
        List<Integer> yesterday = questIds(ensure(userKey));
        recordDiary(userKey, yesterday.getFirst());
        clock.reset();
        ensure(userKey);
        ensure(other);

        reset(userKey).andExpect(jsonPath("$.data.deletedQuestCount").value(3));

        assertThat(dailyQuestSetRepository.findByUserKeyAndQuestDate(userKey, TODAY.minusDays(1))).isPresent();
        assertThat(diaryRepository.findAllWithQuestByUserKey(userKey)).hasSize(1);
        assertThat(dailyQuestSetRepository.findByUserKeyAndQuestDate(other, TODAY)).isPresent();
    }

    @Test
    void X_User_Key가_없으면_401() throws Exception {
        mockMvc.perform(delete("/api/dev/daily-quests/today"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("USER_KEY_REQUIRED"));
    }

    private ResultActions reset(String userKey) throws Exception {
        return mockMvc.perform(delete("/api/dev/daily-quests/today").header("X-User-Key", userKey));
    }

    private ResultActions ensure(String userKey) throws Exception {
        return mockMvc.perform(post("/api/daily-quests/ensure")
                .header("X-User-Key", userKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"latitude\":37.5718,\"longitude\":126.9620}"));
    }

    private void recordDiary(String userKey, Integer questId) {
        transactionTemplate.executeWithoutResult(status -> {
            Quest quest = questRepository.findWithSetById(questId.longValue()).orElseThrow();
            quest.record(LocalDateTime.now(clock));
            diaryRepository.save(Diary.create(userKey, quest, UUID.randomUUID().toString(), "title",
                    "diaries/x.jpg", Outcome.EXPERIENCED, null, LocalDateTime.now(clock)));
        });
    }

    private String newUserWithProfile() throws Exception {
        String userKey = UUID.randomUUID().toString();
        mockMvc.perform(put("/api/profile")
                        .header("X-User-Key", userKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname":"하나","locale":"ko","birthDate":"2003-04-12",
                                 "stayStartDate":"2026-03-01","stayEndDate":"2027-02-28","interests":["KPOP","FOOD"]}
                                """))
                .andExpect(status().isOk());
        return userKey;
    }

    private List<Integer> questIds(ResultActions actions) throws Exception {
        String body = actions.andReturn().getResponse().getContentAsString();
        return new ArrayList<>(JsonPath.read(body, "$.data[*].questId"));
    }
}
