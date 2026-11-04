# Thingkerton Server

명지대학교 해커톤을 위한 Spring Boot 백엔드 프로젝트입니다.

## 기술 스택

| 구분 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.2 |
| Build | Gradle |
| Database | AWS RDS MySQL |
| ORM | Spring Data JPA |
| API Documentation | SpringDoc OpenAPI (Swagger UI) |
| Validation | Spring Validation |

## 포함된 초기 설정

- 공통 API 응답과 성공·에러 코드
- 전역 비즈니스·Validation 예외 처리
- Swagger/OpenAPI와 CORS 설정
- JPA 생성·수정 시간 감사 필드
- RDS MySQL 연결 설정
- RDS 없이 실행되는 애플리케이션 컨텍스트 테스트

## 프로젝트 구조

```text
src
├─ main
│  ├─ java/sopt/thingkerton
│  │  ├─ ThingkertonApplication.java
│  │  └─ global
│  │     ├─ code
│  │     ├─ config
│  │     ├─ dto
│  │     ├─ entity
│  │     └─ exception
│  └─ resources/application.yaml
└─ test
```

## 로컬 실행

1. `.env.example`을 `.env`로 복사하고 RDS 접속 정보를 입력합니다.

```properties
DB_URL=jdbc:mysql://<rds-endpoint>:3306/<database>?serverTimezone=Asia/Seoul
DB_USERNAME=<username>
DB_PASSWORD=<password>
SERVER_PORT=8080
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

2. 애플리케이션을 실행합니다.

```powershell
.\gradlew.bat bootRun
```

3. `http://localhost:8080/swagger-ui/index.html`에서 API 문서를 확인합니다.

## 테스트

```powershell
.\gradlew.bat test
```

테스트는 인메모리 H2를 MySQL 호환 모드로 사용하므로 RDS 접속 정보가 필요하지 않습니다.

## 협업 규칙

- 브랜치: `{type}/#{issue-number}-{description}`
- 커밋: `{type}: {description} - #{issue-number}`
- 타입: `feat`, `fix`, `refactor`, `docs`, `style`, `test`, `build`, `chore`, `ci`

EC2 배포와 배포용 GitHub Actions는 아직 포함하지 않았습니다.
