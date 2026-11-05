package sopt.thingkerton.global.infra.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import sopt.thingkerton.global.exception.BusinessException;

class CloudflareImageClientTest {

    private static final String URL = "https://api.cloudflare.com/client/v4/accounts/acc-1/ai/run/"
            + "@cf/black-forest-labs/flux-1-schnell";
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1, 2, 3};

    private MockRestServiceServer server;
    private CloudflareImageClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new CloudflareImageClient(
                new CloudflareAiProperties("acc-1", "token-1", "@cf/black-forest-labs/flux-1-schnell", 4), builder);
    }

    @Test
    void 프롬프트와_step을_보내고_base64_이미지를_디코딩한다() {
        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer token-1"))
                .andExpect(jsonPath("$.prompt").value("a market"))
                .andExpect(jsonPath("$.steps").value(4))
                .andRespond(withSuccess("""
                        {"success":true,"errors":[],"result":{"image":"%s","usage":{"neurons":172.8}}}
                        """.formatted(Base64.getEncoder().encodeToString(JPEG)), MediaType.APPLICATION_JSON));

        AiGeneratedImage image = client.generate("a market");

        assertThat(image.data()).isEqualTo(JPEG);
        assertThat(image.contentType()).isEqualTo("image/jpeg");
        server.verify();
    }

    @Test
    void 무료_한도를_넘겨_429면_AI_RATE_LIMITED() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> client.generate("a market"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(AiErrorCode.AI_RATE_LIMITED);
    }

    @Test
    void 이미지가_없는_응답이면_AI_CALL_FAILED() {
        server.expect(requestTo(URL)).andRespond(withSuccess(
                "{\"success\":false,\"errors\":[{\"message\":\"bad\"}],\"result\":null}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.generate("a market"))
                .extracting("errorCode").isEqualTo(AiErrorCode.AI_CALL_FAILED);
    }

    @Test
    void 키가_설정되지_않으면_호출하지_않고_AI_CALL_FAILED() {
        CloudflareImageClient unconfigured = new CloudflareImageClient(
                new CloudflareAiProperties("", "", "@cf/black-forest-labs/flux-1-schnell", 4), RestClient.builder());

        assertThatThrownBy(() -> unconfigured.generate("a market"))
                .extracting("errorCode").isEqualTo(AiErrorCode.AI_CALL_FAILED);
    }

    @Test
    void 매직_바이트로_이미지_형식을_판별한다() {
        assertThat(CloudflareImageClient.contentTypeOf(JPEG)).isEqualTo("image/jpeg");
        assertThat(CloudflareImageClient.contentTypeOf(new byte[]{(byte) 0x89, 'P', 'N', 'G', 0})).isEqualTo("image/png");
        assertThat(CloudflareImageClient.contentTypeOf("RIFF0000WEBPxx".getBytes())).isEqualTo("image/webp");
    }
}
