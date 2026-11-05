package sopt.thingkerton.domain.quest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sopt.thingkerton.domain.profile.entity.AppLocale;
import sopt.thingkerton.global.converter.StringListConverter;

/**
 * 퀘스트의 언어별 설명. 생성 언어로 먼저 저장하고, 다른 언어 요청 시 번역해 캐시한다.
 * <p>
 * JPA는 식별자에 AttributeConverter를 적용하지 않으므로 locale은 코드 문자열(ko, en...)로 저장한다.
 */
@Getter
@Entity
@IdClass(QuestTextId.class)
@Table(name = "quest_texts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestText {

    @Id
    @Column(name = "quest_id")
    private Long questId;

    @Id
    @Column(name = "locale", length = 5)
    private String locale;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Convert(converter = StringListConverter.class)
    @Column(nullable = false, columnDefinition = "text")
    private List<String> steps = new ArrayList<>();

    private QuestText(Long questId, AppLocale locale, String title, String reason, String description,
                      List<String> steps) {
        this.questId = questId;
        this.locale = locale.getCode();
        this.title = title;
        this.reason = reason;
        this.description = description;
        this.steps = new ArrayList<>(steps);
    }

    public static QuestText create(Long questId, AppLocale locale, String title, String reason,
                                   String description, List<String> steps) {
        return new QuestText(questId, locale, title, reason, description, steps);
    }

    public AppLocale getAppLocale() {
        return AppLocale.find(locale).orElseThrow();
    }
}
