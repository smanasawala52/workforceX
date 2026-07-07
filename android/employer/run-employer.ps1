# ============================================================
# WorkforceX - Employer App: Build, Run & Diagnose
# ============================================================

param(
    [string]$AvdName = "",
    [string]$SdkPath = ""
)

# ... (Steps 1-5 remain the same)

# ── Step 6: Launch App & Capture Logs ───────────────────────────────────────

Write-Host ""
Write-Host "Clearing old device logs..." -ForegroundColor Yellow
adb logcat -c

Write-Host "Launching WorkforceX Employer app..." -ForegroundColor Magenta
adb shell am start -n com.workforcex.employer/.ui.auth.SplashActivity

Write-Host "Waiting 8 seconds for app to launch and make API calls..." -ForegroundColor Yellow
Start-Sleep -Seconds 8

Write-Host "Checking for logs..." -ForegroundColor Yellow

# --- Crash Log Check ---
$crashLog = adb logcat -d | Select-String "FATAL EXCEPTION" -Context 0, 30
if ($crashLog) {
    Write-Host "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" -ForegroundColor Red
    Write-Host "!!    A C R A S H   W A S   F O U N D   !!" -ForegroundColor Red
    Write-Host "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" -ForegroundColor Red
    Write-Host ""
    Write-Host "--- CRASH LOG ---" -ForegroundColor Yellow
    Write-Host $crashLog -ForegroundColor Red
    Write-Host "-----------------" -ForegroundColor Yellow
    exit 1
}

# --- Network Log Check ---
$networkLog = adb logcat -d -s "OkHttp"
if ($networkLog) {
    Write-Host ""
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host "          NETWORK ACTIVITY LOG            " -ForegroundColor Cyan
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host $networkLog
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host "The log above shows the API requests and responses." -ForegroundColor White
    Write-Host "Look for HTTP errors (like 404 Not Found or 500 Server Error)." -ForegroundColor White
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "No network activity logged from the app." -ForegroundColor Yellow
    Write-Host "This might indicate a problem before the API calls are made." -ForegroundColor Yellow
    Write-Host ""
}

Write-Host "Script finished." -ForegroundColor Green
Write-Host ""
