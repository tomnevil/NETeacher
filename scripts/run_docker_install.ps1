# 以管理员提权启动 Docker Desktop 安装（弹 UAC，需用户在桌面点击允许）
$psi = New-Object System.Diagnostics.ProcessStartInfo
$psi.FileName = "winget"
$psi.Arguments = "install -e --id Docker.DockerDesktop --accept-package-agreements --accept-source-agreements --disable-interactivity"
$psi.Verb = "runas"
$psi.UseShellExecute = $true
[System.Diagnostics.Process]::Start($psi) | Out-Null
Write-Host "Docker Desktop installer launched. Approve the UAC prompt to continue installing..."

$ready = $false
for ($i = 1; $i -le 80; $i++) {
  Start-Sleep -Seconds 3
  $env:Path = [Environment]::GetEnvironmentVariable('Path', 'Machine') + ';' + [Environment]::GetEnvironmentVariable('Path', 'User')
  try {
    $d = Get-Command docker -ErrorAction SilentlyContinue
    if ($d) {
      $v = & $d.Source --version 2>$null
      if ($v) { Write-Host ("try $i : Docker CLI ready -> " + $v); $ready = $true; break }
    }
  } catch {}
  Write-Host ("try $i : installing... Docker Desktop not ready yet")
}

if ($ready) {
  Write-Host "=== Docker CLI installed. ==="
  Write-Host "Next: launch Docker Desktop (first launch enables WSL2/Hyper-V; may require a restart), then run:"
  Write-Host "    cd e:\Tom\Projects\NETeacher\backend ; docker compose up -d"
} else {
  Write-Host "=== Install still in progress or needs a restart. Open Docker Desktop manually after it finishes. ==="
}
