$env:Path = [Environment]::GetEnvironmentVariable('Path', 'Machine') + ';' + [Environment]::GetEnvironmentVariable('Path', 'User')

# 若 daemon 未运行，启动 Docker Desktop（首次启动会引导启用 WSL2）
try { $probe = docker ps 2>$null } catch { $probe = $null }
if (-not $probe) {
  $dde = "C:\Program Files\Docker\Docker\Docker Desktop.exe"
  if (Test-Path $dde) {
    Write-Host "Starting Docker Desktop (first launch may prompt to enable WSL2 / accept terms)..."
    Start-Process -FilePath $dde
  } else {
    Write-Host "Docker Desktop.exe not found at: $dde"
  }
}

$daemon = $false
for ($i = 1; $i -le 100; $i++) {
  Start-Sleep -Seconds 3
  try {
    $sv = docker info --format '{{.ServerVersion}}' 2>$null
    if ($sv) { Write-Host ("try $i : docker daemon ready (server $sv)"); $daemon = $true; break }
  } catch {}
  Write-Host ("try $i : waiting for Docker daemon...")
}

if (-not $daemon) {
  Write-Host "=== Docker daemon not ready. If prompted, allow WSL2 install (admin) and restart, then re-run this script. ==="
  Write-Host "    Manual: wsl --install   (admin, then restart Windows)"
  exit 1
}

Set-Location e:\Tom\Projects\NETeacher\backend
Write-Host "=== docker compose up -d ==="
docker compose up -d 2>&1 | Select-Object -Last 50

Write-Host "=== containers ==="
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
