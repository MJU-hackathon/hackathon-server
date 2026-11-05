package sopt.thingkerton.global.infra.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.genai.Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 앱이 Gemini에 실제로 보내는 키가 설정값과 같은지 확인한다.
 * <p>
 * AiConfig가 너무 일찍 생성되면 {@code @Value("${...}")}가 해석되지 않은 문자열 그대로 주입돼
 * Gemini가 {@code API_KEY_INVALID}를 반환한다 (2026-11-05 운영 장애).
 */
@SpringBootTest
@ActiveProfiles("test")
class GenAiClientConfigTest {

    @Autowired
    private Client client;

    @Test
    void Gemini_클라이언트는_설정된_API_키를_그대로_사용한다() {
        assertThat(client.apiKey()).isEqualTo("test-api-key");
    }
}
