package sopt.thingkerton.global.auth;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import sopt.thingkerton.global.code.GlobalErrorCode;
import sopt.thingkerton.global.exception.BusinessException;

@Component
public class UserKeyArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String HEADER_NAME = "X-User-Key";
    private static final int MAX_LENGTH = 64;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(UserKey.class)
                && String.class.equals(parameter.getParameterType());
    }

    @Override
    public String resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        String userKey = webRequest.getHeader(HEADER_NAME);
        if (!StringUtils.hasText(userKey)) {
            throw new BusinessException(GlobalErrorCode.USER_KEY_REQUIRED);
        }
        String trimmed = userKey.trim();
        if (trimmed.length() > MAX_LENGTH) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
        return trimmed;
    }
}
