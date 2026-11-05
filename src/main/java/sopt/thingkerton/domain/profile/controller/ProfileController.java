package sopt.thingkerton.domain.profile.controller;

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

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController implements ProfileApi {

    private final ProfileService profileService;

    @GetMapping
    @Override
    public CommonResponse<ProfileResponse> getProfile(@UserKey String userKey) {
        return CommonResponse.success(ProfileSuccessCode.GET_PROFILE,
                ProfileResponse.from(profileService.getSummary(userKey)));
    }

    @PutMapping
    @Override
    public CommonResponse<ProfileResponse> saveProfile(
            @UserKey String userKey,
            @Valid @RequestBody ProfileUpdateRequest request) {
        return CommonResponse.success(ProfileSuccessCode.UPDATE_PROFILE,
                ProfileResponse.from(profileService.saveProfile(userKey, request.toInfo())));
    }
}
