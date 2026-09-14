@echo off
setlocal
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-dev.ps1"
if errorlevel 1 (
  echo.
  echo [Danta] Server start failed. See the message above.
  pause
)
endlocal
