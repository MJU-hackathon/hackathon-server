package sopt.thingkerton.global.infra.ai;

import com.google.genai.Client;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.HttpRetryOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@EnableConfigurationProperties(CloudflareAiProperties.class)
public class AiConfig {

    /**
     * Gemini 클라이언트. Spring AI 자동 설정 대신 직접 만들어 SDK 재시도를 끈다.
     * <p>
     * SDK는 429·5xx를 지수 백오프로 재시도하는데, 무료 한도 소진(429)일 때 재시도로 응답 제한 시간을 다 써 버린다.
     * 앱 타임아웃(10초) 안에 응답해야 하므로 한 번만 호출하고, 실패하면 호출 측이 바로 데모·원문으로 대체한다.
     * <p>
     * 이 빈은 Spring AI 조건 평가 중 일찍 생성된다. 이때는 {@code @Value("${...}")}가 해석되지 않아
     * 키 대신 문자열 {@code "${spring.ai.google.genai.api-key}"}가 들어가 Gemini가 API_KEY_INVALID를 반환했다.
     * 그래서 {@link Environment}에서 직접 읽고, 설정 클래스가 일찍 만들어지지 않도록 static으로 둔다.
     */
    @Bean
    public static Client googleGenAiClient(Environment environment) {
        return createGenAiClient(environment.getRequiredProperty("spring.ai.google.genai.api-key"));
    }

    static Client createGenAiClient(String apiKey) {
        return Client.builder()
                .apiKey(apiKey)
                .httpOptions(HttpOptions.builder()
                        .retryOptions(HttpRetryOptions.builder().attempts(1).build())
                        .build())
                .build();
    }

    /**
     * 모델, temperature 등 기본 옵션은 application.yaml의 spring.ai.google.genai.chat.options를 따른다.
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
