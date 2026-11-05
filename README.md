# Thingkerton Server

명지대학교 해커톤을 위한 Spring Boot 백엔드 프로젝트입니다.

## 기술 스택

| 구분 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.2 |
| Build | Gradle |
| Database | MySQL 8.4 (운영: AWS RDS, 로컬: Docker Compose) |
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

## 문서

| 문서 | 내용 |
|---|---|
| [API 명세서](docs/동네메이트_API_명세서.md) | 엔드포인트, 요청·응답, 에러 코드, 상태 전이, 퀘스트 생성 규칙 |
| [ERD](docs/동네메이트_ERD.md) | 테이블, 관계, 인덱스·제약 |
| [AGENTS.md](AGENTS.md) | 코딩 에이전트·개발자 공통 구현 가이드 (Claude Code는 로컬 `CLAUDE.md`에 `@AGENTS.md` 추가) |

## 로컬 실행

로컬 DB는 Docker Compose로 띄운 MySQL 8.4를 사용합니다. (운영 RDS와 같은 버전)
Docker Compose와 Spring이 같은 `.env`를 함께 읽기 때문에 계정 정보는 `.env` 한 곳에서만 관리합니다.

1. `.env`를 만듭니다. 예시 값 그대로 사용해도 됩니다.

```bash
cp .env.example .env        # Windows: copy .env.example .env
```

2. MySQL을 실행합니다. `thingkerton` DB와 앱 계정이 자동으로 만들어집니다.

```bash
docker compose up -d
docker compose ps           # STATUS가 healthy가 되면 준비 완료
```

3. 애플리케이션을 실행합니다.

```bash
./gradlew bootRun           # Windows: .\gradlew.bat bootRun
```

4. `http://localhost:8080/swagger-ui/index.html`에서 API 문서를 확인합니다.

### 자주 쓰는 명령

| 명령 | 설명 |
|---|---|
| `docker compose up -d` | MySQL 실행 |
| `docker compose stop` | MySQL 중지 (데이터 유지) |
| `docker compose logs -f mysql` | MySQL 로그 확인 |
| `docker compose exec mysql mysql -uthingkerton -pthingkerton thingkerton` | MySQL 콘솔 접속 |
| `docker compose down -v` | 컨테이너와 데이터 모두 삭제 (DB 초기화) |

- 로컬에 설치된 MySQL이 이미 3306 포트를 쓰고 있다면 중지하거나, `.env`에 `DB_PORT=3307`을 추가하고 `DB_URL`의 포트도 3307로 바꿉니다.
- 기존 `.env`를 쓰던 경우 `DB_USERNAME`, `DB_PASSWORD`를 `.env.example`과 같이 바꿔야 합니다. 컨테이너의 앱 계정은 처음 생성될 때의 값으로 만들어지므로, 계정을 바꾼 뒤에는 `docker compose down -v`로 초기화합니다.

## 테스트

```powershell
.\gradlew.bat test
```

테스트는 인메모리 H2를 MySQL 호환 모드로 사용하므로 MySQL을 실행하지 않아도 됩니다.

## 협업 규칙

- 브랜치: `{type}/#{issue-number}-{description}`
- 커밋: `{type}: {description} - #{issue-number}`
- 타입: `feat`, `fix`, `refactor`, `docs`, `style`, `test`, `build`, `chore`, `ci`

## 코드 리뷰 (CodeRabbit)

`main` 대상 PR이 열리면 CodeRabbit이 자동으로 한국어 리뷰를 남깁니다. 설정은 `.coderabbit.yaml`에 있습니다.

- Draft PR, 제목에 `WIP` 또는 `[skip review]`가 들어간 PR은 자동 리뷰를 건너뜁니다.
- PR 코멘트로 CodeRabbit에게 요청할 수 있습니다.

| 명령 | 동작 |
|---|---|
| `@coderabbitai review` | 변경분 리뷰 |
| `@coderabbitai full review` | 전체 다시 리뷰 |
| `@coderabbitai pause` / `resume` | 자동 리뷰 일시 중지 / 재개 |
| `@coderabbitai resolve` | 리뷰 코멘트 모두 resolve |

## AI 연동 (Gemini)

Spring AI + Google Gemini API를 사용합니다. 공통 진입점은 `global/infra/ai/AiClient`이며, 도메인 코드는 Spring AI에 직접 의존하지 않고 `AiClient`만 사용합니다.

### API 키 발급

1. https://aistudio.google.com 접속 → **Get API key** → **Create API key**
2. 결제 정보를 등록하지 않으면 무료 등급으로 유지됩니다.
3. `.env`에 추가합니다. (키는 커밋하지 않습니다)

```
GEMINI_API_KEY=발급받은키
# GEMINI_CHAT_MODEL=gemini-3.6-flash   # 생략 시 기본값
```

키가 없어도 서버는 실행되지만 AI 호출은 `AI_CALL_FAILED`로 실패합니다. 배포 서버는 EC2의 `.env`에도 추가해야 합니다.

### 사용법

| 메서드 | 용도 |
|---|---|
| `chat(systemPrompt, message)` | 텍스트 대화 |
| `chatWithImage(systemPrompt, message, AiImage)` | 이미지 포함 대화 (인증 사진 확인 등) |
| `chat(systemPrompt, message, Type.class)` | 응답을 record 등으로 변환 (JSON) |
| `chatWithImage(systemPrompt, message, AiImage, Type.class)` | 이미지 + 구조화 응답 |

```java
record Verification(boolean passed, String reason) {}

Verification result = aiClient.chatWithImage(
        "너는 퀘스트 인증 심사관이야.",
        "이 사진에 파전이 있는지 판정해줘.",
        AiImage.from(file),
        Verification.class);
```

- 요청 한도 초과 시 `AI_RATE_LIMITED`(429), 그 외 실패 시 `AI_CALL_FAILED`(503)
- 연동 확인용 임시 API: Swagger의 **AI Test** (`POST /api/ai/test`, `POST /api/ai/test/image`)
- 테스트는 실제 Gemini를 호출하지 않으므로 키 없이 통과합니다.

## 이미지 업로드 (S3)

| 항목 | 값 |
|---|---|
| 버킷 | `mju-hackathon-photos-2026` (ap-southeast-2, 퍼블릭 읽기) |
| 허용 형식 | jpg, png, webp / 최대 10MB |
| 파일 경로 | `{dir}/{UUID}.{확장자}` |
| URL | `https://mju-hackathon-photos-2026.s3.ap-southeast-2.amazonaws.com/{key}` |

- 다른 기능에서는 `S3Uploader.uploadImage(file, dir)`를 사용합니다. 반환값의 `url`은 DB에 저장하고, `key`는 삭제할 때 사용합니다.
- 테스트용 API: `POST /api/images` (multipart `file`)
- 자격 증명은 코드에 두지 않습니다. EC2는 IAM Role을 사용하고, 로컬에서 업로드를 테스트하려면 `aws configure`로 개인 키를 설정합니다.

## CI/CD

| 워크플로우 | 트리거 | 동작 |
|---|---|---|
| `ci.yml` | `main` 대상 PR | `./gradlew build` (컴파일 + 테스트) |
| `cd.yml` | `main` push, 수동 실행 | `app.jar` 빌드 → Docker 이미지 빌드·Docker Hub push(`:커밋SHA`, `:latest`) → EC2에서 pull 후 컨테이너 교체 → 헬스체크(실패 시 이전 이미지로 롤백) |
| `nginx.yml` | `main` push 중 `deploy/nginx/**` 변경, 수동 실행 | nginx 설정 전송 → `nginx -t` 통과 시 reload, 실패 시 이전 설정으로 복구 |

### 서버 구성

```text
https://soptinthingji.p-e.kr ─▶ nginx :443 (Let's Encrypt) ─▶ Docker 컨테이너 hackathon (Spring 127.0.0.1:8080) ─▶ AWS RDS MySQL
```

| 항목 | 값 |
|---|---|
| 서버 | EC2 Ubuntu (`ubuntu` 사용자) |
| 앱 | Docker 컨테이너 `hackathon` (이미지 `{DOCKERHUB_USERNAME}/hackathon-server`, Docker Hub private 저장소, `--network host`, `--restart unless-stopped`) |
| 환경 변수 | `/home/ubuntu/hackathon-server/.env` (`docker run --env-file`로 전달. 따옴표 없는 `KEY=VALUE` 형식 유지) |
| 이전 방식 | `hackathon.service`(systemd, `app.jar`)는 비활성화 상태로 남겨 둠. 첫 전환 실패 시 CD가 자동 복구에 사용 |
| 리버스 프록시 | `/etc/nginx/sites-available/hackathon-server` (`sites-enabled`에 링크). 원본은 레포의 `deploy/nginx/hackathon-server.conf` |

컨테이너 실행 방식 (`cd.yml`이 자동으로 수행):

```bash
docker run -d --name hackathon --restart unless-stopped --network host \
  --env-file /home/ubuntu/hackathon-server/.env -e SERVER_ADDRESS=127.0.0.1 \
  {DOCKERHUB_USERNAME}/hackathon-server:<커밋SHA>
```

- `--network host`: 컨테이너에서도 EC2 IAM Role(S3 업로드)을 그대로 사용합니다.
- `SERVER_ADDRESS=127.0.0.1`: Spring은 서버 내부에서만 받고, 외부 요청은 nginx(443)를 거칩니다.
- Docker 로그는 컨테이너당 20MB × 3개까지 보관합니다(`/etc/docker/daemon.json`).

### GitHub Secrets

Settings → Secrets and variables → Actions에 등록합니다. DB 접속 정보는 서버의 `.env`에만 두고 GitHub에는 등록하지 않습니다.

| 이름 | 값 |
|---|---|
| `EC2_HOST` | EC2 퍼블릭 IP |
| `EC2_USERNAME` | `ubuntu` |
| `EC2_SSH_KEY` | `.pem` 키 파일 내용 전체 |
| `DOCKERHUB_USERNAME` | Docker Hub 아이디 |
| `DOCKERHUB_TOKEN` | Docker Hub Access Token (Account settings → Personal access tokens, 권한 Read & Write) |

Docker Hub에는 `hackathon-server` 저장소를 **private**으로 미리 만들어 둡니다. 없으면 첫 push 때 계정 기본 설정(보통 public)으로 생성됩니다.

### 운영 명령어

```bash
docker ps                                   # 상태 확인
docker logs -f --tail 100 hackathon         # 실시간 로그
docker restart hackathon                    # 수동 재시작
docker images {DOCKERHUB_USERNAME}/hackathon-server   # 롤백 가능한 이미지 (최근 3개)

# 수동 롤백: 원하는 이미지 태그로 교체
docker rm -f hackathon && docker run -d --name hackathon --restart unless-stopped --network host \
  --env-file /home/ubuntu/hackathon-server/.env -e SERVER_ADDRESS=127.0.0.1 \
  {DOCKERHUB_USERNAME}/hackathon-server:<커밋SHA>
```

- 시연 중 자동 배포를 막으려면 Actions → CD → Disable workflow로 잠시 꺼둡니다.

### nginx 설정

- 설정 원본은 `deploy/nginx/hackathon-server.conf`입니다. **서버에서 직접 수정하지 말고** 이 파일을 수정해 PR로 반영합니다. 서버에서 직접 고치면 다음 적용 때 덮어써집니다.
- main에 머지되면 `nginx.yml`이 서버에 복사하고 `nginx -t`로 검사한 뒤 reload합니다. 검사에 실패하면 이전 설정으로 되돌리고 워크플로우가 실패합니다.
- 백업은 서버 `~/hackathon-server/nginx/backup/`에 최근 5개를 남깁니다.
- 업로드 제한은 `client_max_body_size 10M`입니다 (nginx 기본값은 1MB).
- `# managed by Certbot` 줄(SSL 인증서 경로 등)은 Certbot이 추가한 내용이므로 그대로 둡니다. 인증서 갱신은 서버의 `certbot.timer`가 처리합니다.
