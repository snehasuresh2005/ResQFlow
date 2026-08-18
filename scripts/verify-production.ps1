# ResQFlow production verify script
$ErrorActionPreference = "Stop"

Write-Output "=========================================="
Write-Output "RESQFLOW - Production Deployment Verifier"
Write-Output "=========================================="

Write-Output "[1/4] Validating Docker Compose configuration..."
docker compose --env-file .env.production.local -f docker-compose.prod.yml config > $null
if ($LASTEXITCODE -ne 0) {
    Write-Output "[FAIL] Docker Compose configuration is invalid."
    exit 1
}
Write-Output "[OK] Docker Compose configuration is valid."

Write-Output "[2/4] Checking running container statuses..."
$services = @("resqflow-postgres", "resqflow-redis", "resqflow-kafka", "resqflow-backend", "resqflow-frontend")

foreach ($service in $services) {
    $running = docker ps -q -f name="^/$service$"
    if ($running) {
        $health = docker inspect --format="{{if .State.Health}}{{.State.Health.Status}}{{else}}running{{end}}" $service 2>$null
        Write-Output "[OK] Container $service is RUNNING (Health: $health)"
    } else {
        Write-Output "[FAIL] Container $service is NOT running!"
        exit 1
    }
}

Write-Output "[3/4] Testing backend Spring Actuator health endpoint..."
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method Get -TimeoutSec 5
    $status = $response.status
    Write-Output "[OK] Backend Health: Status = $status"
} catch {
    Write-Output "[FAIL] Failed to query backend Actuator health: $_"
    exit 1
}

Write-Output "[4/4] Testing frontend response..."
try {
    $webResponse = Invoke-WebRequest -Uri "http://localhost:3000" -Method Get -TimeoutSec 5 -UseBasicParsing
    $statusCode = $webResponse.StatusCode
    if ($statusCode -eq 200) {
        Write-Output "[OK] Frontend interface is ONLINE (HTTP 200)"
    } else {
        Write-Output "[FAIL] Frontend returned status: $statusCode"
        exit 1
    }
} catch {
    Write-Output "[FAIL] Failed to query frontend interface: $_"
    exit 1
}

Write-Output "=========================================="
Write-Output "[SUCCESS] All production validation checks PASSED!"
Write-Output "=========================================="
