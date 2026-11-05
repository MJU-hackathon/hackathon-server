package sopt.thingkerton.global.infra.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.genai.errors.ApiException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.mock.web.MockMultipartFile;
import sopt.thingkerton.global.exception.BusinessException;

class AiClientTest {

    private FakeChatModel chatModel;
    private AiClient aiClient;

    @BeforeEach
    void setUp() {
        chatModel = new FakeChatModel();
        aiClient = new AiClient(ChatClient.create(chatModel));
    }

    @Test
    void 시스템_프롬프트와_메시지를_보내고_응답_텍스트를_반환한다() {
        chatModel.respond("비 오는 날엔 파전이지!");

        String answer = aiClient.chat("너는 동네 친구야.", "비 오는 날 뭐 먹어?");

        assertThat(answer).isEqualTo("비 오는 날엔 파전이지!");
        assertThat(chatModel.lastPrompt.getInstructions())
                .anySatisfy(message -> assertThat(message).isInstanceOf(SystemMessage.class)
                        .extracting(m -> m.getText()).isEqualTo("너는 동네 친구야."));
        assertThat(chatModel.lastPrompt.getUserMessage().getText()).isEqualTo("비 오는 날 뭐 먹어?");
    }

    @Test
    void 여러_part로_나뉜_응답을_하나로_이어_붙인다() {
        chatModel.respond("비 오는 날엔 ", "파전이지!");

        assertThat(aiClient.chat(null, "비 오는 날 뭐 먹어?")).isEqualTo("비 오는 날엔 파전이지!");
    }

    @Test
    void 빈_응답이면_AI_CALL_FAILED_예외를_던진다() {
        chatModel.respond("");

        assertThatThrownBy(() -> aiClient.chat(null, "안녕"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(AiErrorCode.AI_CALL_FAILED);
    }

    @Test
    void 시스템_프롬프트가_없으면_사용자_메시지만_보낸다() {
        chatModel.respond("안녕!");

        aiClient.chat(null, "안녕");

        assertThat(chatModel.lastPrompt.getInstructions()).noneMatch(SystemMessage.class::isInstance);
    }

    @Test
    void 이미지를_함께_보낸다() {
        chatModel.respond("파전 사진이에요.");
        AiImage image = AiImage.from(new MockMultipartFile("file", "a.png", "image/png", new byte[]{1, 2, 3}));

        String answer = aiClient.chatWithImage(null, "무슨 사진이야?", image);

        UserMessage userMessage = chatModel.lastPrompt.getUserMessage();
        assertThat(answer).isEqualTo("파전 사진이에요.");
        assertThat(userMessage.getMedia()).hasSize(1);
        assertThat(userMessage.getMedia().getFirst().getMimeType().toString()).isEqualTo("image/png");
    }

    @Test
    void 응답을_지정한_타입으로_변환한다() {
        chatModel.respond("{\"passed\": true, ", "\"reason\": \"파전이 보입니다.\"}");

        Verification result = aiClient.chat(null, "인증 판정해줘", Verification.class);

        assertThat(result).isEqualTo(new Verification(true, "파전이 보입니다."));
        assertThat(chatModel.lastPrompt.getUserMessage().getText()).startsWith("인증 판정해줘").contains("JSON");
    }

    @Test
    void JSON으로_변환할_수_없는_응답이면_AI_CALL_FAILED_예외를_던진다() {
        chatModel.respond("인증에 성공했어요!");

        assertThatThrownBy(() -> aiClient.chat(null, "인증 판정해줘", Verification.class))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(AiErrorCode.AI_CALL_FAILED);
    }

    @Test
    void 요청_한도를_초과하면_AI_RATE_LIMITED_예외를_던진다() {
        chatModel.fail(new RuntimeException(new ApiException(429, "RESOURCE_EXHAUSTED", "quota exceeded")));

        assertThatThrownBy(() -> aiClient.chat(null, "안녕"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(AiErrorCode.AI_RATE_LIMITED);
    }

    @Test
    void 그_외_호출_실패는_AI_CALL_FAILED_예외를_던진다() {
        chatModel.fail(new ApiException(403, "PERMISSION_DENIED", "invalid api key"));

        assertThatThrownBy(() -> aiClient.chat(null, "안녕"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(AiErrorCode.AI_CALL_FAILED);
    }

    @Test
    void 지원하지_않는_이미지_형식은_INVALID_AI_IMAGE_예외를_던진다() {
        MockMultipartFile gif = new MockMultipartFile("file", "a.gif", "image/gif", new byte[]{1});

        assertThatThrownBy(() -> AiImage.from(gif))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(AiErrorCode.INVALID_AI_IMAGE);
    }

    record Verification(boolean passed, String reason) {
    }

    private static class FakeChatModel implements ChatModel {

        private Prompt lastPrompt;
        private List<String> parts = List.of();
        private RuntimeException failure;

        void respond(String... parts) {
            this.parts = List.of(parts);
            this.failure = null;
        }

        void fail(RuntimeException failure) {
            this.failure = failure;
        }

        @Override
        public ChatResponse call(Prompt prompt) {
            this.lastPrompt = prompt;
            if (failure != null) {
                throw failure;
            }
            return new ChatResponse(parts.stream()
                    .map(part -> new Generation(new AssistantMessage(part)))
                    .toList());
        }
    }
}
