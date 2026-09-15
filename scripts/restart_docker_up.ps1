# restart_docker_up.ps1 - clean restart Docker Desktop, wait for engine, bring up infra
$ErrorActionPreference = 'Stop'
$env:Path = [Environment]::GetEnvironmentVariable('Path', 'Machine') + ';' + [Environment]::GetEnvironmentVariable('Path', 'User')

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
