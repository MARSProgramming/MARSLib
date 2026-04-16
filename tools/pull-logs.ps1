#!/usr/bin/env pwsh
<#
.SYNOPSIS
    MARSLib Log Puller — Automatically downloads .wpilog files from the RoboRIO
    when the robot is disabled and tethered via USB or Ethernet.

.DESCRIPTION
    This script runs on the driver station laptop. It periodically checks if
    the RoboRIO is reachable, scans for new .wpilog files, and downloads them
    via SCP to a local directory. It is designed to be left running in the
    background during practice sessions.

    Prerequisites:
      - OpenSSH client installed (ships with Windows 10+)
      - USB tether or Ethernet connection to the RoboRIO

.PARAMETER OutputDir
    Local directory to save downloaded logs. Defaults to ~/Documents/FRC-Logs.

.PARAMETER RoboRioIp
    IP address of the RoboRIO. Defaults to 172.22.11.2 (USB tether).
    For Ethernet/radio, use 10.TE.AM.2 (e.g., 10.26.14.2).

.PARAMETER PollIntervalSeconds
    How often to check for new logs. Defaults to 5 seconds.

.EXAMPLE
    .\pull-logs.ps1
    .\pull-logs.ps1 -OutputDir "D:\CompetitionLogs" -RoboRioIp "10.26.14.2"
#>

param(
    [string]$OutputDir = "$env:USERPROFILE\Documents\FRC-Logs",
    [string]$RoboRioIp = "roborio-2614-frc.local",
    [int]$PollIntervalSeconds = 5
)

# ── Banner ──────────────────────────────────────────────────────────────────
Write-Host ""
Write-Host "  ╔══════════════════════════════════════════════════╗" -ForegroundColor Red
Write-Host "  ║       MARSLib Automatic Log Puller v1.0          ║" -ForegroundColor Red
Write-Host "  ║       FRC Team 2614 — Mountaineer Area RoboticS  ║" -ForegroundColor Red
Write-Host "  ╚══════════════════════════════════════════════════╝" -ForegroundColor Red
Write-Host ""
Write-Host "  Target RoboRIO:  $RoboRioIp" -ForegroundColor Cyan
Write-Host "  Output Dir:      $OutputDir" -ForegroundColor Cyan
Write-Host "  Poll Interval:   ${PollIntervalSeconds}s" -ForegroundColor Cyan
Write-Host ""

# ── Ensure output directory exists ──────────────────────────────────────────
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
    Write-Host "  [CREATED] Output directory: $OutputDir" -ForegroundColor Green
}

# ── Track already-downloaded files ──────────────────────────────────────────
$manifestPath = Join-Path $OutputDir ".downloaded_manifest.txt"
$downloaded = @{}
if (Test-Path $manifestPath) {
    Get-Content $manifestPath | ForEach-Object { $downloaded[$_] = $true }
}

function Save-Manifest {
    $downloaded.Keys | Out-File -FilePath $manifestPath -Encoding UTF8
}

# ── Remote log directories to scan ──────────────────────────────────────────
$remoteDirs = @("/U/logs", "/home/lvuser/logs")

# ── SCP options for passwordless roboRIO access ─────────────────────────────
$scpOpts = @(
    "-o", "StrictHostKeyChecking=no",
    "-o", "UserKnownHostsFile=/dev/null",
    "-o", "ConnectTimeout=3",
    "-o", "LogLevel=ERROR",
    "-o", "BatchMode=yes"
)

function Test-RoboRioReachable {
    try {
        $tcp = New-Object System.Net.Sockets.TcpClient
        $result = $tcp.BeginConnect($RoboRioIp, 22, $null, $null)
        $success = $result.AsyncWaitHandle.WaitOne(2000)
        if ($success) {
            $tcp.EndConnect($result)
            $tcp.Close()
            return $true
        }
        $tcp.Close()
        return $false
    }
    catch {
        return $false
    }
}

function Get-RemoteFileList {
    param([string]$remoteDir)

    try {
        # List .wpilog and .hoot files in the remote directory
        $result = ssh @scpOpts "lvuser@$RoboRioIp" "ls -1 $remoteDir 2>/dev/null" 2>$null
        if ($LASTEXITCODE -ne 0) { return @() }

        return $result | Where-Object {
            $_ -match '\.(wpilog|hoot)$'
        }
    }
    catch {
        return @()
    }
}

function Download-LogFile {
    param(
        [string]$remoteDir,
        [string]$fileName
    )

    $remotePath = "$remoteDir/$fileName"
    $localPath = Join-Path $OutputDir $fileName

    # Prefix with date subdirectory for organization
    $dateDir = Join-Path $OutputDir (Get-Date -Format "yyyy-MM-dd")
    if (-not (Test-Path $dateDir)) {
        New-Item -ItemType Directory -Path $dateDir -Force | Out-Null
    }
    $localPath = Join-Path $dateDir $fileName

    Write-Host "  [DOWNLOADING] $remotePath -> $localPath" -ForegroundColor Yellow
    scp @scpOpts "lvuser@${RoboRioIp}:${remotePath}" "$localPath" 2>$null

    if ($LASTEXITCODE -eq 0) {
        Write-Host "  [SUCCESS] $fileName downloaded" -ForegroundColor Green
        return $true
    }
    else {
        Write-Host "  [FAILED] Could not download $fileName" -ForegroundColor Red
        return $false
    }
}

# ── Main Loop ───────────────────────────────────────────────────────────────
Write-Host "  Waiting for RoboRIO connection..." -ForegroundColor DarkGray
$wasConnected = $false
$totalDownloaded = 0

try {
    while ($true) {
        $reachable = Test-RoboRioReachable

        if ($reachable -and -not $wasConnected) {
            Write-Host ""
            Write-Host "  [CONNECTED] RoboRIO detected at $RoboRioIp" -ForegroundColor Green
            $wasConnected = $true
        }
        elseif (-not $reachable -and $wasConnected) {
            Write-Host "  [DISCONNECTED] RoboRIO lost" -ForegroundColor DarkGray
            $wasConnected = $false
        }

        if ($reachable) {
            foreach ($dir in $remoteDirs) {
                $files = Get-RemoteFileList -remoteDir $dir
                foreach ($file in $files) {
                    $key = "${dir}/${file}"
                    if (-not $downloaded.ContainsKey($key)) {
                        $success = Download-LogFile -remoteDir $dir -fileName $file
                        if ($success) {
                            $downloaded[$key] = $true
                            Save-Manifest
                            $totalDownloaded++
                        }
                    }
                }
            }
        }

        Start-Sleep -Seconds $PollIntervalSeconds
    }
}
catch {
    Write-Host ""
    Write-Host "  [STOPPED] Log puller terminated." -ForegroundColor DarkGray
}
finally {
    Write-Host ""
    Write-Host "  Total logs downloaded this session: $totalDownloaded" -ForegroundColor Cyan
    Save-Manifest
}
