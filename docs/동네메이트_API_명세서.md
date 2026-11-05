# 동네메이트 API 명세서

작성일: 2026-11-06

MVP 확정본(2026-11-06) 기준으로 다시 작성했다. 정책은 '백엔드 핸드오프 · MVP 확정본'을 따르고, 경로·필드명은 개발 제안이다.

## 개요

API는 8개다. 로그인·회원 인증·일기 수정/삭제·재도전·필터 API는 만들지 않는다.

### 공통 규약

- Base URL: `/api` (기존 컨트롤러 `/api/locations`, `/api/images`와 동일)
- 사용자 구분: 로그인이 없으므로 앱이 최초 실행 시 UUID를 만들어 기기에 저장하고, 모든 요청에 `X-User-Key: {uuid}` 헤더로 보낸다. 재설치 후 복구는 MVP 범위 밖.
- 언어: `locale` = `ko` | `en` | `vi` | `zh`(간체) | `ja`. 퀘스트 제목·추천 근거·소개·단계를 이 언어로 내려준다. 쿼리로 안 보내면 프로필 언어를 쓴다.
- 날짜 기준: 모든 "오늘"은 Asia/Seoul 자정 기준. 날짜 `YYYY-MM-DD`, 일시 ISO 8601(`+09:00`).
- 메모는 번역·덮어쓰기 없이 원문 그대로 저장·반환한다.

### 응답 포맷

모든 응답은 기존 공통 응답 `CommonResponse`(`global/dto/CommonResponse.java`) 형태를 따른다.

```json
{ "status": 200, "code": "GET_PROFILE", "message": "프로필 조회에 성공했습니다.", "data": { } }
```

- `code`는 성공 시 `{Domain}SuccessCode`, 실패 시 `{Domain}ErrorCode`(`ErrorCode` 구현 enum)의 이름이다.
- 성공 응답은 모두 HTTP `200 OK`, `status: 200`이다(생성 API 포함).
- 실패 시 HTTP 상태와 `status`가 에러 코드의 상태를 따르고 `data`는 `null`이다.

```json
{ "status": 400, "code": "MEMO_TOO_LONG", "message": "메모는 50자까지 쓸 수 있어요", "data": null }
```

공통 에러 코드 (도메인별 코드는 각 API의 표 참고)

| HTTP | code | 상황 |
| --- | --- | --- |
| 400 | INVALID_INPUT | 필수값 누락, 형식 오류 (`GlobalErrorCode`) |
| 401 | USER_KEY_REQUIRED | `X-User-Key` 없음 |
| 404 | PROFILE_NOT_FOUND | 프로필 미설정 사용자 |
| 500 | INTERNAL_SERVER_ERROR | 서버 오류 (`GlobalErrorCode`) |

### 엔드포인트 요약

| 화면 | Method | Path | 설명 |
| --- | --- | --- | --- |
| WF-01 | GET | /profile | 프로필 조회 |
| WF-01 | PUT | /profile | 프로필 저장(최초·수정 공용) |
| WF-02 | POST | /daily-quests/ensure | 오늘 묶음 생성 또는 기존 묶음 반환 |
| WF-02 | GET | /daily-quests/today | 오늘 남은 퀘스트 조회 |
| WF-03 | GET | /quests/{questId} | 퀘스트 상세 |
| WF-03 | POST | /quests/{questId}/pass | 패스(홈에서 제거, 보충 없음) |
| WF-04 | POST | /diaries | 일기 저장(사진 포함, multipart) |
| WF-05·05b | GET | /diaries | 일기 목록 + 집계 |
| 개발용 | DELETE | /dev/daily-quests/today | 오늘 묶음 초기화(`APP_DEV_RESET_ENABLED=true`일 때만) |

## 프로필 (WF-01·내 정보)

WF-01 '회원가입'은 인증이 아니라 프로필 설정이다. 최초 설정과 '내 정보' 수정 모두 `PUT /profile` 하나로 처리한다. 국가·생활 동네 입력은 없다.

### GET /profile

앱 시작 시 호출해 프로필이 있으면 WF-02, 없으면(`404 PROFILE_NOT_FOUND`) WF-01로 보낸다.

```json
{
  "status": 200,
  "code": "GET_PROFILE",
  "message": "프로필 조회에 성공했습니다.",
  "data": {
    "nickname": "하나",
    "locale": "vi",
    "birthDate": "2003-04-12",
    "stayStartDate": "2026-03-01",
    "stayEndDate": "2027-02-28",
    "interests": ["KPOP", "MOVIE", "FOOD"],
    "dDay": 114,
    "stayProgressPercent": 36
  }
}
```

`dDay`(귀국까지 남은 일수, 당일 0)와 `stayProgressPercent`(0~100)는 홈 상단 표시용이며 서버가 Asia/Seoul 오늘 기준으로 계산한다.

### PUT /profile

| 필드 | 타입 | 필수 | 규칙 |
| --- | --- | --- | --- |
| nickname | string | O | 2~12자 |
| locale | string | O | ko / en / vi / zh / ja |
| birthDate | date | O | |
| stayStartDate | date | O | |
| stayEndDate | date | O | stayStartDate 이후, 같은 날 허용 |
| interests | string[] | O | Interest enum, 1개 이상 |

응답 `200 OK`(`code: UPDATE_PROFILE`): GET과 같은 `data`.

변경 반영 시점

- `locale`: 즉시. 오늘 퀘스트 설명도 새 언어로 내려준다. 퀘스트를 새로 만들지 않고 장소·활동·남은 개수는 그대로다.
- `interests`: 다음 날 묶음부터. 오늘 묶음은 재생성하지 않는다.
- 체류 기간: 홈 D-day·경과 표시에만 쓰고 추천 입력에는 넣지 않는다.

| HTTP | code | 상황 |
| --- | --- | --- |
| 400 | INVALID_NICKNAME | 2~12자 위반 |
| 400 | INVALID_STAY_PERIOD | 종료일 < 시작일 |
| 400 | INTEREST_REQUIRED | 관심사 0개 |
| 400 | UNSUPPORTED_LOCALE | 지원하지 않는 언어 |

## 오늘의 퀘스트 (WF-02)

홈 진입 시 앱은 기기에서 얻은 현재 위도·경도를 `POST /daily-quests/ensure`로 보낸다. 서버는 이 좌표를 그대로 AI에 넘겨 반경 3km 안의 퀘스트 3개를 생성하고, AI가 만든 장소 주소를 네이버 Geocoding으로 좌표 변환해 함께 저장한다. 그날 이후 호출은 같은 묶음의 남은 퀘스트를 돌려주며, 재접속·새로고침·재요청으로 3개를 더 주는 일은 없다.

두 API 모두 `data`가 홈 카드 리스트(배열)다.

### POST /daily-quests/ensure

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| latitude | number | O | 기기 현재 위도(OS 위치 권한 필수), -90~90 |
| longitude | number | O | 기기 현재 경도, -180~180 |
| locale | string | X | 생략 시 프로필 언어 |

```json
{ "latitude": 37.5718, "longitude": 126.9620, "locale": "ko" }
```

- 오늘(Asia/Seoul) 묶음이 없으면: 요청 좌표 + 그 시점 관심사를 AI 프롬프트에 넣어 "이 좌표 반경 3km 안" 퀘스트 3개 생성 → 저장 → 반환.
- 이미 있으면: 좌표·관심사가 달라도 기존 묶음의 남은 퀘스트 반환. 패스·기록 상태를 되돌리지 않는다.
- 같은 사용자·날짜에 묶음은 하나만 유지한다(동시 호출 시에도 유니크 제약으로 보장).
- AI 생성 실패 시 팀이 준비한 데모 퀘스트 3개로 대체한다.

응답 `200 OK`

```json
{
  "status": 200,
  "code": "ENSURE_DAILY_QUESTS",
  "message": "오늘의 퀘스트 조회에 성공했습니다.",
  "data": [
    {
      "questId": 1201,
      "category": "FOOD",
      "estimatedMinutes": 40,
      "title": "시장에서 만나는 한입",
      "placeName": "영천시장",
      "imageUrl": "https://cdn.example.com/quest-images/market.jpg",
      "reason": "관심사 '음식'을 바탕으로 추천했어요"
    },
    {
      "questId": 1202,
      "category": "TOURISM",
      "estimatedMinutes": 30,
      "title": "독립문 앞, 서울의 한 장면",
      "placeName": "독립문",
      "imageUrl": "https://cdn.example.com/quest-images/landmark.jpg",
      "reason": "관심사 '관광'을 바탕으로 추천했어요"
    }
  ]
}
```

카드 항목과 필드 대응

| 카드 위치 | 필드 |
| --- | --- |
| 1행 태그 | `category` (앱에서 locale에 맞는 라벨로 표시, 예: FOOD → 음식) |
| 1행 시계 | `estimatedMinutes` ("약 40분") |
| 썸네일 | `imageUrl` (팀 준비 이미지) |
| 제목 | `title` |
| 장소 핀 아이콘 | `placeName` |
| 3행 | `reason` (추천 근거 한 줄) |
| 카드 탭 | `questId`로 WF-03 상세 이동 |

### GET /daily-quests/today

쿼리 `locale`(선택). 오늘 묶음의 남은(`AVAILABLE`) 퀘스트를 ensure와 같은 카드 리스트로 반환한다.

- 남은 퀘스트 = 최초 3개 − 패스 − 기록.
- 오늘 묶음이 아직 없으면 `404 DAILY_SET_NOT_FOUND` — 앱은 이 경우 ensure를 호출한다.
- 묶음은 있는데 빈 배열 `[]`이면 앱은 "오늘 진행할 퀘스트가 없어요!"를 표시한다. 일기가 없는 상태(WF-05b)와는 별개다.

| HTTP | code | 상황 |
| --- | --- | --- |
| 404 | DAILY_SET_NOT_FOUND | 오늘(Asia/Seoul) 묶음이 아직 없음 → 앱이 ensure 호출 |

## 퀘스트 상세·패스 (WF-03)

### GET /quests/{questId}

쿼리 `locale`(선택). 같은 장소·활동을 요청 언어로 설명한다. 본인 묶음의 퀘스트만 조회할 수 있다.

```json
{
  "status": 200,
  "code": "GET_QUEST_DETAIL",
  "message": "퀘스트 상세 조회에 성공했습니다.",
  "data": {
    "questId": 1201,
    "dailySetId": 501,
    "placeKey": "yeongcheon-market",
    "activityKey": "order-market-food",
    "category": "FOOD",
    "estimatedMinutes": 40,
    "title": "시장에서 만나는 첫 번째 한입",
    "reason": "관심사 '음식'을 바탕으로 AI가 추천했어요",
    "description": "서대문 영천시장에서 한국 시장 음식을 직접 골라 주문해 보는 퀘스트예요.",
    "steps": [
      "시장에서 먹어보고 싶은 음식 찾기",
      "한국어로 주문해 보기",
      "음식이나 시장 풍경 사진 찍기"
    ],
    "placeName": "영천시장",
    "address": "서울특별시 서대문구 통일로 189-1",
    "latitude": 37.571234,
    "longitude": 126.961234,
    "imageUrl": "https://cdn.example.com/quest-images/market.jpg",
    "locale": "ko",
    "status": "AVAILABLE"
  }
}
```

- `placeName`·`address`는 AI 생성값이며 실존 검증된 데이터가 아니다.
- `latitude`·`longitude`는 퀘스트 생성 시 `address`를 네이버 Geocoding(`GET /api/locations/geocode`와 같은 로직)으로 변환해 저장한 값이다. 주소가 없거나 변환에 실패하면 셋 다 또는 좌표만 `null`이며, 앱은 이때 지도를 숨긴다.
- `imageUrl`은 팀이 준비한 대표 이미지(카테고리·활동 매핑). AI 이미지 생성 없음.
- `status`가 `AVAILABLE`이 아니면 앱은 '사진으로 기록하기'·'이번엔 패스'를 비활성화한다.

### POST /quests/{questId}/pass

요청 본문 없음. 상태를 `SKIPPED`로 바꾸고 홈에서 뺀다. 새 퀘스트를 보충하지 않으며 패스 일기도 만들지 않는다. 앱은 WF-02로 돌아가 today를 다시 조회한다.

```json
{ "status": 200, "code": "PASS_QUEST", "message": "퀘스트를 패스했습니다.", "data": { "questId": 1201, "status": "SKIPPED", "remainingCount": 2 } }
```

| HTTP | code | 상황 |
| --- | --- | --- |
| 409 | QUEST_NOT_AVAILABLE | 이미 패스·기록·종료된 퀘스트 |
| 404 | QUEST_NOT_FOUND | 없거나 본인 묶음이 아님 |

카메라 취소·작성 취소는 패스가 아니므로 이 API를 부르지 않는다.

## 일기 저장 (WF-04)

저장 한 번으로 일기 생성과 홈 제거(`RECORDED`)가 함께 반영된다. 경험 결과와 관계없이 홈에서 빠진다. 사진 검수·AI 성공 판정은 없다.

앱 흐름: WF-03 '사진으로 기록하기' → 앱이 `draftId`(UUID)와 `draftStartedAt`(현재 시각)을 만들어 보관 → 기본 카메라 → 촬영 사진으로 WF-04 → 저장. 다시 찍기는 없고, 사진을 바꾸려면 작성을 취소하고 상세에서 다시 시작한다(새 draftId).

### POST /diaries

`Content-Type: multipart/form-data`. 서버가 사진을 S3에 저장한다.

| 파트 | 타입 | 필수 | 규칙 |
| --- | --- | --- | --- |
| photo | file | O | 1장. jpeg/png/heic, 최대 10MB |
| questId | number | O | |
| outcome | string | O | `EXPERIENCED` / `NOT_EXPERIENCED`, 사용자가 직접 선택 |
| memo | string | X | 최대 50자(51자 거부), 공란 허용, 원문 저장 |
| draftId | string | O | 촬영 시작 시 만든 UUID. 중복 제출 방지 키 |
| draftStartedAt | datetime | O | 촬영 시작 시각. 자정 예외 판정용 |

저장 허용 조건

- 퀘스트 상태가 `AVAILABLE`이면 저장.
- 상태가 `EXPIRED`여도 `draftStartedAt`이 그 퀘스트 묶음 날짜(Asia/Seoul) 안이면 저장(전날 촬영 후 작성 중 예외). 아니면 거부.
- 같은 `draftId`로 다시 오면 새로 만들지 않고 기존 일기를 `200`으로 돌려준다. 퀘스트당 일기는 1개.

응답 `200 OK`(`code: CREATE_DIARY`): `data`는 WF-05 목록의 Diary 객체와 같다. 같은 `draftId` 재제출로 기존 일기를 돌려줄 때도 `200`이다.

| HTTP | code | 상황 |
| --- | --- | --- |
| 400 | PHOTO_REQUIRED | 사진 누락 또는 2장 이상 |
| 400 | OUTCOME_REQUIRED | outcome 누락 |
| 400 | MEMO_TOO_LONG | 50자 초과 |
| 400 | INVALID_PHOTO | 형식 위반 |
| 413 | FILE_TOO_LARGE | 10MB 초과 (서버 multipart 제한에서 먼저 거부, `S3ErrorCode`) |
| 404 | QUEST_NOT_FOUND | 없거나 본인 묶음이 아님 |
| 409 | QUEST_NOT_AVAILABLE | 패스된 퀘스트, 또는 자정 예외에 해당하지 않는 종료 퀘스트 |
| 409 | ALREADY_RECORDED | 다른 draftId로 이미 기록된 퀘스트 |

저장 실패 시 앱은 사진·결과·메모를 유지하고 같은 `draftId`로 재시도한다.

## 일기 목록 (WF-05·05b)

### GET /diaries

쿼리 `locale`(선택). 조회 전용이며 수정·삭제·상세·결과 필터 API는 없다. `totalCount`가 0이면 앱은 WF-05b를 보여주고 '동네 퀘스트 보기'로 WF-02에 보낸다.

```json
{
  "status": 200,
  "code": "GET_DIARIES",
  "message": "일기 목록 조회에 성공했습니다.",
  "data": {
    "totalCount": 2,
    "experiencedCount": 1,
    "notExperiencedCount": 1,
    "groups": [
      {
        "yearMonth": "2026-11",
        "diaries": [
          {
            "diaryId": 31,
            "questId": 1201,
            "title": "시장에서 만나는 첫 번째 한입",
            "placeName": "영천시장",
            "photoUrl": "https://cdn.example.com/diaries/u1/31.jpg",
            "outcome": "NOT_EXPERIENCED",
            "memo": "시장이 문을 닫아 다음에 다시 오기로 했다.",
            "recordedAt": "2026-11-05T18:20:00+09:00"
          },
          {
            "diaryId": 28,
            "questId": 1188,
            "title": "독립문 앞, 서울의 한 장면",
            "placeName": "독립문",
            "photoUrl": "https://cdn.example.com/diaries/u1/28.jpg",
            "outcome": "EXPERIENCED",
            "memo": null,
            "recordedAt": "2026-11-02T15:04:00+09:00"
          }
        ]
      }
    ]
  }
}
```

- 정렬: 작성 시각 최신순, 월별 묶음.
- `title`·`placeName`은 `locale`에 맞춘 퀘스트 설명이다. 장소·활동 정체성은 같고 표현만 바뀐다. `memo`는 언어와 관계없이 원문.
- 못 경험한 기록도 정상 일기로 같은 목록에 나온다.
- MVP 데이터 규모가 작으므로 페이지네이션 없이 전체 반환한다(필요해지면 cursor 추가).

## 개발용 (QA)

### DELETE /dev/daily-quests/today

같은 날에는 `ensure`가 기존 묶음만 반환하므로, 퀘스트 생성 흐름을 다시 테스트할 때 오늘 묶음을 지운다.

- 서버 환경변수 `APP_DEV_RESET_ENABLED=true`일 때만 존재한다. 기본값은 꺼짐이며, 꺼져 있으면 404. **운영에서는 켜지 않는다.**
- 헤더 `X-User-Key` 필수. 해당 사용자의 오늘(Asia/Seoul) 묶음과 그 퀘스트·번역·일기를 삭제한다.
- 다른 날짜의 묶음·일기(중복 추천 제외 목록)는 그대로 둔다. 일기 사진(S3)은 삭제하지 않는다.
- 묶음이 없어도 200이며 `deletedQuestCount`는 0.
- 삭제 후 `today`는 `404 DAILY_SET_NOT_FOUND`, `ensure`는 새 묶음을 생성한다.

```json
{
  "status": 200,
  "code": "RESET_TODAY_QUESTS",
  "message": "오늘 퀘스트 묶음을 초기화했습니다.",
  "data": { "deletedQuestCount": 3 }
}
```

## 상태·데이터 모델

### 퀘스트 상태 (UserQuest.status)

모든 퀘스트는 `AVAILABLE`로 시작해 아래 셋 중 하나로 끝난다. 경험 결과는 상태가 아니라 일기의 `outcome`에 저장한다.

| 전이 | 계기 | 홈 표시 |
| --- | --- | --- |
| 생성 → AVAILABLE | 그날 첫 ensure | O |
| AVAILABLE → SKIPPED | 패스 | X, 보충 없음 |
| AVAILABLE → RECORDED | 일기 저장(경험/못 경험 무관) | X |
| AVAILABLE → EXPIRED | 다음 날 자정(Asia/Seoul) | X |
| EXPIRED → RECORDED | 전날 촬영 시작한 초안 저장(예외) | X |

카메라 취소·작성 취소는 상태를 바꾸지 않는다. `EXPIRED`는 배치 없이 조회 시점에 "묶음 날짜 < 오늘"로 판정해도 된다.

### Enum

| Enum | 값 |
| --- | --- |
| Interest / Category | KPOP, DRAMA, MOVIE, BEAUTY, FOOD, WEBTOON, GAME, FASHION, TOURISM |
| Locale | ko, en, vi, zh, ja |
| QuestStatus | AVAILABLE, SKIPPED, RECORDED, EXPIRED |
| Outcome | EXPERIENCED, NOT_EXPERIENCED |

### 테이블(초안)

| 테이블 | 주요 컬럼 | 제약 |
| --- | --- | --- |
| profiles | user_key, nickname, locale, birth_date, stay_start_date, stay_end_date, created_at, updated_at | PK user_key |
| profile_interests | user_key, interest | PK(user_key, interest) |
| daily_quest_sets | id, user_key, quest_date, created_at, gen_latitude, gen_longitude, interests_snapshot, is_fallback | UNIQUE(user_key, quest_date) |
| quests | id, daily_set_id, place_key, activity_key, category, estimated_minutes, place_name, image_ref, address, latitude, longitude, status, status_changed_at | address·latitude·longitude nullable |
| quest_texts | quest_id, locale, title, reason, description, steps(json) | PK(quest_id, locale) |
| diaries | id, user_key, quest_id, draft_id, place_key, activity_key, title_snapshot, place_snapshot, photo_key, outcome, memo, draft_started_at, created_at | UNIQUE(quest_id), UNIQUE(draft_id) |

- MVP는 퀘스트를 사용자별로 생성하므로 UserQuest를 따로 두지 않고 `quests.status`로 합쳤다.
- 관심사는 `profile_interests` 테이블로 분리한다(상세는 ERD 참고).
- `quest_texts`는 생성 언어로 먼저 저장하고, 다른 언어 요청 시 번역해 캐시한다.

## 퀘스트 생성 규칙 (ensure 내부)

ensure가 그날 첫 호출일 때만 LLM을 부른다. 장소·활동까지 AI가 자유 생성하고, MVP는 실존 검색 대조를 하지 않는다.

| 항목 | 규칙 |
| --- | --- |
| 개수 | 3개 |
| 위치 | 요청 좌표 기준 반경 3,000m 이내(생성 요구조건, 검증 아님) |
| 관심사 | 선호 가중치. 선택하지 않은 분야도 섞어 추천 |
| 체류 기간 | 입력에서 제외 |
| 제외 | 과거 `EXPERIENCED` 일기의 (place_key, activity_key) 조합 |
| 재추천 허용 | `SKIPPED`·`NOT_EXPERIENCED` 조합은 다음 날부터 가능, 같은 장소의 다른 활동도 가능 |
| 출력 언어 | 요청 `locale` |
| 이미지 | 생성된 category·activity로 팀 이미지 매핑 |
| 실패 | 팀 준비 데모 3개로 대체(`is_fallback=true`) |

LLM 출력은 퀘스트 응답 구조에 맞춘 JSON으로 받는다.

```json
{
  "quests": [
    {
      "placeKey": "yeongcheon-market",
      "activityKey": "order-market-food",
      "category": "FOOD",
      "estimatedMinutes": 40,
      "placeName": "영천시장",
      "address": "서울특별시 서대문구 통일로 189-1",
      "title": "시장에서 만나는 첫 번째 한입",
      "reason": "관심사 '음식'을 바탕으로 추천했어요",
      "description": "...",
      "steps": ["...", "...", "..."]
    }
  ]
}
```

`placeKey`·`activityKey`는 표현이 달라도 같은 경험을 묶기 위한 정규화 키다(소문자·공백 제거 등, 방식은 개발 결정). 제외 목록은 이 키 쌍으로 프롬프트에 넣고, 응답에서도 한 번 더 걸러낸다.

### 위치 처리

- 요청 위치는 앱이 OS 위치 권한으로 얻은 위도·경도다. 서버는 이 값을 `daily_quest_sets.gen_latitude/gen_longitude`에 저장하고 프롬프트에 그대로 넣는다.
- 프롬프트 예: "사용자 관심사는 음식, 영화, KPOP이다. 위도 37.5718, 경도 126.9620 지점에서 반경 3km 안에 있는 장소로 퀘스트 3개를 만들어라. 관심사를 우선 반영하되 다른 분야도 섞어라. 다음 장소+활동 조합은 제외하라: [이미 경험한 목록]."
- AI는 장소마다 도로명 주소(`address`)도 만든다. 서버는 생성 직후 이 주소를 네이버 Geocoding(`LocationService`, `GET /api/locations/geocode`와 같은 로직)으로 변환해 `quests.latitude/longitude`에 저장한다.
  - 3곳을 병렬로 변환하고 제한 시간(기본 3초)을 넘기거나 실패하면 좌표 없이 저장한다. 퀘스트 생성은 실패시키지 않는다.
  - 상세 조회 때는 다시 변환하지 않고 저장된 값을 내려준다.
- 변환한 좌표가 실제로 요청 좌표 3km 안인지는 검증하지 않는다.

## 개발팀 결정 필요 사항

핸드오프 문서가 개발자 판단으로 넘긴 항목이다. 이 문서는 괄호 안 안으로 작성했다.

- [ ] 익명 사용자 구분 방식 (앱 생성 UUID + `X-User-Key` 헤더)
- [ ] 사진 업로드 방식 (multipart 일기 요청에 포함, 서버가 S3 저장 / 대안: Presigned URL 분리)
- [ ] 자정 초안 예외 판정 (클라이언트 `draftStartedAt` 신뢰 / 대안: 서버가 촬영 시작 시 토큰 발급)
- [ ] place_key·activity_key 정규화 방식
- [ ] 다국어 설명 생성 방식 (요청 시 번역 후 캐시 / 대안: 생성 시 5개 언어 동시 생성)
- [ ] LLM 제공사
- [ ] 사진 최대 크기·압축, 일기 목록 페이지네이션 여부
- [ ] 팀 자산 전달: 대표 이미지와 매핑 기준, 데모 퀘스트 3개, API 키(문서에 원문 기재 금지)
