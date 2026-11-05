package sopt.thingkerton.domain.quest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sopt.thingkerton.domain.profile.entity.Interest;
import sopt.thingkerton.domain.profile.entity.InterestListConverter;
import sopt.thingkerton.global.entity.BaseTimeEntity;

/**
 * 사용자·날짜(Asia/Seoul)별 퀘스트 묶음. 생성 시점의 위치·관심사를 저장한다.
 */
@Getter
@Entity
@Table(name = "daily_quest_sets", uniqueConstraints = @UniqueConstraint(
        name = "uk_daily_quest_sets_user_date", columnNames = {"user_key", "quest_date"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyQuestSet extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_key", nullable = false, length = 64)
    private String userKey;

    @Column(name = "quest_date", nullable = false)
    private LocalDate questDate;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal genLatitude;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal genLongitude;

    @Convert(converter = InterestListConverter.class)
    @Column(nullable = false, columnDefinition = "text")
    private List<Interest> interestsSnapshot = new ArrayList<>();

    @Column(nullable = false)
    private boolean isFallback;

    private DailyQuestSet(String userKey, LocalDate questDate, BigDecimal genLatitude, BigDecimal genLongitude,
                          List<Interest> interestsSnapshot, boolean isFallback) {
        this.userKey = userKey;
        this.questDate = questDate;
        this.genLatitude = genLatitude;
        this.genLongitude = genLongitude;
        this.interestsSnapshot = new ArrayList<>(interestsSnapshot);
        this.isFallback = isFallback;
    }

    public static DailyQuestSet create(String userKey, LocalDate questDate, BigDecimal genLatitude,
                                       BigDecimal genLongitude, List<Interest> interestsSnapshot,
                                       boolean isFallback) {
        return new DailyQuestSet(userKey, questDate, genLatitude, genLongitude, interestsSnapshot, isFallback);
    }

    public boolean isOwnedBy(String userKey) {
        return this.userKey.equals(userKey);
    }

    public boolean isBefore(LocalDate today) {
        return questDate.isBefore(today);
    }
}
