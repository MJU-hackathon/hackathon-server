package sopt.thingkerton.global.swagger;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SwaggerDocumentationTest {

    @Value("${local.server.port}")
    int port;

    private String body;
    private DocumentContext doc;

    @BeforeEach
    void setUp() throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/v3/api-docs")).GET().build();
        var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        body = response.body();
        doc = JsonPath.parse(body);
        Files.writeString(Path.of("build", "api-docs.json"), body);
    }

    @Test
    void 사용자_API는_X_User_Key를_쿼리_파라미터로_노출하지_않는다() {
        List<String> names = doc.read("$.paths..parameters[*].name");
        assertThat(names).doesNotContain("userKey", "request");
        assertThat(body).contains("\"X-User-Key\"");
    }

    @Test
    void 모든_API에_상세_설명과_에러_예시가_있다() {
        assertThat(body).contains(
                "/api/profile", "/api/daily-quests/ensure", "/api/daily-quests/today",
                "/api/quests/{questId}", "/api/quests/{questId}/pass", "/api/diaries", "/api/images", "/api/ai/test",
                "PROFILE_NOT_FOUND", "INVALID_NICKNAME", "DAILY_SET_NOT_FOUND", "QUEST_NOT_AVAILABLE",
                "PHOTO_REQUIRED", "MEMO_TOO_LONG", "ALREADY_RECORDED", "USER_KEY_REQUIRED", "FILE_TOO_LARGE");
        String ensureDescription = doc.read("$.paths['/api/daily-quests/ensure'].post.description");
        assertThat(ensureDescription).contains("15초", "반경 3km");
    }

    @Test
    void 일기_저장은_multipart_요청_본문으로_문서화된다() {
        String ref = doc.read("$.paths['/api/diaries'].post.requestBody.content['multipart/form-data'].schema['$ref']");
        assertThat(ref).isEqualTo("#/components/schemas/DiaryCreateRequest");

        Map<String, Object> properties = doc.read("$.components.schemas.DiaryCreateRequest.properties");
        assertThat(properties).containsKeys("photo", "questId", "outcome", "memo", "draftId", "draftStartedAt");
        List<String> required = doc.read("$.components.schemas.DiaryCreateRequest.required");
        assertThat(required).contains("photo", "questId", "outcome", "draftId", "draftStartedAt").doesNotContain("memo");
    }
}
