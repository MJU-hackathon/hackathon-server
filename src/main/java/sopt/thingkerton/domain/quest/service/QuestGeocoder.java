package sopt.thingkerton.domain.quest.service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import sopt.thingkerton.domain.location.model.Coordinate;
import sopt.thingkerton.domain.location.service.LocationService;

/**
 * 생성된 퀘스트의 장소 주소를 좌표로 변환한다({@link LocationService#geocode}, 네이버 Geocoding).
 * <p>
 * 퀘스트 생성 시 한 번만 호출하고 결과를 저장한다. 주소가 없거나 변환에 실패·시간 초과하면 좌표 없이 둔다
 * (좌표가 없어도 퀘스트는 정상 생성된다).
 */
@Slf4j
@Component
public class QuestGeocoder implements DisposableBean {

    private final LocationService locationService;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public QuestGeocoder(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * 좌표가 없는 퀘스트만 병렬로 변환한다. 순서는 유지한다.
     *
     * @param timeout 제한 시간 (호출 측이 마감까지 남은 시간으로 정한다)
     */
    public List<GeneratedQuest> attachCoordinates(List<GeneratedQuest> quests, Duration timeout) {
        List<CompletableFuture<GeneratedQuest>> futures = quests.stream()
                .map(quest -> needsGeocoding(quest)
                        ? CompletableFuture.supplyAsync(() -> geocode(quest), executor)
                        .completeOnTimeout(quest, timeout.toMillis(), TimeUnit.MILLISECONDS)
                        : CompletableFuture.completedFuture(quest))
                .toList();
        return futures.stream().map(CompletableFuture::join).toList();
    }

    private boolean needsGeocoding(GeneratedQuest quest) {
        return quest.coordinate() == null && StringUtils.hasText(quest.address());
    }

    private GeneratedQuest geocode(GeneratedQuest quest) {
        try {
            Coordinate coordinate = locationService.geocode(quest.address());
            return quest.withCoordinate(coordinate);
        } catch (RuntimeException exception) {
            log.info("Quest geocoding failed. place={}, address={}, reason={}", quest.placeName(), quest.address(),
                    exception.getMessage());
            return quest;
        }
    }

    @Override
    public void destroy() {
        executor.shutdownNow();
    }
}
