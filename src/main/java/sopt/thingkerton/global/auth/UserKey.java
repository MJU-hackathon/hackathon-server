package sopt.thingkerton.global.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 파라미터에 {@code X-User-Key} 헤더 값을 주입한다. 로그인이 없으므로 이 값으로 사용자를 구분한다.
 * <p>
 * 헤더가 없거나 비어 있으면 {@code 401 USER_KEY_REQUIRED}.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface UserKey {
}
