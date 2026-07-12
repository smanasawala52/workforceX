# ============================================================
# WorkforceX Employer App Builder
# Cleans and builds release APK for the Employer App
# ============================================================
clear
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process

$env:ANDROID_HOME = "C:\Users\shabb\AppData\Local\Android\Sdk"
$employerPath = "C:\Repo\workforceX\android\employer"

Write-Host ">>> Starting build for: WorkforceX Employer App" -ForegroundColor Cyan

# Ensure we are in the correct directory
Set-Location $employerPath

# Build the release APK
.\gradlew clean assembleRelease

if ($LASTEXITCODE -eq 0) {
    $apkPath = "\app\build\outputs\apk\release\employer-app-release.apk"
    Write-Host "`nBUILD SUCCESSFUL" -ForegroundColor Green
    Write-Host "Location: $employerPath\$apkPath" -ForegroundColor Yellow

    # Check if a device is connected to install immediately
    $device = adb devices | Select-String -Pattern "device`$"
    if ($device) {
        Write-Host "`nInstalling to connected device..." -ForegroundColor Cyan
        adb install -r $apkPath
        Write-Host "Installation complete." -ForegroundColor Green
    } else {
        Write-Host "`nNo device detected for automatic installation." -ForegroundColor Gray
    }
} else {
    Write-Host "`nBUILD FAILED. Check your Gradle configuration." -ForegroundColor Red
    exit $LASTEXITCODE
}