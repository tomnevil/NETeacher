# fix_daemon_and_up.ps1 - rewrite daemon.json WITHOUT BOM, restart Docker, bring up infra
$ErrorActionPreference = 'Stop'
$env:Path = [Environment]::GetEnvironmentVariable('Path', 'Machine') + ';' + [Environment]::GetEnvironmentVariable('Path', 'User')

# 1) write daemon.json as UTF-8 WITHOUT BOM (use .NET WriteAllText)
$dir = Join-Path $env:USERPROFILE '.docker'
New-Item -ItemType Directory -Force -Path $dir | Out-Null
$json = '{' + [Environment]::NewLine + '  "registry-mirrors": [' + [Environment]::NewLine + '    "https://docker.m.daocloud.io"' + [Environment]::NewLine + '  ]' + [Environment]::NewLine + '}'
[System.IO.File]::WriteAllText((Join-Path $dir 'daemon.json'), $json)
Write-Host ("wrote daemon.json (no BOM): " + $json)
$bytes = [System.IO.File]::ReadAllBytes((Join-Path $dir 'daemon.json'))
Write-Host ("first 3 bytes (should be 123 10 32, NOT 239 187 191): " + $bytes[0] + " " + $bytes[1] + " " + $bytes[2])

# 2) clean restart Docker Desktop
Write-Host "killing all docker processes..."
Get-Process -Name 'Docker Desktop','com.docker.backend','dockerd','vpnkit' -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 5
& C:\Windows\System32\wsl.exe --shutdown 2>$null
Start-Sleep -Seconds 3
Write-Host "starting Docker Desktop..."
Start-Process -FilePath 'C:\Program Files\Docker\Docker\Docker Desktop.exe'

$daemon = $false
for ($i = 1; $i -le 120; $i++) {
    Start-Sleep -Seconds 3
    try {
        $sv = docker info --format '{{.ServerVersion}}' 2>$null
        if ($sv) { Write-Host ("try $i : docker daemon ready (server $sv)"); $daemon = $true; break }
    } catch {}
    Write-Host ("try $i : waiting for Docker daemon...")
}
if (-not $daemon) { Write-Host "=== Docker daemon not ready after restart, abort. ==="; exit 1 }

Set-Location e:\Tom\Projects\NETeacher\backend
Write-Host "=== docker compose up -d ==="
docker compose up -d 2>&1 | Select-Object -Last 60

Write-Host "=== containers ==="
docker ps --format 'table {{.Names}}	{{.Status}}	{{.Ports}}'
