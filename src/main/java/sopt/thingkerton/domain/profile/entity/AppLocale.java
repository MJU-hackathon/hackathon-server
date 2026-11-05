package sopt.thingkerton.domain.profile.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;

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
}
