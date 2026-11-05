package sopt.thingkerton.domain.quest.service;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param generationTimeout 퀘스트 생성 AI 호출 제한 시간. 넘으면 데모 퀘스트로 대체
 * @param translationTimeout 설명 번역 AI 호출 제한 시간. 넘으면 원문 설명 반환
 * @param geocodingTimeout 장소 주소 → 좌표 변환 제한 시간(3곳 병렬). 넘으면 좌표 없이 저장
 * @param imageGenerationTimeout 대표 이미지 AI 생성 제한 시간(3장 병렬). 넘으면 카테고리 기본 이미지로 저장
 */
@ConfigurationProperties("app.quest")
public record QuestProperties(
        Duration generationTimeout,
        Duration translationTimeout,
        Duration geocodingTimeout,
        Duration imageGenerationTimeout
) {
}
