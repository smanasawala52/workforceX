clear
git pull origin main

Write-Host "This script is a placeholder for running an employer-specific web app."
Write-Host "Currently, it starts the main WorkforceX web application."
$PSScriptRoot = Split-Path -Parent -Path $MyInvocation.MyCommand.Definition
cd (Join-Path $PSScriptRoot "web/website")

Write-Host "Ensuring all dependencies are installed. This might take a moment..."
npm install

Write-Host "Starting the React development server. The app will open in your browser automatically."
npm start