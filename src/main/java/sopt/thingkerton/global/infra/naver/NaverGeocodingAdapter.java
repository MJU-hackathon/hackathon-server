package sopt.thingkerton.global.infra.naver;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import sopt.thingkerton.domain.location.code.LocationErrorCode;
import sopt.thingkerton.domain.location.model.Coordinate;
import sopt.thingkerton.domain.location.service.port.GeocodingPort;
import sopt.thingkerton.global.exception.BusinessException;
import sopt.thingkerton.global.infra.naver.dto.NaverGeocodingResponse;

@Component
public class NaverGeocodingAdapter implements GeocodingPort {
    private final RestClient client;

    public NaverGeocodingAdapter(RestClient.Builder builder, NaverMapsProperties properties) {
        this.client = builder.baseUrl("https://maps.apigw.ntruss.com")
                .defaultHeader("x-ncp-apigw-api-key-id", properties.apiKeyId())
                .defaultHeader("x-ncp-apigw-api-key", properties.apiKey())
                .build();
    }

    @Override
    public Coordinate geocode(String address) {
        try {
            NaverGeocodingResponse response = client.get()
                    .uri(uri -> uri.path("/map-geocode/v2/geocode").queryParam("query", address).build())
                    .header("Accept", "application/json")
                    .retrieve().body(NaverGeocodingResponse.class);
            if (response == null || !"OK".equals(response.status()) || response.addresses() == null) {
                throw new BusinessException(LocationErrorCode.GEOCODING_FAILED);
            }
            if (response.addresses().isEmpty()) {
                throw new BusinessException(LocationErrorCode.ADDRESS_NOT_FOUND);
            }
            var result = response.addresses().getFirst();
            return new Coordinate(Double.parseDouble(result.y()), Double.parseDouble(result.x()));
        } catch (RestClientException | NumberFormatException | NullPointerException exception) {
            throw new BusinessException(LocationErrorCode.GEOCODING_FAILED);
        }
    }
}
