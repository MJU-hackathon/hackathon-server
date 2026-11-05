package sopt.thingkerton.global.infra.naver.dto;

import java.util.List;

public record NaverGeocodingResponse(String status, List<Address> addresses) {
    public record Address(String x, String y) {
    }
}
