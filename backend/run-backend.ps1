# ============================================================
# WorkforceX Backend Runner
# Clears existing environment variables, builds the project,
# sets production environment variables, and starts Spring Boot.
# ============================================================
clear
Write-Host ""
Write-Host "======================================" -ForegroundColor Green
Write-Host "   WorkforceX Backend Startup         " -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Green
Write-Host ""

# Remove existing environment variables if present
@(
    "SPRING_DATASOURCE_DRIVERCLASSNAME",
    "SPRING_DATASOURCE_URL",
    "SPRING_DATASOURCE_USERNAME",
    "SPRING_DATASOURCE_PASSWORD",
    "APP_JWT_SECRET",
    "SPRING_PROFILES_ACTIVE"
) | ForEach-Object {
    Remove-Item "Env:$_" -ErrorAction SilentlyContinue
}

Write-Host "Building backend..." -ForegroundColor Cyan
mvn clean install

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "Build failed. Backend will not start." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "Setting production environment..." -ForegroundColor Cyan

$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://db.hzpfusegsixiyrcxllvl.supabase.co:5432/postgres"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "sMPQW@23091988"
$env:APP_JWT_SECRET = "workforcex-prod-test-production-use-1234567890"
$env:SPRING_PROFILES_ACTIVE = "prod"

Write-Host "Starting Spring Boot..." -ForegroundColor Green
mvn spring-boot:run