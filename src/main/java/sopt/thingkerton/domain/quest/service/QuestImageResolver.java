package sopt.thingkerton.domain.quest.service;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sopt.thingkerton.domain.profile.entity.Interest;

/**
 * 퀘스트 대표 이미지. AI 이미지 생성 없이 팀이 준비한 이미지를 카테고리로 매핑한다.
 * <p>
 * TODO: 팀이 전달하는 이미지·매핑 기준(카테고리·활동)으로 교체. 지금은 카테고리별 임시 파일명이다.
 */
@Component
public class QuestImageResolver {

    private final String baseUrl;

    public QuestImageResolver(@Value("${app.quest.image-base-url}") String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public String resolveRef(Interest category, String activityKey) {
        return category.name().toLowerCase(Locale.ROOT) + ".jpg";
    }

    public String toUrl(String imageRef) {
        return baseUrl + "/" + imageRef;
    }
}
