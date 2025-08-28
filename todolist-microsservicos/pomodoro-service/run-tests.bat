@echo off
echo Building test image...
docker build -f Dockerfile.test -t pomodoro-service-test .

echo Running tests...
docker run --rm pomodoro-service-test

echo Test execution completed.