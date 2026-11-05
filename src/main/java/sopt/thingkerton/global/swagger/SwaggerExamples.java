package sopt.thingkerton.global.swagger;

/**
 * 여러 API가 함께 쓰는 Swagger 응답 예시. 어노테이션 값으로 쓰기 위해 상수로 둔다.
 */
public final class SwaggerExamples {

    public static final String USER_KEY_REQUIRED = """
            {"status":401,"code":"USER_KEY_REQUIRED","message":"X-User-Key 헤더가 필요합니다.","data":null}
            """;

    public static final String PROFILE_NOT_FOUND = """
            {"status":404,"code":"PROFILE_NOT_FOUND","message":"프로필을 먼저 설정해 주세요.","data":null}
            """;

    public static final String INVALID_INPUT = """
            {"status":400,"code":"INVALID_INPUT","message":"요청 형식이 올바르지 않습니다.","data":null}
            """;

    public static final String UNSUPPORTED_LOCALE = """
            {"status":400,"code":"UNSUPPORTED_LOCALE","message":"지원하지 않는 언어입니다.","data":null}
            """;

    public static final String QUEST_NOT_FOUND = """
            {"status":404,"code":"QUEST_NOT_FOUND","message":"퀘스트를 찾을 수 없습니다.","data":null}
            """;

    public static final String QUEST_NOT_AVAILABLE = """
            {"status":409,"code":"QUEST_NOT_AVAILABLE","message":"이미 패스했거나 기록했거나 종료된 퀘스트입니다.","data":null}
            """;

    public static final String INTERNAL_SERVER_ERROR = """
            {"status":500,"code":"INTERNAL_SERVER_ERROR","message":"오류가 발생했습니다. 다시 시도해 주세요.","data":null}
            """;

    /** 모든 사용자 API 설명 끝에 붙이는 공통 안내. */
    public static final String USER_KEY_NOTE = """


            **공통**
            - 헤더 `X-User-Key: {uuid}` 필수 (Swagger 오른쪽 위 Authorize에 입력). 없으면 `401 USER_KEY_REQUIRED`
            - 응답은 `{ status, code, message, data }` 형태이며, 화면 분기는 `code`로 합니다.
            """;

    public static final String LOCALE_PARAM = "응답 문구 언어. `ko` · `en` · `vi` · `zh`(간체) · `ja`. 생략하면 프로필 언어. "
            + "처음 요청하는 언어는 AI 번역 때문에 3~4초 걸리고(최대 10초, 넘으면 원문 언어로 응답), 이후에는 캐시됩니다.";

    private SwaggerExamples() {
    }
}
