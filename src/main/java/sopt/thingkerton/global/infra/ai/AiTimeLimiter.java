package sopt.thingkerton.global.infra.ai;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;
import sopt.thingkerton.global.exception.BusinessException;

/**
 * AI 호출에 시간 제한을 건다. LLM 응답이 늦어도 요청 스레드가 제한 시간 이상 기다리지 않도록 별도(가상) 스레드에서 실행한다.
 * <p>
 * 제한 시간을 넘기면 작업을 취소하고 {@link AiErrorCode#AI_TIMEOUT}을 던진다. 호출 측이 대체 동작(데모 퀘스트, 원문 등)을 정한다.
 */
@Slf4j
@Component
public class AiTimeLimiter implements DisposableBean {

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public <T> T call(Supplier<T> task, Duration timeout) {
        Future<T> future = executor.submit(task::get);
        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException exception) {
            future.cancel(true);
            log.warn("AI call timed out after {}ms", timeout.toMillis());
            throw new BusinessException(AiErrorCode.AI_TIMEOUT);
        } catch (ExecutionException exception) {
            if (exception.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new BusinessException(AiErrorCode.AI_CALL_FAILED);
        } catch (InterruptedException exception) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new BusinessException(AiErrorCode.AI_CALL_FAILED);
        }
    }

    @Override
    public void destroy() {
        executor.shutdownNow();
    }
}
