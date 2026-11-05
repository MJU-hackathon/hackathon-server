package sopt.thingkerton.global.infra.naver;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import sopt.thingkerton.domain.location.code.LocationErrorCode;
import sopt.thingkerton.domain.location.model.Coordinate;
import sopt.thingkerton.global.exception.BusinessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class NaverGeocodingAdapterTest {
    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final NaverGeocodingAdapter adapter = new NaverGeocodingAdapter(builder, new NaverMapsProperties("id", "key"));

    @Test
    void mapsCoordinatesAndSendsCredentials() {
        server.expect(requestTo("https://maps.apigw.ntruss.com/map-geocode/v2/geocode?query=Seoul"))
                .andExpect(header("x-ncp-apigw-api-key-id", "id"))
                .andExpect(header("x-ncp-apigw-api-key", "key"))
                .andRespond(withSuccess("{\"status\":\"OK\",\"addresses\":[{\"x\":\"126.9\",\"y\":\"37.5\"}]}", MediaType.APPLICATION_JSON));

        assertThat(adapter.geocode("Seoul")).isEqualTo(new Coordinate(37.5, 126.9));
        server.verify();
    }

    @Test
    void handlesNoResults() {
        server.expect(requestTo("https://maps.apigw.ntruss.com/map-geocode/v2/geocode?query=unknown"))
                .andRespond(withSuccess("{\"status\":\"OK\",\"addresses\":[]}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> adapter.geocode("unknown"))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(LocationErrorCode.ADDRESS_NOT_FOUND);
    }
}
