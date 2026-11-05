package sopt.thingkerton.support;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import sopt.thingkerton.domain.profile.entity.Interest;
import sopt.thingkerton.domain.quest.entity.PlaceActivityKey;
import sopt.thingkerton.domain.quest.service.GeneratedQuest;
import sopt.thingkerton.domain.quest.service.QuestGenerationCommand;
import sopt.thingkerton.domain.quest.service.QuestGenerator;

/**
 * 호출 기록을 남기는 가짜 생성기. 매 호출마다 다른 키로 퀘스트 3개를 만든다.
 */
public class FakeQuestGenerator implements QuestGenerator {

    private final List<QuestGenerationCommand> commands = new CopyOnWriteArrayList<>();
    private final AtomicBoolean failing = new AtomicBoolean(false);
    private volatile long delayMillis;
    private volatile List<GeneratedQuest> fixedResult;

    @Override
    public List<GeneratedQuest> generate(QuestGenerationCommand command) {
        commands.add(command);
        sleep();
        if (failing.get()) {
            throw new IllegalStateException("fake AI failure");
        }
        if (fixedResult != null) {
            return fixedResult;
        }
        String suffix = command.locale().getCode() + "-" + commands.size();
        return List.of(
                quest("place-a-" + suffix, Interest.FOOD, command),
                quest("place-b-" + suffix, Interest.KPOP, command),
                quest("place-c-" + suffix, Interest.TOURISM, command));
    }

    public static GeneratedQuest quest(String placeKey, Interest category, QuestGenerationCommand command) {
        String lang = command == null ? "ko" : command.locale().getCode();
        return new GeneratedQuest(new PlaceActivityKey(placeKey, "activity"), category, 30, placeKey,
                "서울특별시 서대문구 " + placeKey, null,
                "[" + lang + "] title " + placeKey, "[" + lang + "] reason", "[" + lang + "] description",
                List.of("[" + lang + "] step1", "[" + lang + "] step2", "[" + lang + "] step3"));
    }

    public List<QuestGenerationCommand> commands() {
        return commands;
    }

    public void setFailing(boolean value) {
        failing.set(value);
    }

    public void setDelayMillis(long delayMillis) {
        this.delayMillis = delayMillis;
    }

    public void setFixedResult(List<GeneratedQuest> fixedResult) {
        this.fixedResult = fixedResult;
    }

    public void reset() {
        commands.clear();
        failing.set(false);
        delayMillis = 0;
        fixedResult = null;
    }

    private void sleep() {
        if (delayMillis <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
