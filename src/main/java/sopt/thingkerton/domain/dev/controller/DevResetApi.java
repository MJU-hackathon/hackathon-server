package sopt.thingkerton.domain.dev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import sopt.thingkerton.domain.dev.dto.DevResetResponse;
import sopt.thingkerton.global.dto.CommonResponse;

@Tag(name = "Dev", description = "개발·QA용 API. APP_DEV_RESET_ENABLED=true일 때만 노출 (운영에서는 끄기)")
@SecurityRequirement(name = "UserKey")
public interface DevResetApi {

    @Operation(
            summary = "오늘 퀘스트 묶음 초기화",
            description = "오늘(Asia/Seoul) 묶음과 그 퀘스트·번역·일기를 삭제합니다. 이후 ensure를 호출하면 새 묶음이 생성됩니다. "
                    + "묶음이 없어도 200이며 deletedQuestCount는 0입니다. 다른 날짜의 일기는 삭제하지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 64자 초과 X-User-Key"),
            @ApiResponse(responseCode = "401", description = "USER_KEY_REQUIRED: X-User-Key 헤더가 없거나 공백임"),
            @ApiResponse(responseCode = "404", description = "APP_DEV_RESET_ENABLED가 꺼져 있어 API가 없음"),
            @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: 처리되지 않은 서버 오류")
    })
    CommonResponse<DevResetResponse> resetToday(String userKey);
}
