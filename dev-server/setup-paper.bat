@echo off
setlocal
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup-paper.ps1"
if errorlevel 1 (
  echo.
  echo [Danta] Setup failed. See the message above.
  pause
) else (
  echo.
  pause
)
endlocal
