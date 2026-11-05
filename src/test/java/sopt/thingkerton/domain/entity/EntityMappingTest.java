package sopt.thingkerton.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import sopt.thingkerton.domain.diary.entity.Diary;
import sopt.thingkerton.domain.diary.entity.Outcome;
import sopt.thingkerton.domain.diary.repository.DiaryRepository;
import sopt.thingkerton.domain.location.model.Coordinate;
import sopt.thingkerton.domain.profile.entity.AppLocale;
import sopt.thingkerton.domain.profile.entity.Interest;
import sopt.thingkerton.domain.profile.entity.Profile;
import sopt.thingkerton.domain.profile.entity.ProfileInfo;
import sopt.thingkerton.domain.profile.repository.ProfileRepository;
import sopt.thingkerton.domain.quest.entity.DailyQuestSet;
import sopt.thingkerton.domain.quest.entity.Quest;
import sopt.thingkerton.domain.quest.entity.QuestStatus;
import sopt.thingkerton.domain.quest.entity.QuestText;
import sopt.thingkerton.domain.quest.entity.QuestTextId;
import sopt.thingkerton.domain.quest.repository.DailyQuestSetRepository;
import sopt.thingkerton.domain.quest.repository.QuestRepository;
import sopt.thingkerton.domain.quest.repository.QuestTextRepository;
import sopt.thingkerton.global.time.TimeConfig;
import sopt.thingkerton.support.FixedClockConfig;

@DataJpaTest
@ActiveProfiles("test")
@Import({TimeConfig.class, FixedClockConfig.class})
class EntityMappingTest {

    private static final LocalDate TODAY = FixedClockConfig.NOW.toLocalDate();
    private static final LocalDateTime NOW = FixedClockConfig.NOW.toLocalDateTime();

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private DailyQuestSetRepository dailyQuestSetRepository;
    @Autowired
    private QuestRepository questRepository;
    @Autowired
    private QuestTextRepository questTextRepository;
    @Autowired
    private DiaryRepository diaryRepository;

    @Test
    void 프로필과_관심사를_저장하고_감사_시각은_서울_Clock을_따른다() {
        profileRepository.saveAndFlush(Profile.create("user-1", new ProfileInfo("하나", AppLocale.VI,
                LocalDate.of(2003, 4, 12), LocalDate.of(2026, 3, 1), LocalDate.of(2027, 2, 28),
                EnumSet.of(Interest.FOOD, Interest.KPOP))));
        entityManager.clear();

        Profile found = profileRepository.findById("user-1").orElseThrow();

        assertThat(found.getLocale()).isEqualTo(AppLocale.VI);
        assertThat(found.getInterests()).containsExactlyInAnyOrder(Interest.FOOD, Interest.KPOP);
        assertThat(found.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void 퀘스트_주소와_좌표를_소수점_6자리로_저장하고_좌표가_없으면_null() {
        DailyQuestSet set = dailyQuestSetRepository.saveAndFlush(set("user-1", TODAY));
        Quest withCoordinate = questRepository.saveAndFlush(quest(set));
        Quest withoutCoordinate = questRepository.saveAndFlush(Quest.create(set, "p", "a", Interest.GAME, 30, "장소",
                "game.jpg", null, null, NOW));
        entityManager.clear();

        Quest found = questRepository.findById(withCoordinate.getId()).orElseThrow();
        assertThat(found.getAddress()).isEqualTo("서울특별시 서대문구 통일로 189-1");
        assertThat(found.getLatitude()).isEqualByComparingTo("37.571800");
        assertThat(found.getLongitude()).isEqualByComparingTo("126.962000");
        Quest empty = questRepository.findById(withoutCoordinate.getId()).orElseThrow();
        assertThat(empty.getAddress()).isNull();
        assertThat(empty.getLatitude()).isNull();
    }

    @Test
    void 같은_사용자_같은_날짜의_묶음은_하나만_저장된다() {
        dailyQuestSetRepository.saveAndFlush(set("user-1", TODAY));

        assertThatThrownBy(() -> dailyQuestSetRepository.saveAndFlush(set("user-1", TODAY)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 관심사_스냅샷과_퀘스트_단계는_목록으로_복원된다() {
        DailyQuestSet set = dailyQuestSetRepository.saveAndFlush(set("user-1", TODAY));
        Quest quest = questRepository.saveAndFlush(quest(set));
        questTextRepository.saveAndFlush(QuestText.create(quest.getId(), AppLocale.KO, "제목", "근거", "설명",
                List.of("하나", "둘", "셋")));
        entityManager.clear();

        assertThat(dailyQuestSetRepository.findById(set.getId()).orElseThrow().getInterestsSnapshot())
                .containsExactly(Interest.FOOD, Interest.MOVIE);
        assertThat(questTextRepository.findById(new QuestTextId(quest.getId(), "ko")).orElseThrow().getSteps())
                .containsExactly("하나", "둘", "셋");
    }

    @Test
    void 묶음_날짜가_지난_AVAILABLE_퀘스트는_EXPIRED로_판정한다() {
        DailyQuestSet yesterday = dailyQuestSetRepository.saveAndFlush(set("user-1", TODAY.minusDays(1)));
        Quest quest = questRepository.saveAndFlush(quest(yesterday));

        assertThat(quest.currentStatus(TODAY)).isEqualTo(QuestStatus.EXPIRED);
        assertThat(quest.currentStatus(TODAY.minusDays(1))).isEqualTo(QuestStatus.AVAILABLE);
    }

    @Test
    void 퀘스트당_일기는_하나_draftId는_중복될_수_없다() {
        DailyQuestSet set = dailyQuestSetRepository.saveAndFlush(set("user-1", TODAY));
        Quest quest1 = questRepository.saveAndFlush(quest(set));
        Quest quest2 = questRepository.saveAndFlush(quest(set));
        diaryRepository.saveAndFlush(diary(quest1, "draft-1"));

        assertThatThrownBy(() -> diaryRepository.saveAndFlush(diary(quest1, "draft-2")))
                .isInstanceOf(DataIntegrityViolationException.class);
        entityManager.clear();
        assertThatThrownBy(() -> diaryRepository.saveAndFlush(diary(
                questRepository.findById(quest2.getId()).orElseThrow(), "draft-1")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private DailyQuestSet set(String userKey, LocalDate date) {
        return DailyQuestSet.create(userKey, date, new BigDecimal("37.571800"), new BigDecimal("126.962000"),
                List.of(Interest.FOOD, Interest.MOVIE), false);
    }

    private Quest quest(DailyQuestSet set) {
        return Quest.create(set, "yeongcheon-market", "order-market-food", Interest.FOOD, 40, "영천시장",
                "market", "서울특별시 서대문구 통일로 189-1", new Coordinate(37.5718, 126.962), NOW);
    }

    private Diary diary(Quest quest, String draftId) {
        return Diary.create("user-1", quest, draftId, "제목", "diaries/user-1/a.jpg", Outcome.EXPERIENCED, null, NOW);
    }
}
