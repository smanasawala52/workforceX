# ============================================================
# WorkforceX - Start Everything
# Run this from: C:\Repo\workforceX\
# Opens 3 PowerShell windows:
#   1. Backend (Spring Boot)
#   2. Worker app build & run
#   3. Employer app build & run
# ============================================================

param(
    [string]$AvdName = "",
    [string]$SdkPath = ""
)

Write-Host ""
Write-Host "======================================" -ForegroundColor Green
Write-Host "  WorkforceX - Starting Everything   " -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Green
Write-Host ""

$root = $PSScriptRoot

# ── Backend ───────────────────────────────────────────────────────────────────
Write-Host "Starting backend in a new window..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", `
    "cd '$root\backend'; .\run-backend.ps1"

Write-Host "Waiting 60s for backend to start before launching apps..." -ForegroundColor Yellow
Start-Sleep -Seconds 60

# ── Worker App ────────────────────────────────────────────────────────────────
Write-Host "Starting Worker app in a new window..." -ForegroundColor Cyan
$workerArgs = "-NoExit -Command cd '$root\android\worker'; .\run-worker-kmp.ps1"
if ($AvdName -ne "") { $workerArgs += " -AvdName '$AvdName'" }
if ($SdkPath -ne "") { $workerArgs += " -SdkPath '$SdkPath'" }
Start-Process powershell -ArgumentList $workerArgs

# ── Employer App ──────────────────────────────────────────────────────────────
Write-Host "Starting Employer app in a new window..." -ForegroundColor Magenta
$employerArgs = "-NoExit -Command cd '$root\android\employer'; .\run-employer-kmp.ps1"
if ($AvdName -ne "") { $employerArgs += " -AvdName '$AvdName'" }
if ($SdkPath -ne "") { $employerArgs += " -SdkPath '$SdkPath'" }
Start-Process powershell -ArgumentList $employerArgs

Write-Host ""
Write-Host "All three processes launched!" -ForegroundColor Green
Write-Host ""
Write-Host "If AVD is wrong, stop and re-run with:" -ForegroundColor Yellow
Write-Host "  .\start-all.ps1 -AvdName 'Your_AVD_Name'" -ForegroundColor Yellow
Write-Host "  .\start-all.ps1 -AvdName 'Your_AVD_Name' -SdkPath 'C:\Your\Sdk'" -ForegroundColor Yellow
Write-Host ""
