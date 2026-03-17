@echo off
echo Building ARIA...
mvnw.cmd clean package -DskipTests
echo.
echo Done. Run run.bat to start ARIA.
pause
