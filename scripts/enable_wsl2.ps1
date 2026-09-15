# enable_wsl2.ps1 - self-elevating wsl --install
$ErrorActionPreference = 'Stop'
$status = Join-Path $PSScriptRoot 'wsl2_status.txt'

function Test-Admin {
    $id = [Security.Principal.WindowsIdentity]::GetCurrent()
    $p = New-Object Security.Principal.WindowsPrincipal($id)
    return $p.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
}

if (-not (Test-Admin)) {
    Write-Host "=== non-admin, relaunching elevated (approve UAC) ==="
    $proc = Start-Process -FilePath 'powershell.exe' `
        -ArgumentList "-NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`"" `
        -Verb RunAs -PassThru
    while (-not $proc.HasExited) { Write-Host "." -NoNewline; Start-Sleep -Seconds 3 }
    Write-Host ""
    Write-Host "=== elevated process ended, status below ==="
    if (Test-Path $status) { Get-Content -Encoding UTF8 $status | Write-Host } else { Write-Host "status file not found" }
    exit 0
}

"" | Set-Content -Encoding utf8 $status
("[" + (Get-Date -Format 'yyyy-MM-dd HH:mm:ss') + "] admin: running wsl --install") | Out-File -Encoding utf8 -Append $status
try {
    & C:\Windows\System32\wsl.exe --install 2>&1 | Out-String | Out-File -Encoding utf8 -Append $status
    "WSL_INSTALL_EXIT_OK" | Out-File -Encoding utf8 -Append $status
} catch {
    ("WSL_INSTALL_ERROR: " + $_.Exception.Message) | Out-File -Encoding utf8 -Append $status
}
& C:\Windows\System32\wsl.exe --version 2>&1 | Out-String | Out-File -Encoding utf8 -Append $status
"=== admin ended ===" | Out-File -Encoding utf8 -Append $status
