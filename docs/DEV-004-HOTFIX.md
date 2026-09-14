# DEV-004 build/deploy hotfix

`build-and-deploy.ps1`의 Java 버전 탐지 시 `cmd /c` 따옴표 처리가 잘못되어 Windows에서 `\`를 명령으로 해석하던 문제를 수정했다.

Java 탐지는 이제 `System.Diagnostics.ProcessStartInfo`로 `java.exe -version`을 직접 실행하고 stdout/stderr를 캡처한다. `DANTA_JAVA_HOME`, `JAVA_HOME`, PATH 탐색 순서는 유지한다.
