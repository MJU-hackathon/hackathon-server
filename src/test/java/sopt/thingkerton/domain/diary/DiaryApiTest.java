package sopt.thingkerton.domain.diary;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.web.multipart.MultipartFile;
import sopt.thingkerton.global.infra.s3.S3Uploader;
import sopt.thingkerton.global.infra.s3.UploadedFile;
import sopt.thingkerton.support.FakeAiConfig;
import sopt.thingkerton.support.FakeQuestGenerator;
import sopt.thingkerton.support.FakeQuestTranslator;
import sopt.thingkerton.support.FixedClockConfig;
import sopt.thingkerton.support.MutableClock;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({FixedClockConfig.class, FakeAiConfig.class})
class DiaryApiTest {

    private static final String BUCKET_URL = "https://bucket.example.com/";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MutableClock clock;
    @Autowired
    private FakeQuestGenerator generator;
    @Autowired
    private FakeQuestTranslator translator;
    @MockitoBean
    private S3Uploader s3Uploader;

    @BeforeEach
    void setUp() {
        when(s3Uploader.uploadImage(any(MultipartFile.class), anyString(), anySet())).thenAnswer(invocation -> {
            String key = invocation.getArgument(1) + "/" + UUID.randomUUID() + ".jpg";
            return new UploadedFile(key, BUCKET_URL + key);
        });
        when(s3Uploader.getUrl(anyString())).thenAnswer(invocation -> BUCKET_URL + invocation.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        clock.reset();
        generator.reset();
        translator.reset();
    }

    @Test
    void 경험_못경험_일기를_저장하면_둘_다_today에서_빠진다() throws Exception {
        String userKey = newUserWithProfile();
        List<Integer> ids = ensure(userKey);

        createDiary(userKey, ids.get(0), "EXPERIENCED", "재밌었다", newDraftId(), "2026-11-06T09:30:00+09:00")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CREATE_DIARY"))
                .andExpect(jsonPath("$.data.questId").value(ids.get(0)))
                .andExpect(jsonPath("$.data.outcome").value("EXPERIENCED"))
                .andExpect(jsonPath("$.data.memo").value("재밌었다"))
                .andExpect(jsonPath("$.data.photoUrl").value(org.hamcrest.Matchers.startsWith(BUCKET_URL + "diaries/" + userKey)))
                .andExpect(jsonPath("$.data.recordedAt").value("2026-11-06T10:00:00+09:00"));
        createDiary(userKey, ids.get(1), "NOT_EXPERIENCED", "", newDraftId(), "2026-11-06T09:40:00+09:00")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memo").doesNotExist());

        mockMvc.perform(get("/api/daily-quests/today").header("X-User-Key", userKey))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].questId").value(ids.get(2)));
        mockMvc.perform(get("/api/quests/{id}", ids.get(0)).header("X-User-Key", userKey))
                .andExpect(jsonPath("$.data.status").value("RECORDED"));

        mockMvc.perform(get("/api/diaries").header("X-User-Key", userKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_DIARIES"))
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.experiencedCount").value(1))
                .andExpect(jsonPath("$.data.notExperiencedCount").value(1))
                .andExpect(jsonPath("$.data.groups.length()").value(1))
                .andExpect(jsonPath("$.data.groups[0].yearMonth").value("2026-11"))
                .andExpect(jsonPath("$.data.groups[0].diaries[0].questId").value(ids.get(1)))
                .andExpect(jsonPath("$.data.groups[0].diaries[1].questId").value(ids.get(0)));
    }

    @Test
    void 사진_없음_2장_형식오류와_outcome_없음은_400() throws Exception {
        String userKey = newUserWithProfile();
        Integer questId = ensure(userKey).getFirst();

        mockMvc.perform(form(userKey, questId, "EXPERIENCED", null, newDraftId(), "2026-11-06T09:30:00+09:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PHOTO_REQUIRED"));
        mockMvc.perform(form(userKey, questId, "EXPERIENCED", null, newDraftId(), "2026-11-06T09:30:00+09:00")
                        .file(photo("image/jpeg")).file(photo("image/jpeg")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PHOTO_REQUIRED"));
        mockMvc.perform(form(userKey, questId, "EXPERIENCED", null, newDraftId(), "2026-11-06T09:30:00+09:00")
                        .file(photo("image/gif")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PHOTO"));
        createDiary(userKey, questId, null, null, newDraftId(), "2026-11-06T09:30:00+09:00")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("OUTCOME_REQUIRED"));

        verify(s3Uploader, never()).uploadImage(any(MultipartFile.class), anyString(), anySet());
    }

    @Test
    void 메모는_50자까지_허용하고_51자는_거부한다() throws Exception {
        String userKey = newUserWithProfile();
        List<Integer> ids = ensure(userKey);

        createDiary(userKey, ids.get(0), "EXPERIENCED", "가".repeat(51), newDraftId(), "2026-11-06T09:30:00+09:00")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MEMO_TOO_LONG"));
        createDiary(userKey, ids.get(0), "EXPERIENCED", "가".repeat(50), newDraftId(), "2026-11-06T09:30:00+09:00")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memo").value("가".repeat(50)));
    }

    @Test
    void 같은_draftId를_두_번_제출하면_일기는_1개이고_두_번째도_200() throws Exception {
        String userKey = newUserWithProfile();
        Integer questId = ensure(userKey).getFirst();
        String draftId = newDraftId();

        Integer first = JsonPath.read(createDiary(userKey, questId, "EXPERIENCED", "메모", draftId,
                "2026-11-06T09:30:00+09:00").andExpect(status().isOk()).andReturn().getResponse().getContentAsString(),
                "$.data.diaryId");
        createDiary(userKey, questId, "EXPERIENCED", "메모", draftId, "2026-11-06T09:30:00+09:00")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.diaryId").value(first));

        mockMvc.perform(get("/api/diaries").header("X-User-Key", userKey))
                .andExpect(jsonPath("$.data.totalCount").value(1));
        verify(s3Uploader, times(1)).uploadImage(any(MultipartFile.class), anyString(), anySet());
    }

    @Test
    void 다른_draftId로_이미_기록한_퀘스트와_패스한_퀘스트는_409() throws Exception {
        String userKey = newUserWithProfile();
        List<Integer> ids = ensure(userKey);
        createDiary(userKey, ids.get(0), "EXPERIENCED", null, newDraftId(), "2026-11-06T09:30:00+09:00")
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/quests/{id}/pass", ids.get(1)).header("X-User-Key", userKey))
                .andExpect(status().isOk());

        createDiary(userKey, ids.get(0), "EXPERIENCED", null, newDraftId(), "2026-11-06T09:31:00+09:00")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ALREADY_RECORDED"));
        createDiary(userKey, ids.get(1), "EXPERIENCED", null, newDraftId(), "2026-11-06T09:31:00+09:00")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("QUEST_NOT_AVAILABLE"));
        createDiary(newUserWithProfile(), ids.get(2), "EXPERIENCED", null, newDraftId(), "2026-11-06T09:31:00+09:00")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("QUEST_NOT_FOUND"));
    }

    @Test
    void 전날_묶음_퀘스트는_촬영_시작이_전날이면_저장하고_오늘이면_409() throws Exception {
        String userKey = newUserWithProfile();
        List<Integer> ids = ensure(userKey);

        clock.set(FixedClockConfig.NOW.plusDays(1).withHour(0).withMinute(5));

        createDiary(userKey, ids.get(0), "EXPERIENCED", null, newDraftId(), "2026-11-06T23:58:00+09:00")
                .andExpect(status().isOk());
        // UTC로 보낸 전날 시각(2026-11-06 23:59 KST)도 허용
        createDiary(userKey, ids.get(1), "NOT_EXPERIENCED", null, newDraftId(), "2026-11-06T14:59:00Z")
                .andExpect(status().isOk());
        createDiary(userKey, ids.get(2), "EXPERIENCED", null, newDraftId(), "2026-11-07T00:01:00+09:00")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("QUEST_NOT_AVAILABLE"));
    }

    @Test
    void 일기가_없으면_totalCount_0() throws Exception {
        mockMvc.perform(get("/api/diaries").header("X-User-Key", newUserWithProfile()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(0))
                .andExpect(jsonPath("$.data.experiencedCount").value(0))
                .andExpect(jsonPath("$.data.groups.length()").value(0));
    }

    @Test
    void locale을_바꾸면_제목은_새_언어_메모는_원문을_유지한다() throws Exception {
        String userKey = newUserWithProfile();
        Integer questId = ensure(userKey).getFirst();
        createDiary(userKey, questId, "EXPERIENCED", "시장이 문을 닫았다", newDraftId(), "2026-11-06T09:30:00+09:00")
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/profile")
                        .header("X-User-Key", userKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody("en")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/diaries").header("X-User-Key", userKey))
                .andExpect(jsonPath("$.data.groups[0].diaries[0].title").value(org.hamcrest.Matchers.startsWith("[en]")))
                .andExpect(jsonPath("$.data.groups[0].diaries[0].memo").value("시장이 문을 닫았다"));
        mockMvc.perform(get("/api/diaries").header("X-User-Key", userKey).param("locale", "ja"))
                .andExpect(jsonPath("$.data.groups[0].diaries[0].title").value(org.hamcrest.Matchers.startsWith("[ja]")))
                .andExpect(jsonPath("$.data.groups[0].diaries[0].memo").value("시장이 문을 닫았다"));
    }

    private ResultActions createDiary(String userKey, Integer questId, String outcome, String memo, String draftId,
                                      String draftStartedAt) throws Exception {
        return mockMvc.perform(form(userKey, questId, outcome, memo, draftId, draftStartedAt).file(photo("image/jpeg")));
    }

    private MockMultipartHttpServletRequestBuilder form(String userKey, Integer questId, String outcome, String memo,
                                                        String draftId, String draftStartedAt) {
        MockMultipartHttpServletRequestBuilder builder = multipart("/api/diaries");
        builder.header("X-User-Key", userKey);
        builder.param("questId", String.valueOf(questId));
        builder.param("draftId", draftId);
        builder.param("draftStartedAt", draftStartedAt);
        if (outcome != null) {
            builder.param("outcome", outcome);
        }
        if (memo != null) {
            builder.param("memo", memo);
        }
        return builder;
    }

    private MockMultipartFile photo(String contentType) {
        return new MockMultipartFile("photo", "photo", contentType, new byte[]{1, 2, 3});
    }

    private String newUserWithProfile() throws Exception {
        String userKey = UUID.randomUUID().toString();
        mockMvc.perform(put("/api/profile")
                        .header("X-User-Key", userKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody("ko")))
                .andExpect(status().isOk());
        return userKey;
    }

    private String profileBody(String locale) {
        return """
                {"nickname":"하나","locale":"%s","birthDate":"2003-04-12",
                 "stayStartDate":"2026-03-01","stayEndDate":"2027-02-28","interests":["KPOP","FOOD"]}
                """.formatted(locale);
    }

    private List<Integer> ensure(String userKey) throws Exception {
        String body = mockMvc.perform(post("/api/daily-quests/ensure")
                        .header("X-User-Key", userKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"latitude\":37.5718,\"longitude\":126.9620}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return new ArrayList<>(JsonPath.read(body, "$.data[*].questId"));
    }

    private String newDraftId() {
        return UUID.randomUUID().toString();
    }
}
