package sopt.thingkerton.domain.quest.controller;

import static sopt.thingkerton.global.swagger.SwaggerExamples.LOCALE_PARAM;
import static sopt.thingkerton.global.swagger.SwaggerExamples.PROFILE_NOT_FOUND;
import static sopt.thingkerton.global.swagger.SwaggerExamples.QUEST_NOT_AVAILABLE;
import static sopt.thingkerton.global.swagger.SwaggerExamples.QUEST_NOT_FOUND;
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
import sopt.thingkerton.domain.quest.dto.QuestDetailResponse;
import sopt.thingkerton.domain.quest.dto.QuestPassResponse;
import sopt.thingkerton.global.auth.UserKey;
import sopt.thingkerton.global.dto.CommonResponse;

@Tag(name = "Quest", description = """
        퀘스트 상세·패스 (WF-03). 본인 묶음의 퀘스트만 다룰 수 있고, 다른 사용자의 퀘스트는 `404 QUEST_NOT_FOUND`입니다.
        """)
public interface QuestApi {

    @Operation(
            summary = "퀘스트 상세",
            description = """
                    홈 카드를 탭하면 호출합니다. 같은 장소·활동을 요청 언어로 설명합니다.

                    **`status`에 따른 버튼 상태**

                    | status | 의미 | '이번엔 패스' · '사진으로 기록하기' |
                    | --- | --- | --- |
                    | `AVAILABLE` | 진행 가능 | 활성 |
                    | `SKIPPED` | 패스함 | 비활성 |
                    | `RECORDED` | 일기 저장함 | 비활성 |
                    | `EXPIRED` | 지난 날 퀘스트 (Asia/Seoul 자정 경과) | 비활성 |

                    **필드 안내**
                    - `steps`: 보통 3단계, 마지막은 사진 찍기 → "오늘의 작은 도전"
                    - `placeName`: AI 생성 장소명. 번역·실존 검증하지 않고 좌표는 주지 않습니다(지도 미사용)
                    - `locale`: 설명의 **실제** 언어. 번역이 실패하면 요청 언어와 다를 수 있습니다
                    - `placeKey`·`activityKey`: 같은 경험을 묶는 정규화 키 (화면 표시용 아님)
                    """ + USER_KEY_NOTE
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상세 조회 성공", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "한국어", value = """
                            {"status":200,"code":"GET_QUEST_DETAIL","message":"퀘스트 상세 조회에 성공했습니다.","data":{"questId":2,"dailySetId":1,"placeKey":"yeongcheon-market","activityKey":"order-market-food","category":"FOOD","estimatedMinutes":40,"title":"영천시장 수제 꽈배기 맛보기","reason":"맛있는 음식을 즐기는 당신을 위한 전통시장 간식 탐방!","description":"서대문 영천시장에서 한국 시장 음식을 직접 골라 주문해 보는 퀘스트예요.","steps":["시장에서 먹어보고 싶은 음식 찾기","한국어로 주문해 보기","음식이나 시장 풍경 사진 찍기"],"placeName":"영천시장","imageUrl":"https://cdn.example.com/quest-images/food.jpg","locale":"ko","status":"AVAILABLE"}}
                            """),
                    @ExampleObject(name = "locale=vi", value = """
                            {"status":200,"code":"GET_QUEST_DETAIL","message":"퀘스트 상세 조회에 성공했습니다.","data":{"questId":1,"dailySetId":1,"placeKey":"cinecube-gwanghwamun","activityKey":"watch-indie-film-poster","category":"MOVIE","estimatedMinutes":30,"title":"Khám phá rạp chiếu phim nghệ thuật Cinecube","reason":"Gợi ý về một rạp chiếu phim nghệ thuật tiêu biểu dành cho bạn!","description":"Xem qua nhiều áp phích và tờ rơi giới thiệu các bộ phim đang chiếu tại Cinecube.","steps":["Đi xuống sảnh của Cinecube và khám phá khu vực áp phích phim.","Xem các tờ rơi phim độc lập.","Chụp ảnh với phông nền là quầy vé hoặc bức tường áp phích."],"placeName":"씨네큐브 광화문","imageUrl":"https://cdn.example.com/quest-images/movie.jpg","locale":"vi","status":"AVAILABLE"}}
                            """),
                    @ExampleObject(name = "지난 날 퀘스트 (EXPIRED)", value = """
                            {"status":200,"code":"GET_QUEST_DETAIL","message":"퀘스트 상세 조회에 성공했습니다.","data":{"questId":2,"dailySetId":1,"placeKey":"yeongcheon-market","activityKey":"order-market-food","category":"FOOD","estimatedMinutes":40,"title":"영천시장 수제 꽈배기 맛보기","reason":"맛있는 음식을 즐기는 당신을 위한 전통시장 간식 탐방!","description":"서대문 영천시장에서 한국 시장 음식을 직접 골라 주문해 보는 퀘스트예요.","steps":["시장에서 먹어보고 싶은 음식 찾기","한국어로 주문해 보기","음식이나 시장 풍경 사진 찍기"],"placeName":"영천시장","imageUrl":"https://cdn.example.com/quest-images/food.jpg","locale":"ko","status":"EXPIRED"}}
                            """)
            })),
            @ApiResponse(responseCode = "400", description = "지원하지 않는 언어", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "UNSUPPORTED_LOCALE", value = UNSUPPORTED_LOCALE))),
            @ApiResponse(responseCode = "401", description = "`X-User-Key` 헤더 없음", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "USER_KEY_REQUIRED", value = USER_KEY_REQUIRED))),
            @ApiResponse(responseCode = "404", description = "없는 퀘스트, 다른 사용자의 퀘스트, 프로필 미설정", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "QUEST_NOT_FOUND", value = QUEST_NOT_FOUND),
                    @ExampleObject(name = "PROFILE_NOT_FOUND", summary = "locale 생략 + 프로필 없음", value = PROFILE_NOT_FOUND)
            }))
    })
    CommonResponse<QuestDetailResponse> getDetail(
            @Parameter(hidden = true) @UserKey String userKey,
            @Parameter(description = "퀘스트 ID (홈 카드의 questId)", example = "2", required = true) Long questId,
            @Parameter(description = LOCALE_PARAM, example = "vi",
                    schema = @Schema(allowableValues = {"ko", "en", "vi", "zh", "ja"})) String locale);

    @Operation(
            summary = "퀘스트 패스",
            description = """
                    '이번엔 패스' 버튼. 퀘스트를 `SKIPPED`로 바꿔 홈에서 뺍니다. 요청 본문은 없습니다.

                    - 새 퀘스트를 **보충하지 않고**, 패스 일기도 만들지 않습니다.
                    - `remainingCount`: 패스 후 오늘 남은 퀘스트 수. 앱은 WF-02로 돌아가 `GET /api/daily-quests/today`를 다시 호출합니다.
                    - 이미 패스·기록했거나 지난 날 퀘스트면 `409 QUEST_NOT_AVAILABLE`.
                    - **카메라 취소·일기 작성 취소는 패스가 아니므로** 이 API를 부르지 않습니다.
                    """ + USER_KEY_NOTE
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "패스 성공", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "패스됨", value = """
                            {"status":200,"code":"PASS_QUEST","message":"퀘스트를 패스했습니다.","data":{"questId":3,"status":"SKIPPED","remainingCount":2}}
                            """))),
            @ApiResponse(responseCode = "401", description = "`X-User-Key` 헤더 없음", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "USER_KEY_REQUIRED", value = USER_KEY_REQUIRED))),
            @ApiResponse(responseCode = "404", description = "없는 퀘스트 또는 다른 사용자의 퀘스트", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "QUEST_NOT_FOUND", value = QUEST_NOT_FOUND))),
            @ApiResponse(responseCode = "409", description = "이미 패스·기록했거나 지난 날 퀘스트", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "QUEST_NOT_AVAILABLE", value = QUEST_NOT_AVAILABLE)))
    })
    CommonResponse<QuestPassResponse> pass(
            @Parameter(hidden = true) @UserKey String userKey,
            @Parameter(description = "패스할 퀘스트 ID", example = "3", required = true) Long questId);
}
