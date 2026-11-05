package sopt.thingkerton.domain.profile.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;
import sopt.thingkerton.domain.profile.code.ProfileErrorCode;
import sopt.thingkerton.global.exception.BusinessException;

/**
 * 지원 언어. API·DB에는 소문자 코드(ko, en, vi, zh, ja)로 주고받는다.
 */
@Getter
public enum AppLocale {
    KO("ko", "한국어"),
    EN("en", "English"),
    VI("vi", "Tiếng Việt"),
    ZH("zh", "简体中文"),
    JA("ja", "日本語");

    private final String code;
    private final String displayName;

    AppLocale(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public static Optional<AppLocale> find(String code) {
        if (code == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(locale -> locale.code.equalsIgnoreCase(code.trim()))
                .findFirst();
    }

    /**
     * 요청 값을 변환한다. 지원하지 않는 코드면 {@code 400 UNSUPPORTED_LOCALE}.
     */
    public static AppLocale from(String code) {
        return find(code).orElseThrow(() -> new BusinessException(ProfileErrorCode.UNSUPPORTED_LOCALE));
    }

    /**
     * 선택 파라미터용. 값이 없으면 null, 있는데 지원하지 않으면 {@code 400 UNSUPPORTED_LOCALE}.
     */
    public static AppLocale fromNullable(String code) {
        return code == null || code.isBlank() ? null : from(code);
    }
}
