#!/bin/bash

# Script para executar todos os testes de integração do sistema de microsserviços
echo "🧪 Executando testes de integração do sistema TodoList Microsserviços"
echo "=================================================================="

# Cores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Função para log com cor
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Verifica se Docker está rodando
if ! docker info > /dev/null 2>&1; then
    log_error "Docker não está rodando. Por favor, inicie o Docker primeiro."
    exit 1
fi

# Verifica se Docker Compose está disponível
if ! command -v docker-compose &> /dev/null; then
    log_error "Docker Compose não encontrado. Por favor, instale o Docker Compose."
    exit 1
fi

# 1. Executar testes de integração individuais de cada serviço
log_info "1. Executando testes de integração do auth-service..."
cd auth-service
./mvnw clean test -Dtest="*IntegrationTest"
AUTH_RESULT=$?

log_info "2. Executando testes de integração do task-service..."
cd ../task-service
./mvnw clean test -Dtest="*IntegrationTest" 
TASK_RESULT=$?

log_info "3. Executando testes de integração do pomodoro-service..."
cd ../pomodoro-service
./mvnw clean test -Dtest="*IntegrationTest"
POMODORO_RESULT=$?

# 2. Executar testes end-to-end
cd ..
log_info "4. Executando testes end-to-end com Docker Compose..."

# Para os serviços antes de executar testes E2E
log_info "Parando serviços existentes..."
docker-compose down > /dev/null 2>&1

# Executa os testes end-to-end
./mvnw clean test -Dtest="*EndToEndIntegrationTest" -Pintegration-test
E2E_RESULT=$?

# 3. Gerar relatório de resultados
echo ""
echo "📊 RELATÓRIO DE TESTES DE INTEGRAÇÃO"
echo "===================================="

if [ $AUTH_RESULT -eq 0 ]; then
    log_info "✅ Auth Service - PASSOU"
else
    log_error "❌ Auth Service - FALHOU"
fi

if [ $TASK_RESULT -eq 0 ]; then
    log_info "✅ Task Service - PASSOU"
else
    log_error "❌ Task Service - FALHOU"
fi

if [ $POMODORO_RESULT -eq 0 ]; then
    log_info "✅ Pomodoro Service - PASSOU"
else
    log_error "❌ Pomodoro Service - FALHOU"
fi

if [ $E2E_RESULT -eq 0 ]; then
    log_info "✅ End-to-End Tests - PASSOU"
else
    log_error "❌ End-to-End Tests - FALHOU"
fi

# Resultado final
TOTAL_FAILURES=$((AUTH_RESULT + TASK_RESULT + POMODORO_RESULT + E2E_RESULT))

echo ""
if [ $TOTAL_FAILURES -eq 0 ]; then
    log_info "🎉 TODOS OS TESTES DE INTEGRAÇÃO PASSARAM!"
    echo "O sistema está funcionando corretamente em todos os níveis."
else
    log_error "💥 ALGUNS TESTES FALHARAM!"
    echo "Por favor, verifique os logs acima para detalhes sobre as falhas."
fi

echo ""
log_info "Para executar tipos específicos de teste:"
echo "  - Apenas testes individuais: ./run-integration-tests.sh --unit-only"
echo "  - Apenas testes E2E: ./run-integration-tests.sh --e2e-only" 
echo "  - Ver logs detalhados: ./run-integration-tests.sh --verbose"

exit $TOTAL_FAILURES