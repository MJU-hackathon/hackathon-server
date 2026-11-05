package sopt.thingkerton.domain.location.controller.dto;

import sopt.thingkerton.domain.location.model.Coordinate;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "입력한 주소와 변환된 좌표")
public record LocationResponse(
        @Schema(description = "요청에 입력한 주소", example = "서울특별시 서대문구 거북골로 34") String address,
        @Schema(description = "위도", example = "37.5805970") double latitude,
        @Schema(description = "경도", example = "126.9230255") double longitude
) {
    public static LocationResponse from(String address, Coordinate coordinate) {
        return new LocationResponse(address, coordinate.latitude(), coordinate.longitude());
    }
}
