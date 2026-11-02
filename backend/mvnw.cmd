@echo off
setlocal
where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    mvn %*
) else if exist "C:\Users\SARANYA\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd" (
    "C:\Users\SARANYA\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd" %*
) else (
    echo [ERROR] Maven binary not found. Please install Maven or add it to PATH.
    exit /b 1
)
endlocal
