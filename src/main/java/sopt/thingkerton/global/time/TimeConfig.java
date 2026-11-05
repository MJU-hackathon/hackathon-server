package sopt.thingkerton.global.time;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Asia/Seoul {@link Clock}을 빈으로 등록하고, JPA 감사 시각(created_at 등)도 같은 Clock으로 기록한다.
 * 테스트에서는 고정 Clock 빈으로 교체해 날짜 경계를 검증한다.
 */
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class TimeConfig {

    @Bean
    public Clock clock() {
        return Clock.system(SeoulTime.ZONE);
    }

    @Bean
    public DateTimeProvider auditingDateTimeProvider(Clock clock) {
        return () -> Optional.of(LocalDateTime.now(clock));
    }
}
