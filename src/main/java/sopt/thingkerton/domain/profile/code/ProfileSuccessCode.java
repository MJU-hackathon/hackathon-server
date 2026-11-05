package sopt.thingkerton.domain.profile.code;

import lombok.Getter;
import sopt.thingkerton.global.code.SuccessCode;

@Getter
public enum ProfileSuccessCode implements SuccessCode {
    GET_PROFILE("프로필 조회에 성공했습니다."),
    UPDATE_PROFILE("프로필을 저장했습니다.");

    private final String message;

    ProfileSuccessCode(String message) {
        this.message = message;
    }
}
