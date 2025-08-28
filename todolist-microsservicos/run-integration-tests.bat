@echo off
setlocal enabledelayedexpansion

echo 🧪 Executando testes de integração do sistema TodoList Microsserviços
echo ==================================================================

REM Verifica se Docker está rodando
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker não está rodando. Por favor, inicie o Docker primeiro.
    exit /b 1
)

REM 1. Executar testes de integração individuais de cada serviço
echo [INFO] 1. Executando testes de integração do auth-service...
cd auth-service
call mvnw clean test -Dtest="*IntegrationTest"
set AUTH_RESULT=!errorlevel!

echo [INFO] 2. Executando testes de integração do task-service...
cd ..\task-service
call mvnw clean test -Dtest="*IntegrationTest"
set TASK_RESULT=!errorlevel!

echo [INFO] 3. Executando testes de integração do pomodoro-service...
cd ..\pomodoro-service
call mvnw clean test -Dtest="*IntegrationTest"
set POMODORO_RESULT=!errorlevel!

REM 2. Executar testes end-to-end
cd ..
echo [INFO] 4. Executando testes end-to-end com Docker Compose...

REM Para os serviços antes de executar testes E2E
echo [INFO] Parando serviços existentes...
docker-compose down >nul 2>&1

REM Executa os testes end-to-end
call mvnw clean test -Dtest="*EndToEndIntegrationTest" -Pintegration-test
set E2E_RESULT=!errorlevel!

REM 3. Gerar relatório de resultados
echo.
echo 📊 RELATÓRIO DE TESTES DE INTEGRAÇÃO
echo ====================================

if !AUTH_RESULT! equ 0 (
    echo [INFO] ✅ Auth Service - PASSOU
) else (
    echo [ERROR] ❌ Auth Service - FALHOU
)

if !TASK_RESULT! equ 0 (
    echo [INFO] ✅ Task Service - PASSOU
) else (
    echo [ERROR] ❌ Task Service - FALHOU
)

if !POMODORO_RESULT! equ 0 (
    echo [INFO] ✅ Pomodoro Service - PASSOU
) else (
    echo [ERROR] ❌ Pomodoro Service - FALHOU
)

if !E2E_RESULT! equ 0 (
    echo [INFO] ✅ End-to-End Tests - PASSOU
) else (
    echo [ERROR] ❌ End-to-End Tests - FALHOU
)

REM Resultado final
set /a TOTAL_FAILURES=!AUTH_RESULT! + !TASK_RESULT! + !POMODORO_RESULT! + !E2E_RESULT!

echo.
if !TOTAL_FAILURES! equ 0 (
    echo [INFO] 🎉 TODOS OS TESTES DE INTEGRAÇÃO PASSARAM!
    echo O sistema está funcionando corretamente em todos os níveis.
) else (
    echo [ERROR] 💥 ALGUNS TESTES FALHARAM!
    echo Por favor, verifique os logs acima para detalhes sobre as falhas.
)

echo.
echo [INFO] Para mais opções:
echo   - Consulte run-integration-tests.sh para versão Linux/Mac
echo   - Execute testes individuais: mvnw test -Dtest=*IntegrationTest em cada diretório de serviço

exit /b !TOTAL_FAILURES!