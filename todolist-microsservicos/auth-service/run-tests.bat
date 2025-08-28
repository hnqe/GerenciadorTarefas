@echo off
echo Running auth-service tests using Docker...
echo.

echo Building test image...
docker build -f Dockerfile.test -t auth-service-test .

if %errorlevel% neq 0 (
    echo Failed to build test image
    exit /b 1
)

echo.
echo Running all tests (excluding TestContainers-based integration test)...
docker run --rm auth-service-test ./mvnw test -Dtest="!AuthServiceIntegrationTest"

echo.
echo Tests completed!
pause