package sopt.thingkerton.domain.profile.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sopt.thingkerton.global.entity.BaseTimeEntity;

@Getter
@Entity
@Table(name = "profiles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile extends BaseTimeEntity {

    @Id
    @Column(name = "user_key", length = 64)
    private String userKey;

    @Column(nullable = false, length = 12)
    private String nickname;

    /** 언어 코드(ko, en, vi, zh, ja). 조회는 {@link #getLocale()}로 enum을 받는다. */
    @Column(nullable = false, length = 5)
    private String locale;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false)
    private LocalDate stayStartDate;

    @Column(nullable = false)
    private LocalDate stayEndDate;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "profile_interests", joinColumns = @JoinColumn(name = "user_key"))
    @Enumerated(EnumType.STRING)
    @Column(name = "interest", nullable = false, length = 20)
    private Set<Interest> interests = EnumSet.noneOf(Interest.class);

    private Profile(String userKey) {
        this.userKey = userKey;
    }

    public static Profile create(String userKey, ProfileInfo info) {
        Profile profile = new Profile(userKey);
        profile.update(info);
        return profile;
    }

    public void update(ProfileInfo info) {
        this.nickname = info.nickname();
        this.locale = info.locale().getCode();
        this.birthDate = info.birthDate();
        this.stayStartDate = info.stayStartDate();
        this.stayEndDate = info.stayEndDate();
        this.interests.clear();
        this.interests.addAll(info.interests());
    }

    public AppLocale getLocale() {
        return AppLocale.find(locale).orElseThrow();
    }

    /**
     * 귀국까지 남은 일수. 귀국 당일 0, 지난 경우도 0.
     */
    public long dDay(LocalDate today) {
        return Math.max(0, ChronoUnit.DAYS.between(today, stayEndDate));
    }

    /**
     * 체류 기간 경과율(0~100, 내림).
     */
    public int stayProgressPercent(LocalDate today) {
        long total = ChronoUnit.DAYS.between(stayStartDate, stayEndDate);
        if (total <= 0) {
            return today.isBefore(stayStartDate) ? 0 : 100;
        }
        long elapsed = Math.clamp(ChronoUnit.DAYS.between(stayStartDate, today), 0, total);
        return (int) (elapsed * 100 / total);
    }
}
