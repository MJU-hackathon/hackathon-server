package sopt.thingkerton.domain.profile.controller;

import static sopt.thingkerton.global.swagger.SwaggerExamples.INVALID_INPUT;
import static sopt.thingkerton.global.swagger.SwaggerExamples.PROFILE_NOT_FOUND;
import static sopt.thingkerton.global.swagger.SwaggerExamples.USER_KEY_NOTE;
import static sopt.thingkerton.global.swagger.SwaggerExamples.USER_KEY_REQUIRED;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import sopt.thingkerton.domain.profile.dto.ProfileResponse;
import sopt.thingkerton.domain.profile.dto.ProfileUpdateRequest;
import sopt.thingkerton.global.auth.UserKey;
import sopt.thingkerton.global.dto.CommonResponse;

@Tag(name = "Profile", description = """
        프로필 (WF-01 정보 설정 · 내 정보).
        로그인이 없으므로 '회원가입'은 인증이 아니라 프로필 설정입니다. 최초 설정과 수정은 `PUT /api/profile` 하나로 처리합니다.
        """)
public interface ProfileApi {

    String PROFILE_EXAMPLE = """
            {"status":200,"code":"GET_PROFILE","message":"프로필 조회에 성공했습니다.","data":{"nickname":"하나","locale":"ko","birthDate":"2003-04-12","stayStartDate":"2026-03-01","stayEndDate":"2027-02-28","interests":["KPOP","MOVIE","FOOD"],"dDay":114,"stayProgressPercent":68}}
            """;

    @Operation(
            summary = "프로필 조회",
            description = """
                    앱 시작 시 호출해 화면을 분기합니다.

                    | 결과 | 앱 동작 |
                    | --- | --- |
                    | `200` | 홈(WF-02)으로 이동 |
                    | `404 PROFILE_NOT_FOUND` | 프로필 설정(WF-01)으로 이동 |

                    - `dDay`: 귀국까지 남은 일수 (Asia/Seoul 오늘 기준, 귀국 당일과 지난 경우 0) → 홈 상단 `D-114`
                    - `stayProgressPercent`: 체류 기간 경과율 0~100 (내림) → 홈 상단 진행 바
                    - `interests`: enum 선언 순서로 정렬
                    """ + USER_KEY_NOTE
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 있음", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "프로필", value = PROFILE_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "`X-User-Key` 헤더 없음", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "USER_KEY_REQUIRED", value = USER_KEY_REQUIRED))),
            @ApiResponse(responseCode = "404", description = "프로필 미설정 → WF-01로 이동", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "PROFILE_NOT_FOUND", value = PROFILE_NOT_FOUND)))
    })
    CommonResponse<ProfileResponse> getProfile(@Parameter(hidden = true) @UserKey String userKey);

    @Operation(
            summary = "프로필 저장 (최초 설정·수정 공용)",
            description = """
                    프로필이 없으면 만들고, 있으면 요청 값으로 전부 덮어씁니다. 응답 `data`는 `GET /api/profile`과 같습니다.

                    **검증 규칙**

                    | 필드 | 규칙 | 위반 시 |
                    | --- | --- | --- |
                    | nickname | 앞뒤 공백 제거 후 2~12자 | `400 INVALID_NICKNAME` |
                    | locale | `ko` · `en` · `vi` · `zh` · `ja` | `400 UNSUPPORTED_LOCALE` |
                    | stayEndDate | stayStartDate와 같거나 이후 | `400 INVALID_STAY_PERIOD` |
                    | interests | 1개 이상 | `400 INTEREST_REQUIRED` |
                    | 그 외 | 필수값 누락, 날짜 형식 오류, 없는 관심사 값 | `400 INVALID_INPUT` |

                    **변경 반영 시점**
                    - `locale`: 즉시. 오늘 퀘스트 설명도 새 언어로 내려줍니다 (퀘스트를 새로 만들지 않음)
                    - `interests`: 다음 날 퀘스트부터. 오늘 퀘스트는 다시 만들지 않습니다
                    - 체류 기간: 홈 D-day·진행 바에만 쓰이고 퀘스트 추천에는 쓰이지 않습니다
                    """ + USER_KEY_NOTE
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "저장됨", value = """
                            {"status":200,"code":"UPDATE_PROFILE","message":"프로필을 저장했습니다.","data":{"nickname":"하나","locale":"ko","birthDate":"2003-04-12","stayStartDate":"2026-03-01","stayEndDate":"2027-02-28","interests":["KPOP","MOVIE","FOOD"],"dDay":114,"stayProgressPercent":68}}
                            """))),
            @ApiResponse(responseCode = "400", description = "입력값 위반", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "INVALID_NICKNAME", summary = "닉네임 1자 또는 13자 이상", value = """
                            {"status":400,"code":"INVALID_NICKNAME","message":"닉네임은 2~12자로 입력해 주세요.","data":null}
                            """),
                    @ExampleObject(name = "INVALID_STAY_PERIOD", summary = "종료일이 시작일보다 앞", value = """
                            {"status":400,"code":"INVALID_STAY_PERIOD","message":"체류 종료일은 시작일 이후여야 합니다.","data":null}
                            """),
                    @ExampleObject(name = "INTEREST_REQUIRED", summary = "관심사 0개", value = """
                            {"status":400,"code":"INTEREST_REQUIRED","message":"관심사를 1개 이상 선택해 주세요.","data":null}
                            """),
                    @ExampleObject(name = "UNSUPPORTED_LOCALE", summary = "지원하지 않는 언어", value = """
                            {"status":400,"code":"UNSUPPORTED_LOCALE","message":"지원하지 않는 언어입니다.","data":null}
                            """),
                    @ExampleObject(name = "INVALID_INPUT (필수값 누락)", summary = "Bean Validation 메시지가 그대로 내려감", value = """
                            {"status":400,"code":"INVALID_INPUT","message":"생년월일을 입력해 주세요.","data":null}
                            """),
                    @ExampleObject(name = "INVALID_INPUT (형식 오류)", summary = "날짜 형식 오류, 없는 관심사 값 등", value = INVALID_INPUT)
            })),
            @ApiResponse(responseCode = "401", description = "`X-User-Key` 헤더 없음", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "USER_KEY_REQUIRED", value = USER_KEY_REQUIRED)))
    })
    CommonResponse<ProfileResponse> saveProfile(@Parameter(hidden = true) @UserKey String userKey,
                                                ProfileUpdateRequest request);
}
