package sopt.thingkerton.domain.diary.entity;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sopt.thingkerton.domain.quest.entity.Quest;
import sopt.thingkerton.global.entity.BaseTimeEntity;

@Getter
@Entity
@Table(name = "diaries",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_diaries_quest", columnNames = "quest_id"),
                @UniqueConstraint(name = "uk_diaries_draft", columnNames = "draft_id")
        },
        indexes = {
                @Index(name = "idx_diaries_user_created", columnList = "user_key, created_at"),
                @Index(name = "idx_diaries_user_outcome_key", columnList = "user_key, outcome, place_key, activity_key")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Diary extends BaseTimeEntity {

    public static final int MEMO_MAX_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_key", nullable = false, length = 64)
    private String userKey;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest;

    @Column(name = "draft_id", nullable = false, length = 64)
    private String draftId;

    @Column(name = "place_key", nullable = false, length = 100)
    private String placeKey;

    @Column(name = "activity_key", nullable = false, length = 100)
    private String activityKey;

    @Column(nullable = false, length = 100)
    private String titleSnapshot;

    @Column(nullable = false, length = 100)
    private String placeSnapshot;

    @Column(nullable = false, length = 255)
    private String photoKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Outcome outcome;

    @Column(length = MEMO_MAX_LENGTH)
    private String memo;

    @Column(nullable = false)
    private LocalDateTime draftStartedAt;

    private Diary(String userKey, Quest quest, String draftId, String titleSnapshot, String photoKey,
                  Outcome outcome, String memo, LocalDateTime draftStartedAt) {
        this.userKey = userKey;
        this.quest = quest;
        this.draftId = draftId;
        this.placeKey = quest.getPlaceKey();
        this.activityKey = quest.getActivityKey();
        this.titleSnapshot = titleSnapshot;
        this.placeSnapshot = quest.getPlaceName();
        this.photoKey = photoKey;
        this.outcome = outcome;
        this.memo = memo;
        this.draftStartedAt = draftStartedAt;
    }

    public static Diary create(String userKey, Quest quest, String draftId, String titleSnapshot, String photoKey,
                               Outcome outcome, String memo, LocalDateTime draftStartedAt) {
        return new Diary(userKey, quest, draftId, titleSnapshot, photoKey, outcome, memo, draftStartedAt);
    }
}
