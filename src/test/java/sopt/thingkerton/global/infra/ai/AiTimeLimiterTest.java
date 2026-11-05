package sopt.thingkerton.global.infra.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import sopt.thingkerton.global.exception.BusinessException;

class AiTimeLimiterTest {

    private final AiTimeLimiter limiter = new AiTimeLimiter();

    @AfterEach
    void tearDown() {
        limiter.destroy();
    }

    @Test
    void 제한_시간_안에_끝나면_결과를_반환한다() {
        assertThat(limiter.call(() -> "ok", Duration.ofSeconds(1))).isEqualTo("ok");
    }

    @Test
    void 제한_시간을_넘기면_AI_TIMEOUT() {
        long start = System.currentTimeMillis();

        assertThatThrownBy(() -> limiter.call(() -> {
            sleep(2_000);
            return "late";
        }, Duration.ofMillis(200)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(AiErrorCode.AI_TIMEOUT);
        assertThat(System.currentTimeMillis() - start).isLessThan(1_000);
    }

    @Test
    void 작업의_런타임_예외는_그대로_전달한다() {
        assertThatThrownBy(() -> limiter.call(() -> {
            throw new BusinessException(AiErrorCode.AI_RATE_LIMITED);
        }, Duration.ofSeconds(1)))
                .extracting("errorCode").isEqualTo(AiErrorCode.AI_RATE_LIMITED);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
