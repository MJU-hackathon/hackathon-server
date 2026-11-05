package sopt.thingkerton.domain.diary.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import sopt.thingkerton.domain.diary.dto.DiaryCreateRequest;
import sopt.thingkerton.domain.diary.dto.DiaryListResponse;
import sopt.thingkerton.domain.diary.dto.DiaryResponse;
import sopt.thingkerton.global.dto.CommonResponse;

@Tag(name = "Diary", description = "일기 저장(WF-04)·목록(WF-05)")
@SecurityRequirement(name = "UserKey")
public interface DiaryApi {

    @Operation(
            summary = "일기 저장",
            description = "multipart/form-data로 사진 1장과 기록 정보를 받습니다. 일기 생성과 퀘스트 RECORDED 전환은 한 트랜잭션이며, 같은 draftId 재요청은 기존 일기를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "PHOTO_REQUIRED: 사진이 없거나 1장이 아님 | OUTCOME_REQUIRED: 결과 누락 | "
                    + "MEMO_TOO_LONG: 메모 50자 초과 | INVALID_PHOTO: jpeg/png/heic/heif 이외 형식 또는 10MB 초과 | "
                    + "INVALID_INPUT: questId/draftId/draftStartedAt 누락·형식 오류, 잘못된 outcome 또는 64자 초과 X-User-Key"),
            @ApiResponse(responseCode = "401", description = "USER_KEY_REQUIRED: X-User-Key 헤더가 없거나 공백임"),
            @ApiResponse(responseCode = "404", description = "QUEST_NOT_FOUND: 퀘스트가 없거나 다른 사용자 소유 | PROFILE_NOT_FOUND: 프로필이 없음"),
            @ApiResponse(responseCode = "409", description = "QUEST_NOT_AVAILABLE: 패스됐거나 자정 예외가 적용되지 않는 종료 퀘스트 | "
                    + "ALREADY_RECORDED: 다른 draftId로 이미 기록했거나 다른 사용자의 draftId와 충돌"),
            @ApiResponse(responseCode = "413", description = "FILE_TOO_LARGE: 서버 multipart 제한(10MB) 초과"),
            @ApiResponse(responseCode = "500", description = "UPLOAD_FAILED: S3 업로드 실패 | INTERNAL_SERVER_ERROR: 처리되지 않은 서버 오류")
    })
    CommonResponse<DiaryResponse> create(String userKey, DiaryCreateRequest request);

    @Operation(
            summary = "일기 목록",
            description = "작성 시각 최신순으로 월별 묶음과 결과별 집계를 반환합니다. 일기가 없으면 totalCount 0과 빈 groups를 반환합니다. 메모는 번역하지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 64자 초과 X-User-Key | UNSUPPORTED_LOCALE: ko/en/vi/zh/ja 이외 언어"),
            @ApiResponse(responseCode = "401", description = "USER_KEY_REQUIRED: X-User-Key 헤더가 없거나 공백임"),
            @ApiResponse(responseCode = "404", description = "PROFILE_NOT_FOUND: 프로필이 없음"),
            @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: 처리되지 않은 서버 오류")
    })
    CommonResponse<DiaryListResponse> getDiaries(
            String userKey,
            @Parameter(description = "응답 언어(ko/en/vi/zh/ja). 생략 시 프로필 언어", example = "ko") String locale
    );
}
