@echo off
setlocal
cd /d "%~dp0"
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-and-deploy.ps1"
if errorlevel 1 (
    echo.
    echo [Danta] Build/deploy failed. See the message above.
    pause
    exit /b 1
)
echo.
pause
