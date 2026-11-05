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
| `cd.yml` | `main` push, 수동 실행 | `app.jar` 빌드 → EC2 `~/app`에 전송 → `systemctl restart thingkerton` |

### EC2 최초 세팅 (1회)

1. 보안 그룹에서 22(SSH), 8080 포트를 엽니다.
2. Java 21과 MySQL을 설치합니다. (Ubuntu 기준)

```bash
sudo apt update && sudo apt install -y openjdk-21-jre-headless mysql-server
mkdir -p ~/app
```

3. `~/app/.env`를 작성합니다. 애플리케이션이 실행 디렉터리의 `.env`를 자동으로 읽습니다.
4. `/etc/systemd/system/thingkerton.service`를 생성합니다. (`ubuntu`는 실제 사용자명으로 변경)

```ini
[Unit]
Description=Thingkerton Server
After=network.target mysql.service

[Service]
User=ubuntu
WorkingDirectory=/home/ubuntu/app
ExecStart=/usr/bin/java -jar /home/ubuntu/app/app.jar
Restart=always

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload && sudo systemctl enable thingkerton
```

### GitHub Secrets

Settings → Secrets and variables → Actions에 등록합니다.

| 이름 | 값 |
|---|---|
| `EC2_HOST` | EC2 퍼블릭 IP 또는 도메인 |
| `EC2_USERNAME` | `ubuntu` (Amazon Linux는 `ec2-user`) |
| `EC2_SSH_KEY` | `.pem` 키 파일 내용 전체 |

배포 상태는 EC2에서 `sudo systemctl status thingkerton`, 로그는 `journalctl -u thingkerton -f`로 확인합니다.
