package sopt.thingkerton.domain.quest.service;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param generationTimeout 퀘스트 생성 AI 호출 제한 시간. 넘으면 데모 퀘스트로 대체
 * @param translationTimeout 설명 번역 AI 호출 제한 시간. 넘으면 원문 설명 반환
 */
@ConfigurationProperties("app.quest")
public record QuestProperties(
        Duration generationTimeout,
        Duration translationTimeout
) {
}
