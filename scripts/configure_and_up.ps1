# configure_and_up.ps1 - set Docker registry mirror, restart Docker, bring up infra
$ErrorActionPreference = 'Stop'
$env:Path = [Environment]::GetEnvironmentVariable('Path', 'Machine') + ';' + [Environment]::GetEnvironmentVariable('Path', 'User')

# 1) write daemon.json with DaoCloud mirror
$dir = Join-Path $env:USERPROFILE '.docker'
New-Item -ItemType Directory -Force -Path $dir | Out-Null
$json = @{ 'registry-mirrors' = @('https://docker.m.daocloud.io') } | ConvertTo-Json
Set-Content -Encoding utf8 (Join-Path $dir 'daemon.json') $json
Write-Host ("wrote daemon.json: " + $json)

# 2) restart Docker Desktop to load new config
Write-Host "stopping Docker Desktop..."
Stop-Process -Name 'Docker Desktop' -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 3
& C:\Windows\System32\wsl.exe --shutdown 2>$null
Start-Sleep -Seconds 2
Write-Host "starting Docker Desktop..."
Start-Process -FilePath 'C:\Program Files\Docker\Docker\Docker Desktop.exe'

# 3) wait for daemon
$daemon = $false
for ($i = 1; $i -le 100; $i++) {
    Start-Sleep -Seconds 3
    try {
        $sv = docker info --format '{{.ServerVersion}}' 2>$null
        if ($sv) { Write-Host ("try $i : docker daemon ready (server $sv)"); $daemon = $true; break }
    } catch {}
    Write-Host ("try $i : waiting for Docker daemon...")
}
if (-not $daemon) { Write-Host "=== Docker daemon not ready, abort. ==="; exit 1 }

# 4) bring up infra
Set-Location e:\Tom\Projects\NETeacher\backend
Write-Host "=== docker compose up -d ==="
docker compose up -d 2>&1 | Select-Object -Last 60

Write-Host "=== containers ==="
docker ps --format 'table {{.Names}}	{{.Status}}	{{.Ports}}'
