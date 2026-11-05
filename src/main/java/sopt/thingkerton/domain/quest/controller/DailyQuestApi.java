package sopt.thingkerton.domain.quest.controller;

import static sopt.thingkerton.global.swagger.SwaggerExamples.LOCALE_PARAM;
import static sopt.thingkerton.global.swagger.SwaggerExamples.PROFILE_NOT_FOUND;
import static sopt.thingkerton.global.swagger.SwaggerExamples.UNSUPPORTED_LOCALE;
import static sopt.thingkerton.global.swagger.SwaggerExamples.USER_KEY_NOTE;
import static sopt.thingkerton.global.swagger.SwaggerExamples.USER_KEY_REQUIRED;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import sopt.thingkerton.domain.quest.dto.DailyQuestEnsureRequest;
import sopt.thingkerton.domain.quest.dto.QuestCardResponse;
import sopt.thingkerton.global.auth.UserKey;
import sopt.thingkerton.global.dto.CommonResponse;

@Tag(name = "DailyQuest", description = """
        오늘의 퀘스트 (WF-02 홈).
        사용자·날짜(Asia/Seoul 자정 기준)당 퀘스트 묶음 1개, 퀘스트 3개입니다. 패스·기록해도 새 퀘스트를 보충하지 않습니다.
        """)
public interface DailyQuestApi {

    String CARDS_EXAMPLE = """
            {"status":200,"code":"ENSURE_DAILY_QUESTS","message":"오늘의 퀘스트 조회에 성공했습니다.","data":[
              {"questId":1,"category":"MOVIE","estimatedMinutes":30,"title":"예술영화관 씨네큐브 탐방","placeName":"씨네큐브 광화문","imageUrl":"https://cdn.example.com/quest-images/movie.jpg","reason":"영화에 관심 많은 당신을 위한 대표 예술영화관 추천!"},
              {"questId":2,"category":"FOOD","estimatedMinutes":40,"title":"영천시장 수제 꽈배기 맛보기","placeName":"영천시장","imageUrl":"https://cdn.example.com/quest-images/food.jpg","reason":"맛있는 음식을 즐기는 당신을 위한 전통시장 간식 탐방!"},
              {"questId":3,"category":"TOURISM","estimatedMinutes":45,"title":"조용한 고궁 경희궁 산책","placeName":"경희궁","imageUrl":"https://cdn.example.com/quest-images/tourism.jpg","reason":"서울의 고즈넉한 역사를 경험할 수 있는 특별한 명소!"}
            ]}
            """;

    String CARD_TABLE = """

            **카드 필드 → 화면 위치**

            | 필드 | 화면 |
            | --- | --- |
            | `category` | 1행 태그 (앱에서 언어별 라벨로 표시, 예: FOOD → 음식) |
            | `estimatedMinutes` | 1행 시계 "약 40분" |
            | `imageUrl` | 썸네일 (팀 준비 대표 이미지, 현재 임시 주소) |
            | `title` | 제목 |
            | `placeName` | 장소 핀 (AI 생성 장소명, 번역되지 않음) |
            | `reason` | 3행 추천 근거 한 줄 |
            | `questId` | 카드 탭 → `GET /api/quests/{questId}` (WF-03) |
            """;

    @Operation(
            summary = "오늘 퀘스트 생성 또는 기존 퀘스트 반환",
            description = """
                    홈 진입 시 기기 현재 위치(OS 위치 권한)를 보내 호출합니다. `data`는 홈 카드 배열입니다.

                    **동작**
                    - 오늘(Asia/Seoul) 묶음이 **없으면**: 요청 좌표 반경 3km 안 퀘스트 3개를 AI가 만들어 저장하고 반환합니다.
                      관심사를 우선 반영하되 다른 분야도 섞습니다(약 3개 중 2개 관심사). 이미 '해봤어요'로 기록한 장소+활동 조합은 제외합니다.
                    - 오늘 묶음이 **있으면**: 좌표·관심사가 달라도 새로 만들지 않고 같은 묶음의 **남은(AVAILABLE)** 퀘스트를 반환합니다.
                    - 동시에 여러 번 호출해도 묶음은 하나만 생깁니다.
                    - AI 생성이 실패하거나 **15초**를 넘기면 팀이 준비한 데모 퀘스트 3개로 대체합니다(그날 묶음으로 고정).

                    **응답 시간**: 그날 첫 호출 5~10초(최대 15초), 이후 호출 0.1초 이하.
                    앱 HTTP 타임아웃을 20초 이상으로 두고 로딩 화면을 보여 주세요.

                    **주의**
                    - 장소는 AI가 만든 값이며 실존·거리 검증을 하지 않습니다. 좌표는 내려주지 않습니다.
                    - 다 처리했으면 빈 배열 `[]` → "오늘 진행할 퀘스트가 없어요!"(WF-02b)
                    """ + CARD_TABLE + USER_KEY_NOTE
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "오늘 남은 퀘스트 카드 (0~3개)", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "퀘스트 3개", value = CARDS_EXAMPLE),
                    @ExampleObject(name = "모두 처리함 (WF-02b)", value = """
                            {"status":200,"code":"ENSURE_DAILY_QUESTS","message":"오늘의 퀘스트 조회에 성공했습니다.","data":[]}
                            """)
            })),
            @ApiResponse(responseCode = "400", description = "좌표 누락·범위 위반, 지원하지 않는 언어", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "INVALID_INPUT", summary = "위도 -90~90, 경도 -180~180", value = """
                            {"status":400,"code":"INVALID_INPUT","message":"위도는 -90~90이어야 합니다.","data":null}
                            """),
                    @ExampleObject(name = "UNSUPPORTED_LOCALE", value = UNSUPPORTED_LOCALE)
            })),
            @ApiResponse(responseCode = "401", description = "`X-User-Key` 헤더 없음", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "USER_KEY_REQUIRED", value = USER_KEY_REQUIRED))),
            @ApiResponse(responseCode = "404", description = "프로필 미설정 → WF-01로 이동", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "PROFILE_NOT_FOUND", value = PROFILE_NOT_FOUND)))
    })
    CommonResponse<List<QuestCardResponse>> ensure(@Parameter(hidden = true) @UserKey String userKey,
                                                   DailyQuestEnsureRequest request);

    @Operation(
            summary = "오늘 남은 퀘스트 조회",
            description = """
                    오늘 묶음의 남은(AVAILABLE) 퀘스트를 `ensure`와 같은 카드 배열로 반환합니다. 퀘스트를 만들지 않습니다.
                    패스·일기 저장 후 홈으로 돌아올 때, 탭 이동 시 호출합니다.

                    **화면 분기**

                    | 결과 | 앱 동작 |
                    | --- | --- |
                    | `200` 카드 1~3개 | 홈 카드 표시, "오늘 남은 퀘스트 n/3" |
                    | `200` 빈 배열 `[]` | WF-02b "오늘 진행할 퀘스트가 없어요!" |
                    | `404 DAILY_SET_NOT_FOUND` | 오늘 묶음 없음 → 위치를 받아 `POST /api/daily-quests/ensure` 호출 |

                    남은 퀘스트 = 처음 3개 − 패스 − 기록. 자정(Asia/Seoul)이 지나면 전날 묶음은 끝나고 404가 됩니다.
                    """ + CARD_TABLE + USER_KEY_NOTE
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "오늘 남은 퀘스트 카드 (0~3개)", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "패스 1개 후 2개 남음", value = """
                            {"status":200,"code":"GET_TODAY_QUESTS","message":"오늘 남은 퀘스트 조회에 성공했습니다.","data":[
                              {"questId":1,"category":"MOVIE","estimatedMinutes":30,"title":"예술영화관 씨네큐브 탐방","placeName":"씨네큐브 광화문","imageUrl":"https://cdn.example.com/quest-images/movie.jpg","reason":"영화에 관심 많은 당신을 위한 대표 예술영화관 추천!"},
                              {"questId":2,"category":"FOOD","estimatedMinutes":40,"title":"영천시장 수제 꽈배기 맛보기","placeName":"영천시장","imageUrl":"https://cdn.example.com/quest-images/food.jpg","reason":"맛있는 음식을 즐기는 당신을 위한 전통시장 간식 탐방!"}
                            ]}
                            """),
                    @ExampleObject(name = "locale=en", value = """
                            {"status":200,"code":"GET_TODAY_QUESTS","message":"오늘 남은 퀘스트 조회에 성공했습니다.","data":[
                              {"questId":2,"category":"FOOD","estimatedMinutes":40,"title":"Tasting Handmade Kkwaebaegi at Yeongcheon Market","placeName":"영천시장","imageUrl":"https://cdn.example.com/quest-images/food.jpg","reason":"A traditional market snack tour for you who enjoy delicious food!"}
                            ]}
                            """),
                    @ExampleObject(name = "모두 처리함 (WF-02b)", value = """
                            {"status":200,"code":"GET_TODAY_QUESTS","message":"오늘 남은 퀘스트 조회에 성공했습니다.","data":[]}
                            """)
            })),
            @ApiResponse(responseCode = "400", description = "지원하지 않는 언어", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "UNSUPPORTED_LOCALE", value = UNSUPPORTED_LOCALE))),
            @ApiResponse(responseCode = "401", description = "`X-User-Key` 헤더 없음", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "USER_KEY_REQUIRED", value = USER_KEY_REQUIRED))),
            @ApiResponse(responseCode = "404", description = "오늘 묶음 없음 → ensure 호출 / 프로필 미설정 → WF-01", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "DAILY_SET_NOT_FOUND", value = """
                            {"status":404,"code":"DAILY_SET_NOT_FOUND","message":"오늘의 퀘스트가 아직 없습니다.","data":null}
                            """),
                    @ExampleObject(name = "PROFILE_NOT_FOUND", summary = "locale 생략 + 프로필 없음", value = PROFILE_NOT_FOUND)
            }))
    })
    CommonResponse<List<QuestCardResponse>> getToday(
            @Parameter(hidden = true) @UserKey String userKey,
            @Parameter(description = LOCALE_PARAM, example = "en",
                    schema = @Schema(allowableValues = {"ko", "en", "vi", "zh", "ja"}))
            String locale);
}
