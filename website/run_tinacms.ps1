$fnm = "$env:LOCALAPPDATA\Microsoft\WinGet\Packages\Schniz.fnm_Microsoft.Winget.Source_8wekyb3d8bbwe\fnm.exe"

if (Test-Path $fnm) {
    Write-Host "Activating Node 22 via fnm..."
    & $fnm env --use-on-cd | Out-String | Invoke-Expression
    & $fnm use 22
    
    Write-Host "Starting TinaCMS Dev Server and Astro..."
    npx tinacms dev -c "npm run dev"
} else {
    Write-Host "Could not find fnm. Please install Node 22 via winget."
}
