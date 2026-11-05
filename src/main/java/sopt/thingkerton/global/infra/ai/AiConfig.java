package sopt.thingkerton.global.infra.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AiImageProperties.class, CloudflareAiProperties.class})
public class AiConfig {

    /**
     * 모델, temperature 등 기본 옵션은 application.yaml의 spring.ai.google.genai.chat.options를 따른다.
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
