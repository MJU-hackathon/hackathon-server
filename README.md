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
