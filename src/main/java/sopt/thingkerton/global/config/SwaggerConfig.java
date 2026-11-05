package sopt.thingkerton.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sopt.thingkerton.global.auth.UserKey;
import sopt.thingkerton.global.auth.UserKeyArgumentResolver;

@Configuration
public class SwaggerConfig {

    private static final String USER_KEY_SCHEME = "UserKey";

    static {
        // @UserKey 파라미터는 요청 파라미터가 아니라 헤더이므로 문서에서 숨기고, 아래 보안 스킴으로 입력받는다.
        SpringDocUtils.getConfig().addAnnotationsToIgnore(UserKey.class);
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Thingkerton API")
                        .description("명지대학교 해커톤 API")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(USER_KEY_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(UserKeyArgumentResolver.HEADER_NAME)
                                .description("프로필·퀘스트·일기 API의 필수 헤더. 앱이 최초 실행 시 만든 UUID이며 공백 불가, 최대 64자입니다.")));
    }
}
