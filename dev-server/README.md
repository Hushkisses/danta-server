# DEV-003 Paper development server

고정 환경: Minecraft 26.2 / Paper 26.2 build 123 / Java 25.

## Windows 최초 실행

1. Java 25 JDK를 설치한다.
2. `java -version`에서 25가 표시되는지 확인한다.
3. `setup-paper.bat`을 실행한다.
4. 생성된 `eula.txt`에서 Mojang EULA를 확인하고, 동의하는 경우에만 `eula=true`로 변경한다.
5. `start-dev.bat`을 실행한다.
6. 콘솔에 `Done` 메시지가 나오면 `stop`으로 정상 종료한다.

`setup-paper.ps1`은 PaperMC 공식 Downloads Service를 사용해 **고정된 build 123만** 찾는다. 더 최신 빌드가 있어도 자동 업그레이드하지 않는다. JAR에 공식 SHA-256이 제공되면 다운로드 후 검증한다.

## 기본 메모리

개발 서버는 `-Xms2G -Xmx4G`로 시작한다. 실제 6인 이상 성능 테스트 단계에서는 별도로 조정한다.

## 플러그인

DEV-004부터 빌드한 Danta 플러그인 JAR을 `plugins/`에 복사한다.
