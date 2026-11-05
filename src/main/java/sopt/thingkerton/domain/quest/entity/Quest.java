package sopt.thingkerton.domain.quest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sopt.thingkerton.domain.location.model.Coordinate;
import sopt.thingkerton.domain.profile.entity.Interest;

@Getter
@Entity
@Table(name = "quests", indexes = @Index(name = "idx_quests_set_status", columnList = "daily_set_id, status"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "daily_set_id", nullable = false)
    private DailyQuestSet dailySet;

    @Column(nullable = false, length = 100)
    private String placeKey;

    @Column(nullable = false, length = 100)
    private String activityKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Interest category;

    @Column(nullable = false)
    private int estimatedMinutes;

    @Column(nullable = false, length = 100)
    private String placeName;

    @Column(nullable = false, length = 255)
    private String imageRef;

    /** 장소의 도로명 주소 (AI 생성값). 없으면 null */
    @Column(length = 255)
    private String address;

    /** 주소를 네이버 Geocoding으로 변환한 위도. 변환 실패 시 null */
    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    /** 주소를 네이버 Geocoding으로 변환한 경도. 변환 실패 시 null */
    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestStatus status;

    @Column(nullable = false)
    private LocalDateTime statusChangedAt;

    private Quest(DailyQuestSet dailySet, String placeKey, String activityKey, Interest category,
                  int estimatedMinutes, String placeName, String imageRef, String address, Coordinate coordinate,
                  LocalDateTime createdAt) {
        this.dailySet = dailySet;
        this.placeKey = placeKey;
        this.activityKey = activityKey;
        this.category = category;
        this.estimatedMinutes = estimatedMinutes;
        this.placeName = placeName;
        this.imageRef = imageRef;
        this.address = address;
        if (coordinate != null) {
            this.latitude = BigDecimal.valueOf(coordinate.latitude()).setScale(6, RoundingMode.HALF_UP);
            this.longitude = BigDecimal.valueOf(coordinate.longitude()).setScale(6, RoundingMode.HALF_UP);
        }
        this.status = QuestStatus.AVAILABLE;
        this.statusChangedAt = createdAt;
    }

    /**
     * @param address    도로명 주소. 없으면 null
     * @param coordinate 주소를 변환한 좌표. 없으면 null
     */
    public static Quest create(DailyQuestSet dailySet, String placeKey, String activityKey, Interest category,
                               int estimatedMinutes, String placeName, String imageRef, String address,
                               Coordinate coordinate, LocalDateTime createdAt) {
        return new Quest(dailySet, placeKey, activityKey, category, estimatedMinutes, placeName, imageRef, address,
                coordinate, createdAt);
    }

    /**
     * 오늘(Asia/Seoul) 기준 현재 상태. 묶음 날짜가 지난 AVAILABLE 퀘스트는 EXPIRED다.
     */
    public QuestStatus currentStatus(LocalDate today) {
        if (status == QuestStatus.AVAILABLE && dailySet.isBefore(today)) {
            return QuestStatus.EXPIRED;
        }
        return status;
    }

    public boolean isAvailable(LocalDate today) {
        return currentStatus(today) == QuestStatus.AVAILABLE;
    }

    /**
     * 일기를 저장할 수 있는지. AVAILABLE이면 가능하고, EXPIRED라도 촬영 시작 시각이 묶음 날짜(Asia/Seoul) 안이면
     * 가능하다(전날 촬영 후 자정을 넘겨 작성한 경우).
     *
     * @param draftStartedAt Asia/Seoul 기준 촬영 시작 시각
     */
    public boolean isRecordable(LocalDate today, LocalDateTime draftStartedAt) {
        QuestStatus current = currentStatus(today);
        if (current == QuestStatus.AVAILABLE) {
            return true;
        }
        return current == QuestStatus.EXPIRED && draftStartedAt.toLocalDate().equals(dailySet.getQuestDate());
    }

    public void skip(LocalDateTime now) {
        changeStatus(QuestStatus.SKIPPED, now);
    }

    public void record(LocalDateTime now) {
        changeStatus(QuestStatus.RECORDED, now);
    }

    private void changeStatus(QuestStatus status, LocalDateTime now) {
        this.status = status;
        this.statusChangedAt = now;
    }
}
