package sopt.thingkerton.domain.profile.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sopt.thingkerton.domain.profile.code.ProfileSuccessCode;
import sopt.thingkerton.domain.profile.dto.ProfileResponse;
import sopt.thingkerton.domain.profile.dto.ProfileUpdateRequest;
import sopt.thingkerton.domain.profile.service.ProfileService;
import sopt.thingkerton.global.auth.UserKey;
import sopt.thingkerton.global.dto.CommonResponse;

@Tag(name = "Profile", description = "프로필 (WF-01·내 정보)")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "프로필 조회",
            description = "앱 시작 시 호출. 프로필이 없으면 404 PROFILE_NOT_FOUND → 프로필 설정(WF-01)으로 이동합니다.")
    @GetMapping
    public CommonResponse<ProfileResponse> getProfile(@UserKey String userKey) {
        return CommonResponse.success(ProfileSuccessCode.GET_PROFILE,
                ProfileResponse.from(profileService.getSummary(userKey)));
    }

    @Operation(summary = "프로필 저장",
            description = "최초 설정과 수정 공용. 언어는 즉시, 관심사는 다음 날 퀘스트부터 반영됩니다.")
    @PutMapping
    public CommonResponse<ProfileResponse> saveProfile(
            @UserKey String userKey,
            @Valid @RequestBody ProfileUpdateRequest request) {
        return CommonResponse.success(ProfileSuccessCode.UPDATE_PROFILE,
                ProfileResponse.from(profileService.saveProfile(userKey, request.toInfo())));
    }
}
