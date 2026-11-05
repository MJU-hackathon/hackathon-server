package sopt.thingkerton.support;

import java.time.Clock;
import java.time.ZonedDateTime;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import sopt.thingkerton.global.time.SeoulTime;

/**
 * 테스트용 고정 시각: 2026-11-06 10:00 (Asia/Seoul).
 */
@TestConfiguration
public class FixedClockConfig {

    public static final ZonedDateTime NOW = ZonedDateTime.of(2026, 11, 6, 10, 0, 0, 0, SeoulTime.ZONE);

    @Bean
    @Primary
    public Clock fixedClock() {
        return Clock.fixed(NOW.toInstant(), SeoulTime.ZONE);
    }
}
