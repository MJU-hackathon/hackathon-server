package sopt.thingkerton.domain.quest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import sopt.thingkerton.domain.profile.entity.AppLocale;
import sopt.thingkerton.domain.profile.entity.Interest;
import sopt.thingkerton.domain.quest.entity.PlaceActivityKey;
import sopt.thingkerton.domain.quest.service.GeminiQuestGenerator.Item;
import sopt.thingkerton.domain.quest.service.GeminiQuestGenerator.Payload;
import sopt.thingkerton.global.infra.ai.AiClient;

class GeminiQuestGeneratorTest {

    private final AiClient aiClient = mock(AiClient.class);
    private final GeminiQuestGenerator generator = new GeminiQuestGenerator(aiClient);

    private final QuestGenerationCommand command = new QuestGenerationCommand(
            new BigDecimal("37.571800"), new BigDecimal("126.962000"), List.of(Interest.FOOD, Interest.KPOP),
            Set.of(new PlaceActivityKey("done-place", "done-activity")), AppLocale.VI);

    @Test
    void 프롬프트에_좌표_관심사_제외목록_언어를_넣는다() {
        when(aiClient.chat(anyString(), anyString(), eq(Payload.class)))
                .thenReturn(new Payload(List.of(item("a"), item("b"), item("c"))));

        generator.generate(command);

        ArgumentCaptor<String> prompt = ArgumentCaptor.forClass(String.class);
        Mockito.verify(aiClient).chat(anyString(), prompt.capture(), any());
        assertThat(prompt.getValue())
                .contains("37.571800", "126.962000", "3km", "FOOD, KPOP", "done-place + done-activity", "vi",
                        "address", "road-name address");
    }

    @Test
    void 형식이_잘못되거나_제외_대상인_퀘스트는_버리고_키를_정규화한다() {
        when(aiClient.chat(anyString(), anyString(), eq(Payload.class))).thenReturn(new Payload(List.of(
                new Item("Done Place", "done_activity", "FOOD", 30, "x", null, "t", "r", "d", List.of("s")),
                new Item("p-bad", "a", "SPORTS", 30, "x", null, "t", "r", "d", List.of("s")),
                item("A Place"), item("b"), item("b"), item("c"), item("d"))));

        List<GeneratedQuest> quests = generator.generate(command);

        assertThat(quests).extracting(quest -> quest.key().placeKey()).containsExactly("a-place", "b", "c");
    }

    @Test
    void 주소는_공백을_정리하고_비어_있으면_null_좌표는_아직_없다() {
        when(aiClient.chat(anyString(), anyString(), eq(Payload.class))).thenReturn(new Payload(List.of(
                item("a"),
                new Item("b", "x", "FOOD", 30, "B", "  ", "t", "r", "d", List.of("s")),
                new Item("c", "x", "FOOD", 30, "C", null, "t", "r", "d", List.of("s")))));

        List<GeneratedQuest> quests = generator.generate(command);

        assertThat(quests).extracting(GeneratedQuest::address)
                .containsExactly("서울특별시 서대문구 통일로 247", null, null);
        assertThat(quests).extracting(GeneratedQuest::coordinate).containsOnlyNulls();
    }

    @Test
    void 유효한_퀘스트가_3개보다_적으면_예외() {
        when(aiClient.chat(anyString(), anyString(), eq(Payload.class)))
                .thenReturn(new Payload(List.of(item("a"), item("b"))));

        assertThatThrownBy(() -> generator.generate(command)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 소요_시간은_10_240분으로_보정한다() {
        when(aiClient.chat(anyString(), anyString(), eq(Payload.class))).thenReturn(new Payload(List.of(
                new Item("a", "x", "food", 1, "A", null, "t", "r", "d", List.of("s")),
                new Item("b", "x", "FOOD", 999, "B", "  ", "t", "r", "d", List.of("s")),
                new Item("c", "x", "FOOD", null, "C", null, "t", "r", "d", List.of("s")))));

        assertThat(generator.generate(command)).extracting(GeneratedQuest::estimatedMinutes)
                .containsExactly(10, 240, 30);
    }

    private Item item(String placeKey) {
        return new Item(placeKey, "activity", "FOOD", 30, placeKey, " 서울특별시 서대문구 통일로 247 ", "title", "reason",
                "desc", List.of("s1", "s2", "s3"));
    }
}
