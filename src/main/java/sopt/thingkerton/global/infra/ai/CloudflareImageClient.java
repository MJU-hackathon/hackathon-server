package sopt.thingkerton.global.infra.ai;

import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import sopt.thingkerton.global.exception.BusinessException;

/**
 * Cloudflare Workers AI 이미지 생성 (REST API). 도메인 코드는 Cloudflare API에 직접 의존하지 않고 이 클래스만 사용한다.
 * <p>
 * 무료 사용량(하루 10,000 neuron, 00:00 UTC 초기화)을 넘기면 429로 실패한다.
 * 실패하면 {@link AiErrorCode}로 변환한 {@link BusinessException}을 던진다.
 */
@Slf4j
@Component
public class CloudflareImageClient {

    private static final String BASE_URL = "https://api.cloudflare.com/client/v4";
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);

    private final RestClient client;
    private final CloudflareAiProperties properties;

    /**
     * 공유 {@link RestClient.Builder} 빈은 상태가 바뀌므로 쓰지 않고 전용 빌더를 만든다.
     */
    @Autowired
    public CloudflareImageClient(CloudflareAiProperties properties) {
        this(properties, RestClient.builder().requestFactory(requestFactory()));
    }

    /** 테스트에서 MockRestServiceServer를 연결할 때 사용 */
    CloudflareImageClient(CloudflareAiProperties properties, RestClient.Builder builder) {
        this.properties = properties;
        this.client = builder.baseUrl(BASE_URL).build();
    }

    private static JdkClientHttpRequestFactory requestFactory() {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(READ_TIMEOUT);
        return requestFactory;
    }

    public AiGeneratedImage generate(String prompt) {
        if (!StringUtils.hasText(properties.accountId()) || !StringUtils.hasText(properties.apiToken())) {
            log.warn("Cloudflare AI is not configured (CLOUDFLARE_ACCOUNT_ID / CLOUDFLARE_API_TOKEN)");
            throw new BusinessException(AiErrorCode.AI_CALL_FAILED);
        }
        RunResponse response;
        try {
            response = client.post()
                    // 모델 이름의 '/'가 경로 변수로 인코딩(%2F)되지 않도록 경로에 그대로 붙인다
                    .uri("/accounts/{accountId}/ai/run/" + properties.imageModel(), properties.accountId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody(prompt))
                    .retrieve()
                    .body(RunResponse.class);
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                log.warn("Cloudflare AI rate limited (daily free neurons may be exhausted)");
                throw new BusinessException(AiErrorCode.AI_RATE_LIMITED);
            }
            log.error("Cloudflare AI call failed. status={}", exception.getStatusCode());
            throw new BusinessException(AiErrorCode.AI_CALL_FAILED);
        } catch (RestClientException exception) {
            log.error("Cloudflare AI call failed", exception);
            throw new BusinessException(AiErrorCode.AI_CALL_FAILED);
        }
        return toImage(response);
    }

    private Map<String, Object> requestBody(String prompt) {
        Map<String, Object> body = new HashMap<>();
        body.put("prompt", prompt);
        if (properties.steps() != null) {
            body.put("steps", properties.steps());
        }
        return body;
    }

    private AiGeneratedImage toImage(RunResponse response) {
        if (response == null || !Boolean.TRUE.equals(response.success()) || response.result() == null
                || !StringUtils.hasText(response.result().image())) {
            log.warn("Cloudflare AI response has no image. errors={}", response == null ? null : response.errors());
            throw new BusinessException(AiErrorCode.AI_CALL_FAILED);
        }
        String encoded = response.result().image();
        byte[] data;
        try {
            data = Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException exception) {
            log.warn("Cloudflare AI returned invalid base64 image");
            throw new BusinessException(AiErrorCode.AI_CALL_FAILED);
        }
        return new AiGeneratedImage(data, contentTypeOf(data));
    }

    /**
     * flux-1-schnell은 JPEG를 base64로 돌려준다. 다른 모델을 대비해 매직 바이트로 형식을 판별한다.
     */
    static String contentTypeOf(byte[] data) {
        if (data.length >= 4 && (data[0] & 0xFF) == 0x89 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G') {
            return "image/png";
        }
        if (data.length >= 12 && data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F'
                && data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P') {
            return "image/webp";
        }
        return "image/jpeg";
    }

    record RunResponse(Boolean success, Result result, Object errors) {
    }

    record Result(String image) {
    }
}
