package sopt.thingkerton.support;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import sopt.thingkerton.domain.quest.service.GeneratedQuest;
import sopt.thingkerton.domain.quest.service.QuestImageGenerator;
import sopt.thingkerton.domain.quest.service.QuestImageResolver;

/**
 * 가짜 대표 이미지 생성기. 장소 이름에 "그림실패"가 들어 있으면 실패하고, 그 외에는 {@code quest-images/{placeKey}.png}를 돌려준다.
 */
public class FakeQuestImageGenerator implements QuestImageGenerator {

    public static final String FAIL_MARKER = "그림실패";

    private final List<String> placeNames = new CopyOnWriteArrayList<>();
    private volatile long delayMillis;

    @Override
    public String generate(GeneratedQuest quest) {
        placeNames.add(quest.placeName());
        if (delayMillis > 0) {
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("interrupted");
            }
        }
        if (quest.placeName().contains(FAIL_MARKER)) {
            throw new IllegalStateException("fake image failure");
        }
        return refOf(quest.key().placeKey());
    }

    public static String refOf(String placeKey) {
        return QuestImageResolver.GENERATED_DIR + "/" + placeKey + ".png";
    }

    public List<String> placeNames() {
        return placeNames;
    }

    public void setDelayMillis(long delayMillis) {
        this.delayMillis = delayMillis;
    }

    public void reset() {
        placeNames.clear();
        delayMillis = 0;
    }
}
