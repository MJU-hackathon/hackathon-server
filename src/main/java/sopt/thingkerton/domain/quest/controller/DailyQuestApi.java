package sopt.thingkerton.domain.quest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import sopt.thingkerton.domain.quest.dto.DailyQuestEnsureRequest;
import sopt.thingkerton.domain.quest.dto.QuestCardResponse;
import sopt.thingkerton.global.dto.CommonResponse;

@Tag(name = "DailyQuest", description = "오늘의 퀘스트 (WF-02)")
@SecurityRequirement(name = "UserKey")
public interface DailyQuestApi {

    @Operation(
            summary = "오늘 묶음 생성 또는 기존 묶음 반환",
            description = "그날 첫 호출이면 요청 좌표와 현재 관심사로 퀘스트 3개를 생성합니다. 이후 호출은 기존 묶음의 남은 퀘스트만 반환하며, AI 생성 실패는 데모 퀘스트로 대체합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 위도·경도 누락/범위 위반·잘못된 요청 형식 또는 64자 초과 X-User-Key | "
                    + "UNSUPPORTED_LOCALE: ko/en/vi/zh/ja 이외 언어"),
            @ApiResponse(responseCode = "401", description = "USER_KEY_REQUIRED: X-User-Key 헤더가 없거나 공백임"),
            @ApiResponse(responseCode = "404", description = "PROFILE_NOT_FOUND: 퀘스트 생성에 필요한 프로필이 없음"),
            @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: 저장 등 처리되지 않은 서버 오류")
    })
    CommonResponse<List<QuestCardResponse>> ensure(String userKey, DailyQuestEnsureRequest request);

    @Operation(
            summary = "오늘 남은 퀘스트 조회",
            description = "오늘 묶음의 AVAILABLE 퀘스트를 반환합니다. 모두 처리했으면 빈 배열이며, 묶음이 없으면 ensure를 먼저 호출해야 합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 64자 초과 X-User-Key | UNSUPPORTED_LOCALE: ko/en/vi/zh/ja 이외 언어"),
            @ApiResponse(responseCode = "401", description = "USER_KEY_REQUIRED: X-User-Key 헤더가 없거나 공백임"),
            @ApiResponse(responseCode = "404", description = "PROFILE_NOT_FOUND: 프로필이 없음 | DAILY_SET_NOT_FOUND: 오늘 퀘스트 묶음이 없음"),
            @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: 처리되지 않은 서버 오류")
    })
    CommonResponse<List<QuestCardResponse>> getToday(
            String userKey,
            @Parameter(description = "응답 언어(ko/en/vi/zh/ja). 생략 시 프로필 언어", example = "ko") String locale
    );
}
