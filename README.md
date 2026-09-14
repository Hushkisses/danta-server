# Danta Server

Minecraft 단타 국가전략 서버 프로젝트.

## Fixed development baseline

- Paper target: 26.2 Build #123
- Java: 25
- Build: Gradle multi-module
- DB: PostgreSQL

## Modules

- `core`: Paper API에 의존하지 않는 국가/거점/군단/전투/경제/연구 도메인 및 계산 코드
- `paper-plugin`: Paper 명령어, GUI, 월드, 엔티티, 구조물 연동
- `combat-simulator`: `core`의 전투 계산을 반복 실행하는 독립 시뮬레이터

## Development workflow

대표 개발 PC의 기존 `dev-server` 런타임 폴더를 유지한다. 일반 코드 변경 후에는 서버를 종료하고:

```text
dev-server\\quick-deploy.bat
```

을 실행해 증분 빌드/배포한다. Paper JAR을 다시 받을 필요는 없다.

최초 Paper 개발 서버 준비가 필요한 경우에만:

```text
dev-server\\setup-paper.bat
```

을 사용한다.

## Git / local secrets

로컬 월드, Paper JAR, 런타임 로그, PostgreSQL 비밀번호가 들어 있는 `database.properties`는 Git에 커밋하지 않는다. 관련 규칙은 `.gitignore`에 고정되어 있다.

현재 구현/테스트 상태와 다음 DEV 순서는 `docs/PROJECT-STATE.md`를 기준으로 한다.

## Current status

- Environment/bootstrap: DEV-001~005A complete
- Server foundation: DEV-010~017 complete
- Nation/territory functional tickets: DEV-020~025 complete
- Next: DEV-MAP-001, DEV-MAP-002, then DEV-030 Army
