# ============================================================
# WorkforceX Backend Runner
# Clears existing environment variables, builds the project,
# sets production environment variables, and starts Spring Boot.
# ============================================================
clear
git pull origin uae-impl
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
$env:SUPABASE_S3_ENDPOINT = "https://hzpfusegsixiyrcxllvl.storage.supabase.co/storage/v1/s3"
$env:SUPABASE_S3_REGION = "ap-south-1"
$env:SUPABASE_S3_ACCESS_KEY_ID = "de37470d520a5e18927b282a63d786d2"
$env:SUPABASE_S3_SECRET_ACCESS_KEY = "87ad2c21f89f5431fb7a574c2e07b9b09a5e3680fb0ee866e43d9215939a6d69"
$env:SUPABASE_STORAGE_BUCKET = "verification-documents"

Write-Host "Starting Spring Boot..." -ForegroundColor Green
mvn spring-boot:run
