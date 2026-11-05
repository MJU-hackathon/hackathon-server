package sopt.thingkerton.support;

import java.time.ZonedDateTime;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import sopt.thingkerton.global.time.SeoulTime;

/**
 * 테스트용 시각: 2026-11-06 10:00 (Asia/Seoul)에서 시작. 날짜를 바꾼 테스트는 {@link MutableClock#reset()}으로 되돌린다.
 */
@TestConfiguration
public class FixedClockConfig {

    public static final ZonedDateTime NOW = ZonedDateTime.of(2026, 11, 6, 10, 0, 0, 0, SeoulTime.ZONE);

    @Bean
    @Primary
    public MutableClock fixedClock() {
        return new MutableClock(NOW);
    }
}
