package sopt.thingkerton.domain.profile.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import sopt.thingkerton.domain.profile.dto.ProfileResponse;
import sopt.thingkerton.domain.profile.dto.ProfileUpdateRequest;
import sopt.thingkerton.global.dto.CommonResponse;

@Tag(name = "Profile", description = "프로필 (WF-01·내 정보)")
@SecurityRequirement(name = "UserKey")
public interface ProfileApi {

    @Operation(
            summary = "프로필 조회",
            description = "앱 시작 시 호출합니다. X-User-Key 헤더로 사용자를 구분하며, 프로필이 없으면 프로필 설정 화면으로 이동합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "USER_KEY_REQUIRED: X-User-Key 헤더가 없거나 공백임"),
            @ApiResponse(responseCode = "404", description = "PROFILE_NOT_FOUND: 해당 사용자의 프로필이 없음"),
            @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: 처리되지 않은 서버 오류")
    })
    CommonResponse<ProfileResponse> getProfile(String userKey);

    @Operation(
            summary = "프로필 저장",
            description = "최초 설정과 수정에 함께 사용합니다. locale은 즉시, interests는 다음 날 생성되는 퀘스트부터 반영됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 필수값 누락·잘못된 날짜/관심사 형식 또는 64자 초과 X-User-Key | "
                    + "INVALID_NICKNAME: 닉네임 2~12자 위반 | INVALID_STAY_PERIOD: 종료일이 시작일보다 빠름 | "
                    + "INTEREST_REQUIRED: 관심사가 비어 있음 | UNSUPPORTED_LOCALE: ko/en/vi/zh/ja 이외 언어"),
            @ApiResponse(responseCode = "401", description = "USER_KEY_REQUIRED: X-User-Key 헤더가 없거나 공백임"),
            @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: 처리되지 않은 서버 오류")
    })
    CommonResponse<ProfileResponse> saveProfile(String userKey, ProfileUpdateRequest request);
}
