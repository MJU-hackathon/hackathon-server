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

## CI/CD

| 워크플로우 | 트리거 | 동작 |
|---|---|---|
| `ci.yml` | `main` 대상 PR | `./gradlew build` (컴파일 + 테스트) |
| `cd.yml` | `main` push, 수동 실행 | `app.jar` 빌드 → EC2 `~/hackathon-server`에 전송 → `systemctl restart hackathon` |

### 서버 구성

```text
https://soptinthingji.p-e.kr ─▶ nginx :443 (Let's Encrypt) ─▶ Spring :8080 ─▶ AWS RDS MySQL
```

| 항목 | 값 |
|---|---|
| 서버 | EC2 Ubuntu (`ubuntu` 사용자) |
| 앱 경로 | `/home/ubuntu/hackathon-server/app.jar` |
| 환경 변수 | `/home/ubuntu/hackathon-server/.env` (실행 디렉터리의 `.env`를 자동으로 읽음) |
| 서비스 | `/etc/systemd/system/hackathon.service` |
| 리버스 프록시 | `/etc/nginx/sites-enabled/hackathon-server` |

`hackathon.service`:

```ini
[Unit]
Description=Thingkerton Spring Boot Server
After=network-online.target
Wants=network-online.target

[Service]
User=ubuntu
WorkingDirectory=/home/ubuntu/hackathon-server
ExecStart=/usr/bin/java -jar /home/ubuntu/hackathon-server/app.jar
Restart=on-failure
RestartSec=5
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
```

### GitHub Secrets

Settings → Secrets and variables → Actions에 등록합니다. DB 접속 정보는 서버의 `.env`에만 두고 GitHub에는 등록하지 않습니다.

| 이름 | 값 |
|---|---|
| `EC2_HOST` | EC2 퍼블릭 IP |
| `EC2_USERNAME` | `ubuntu` |
| `EC2_SSH_KEY` | `.pem` 키 파일 내용 전체 |

### 운영 명령어

```bash
sudo systemctl status hackathon      # 상태 확인
journalctl -u hackathon -f           # 실시간 로그
sudo systemctl restart hackathon     # 수동 재시작
```

- 시연 중 자동 배포를 막으려면 Actions → CD → Disable workflow로 잠시 꺼둡니다.
- nginx 기본 업로드 제한은 1MB입니다. 파일 업로드가 필요하면 `client_max_body_size`를 설정합니다.
