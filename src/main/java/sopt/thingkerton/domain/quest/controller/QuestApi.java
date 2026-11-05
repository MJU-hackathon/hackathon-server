package sopt.thingkerton.domain.quest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import sopt.thingkerton.domain.quest.dto.QuestDetailResponse;
import sopt.thingkerton.domain.quest.dto.QuestPassResponse;
import sopt.thingkerton.global.dto.CommonResponse;

@Tag(name = "Quest", description = "퀘스트 상세·패스 (WF-03)")
@SecurityRequirement(name = "UserKey")
public interface QuestApi {

    @Operation(
            summary = "퀘스트 상세",
            description = "본인 묶음의 퀘스트만 조회하며, 장소·활동 설명을 요청 언어로 반환합니다. 번역 실패 시 저장된 원문을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 64자 초과 X-User-Key | "
                    + "UNSUPPORTED_LOCALE: ko/en/vi/zh/ja 이외 언어"),
            @ApiResponse(responseCode = "401", description = "USER_KEY_REQUIRED: X-User-Key 헤더가 없거나 공백임"),
            @ApiResponse(responseCode = "404", description = "QUEST_NOT_FOUND: 퀘스트가 없거나 다른 사용자 소유 | PROFILE_NOT_FOUND: 프로필이 없음"),
            @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: 처리되지 않은 서버 오류")
    })
    CommonResponse<QuestDetailResponse> getDetail(
            String userKey,
            @Parameter(description = "퀘스트 ID", example = "1201") Long questId,
            @Parameter(description = "응답 언어(ko/en/vi/zh/ja). 생략 시 프로필 언어", example = "ko") String locale
    );

    @Operation(
            summary = "퀘스트 패스",
            description = "상태를 SKIPPED로 바꾸고 홈에서 제거합니다. 새 퀘스트를 보충하거나 일기를 만들지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 64자 초과 X-User-Key"),
            @ApiResponse(responseCode = "401", description = "USER_KEY_REQUIRED: X-User-Key 헤더가 없거나 공백임"),
            @ApiResponse(responseCode = "404", description = "QUEST_NOT_FOUND: 퀘스트가 없거나 다른 사용자 소유"),
            @ApiResponse(responseCode = "409", description = "QUEST_NOT_AVAILABLE: 이미 패스·기록했거나 날짜가 지난 퀘스트"),
            @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: 처리되지 않은 서버 오류")
    })
    CommonResponse<QuestPassResponse> pass(
            String userKey,
            @Parameter(description = "퀘스트 ID", example = "1201") Long questId
    );
}
