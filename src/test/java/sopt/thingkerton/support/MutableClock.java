package sopt.thingkerton.support;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * 테스트에서 날짜를 넘기기 위한 Clock. 테스트가 끝나면 {@link #reset()}으로 되돌린다.
 */
public class MutableClock extends Clock {

    private final ZoneId zone;
    private final Instant initial;
    private volatile Instant instant;

    public MutableClock(ZonedDateTime initial) {
        this.zone = initial.getZone();
        this.initial = initial.toInstant();
        this.instant = this.initial;
    }

    public void set(ZonedDateTime dateTime) {
        this.instant = dateTime.toInstant();
    }

    public void reset() {
        this.instant = initial;
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return Clock.fixed(instant, zone);
    }

    @Override
    public Instant instant() {
        return instant;
    }
}
