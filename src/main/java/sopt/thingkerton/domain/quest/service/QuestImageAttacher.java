package sopt.thingkerton.domain.quest.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

/**
 * 생성된 퀘스트에 AI 대표 이미지를 붙인다({@link QuestImageGenerator}).
 * <p>
 * 퀘스트 생성 시 한 번만 호출하고 결과를 저장한다. 3장을 병렬로 만들며, 실패하거나 제한 시간을 넘긴 퀘스트는
 * 이미지 없이 두어 카테고리 기본 이미지를 쓰게 한다(이미지가 없어도 퀘스트는 정상 생성된다).
 */
@Slf4j
@Component
public class QuestImageAttacher implements DisposableBean {

    private final QuestImageGenerator questImageGenerator;
    private final QuestProperties questProperties;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public QuestImageAttacher(QuestImageGenerator questImageGenerator, QuestProperties questProperties) {
        this.questImageGenerator = questImageGenerator;
        this.questProperties = questProperties;
    }

    /**
     * 이미지가 없는 퀘스트만 병렬로 생성한다. 순서는 유지한다.
     * 제한 시간을 넘긴 작업은 취소(인터럽트)한다.
     */
    public List<GeneratedQuest> attachImages(List<GeneratedQuest> quests) {
        List<GeneratedQuest> targets = quests.stream().filter(quest -> quest.imageRef() == null).toList();
        if (targets.isEmpty()) {
            return quests;
        }
        List<Future<String>> futures;
        try {
            futures = executor.invokeAll(
                    targets.stream().<Callable<String>>map(quest -> () -> questImageGenerator.generate(quest)).toList(),
                    questProperties.imageGenerationTimeout().toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return quests;
        }

        List<GeneratedQuest> result = new ArrayList<>(quests.size());
        int targetIndex = 0;
        for (GeneratedQuest quest : quests) {
            if (quest.imageRef() != null) {
                result.add(quest);
                continue;
            }
            String imageRef = imageRefOrNull(quest, futures.get(targetIndex++));
            result.add(imageRef == null ? quest : quest.withImageRef(imageRef));
        }
        return result;
    }

    private String imageRefOrNull(GeneratedQuest quest, Future<String> future) {
        try {
            return future.get();
        } catch (CancellationException exception) {
            log.warn("Quest image generation timed out. place={}", quest.placeName());
        } catch (ExecutionException exception) {
            log.warn("Quest image generation failed. place={}, reason={}", quest.placeName(),
                    exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    @Override
    public void destroy() {
        executor.shutdownNow();
    }
}
