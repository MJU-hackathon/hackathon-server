package sopt.thingkerton.global.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import sopt.thingkerton.global.code.GlobalErrorCode;
import sopt.thingkerton.global.exception.BusinessException;

class UserKeyArgumentResolverTest {

    private final UserKeyArgumentResolver resolver = new UserKeyArgumentResolver();

    @Test
    void 헤더_값을_반환한다() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Key", " 7b0c1d2e-0000-4000-8000-000000000001 ");

        String userKey = resolver.resolveArgument(null, null, new ServletWebRequest(request), null);

        assertThat(userKey).isEqualTo("7b0c1d2e-0000-4000-8000-000000000001");
    }

    @Test
    void 헤더가_없으면_USER_KEY_REQUIRED() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertThatThrownBy(() -> resolver.resolveArgument(null, null, new ServletWebRequest(request), null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(GlobalErrorCode.USER_KEY_REQUIRED);
    }

    @Test
    void 헤더가_공백이면_USER_KEY_REQUIRED() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Key", "  ");

        assertThatThrownBy(() -> resolver.resolveArgument(null, null, new ServletWebRequest(request), null))
                .extracting("errorCode")
                .isEqualTo(GlobalErrorCode.USER_KEY_REQUIRED);
    }
}
