package sopt.thingkerton.domain.location.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import sopt.thingkerton.domain.location.controller.dto.LocationResponse;
import sopt.thingkerton.global.dto.CommonResponse;

@Tag(name = "Location", description = "주소와 좌표 변환 API")
public interface LocationApi {

    @Operation(
            summary = "주소를 위도·경도로 변환",
            description = "도로명 또는 지번 주소를 네이버 Geocoding으로 조회합니다. 검색 결과가 여러 개면 첫 번째 결과의 좌표를 반환합니다. 장소명 검색 API는 아닙니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주소 변환 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {"status":200,"code":"LOCATION_SEARCH_SUCCESS","message":"주소를 좌표로 변환했습니다.","data":{"address":"서울특별시 서대문구 거북골로 34","latitude":37.5805970,"longitude":126.9230255}}
                    """))),
            @ApiResponse(responseCode = "400", description = "주소가 비어 있거나 주소 파라미터가 누락됨", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "빈 주소", value = """
                    {"status":400,"code":"INVALID_ADDRESS","message":"주소를 입력해 주세요.","data":null}
                    """))),
            @ApiResponse(responseCode = "404", description = "검색 결과 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {"status":404,"code":"ADDRESS_NOT_FOUND","message":"주소를 찾을 수 없습니다.","data":null}
                    """))),
            @ApiResponse(responseCode = "502", description = "네이버 Geocoding 요청 또는 응답 처리 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {"status":502,"code":"GEOCODING_FAILED","message":"주소 좌표 변환에 실패했습니다.","data":null}
                    """)))
    })
    CommonResponse<LocationResponse> geocode(
            @Parameter(description = "검색할 도로명 또는 지번 주소. 공백만 입력할 수 없습니다.", required = true,
                    example = "서울특별시 서대문구 거북골로 34") String address
    );
}
