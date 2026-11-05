# 동네메이트 ERD

작성일: 2026-11-06 · 기준: 동네메이트 API 명세서(MVP 확정본)

## 다이어그램

```mermaid
erDiagram
    PROFILES ||--|{ PROFILE_INTERESTS : "선택"
    PROFILES ||--o{ DAILY_QUEST_SETS : "날짜별 1묶음"
    DAILY_QUEST_SETS ||--|{ QUESTS : "3개 포함"
    QUESTS ||--|{ QUEST_TEXTS : "언어별 설명"
    QUESTS ||--o| DIARIES : "최대 1개 기록"
    PROFILES ||--o{ DIARIES : "작성"

    PROFILES {
        varchar user_key PK "앱 생성 UUID (X-User-Key)"
        varchar nickname "2~12자"
        varchar locale "ko/en/vi/zh/ja"
        date birth_date
        date stay_start_date
        date stay_end_date "stay_start_date 이상"
        datetime created_at
        datetime updated_at
    }

    PROFILE_INTERESTS {
        varchar user_key PK, FK
        varchar interest PK "KPOP/DRAMA/MOVIE/BEAUTY/FOOD/WEBTOON/GAME/FASHION/TOURISM"
    }

    DAILY_QUEST_SETS {
        bigint id PK
        varchar user_key FK "UNIQUE(user_key, quest_date)"
        date quest_date "Asia/Seoul 기준"
        decimal gen_latitude "클라이언트가 보낸 생성 시점 위도"
        decimal gen_longitude
        json interests_snapshot "생성 시점 관심사"
        boolean is_fallback "데모 퀘스트 대체 여부"
        datetime created_at
    }

    QUESTS {
        bigint id PK
        bigint daily_set_id FK
        varchar place_key "정규화 장소 키"
        varchar activity_key "정규화 활동 키"
        varchar category
        int estimated_minutes
        varchar place_name "AI 생성값, 미검증"
        varchar image_ref "AI 생성 이미지 S3 키(quest-images/...) 또는 팀 준비 기본 이미지 파일명"
        varchar address "AI 생성 도로명 주소, NULL 허용"
        decimal latitude "address를 네이버 Geocoding으로 변환, 실패 시 NULL"
        decimal longitude "address를 네이버 Geocoding으로 변환, 실패 시 NULL"
        varchar status "AVAILABLE/SKIPPED/RECORDED/EXPIRED"
        datetime status_changed_at
    }

    QUEST_TEXTS {
        bigint quest_id PK, FK
        varchar locale PK
        varchar title
        varchar reason
        text description
        json steps
    }

    DIARIES {
        bigint id PK
        varchar user_key FK
        bigint quest_id FK "UNIQUE"
        varchar draft_id "UNIQUE, 중복 제출 방지"
        varchar place_key "제외 판정용 복사"
        varchar activity_key "제외 판정용 복사"
        varchar title_snapshot "작성 시점 제목"
        varchar place_snapshot "작성 시점 장소명"
        varchar photo_key "S3 키, 1장"
        varchar outcome "EXPERIENCED/NOT_EXPERIENCED"
        varchar memo "최대 50자, 원문, NULL 허용"
        datetime draft_started_at "자정 예외 판정"
        datetime created_at
    }
```

## 관계

| 관계 | 카디널리티 | 설명 |
| --- | --- | --- |
| profiles → profile_interests | 1 : 1..N | 관심사 1개 이상 필수 |
| profiles → daily_quest_sets | 1 : 0..N | 사용자·날짜당 1묶음 (UNIQUE) |
| daily_quest_sets → quests | 1 : 3 | 그날 첫 ensure에서 3개 생성, 보충 없음 |
| quests → quest_texts | 1 : 1..N | 생성 언어 1건 + 요청된 언어 번역 캐시 |
| quests → diaries | 1 : 0..1 | 기록하면 1개, 패스·만료면 없음 |
| profiles → diaries | 1 : 0..N | 목록 조회·집계용 |

## 인덱스·제약

| 테이블 | 제약/인덱스 | 목적 |
| --- | --- | --- |
| daily_quest_sets | UNIQUE(user_key, quest_date) | 같은 날 묶음 중복 생성 방지 |
| quests | INDEX(daily_set_id, status) | 오늘 남은 퀘스트 조회 |
| diaries | UNIQUE(quest_id) | 퀘스트당 일기 1개 |
| diaries | UNIQUE(draft_id) | 중복 제출 시 기존 일기 반환 |
| diaries | INDEX(user_key, created_at) | 목록 최신순 조회 |
| diaries | INDEX(user_key, outcome, place_key, activity_key) | 경험한 장소+활동 제외 목록 조회 |

## 설계 메모

- 퀘스트는 사용자별로 생성되므로 별도 UserQuest 없이 `quests.status`로 상태를 관리한다.
- `EXPIRED`는 배치로 바꾸지 않고 `quest_date < 오늘`이면 만료로 판정해도 된다. 이 경우 status 컬럼에는 AVAILABLE이 남는다.
- 관심사는 API 명세서 초안의 `profiles.interests` 컬럼 대신 `profile_interests` 테이블로 분리했다. JSON 컬럼으로 두고 싶다면 이 테이블을 빼면 된다.
- diaries의 `place_key`·`activity_key`·스냅샷은 quests에서 복사해 둔다. 제외 목록 조회를 조인 없이 하고, 작성 당시 표기를 보존하기 위해서다.
- 데모 퀘스트는 별도 테이블 없이 코드/설정 파일에 두고, 대체 시 quests에 복사 저장한다.
- quests의 latitude·longitude는 퀘스트 생성 시 한 번만 변환해 저장한다(상세 조회 때 외부 호출 없음).
