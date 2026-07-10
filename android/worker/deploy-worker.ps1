# ============================================================
# WorkforceX Worker App Builder
# Cleans and builds release APK for the Worker App
# ============================================================
clear
$env:ANDROID_HOME = "C:\Users\shabb\AppData\Local\Android\Sdk"
$workerPath = "C:\Repo\workforceX\android\worker"

Write-Host ">>> Starting build for: WorkforceX Worker App" -ForegroundColor Cyan

# Ensure we are in the correct directory
Set-Location $workerPath

# Build the release APK
.\gradlew clean assembleRelease

if ($LASTEXITCODE -eq 0) {
    $apkPath = "\app\build\outputs\apk\release\worker-app-release.apk"
    Write-Host "`nBUILD SUCCESSFUL" -ForegroundColor Green
    Write-Host "Location: $workerPath\$apkPath" -ForegroundColor Yellow

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