package sopt.thingkerton.domain.location.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LocationApiDocumentationTest {
    @Value("${local.server.port}")
    int port;

    @Test
    void openApiIncludesSeparatedApiDocumentation() throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/v3/api-docs")).GET().build();
        var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains(
                "/api/profile",
                "/api/daily-quests/ensure",
                "/api/quests/{questId}",
                "/api/diaries",
                "/api/images",
                "/api/ai/test",
                "/api/locations/geocode",
                "X-User-Key",
                "USER_KEY_REQUIRED",
                "UNSUPPORTED_LOCALE",
                "QUEST_NOT_AVAILABLE",
                "ALREADY_RECORDED",
                "FILE_TOO_LARGE",
                "ADDRESS_NOT_FOUND",
                "AI_RATE_LIMITED");

        JsonNode paths = new ObjectMapper().readTree(response.body()).path("paths");
        assertThat(paths.path("/api/profile").path("get").path("security").toString())
                .isEqualTo("[{\"UserKey\":[]}]");
        assertThat(paths.path("/api/daily-quests/ensure").path("post").path("security").toString())
                .isEqualTo("[{\"UserKey\":[]}]");
        assertThat(paths.path("/api/diaries").path("post").path("security").toString())
                .isEqualTo("[{\"UserKey\":[]}]");
        assertThat(paths.path("/api/images").path("post").has("security")).isFalse();
        assertThat(paths.path("/api/ai/test").path("post").has("security")).isFalse();
        assertThat(paths.path("/api/locations/geocode").path("get").has("security")).isFalse();
    }
}
