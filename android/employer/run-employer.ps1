# ============================================================
# WorkforceX - Employer App: Build, Run & Diagnose
# Run this from: android/employer/
# ============================================================

param(
    [string]$AvdName = "",
    [string]$SdkPath = ""
)

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  WorkforceX Employer App - Build & Run  " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# ── Step 1: Find Android SDK ──────────────────────────────────────────────────

$possibleSdkPaths = @(
    $SdkPath,
    "$env:ANDROID_HOME",
    "$env:LOCALAPPDATA\Android\Sdk",
    "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk",
    "C:\Android\Sdk"
)

$sdkFound = ""
foreach ($path in $possibleSdkPaths) {
    if ($path -ne "" -and (Test-Path "$path\platform-tools\adb.exe")) {
        $sdkFound = $path
        break
    }
}

if ($sdkFound -eq "") {
    Write-Host "ERROR: Android SDK not found." -ForegroundColor Red
    Write-Host "Please pass it explicitly:" -ForegroundColor Yellow
    Write-Host "  .\run-employer.ps1 -SdkPath 'C:\Your\Sdk\Path'" -ForegroundColor Yellow
    exit 1
}

Write-Host "Android SDK: $sdkFound" -ForegroundColor Green
$env:ANDROID_HOME = $sdkFound
$env:PATH = "$sdkFound\platform-tools;$sdkFound\emulator;$env:PATH"

# ── Step 2: Check Java ────────────────────────────────────────────────────────

Write-Host ""
Write-Host "Checking Java..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "Java: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Java not found. Install Java 21." -ForegroundColor Red
    exit 1
}

# ── Step 3: Start Emulator ────────────────────────────────────────────────────

Write-Host ""
Write-Host "Checking for running emulators..." -ForegroundColor Yellow
$devices = adb devices 2>&1 | Select-String "emulator"

if ($devices) {
    Write-Host "Emulator already running: $devices" -ForegroundColor Green
} else {
    # List available AVDs
    $avds = & "$sdkFound\emulator\emulator.exe" -list-avds 2>&1
    if (-not $avds) {
        Write-Host "ERROR: No AVDs found. Create one in Android Studio (Tools > AVD Manager)." -ForegroundColor Red
        exit 1
    }

    Write-Host "Available AVDs:" -ForegroundColor Yellow
    $avds | ForEach-Object { Write-Host "  - $_" -ForegroundColor White }

    if ($AvdName -eq "") {
        $AvdName = ($avds | Select-Object -First 1).ToString().Trim()
        Write-Host "Using first AVD: $AvdName" -ForegroundColor Yellow
    }

    Write-Host "Starting emulator: $AvdName" -ForegroundColor Yellow
    Start-Process "$sdkFound\emulator\emulator.exe" -ArgumentList "-avd", $AvdName -NoNewWindow

    Write-Host "Waiting for emulator to boot (this may take ~60 seconds)..." -ForegroundColor Yellow
    $booted = $false
    for ($i = 0; $i -lt 30; $i++) {
        Start-Sleep -Seconds 3
        $status = adb shell getprop sys.boot_completed 2>&1
        if ($status -match "1") {
            $booted = $true
            break
        }
        Write-Host "  Still booting... ($($i * 3)s)" -ForegroundColor Gray
    }

    if (-not $booted) {
        Write-Host "WARNING: Emulator may still be booting. Continuing anyway..." -ForegroundColor Yellow
    } else {
        Write-Host "Emulator booted!" -ForegroundColor Green
        Start-Sleep -Seconds 2
    }
}

# ── Step 4: Build APK ─────────────────────────────────────────────────────────

Write-Host ""
Write-Host "Building Employer app (debug)..." -ForegroundColor Yellow
& .\gradlew assembleDebug

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "BUILD FAILED. Fix errors above and try again." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Build SUCCESS!" -ForegroundColor Green

# ── Step 5: Install APK ───────────────────────────────────────────────────────

Write-Host ""
Write-Host "Installing Employer app on emulator..." -ForegroundColor Yellow
adb install -r app\build\outputs\apk\debug\app-debug.apk

if ($LASTEXITCODE -ne 0) {
    Write-Host "Install FAILED." -ForegroundColor Red
    exit 1
}

Write-Host "Install SUCCESS!" -ForegroundColor Green

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
