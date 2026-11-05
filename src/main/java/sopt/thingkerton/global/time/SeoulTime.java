package sopt.thingkerton.global.time;

import java.time.ZoneId;

/**
 * 서비스의 "오늘"은 서버 기본 시간대와 무관하게 항상 Asia/Seoul 자정 기준이다.
 * 날짜·시각은 주입받은 {@link java.time.Clock}(이 시간대)으로 계산한다.
 */
public final class SeoulTime {

    public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private SeoulTime() {
    }
}
