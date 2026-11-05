package sopt.thingkerton.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * LLM 호출과 네이버 Geocoding을 가짜 구현으로 교체한다.
 */
@TestConfiguration
public class FakeAiConfig {

    @Bean
    @Primary
    public FakeQuestGenerator fakeQuestGenerator() {
        return new FakeQuestGenerator();
    }

    @Bean
    @Primary
    public FakeQuestTranslator fakeQuestTranslator() {
        return new FakeQuestTranslator();
    }

    @Bean
    @Primary
    public FakeGeocodingPort fakeGeocodingPort() {
        return new FakeGeocodingPort();
    }
}
