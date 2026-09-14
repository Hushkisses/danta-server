# Danta Server

Minecraft 단타 국가전략 서버 프로젝트.

## Fixed development baseline

- Minecraft: 26.2
- Paper: 26.2 Build #123
- Java: 25
- Build: Gradle multi-module

## Modules

- `core`: Paper API에 의존하지 않는 국가/거점/군단/전투/경제/연구 도메인 및 계산 코드
- `paper-plugin`: Paper 명령어, GUI, 월드, 엔티티, 구조물 연동
- `combat-simulator`: `core`의 전투 계산을 반복 실행하는 독립 시뮬레이터

## Build

```bash
./gradlew clean build
```

현재 저장소의 Gradle Wrapper 바이너리는 실제 개발 PC에서 DEV-002 검증 시 생성한다.
Java 25가 설치되어 있어야 한다.

## Development server (DEV-003)

Windows에서는 `dev-server/setup-paper.bat`으로 고정 Paper 빌드를 준비한 뒤, Mojang EULA에 동의하는 경우 `eula.txt`를 `eula=true`로 변경하고 `dev-server/start-dev.bat`을 실행한다.


## Current development status

- DEV-001: version lock — complete
- DEV-002: Gradle multi-module skeleton — complete
- DEV-003: Paper development server — server boot confirmed by project owner
- DEV-004: plugin bootstrap + `/danta` — implementation complete, live smoke test pending

Next: DEV-010 RuntimeClockService.

## Quick development build/deploy

On Windows, stop the dev server and run:

```text
dev-server\build-and-deploy.bat
```

The script pins Gradle 9.7.1, requires Java 25, builds the single Danta plugin JAR, and copies it into `dev-server\plugins\`.
