package sopt.thingkerton.domain.diary.controller;

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
import sopt.thingkerton.domain.diary.dto.DiaryCreateRequest;
import sopt.thingkerton.domain.diary.dto.DiaryListResponse;
import sopt.thingkerton.domain.diary.dto.DiaryResponse;
import sopt.thingkerton.global.auth.UserKey;
import sopt.thingkerton.global.dto.CommonResponse;

@Tag(name = "Diary", description = """
        일기 저장(WF-04) · 나의 유학 일기(WF-05). 조회 전용이며 일기 수정·삭제·상세·필터 API는 없습니다.
        """)
public interface DiaryApi {

    String DIARY_EXAMPLE = """
            {"status":200,"code":"CREATE_DIARY","message":"일기를 저장했습니다.","data":{"diaryId":31,"questId":2,"title":"영천시장 수제 꽈배기 맛보기","placeName":"영천시장","photoUrl":"https://mju-hackathon-photos-2026.s3.ap-southeast-2.amazonaws.com/diaries/7b0c1d2e-0000-4000-8000-000000000001/5f1c2a9e.jpg","outcome":"EXPERIENCED","memo":"처음 먹어본 호떡, 생각보다 달았다.","recordedAt":"2026-11-06T18:20:00+09:00"}}
            """;

    @Operation(
            summary = "일기 저장 (사진 포함)",
            description = """
                    '내 유학 일기에 남기기' 버튼. `multipart/form-data` 한 번으로 **사진 S3 업로드 + 일기 생성 + 퀘스트 `RECORDED` 전환**을 함께 처리합니다.
                    경험 결과(해봤어요/못 했어요)와 관계없이 홈에서 빠집니다. 사진 검수·AI 성공 판정은 없습니다.

                    **앱 흐름**
                    1. 상세(WF-03) '사진으로 기록하기' → 앱이 `draftId`(UUID)와 `draftStartedAt`(현재 시각)을 만들어 보관
                    2. 기기 기본 카메라로 촬영 (취소하면 상세 유지, 패스 아님)
                    3. 일기 작성(WF-04): 결과 선택 + 사진 있음 → 저장 버튼 활성화 (WF-04a)
                    4. 저장 성공 → 나의 유학 일기(WF-05)로 이동

                    **규칙**
                    | 항목 | 규칙 | 위반 시 |
                    | --- | --- | --- |
                    | photo | 정확히 1장 | `400 PHOTO_REQUIRED` |
                    | photo 형식 | jpeg · png · heic | `400 INVALID_PHOTO` |
                    | photo 크기 | 10MB 이하 | `413 FILE_TOO_LARGE` |
                    | outcome | 필수 | `400 OUTCOME_REQUIRED` |
                    | memo | 50자 이하, 공란 허용(공란은 `null`로 저장), 원문 그대로 | `400 MEMO_TOO_LONG` |
                    | questId · draftId · draftStartedAt | 필수, draftStartedAt은 ISO 8601 | `400 INVALID_INPUT` |

                    **중복 제출 방지**: 같은 `draftId`로 다시 보내면 새로 만들지 않고 기존 일기를 `200`으로 돌려줍니다(사진도 다시 올리지 않음).
                    저장이 실패하면 사진·결과·메모를 유지하고 **같은 `draftId`로 재시도**하세요. 사진을 바꾸려면 작성을 취소하고 새 `draftId`로 다시 시작합니다.

                    **저장 가능 조건**
                    - 퀘스트가 `AVAILABLE`이면 저장
                    - `EXPIRED`(자정이 지남)여도 `draftStartedAt`이 그 퀘스트 날짜(Asia/Seoul) 안이면 저장 — 전날 밤 촬영 후 자정을 넘겨 작성한 경우
                    - 패스한 퀘스트, 자정 예외에 해당하지 않는 지난 퀘스트 → `409 QUEST_NOT_AVAILABLE`
                    - 다른 `draftId`로 이미 기록한 퀘스트 → `409 ALREADY_RECORDED`

                    응답 `data`는 `GET /api/diaries`의 일기 항목과 같고, `title`은 프로필 언어 제목입니다.
                    """ + USER_KEY_NOTE
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공 또는 같은 draftId 재제출(기존 일기 반환)", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "해봤어요 + 메모", value = DIARY_EXAMPLE),
                    @ExampleObject(name = "못 했어요 + 메모 없음", value = """
                            {"status":200,"code":"CREATE_DIARY","message":"일기를 저장했습니다.","data":{"diaryId":32,"questId":1,"title":"예술영화관 씨네큐브 탐방","placeName":"씨네큐브 광화문","photoUrl":"https://mju-hackathon-photos-2026.s3.ap-southeast-2.amazonaws.com/diaries/7b0c1d2e-0000-4000-8000-000000000001/a81b.jpg","outcome":"NOT_EXPERIENCED","memo":null,"recordedAt":"2026-11-06T19:02:00+09:00"}}
                            """)
            })),
            @ApiResponse(responseCode = "400", description = "입력값 위반", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "PHOTO_REQUIRED", summary = "사진 누락 또는 2장 이상", value = """
                            {"status":400,"code":"PHOTO_REQUIRED","message":"사진 1장을 첨부해 주세요.","data":null}
                            """),
                    @ExampleObject(name = "INVALID_PHOTO", summary = "jpeg·png·heic가 아님", value = """
                            {"status":400,"code":"INVALID_PHOTO","message":"사진은 jpeg, png, heic 형식, 10MB 이하만 올릴 수 있어요.","data":null}
                            """),
                    @ExampleObject(name = "OUTCOME_REQUIRED", value = """
                            {"status":400,"code":"OUTCOME_REQUIRED","message":"경험 결과를 선택해 주세요.","data":null}
                            """),
                    @ExampleObject(name = "MEMO_TOO_LONG", summary = "메모 51자 이상", value = """
                            {"status":400,"code":"MEMO_TOO_LONG","message":"메모는 50자까지 쓸 수 있어요.","data":null}
                            """),
                    @ExampleObject(name = "INVALID_INPUT", summary = "questId·draftId·draftStartedAt 누락·형식 오류, 잘못된 outcome 값", value = """
                            {"status":400,"code":"INVALID_INPUT","message":"유효하지 않은 값입니다.","data":null}
                            """)
            })),
            @ApiResponse(responseCode = "401", description = "`X-User-Key` 헤더 없음", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "USER_KEY_REQUIRED", value = USER_KEY_REQUIRED))),
            @ApiResponse(responseCode = "404", description = "없는 퀘스트 또는 다른 사용자의 퀘스트", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "QUEST_NOT_FOUND", value = QUEST_NOT_FOUND))),
            @ApiResponse(responseCode = "409", description = "기록할 수 없는 퀘스트", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "QUEST_NOT_AVAILABLE", summary = "패스한 퀘스트, 자정 예외가 아닌 지난 퀘스트", value = QUEST_NOT_AVAILABLE),
                    @ExampleObject(name = "ALREADY_RECORDED", summary = "다른 draftId로 이미 기록함", value = """
                            {"status":409,"code":"ALREADY_RECORDED","message":"이미 기록한 퀘스트입니다.","data":null}
                            """)
            })),
            @ApiResponse(responseCode = "413", description = "사진 10MB 초과 (서버 업로드 제한에서 거부)", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "FILE_TOO_LARGE", value = """
                            {"status":413,"code":"FILE_TOO_LARGE","message":"파일 크기는 10MB 이하여야 합니다.","data":null}
                            """))),
            @ApiResponse(responseCode = "500", description = "사진 업로드 실패 → 입력값 유지 후 같은 draftId로 재시도", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "UPLOAD_FAILED", value = """
                            {"status":500,"code":"UPLOAD_FAILED","message":"파일 업로드에 실패했습니다. 다시 시도해 주세요.","data":null}
                            """)))
    })
    CommonResponse<DiaryResponse> create(@Parameter(hidden = true) @UserKey String userKey, DiaryCreateRequest request);

    @Operation(
            summary = "나의 유학 일기 목록",
            description = """
                    나의 유학 일기 탭(WF-05). 일기 전체를 **작성 시각 최신순**으로 **월별 묶음**과 함께 반환합니다. 페이지네이션은 없습니다.

                    | 필드 | 화면 |
                    | --- | --- |
                    | `totalCount` · `experiencedCount` · `notExperiencedCount` | 상단 통계 "전체 기록 / 해봤어요! / 이번엔 못 했어요" |
                    | `groups[].yearMonth` | 월 제목 "2026년 11월" (Asia/Seoul) |
                    | `diaries[].recordedAt` · `placeName` | "2026.11.05 · 영천시장" |
                    | `diaries[].outcome` | 배지 (EXPERIENCED = "✓ 해봤어요!", NOT_EXPERIENCED = "이번엔 못 했어요") |
                    | `diaries[].title` · `memo` | 제목, 한 줄 메모 (`memo`가 `null`이면 숨김) |

                    - `title`은 요청 언어로 바뀌고, `memo`는 언어와 관계없이 사용자가 쓴 원문입니다. `placeName`은 작성 당시 장소명입니다.
                    - 못 경험한 기록도 정상 일기로 같은 목록에 나옵니다.
                    - `totalCount`가 0이면 WF-05b "아직 기록이 없어요" → '동네 퀘스트 보기'로 홈 이동.
                    """ + USER_KEY_NOTE
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "일기 목록", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "일기 2개", value = """
                            {"status":200,"code":"GET_DIARIES","message":"일기 목록 조회에 성공했습니다.","data":{"totalCount":2,"experiencedCount":1,"notExperiencedCount":1,"groups":[{"yearMonth":"2026-11","diaries":[
                              {"diaryId":31,"questId":1201,"title":"시장에서 만나는 첫 번째 한입","placeName":"영천시장","photoUrl":"https://mju-hackathon-photos-2026.s3.ap-southeast-2.amazonaws.com/diaries/u1/31.jpg","outcome":"NOT_EXPERIENCED","memo":"오늘은 시장이 문을 닫아 다음에 다시 오기로 했다.","recordedAt":"2026-11-05T18:20:00+09:00"},
                              {"diaryId":28,"questId":1188,"title":"독립문 앞, 서울의 한 장면","placeName":"독립문","photoUrl":"https://mju-hackathon-photos-2026.s3.ap-southeast-2.amazonaws.com/diaries/u1/28.jpg","outcome":"EXPERIENCED","memo":null,"recordedAt":"2026-11-02T15:04:00+09:00"}
                            ]}]}}
                            """),
                    @ExampleObject(name = "기록 0개 (WF-05b)", value = """
                            {"status":200,"code":"GET_DIARIES","message":"일기 목록 조회에 성공했습니다.","data":{"totalCount":0,"experiencedCount":0,"notExperiencedCount":0,"groups":[]}}
                            """)
            })),
            @ApiResponse(responseCode = "400", description = "지원하지 않는 언어", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "UNSUPPORTED_LOCALE", value = UNSUPPORTED_LOCALE))),
            @ApiResponse(responseCode = "401", description = "`X-User-Key` 헤더 없음", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "USER_KEY_REQUIRED", value = USER_KEY_REQUIRED))),
            @ApiResponse(responseCode = "404", description = "locale 생략 + 프로필 미설정", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "PROFILE_NOT_FOUND", value = PROFILE_NOT_FOUND)))
    })
    CommonResponse<DiaryListResponse> getDiaries(
            @Parameter(hidden = true) @UserKey String userKey,
            @Parameter(description = LOCALE_PARAM, example = "en",
                    schema = @Schema(allowableValues = {"ko", "en", "vi", "zh", "ja"})) String locale);
}
