#!/bin/bash

echo "Running auth-service tests using Docker..."
echo

echo "Building test image..."
docker build -f Dockerfile.test -t auth-service-test .

if [ $? -ne 0 ]; then
    echo "Failed to build test image"
    exit 1
fi

echo
echo "Running all tests (excluding TestContainers-based integration test)..."
docker run --rm auth-service-test ./mvnw test -Dtest='!AuthServiceIntegrationTest'

echo
echo "Tests completed!"