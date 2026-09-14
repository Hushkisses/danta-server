@echo off
setlocal
cd /d "%~dp0"
echo [Danta] Quick plugin build/deploy
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0quick-deploy.ps1"
if errorlevel 1 (
  echo.
  echo [Danta] Quick deploy failed. See the message above.
  pause
  exit /b 1
)
pause
