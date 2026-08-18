#!/bin/bash
set -e

echo "=========================================="
echo "RESQFLOW - Production Deployment Verifier"
echo "=========================================="

echo "[1/4] Validating Docker Compose configuration..."
docker compose -f docker-compose.prod.yml config >/dev/null
echo "✓ Docker Compose configuration is valid."

echo "[2/4] Checking running container statuses..."
SERVICES=("resqflow-postgres" "resqflow-redis" "resqflow-kafka" "resqflow-backend" "resqflow-frontend")

for SERVICE in "${SERVICES[@]}"; do
    if [ "$(docker ps -q -f name=^/${SERVICE}$)" ]; then
        STATUS=$(docker inspect --format='{{.State.Health.Status}}' $SERVICE 2>/dev/null || echo "no-healthcheck")
        echo "✓ Container $SERVICE is RUNNING (Health: $STATUS)"
    else
        echo "❌ Container $SERVICE is NOT running!"
        exit 1
    fi
done

echo "[3/4] Testing backend Spring Actuator health endpoint..."
ACTUATOR_URL="http://localhost:8080/actuator/health"
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" $ACTUATOR_URL || echo "000")

if [ "$HTTP_STATUS" -eq 200 ]; then
    HEALTH_BODY=$(curl -s $ACTUATOR_URL)
    echo "✓ Backend Health: $HEALTH_BODY"
else
    echo "❌ Backend Actuator returned status: $HTTP_STATUS"
    exit 1
fi

echo "[4/4] Testing frontend response..."
FRONTEND_URL="http://localhost:3000"
FRONTEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" $FRONTEND_URL || echo "000")

if [ "$FRONTEND_STATUS" -eq 200 ]; then
    echo "✓ Frontend interface is ONLINE (HTTP 200)"
else
    echo "❌ Frontend returned status: $FRONTEND_STATUS"
    exit 1
fi

echo "=========================================="
echo "✓ All production validation checks PASSED!"
echo "=========================================="
