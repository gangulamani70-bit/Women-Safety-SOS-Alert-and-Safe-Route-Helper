@echo off
setlocal
cd /d "%~dp0"

set "MVN=%USERPROFILE%\.maven\maven-3.10.0\bin\mvn.cmd"
if not exist "%MVN%" (
    where mvn >nul 2>nul
    if errorlevel 1 (
        echo Maven was not found. Install Maven or update this launcher with its location.
        pause
        exit /b 1
    )
    set "MVN=mvn"
)

call "%MVN%" -q "-Dexec.mainClass=womensafety.web.LocalhostServer" compile exec:java
if errorlevel 1 (
    echo The Women Safety localhost app could not start.
    pause
    exit /b 1
)
pause
