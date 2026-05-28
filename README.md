# 🏫 CampusLink (캠퍼스링크)

> **대구대학교 학생들을 위한 디스코드 학생 인증 및 수업 정보 조회 봇**
>
> CampusLink는 대학 커뮤니티(디스코드 서버)의 신뢰성을 확보하기 위한 **학교 이메일 인증 기능**과 대학 생활 편의성을 돕는 **과목 검색 기능**을 유기적으로 연합하여 제공하는 Java 기반 디스코드 봇 프로젝트입니다.

---

## 📌 주요 특징

*   **대구대학교 웹메일 인증 (`@daegu.ac.kr`)**: 대구대학교 웹메일 주소를 사용해 일회용 인증 코드를 발송하고, 이를 디스코드 내에서 검증하여 신뢰성 있는 학생 인증을 제공합니다.
*   **인증 연계 권한 제어**: 과목 검색 기능 등 핵심 서비스는 학생 인증이 완료된 사용자만 접근할 수 있도록 보안 수준을 강화하였습니다.
*   **강력한 과목 검색 기능**: 수강번호, 교과목명, 담당교수, 수강학과, 이수구분 등 다양한 조건으로 개설 강좌 정보를 실시간 조회할 수 있습니다.
*   **자동화된 데이터 동기화**: 봇 구동 시 특정 디렉토리에 위치한 학기별 수업 목록 CSV 파일을 자동으로 스캔하여 데이터베이스와 실시간으로 동기화(Sync)합니다.
*   **유연한 설정 및 권한 관리**: YAML 기반의 설정 로더와 와일드카드(`*`)가 지원되는 자체 권한 노드 관리 시스템(Permission Manager)을 탑재하여 봇 관리가 용이합니다.

---

## 🛠 기술 스택

*   **언어 및 플랫폼**: Java 21+
*   **빌드 도구**: Gradle (Kotlin DSL, ShadowJar 빌드 지원)
*   **디스코드 API**: [JDA (Java Discord API) v6.4.1](https://github.com/discord-jda/JDA)
*   **데이터베이스 및 ORM**: MySQL / jOOQ (Type-safe SQL builder) / HikariCP (Connection Pool)
*   **설정 및 메일 라이브러리**: SnakeYAML, Eclipse Angus Mail (JavaMail API)
*   **로그 라이브러리**: SLF4J / Logback
*   **주요 의존성**: Lombok

---

## 📁 프로젝트 구조

```text
CampusLink/
├── src/main/java/moe/vitamin/campuslink/
│   ├── CampusLink.java            # Main 클래스 및 라이프사이클 관리
│   ├── command/                   # 디스코드 명령어 처리 패키지
│   │   ├── api/                   # Slash/Chat Command 인터페이스 정의
│   │   ├── impl/                  # 공통 명령어 구현체 (Help, Reload)
│   │   └── CommandManager.java    # 명령어 등록 및 매핑 관리
│   ├── config/                    # 설정 파일 로드 및 관리 패키지
│   │   ├── impl/                  # 개별 yaml 설정 매핑 데이터 모델
│   │   └── yaml/                  # YAML 파서 및 유틸리티
│   ├── database/                  # HikariCP 커넥션 풀 매니저
│   ├── discord/                   # JDA 기반 봇 인스턴스(Sora) 및 빌더
│   ├── permission/                # 디스코드 유저 대상 권한 매니저
│   └── service/                   # 주요 비즈니스 서비스
│       ├── certification/         # 이메일 인증 서비스 (API, DB, 프로세스)
│       ├── email/                 # SMTP 발송 서비스 (Angus Mail)
│       └── search/                # 강의 과목 검색 및 CSV 연동 서비스
└── src/main/resources/            # 기본 리소스 및 설정 템플릿
```

---

## ⚙️ 설정 및 배포 가이드

### 1. 사전 준비사항
*   **Java SDK**: JDK 21 이상 설치
*   **Database**: MySQL 서버 구동 및 빈 스키마 생성
*   **Discord Bot**: Discord Developer Portal에서 봇 계정을 생성하고 토큰 발급 및 서버 초대 (Slash Command 및 Message Content Intent 활성화 권장)
*   **SMTP 이메일 계정**: 인증 번호 메일 전송을 담당할 이메일 계정 (예: Gmail의 경우 2차 인증 활성화 후 **앱 비밀번호** 발급 필요)

### 2. 빌드 방법
Gradle Wrapper를 이용하여 독립 실행 가능한 Fat JAR(ShadowJar)를 빌드합니다.
```bash
# Windows
./gradlew.bat build

# Linux / macOS
./gradlew build
```
빌드가 완료되면 `build/libs/` 폴더 내에 `CampusLink.jar` 파일이 생성됩니다.

### 3. 설정 파일 구성 (YAML)
봇을 최초로 실행하거나, 실행 경로 아래에 `config/` 디렉토리를 생성하여 다음 설정 파일들을 위치시켜야 합니다. (최초 실행 시 기본 템플릿이 자동으로 생성됩니다.)

#### 📄 `config/sora.yaml` (디스코드 봇 설정)
```yaml
discord:
  bot-token: "YOUR_DISCORD_BOT_TOKEN_HERE"

command:
  chat:
    prefix: "!"
```

#### 📄 `config/database.yaml` (데이터베이스 설정)
```yaml
database:
  driver-class-name: "com.mysql.cj.jdbc.Driver"
  url: "jdbc:mysql://localhost:3306/campuslink?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
  username: "your_db_user"
  password: "your_db_password"
  hikari-cp:
    maximum-pool-size: 10
    minimum-idle: 10
    max-lifetime: 1800000
    connection-timeout: 30000
    idle-timeout: 600000
    pool-name: "CampusLink-HikariPool"
```

#### 📄 `config/email.yaml` (메일 전송용 SMTP 설정)
```yaml
SMTP:
  host: "smtp.gmail.com"
  port: 587
  auth: true
  credential:
    email: "your-sender-email@gmail.com"
    app-password: "your-gmail-app-password"
  start-tls:
    enable: true
```

#### 📄 `config/certification.yaml` (인증 세부 및 이메일 템플릿 설정)
```yaml
certification:
  expire-after: 180000 # 인증 코드 유효 기간 (밀리초 단위, 기본 3분)
  process-timeout-threshold: 5000 # 인증 처리 프로세스 타임아웃 (밀리초)

email:
  subject: "대구대학교 학생 인증 메일입니다."
  html: |
    <!DOCTYPE html>
    <html>
      <!-- 발송될 이메일 HTML 템플릿 ({code}, {expire_duration} 바인딩 지원) -->
    </html>
```

#### 📄 `config/permission.yaml` (유저별 권한 매핑 설정)
```yaml
roles:
  admin:
    permissions:
      - "*" # 모든 권한
    users:
      - "123456789012345678" # 디스코드 유저 ID 고유값
  user:
    permissions:
      - "campuslink.user.basic"
```

---

## 📊 데이터베이스 스키마 및 데이터 연동

봇이 데이터베이스에 연결되면 자동으로 다음 두 테이블이 생성됩니다.

### 1. `email_certification` (학생 인증 이메일 보관)
*   **Primary Key**: `(email, discord_user_id)`
*   인증이 통과된 사용자의 이메일 주소, 디스코드 고유 ID, 인증을 수행한 디스코드 서버(Guild) ID 및 인증 완료 시각을 기록합니다.

### 2. `class_search` (수업/과목 정보 저장)
*   **Primary Key**: `course_number` (수강번호)
*   과목의 이수구분, 수강학과, 학년, 학점, 교수명, 강의실, 비고, 요일, 시작/종료 시간 정보를 관리합니다.

### 📂 과목 데이터 CSV 동기화 방법
실행 파일(JAR)과 동일한 경로에 `courses` 폴더를 생성하고, 개설 교과목 목록이 포함된 `.csv` 파일들을 저장해 둡니다. 봇이 구동될 때 자동으로 모든 CSV 파일을 읽고 파싱하여 데이터베이스를 실시간 업데이트합니다.

*   **CSV 권장 데이터 컬럼 매핑**:
    1.  `이수구분` (예: 전필, 전선, 교선)
    2.  `학년` (예: 1, 2)
    3.  `수강학과` (예: 컴퓨터소프트웨어전공)
    4.  `수강번호` (예: CS0012)
    5.  `교과목명` (예: 소프트웨어공학)
    6.  `학점` (예: 3)
    7.  `시간` (미사용 컬럼 처리)
    8.  `담당교수` (예: 홍길동)
    9.  `강의시간` (예: `월(09:30-10:45)` 또는 `화(13:00~14:15)`)
        *   *참고: `요일(시작시간-종료시간)` 포맷을 만족하면 시작/종료 시간 및 총 강의 분수를 시스템이 자동으로 분석하여 기입합니다.*
    10. `강의실` (예: 공학관 302호)
    11. `비고` (기타 추가 안내 사항)

---

## 🤖 사용 명령어 (Commands)

### 1. 슬래시 명령어 (Slash Commands)

| 명령어 | 옵션 | 권한 | 설명 |
| :--- | :--- | :--- | :--- |
| `/인증` | `email` (선택)<br>`code` (선택) | 모든 사용자 | 대구대학교 웹메일주소를 입력하여 인증코드를 받거나, 받은 인증코드를 입력하여 최종 인증을 완료합니다. |
| `/인증정보` | 없음 | 모든 사용자 | 사용자의 디스코드 인증 정보(인증 이메일, 완료 시각 등)를 개인 메시지 형태로 확인합니다. |
| `/과목검색` | `방법` (필수 - 수강번호/교과목명/담당교수/수강학과/이수구분)<br>`검색어` (필수) | **학교 인증 완료 유저** | 지정된 검색 방법을 사용하여 개설 과목 정보를 상세 조회합니다. (인증 미완료 시 사용 불가) |
| `/help` | 없음 | 모든 사용자 | 봇이 제공하는 전체 명령어 리스트 및 도움말을 확인합니다. |

### 2. 채팅 명령어 (Chat Commands)
설정된 접두사(예: `!`)를 이용하여 채팅방에서 즉시 실행하는 명령어입니다.

*   **`!reload`**
    *   **요구 권한**: `campuslink.command.reload` 또는 `*`
    *   **설명**: 봇을 끄지 않고 `config/` 내의 설정 파일(sora, database, email, certification, permission)을 즉시 다시 읽어와 적용합니다.

---

## 🤝 기여 안내 및 문의
이 프로젝트는 팀 프로젝트로 진행되며, 버그 리포트 및 기능 개선 제안은 Issue 및 Pull Request를 이용해 주시기 바랍니다.
