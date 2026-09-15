# enable_wsl_features.ps1 - self-elevating: enable WSL + VirtualMachinePlatform via dism
$ErrorActionPreference = 'Stop'
$status = Join-Path $PSScriptRoot 'wsl_feat_status.txt'

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
("[" + (Get-Date -Format 'yyyy-MM-dd HH:mm:ss') + "] admin: enabling features") | Out-File -Encoding utf8 -Append $status

("--- dism enable WSL ---") | Out-File -Encoding utf8 -Append $status
dism /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart | Out-String | Out-File -Encoding utf8 -Append $status

("--- dism enable VirtualMachinePlatform ---") | Out-File -Encoding utf8 -Append $status
dism /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart | Out-String | Out-File -Encoding utf8 -Append $status

("--- wsl --set-default-version 2 (best effort) ---") | Out-File -Encoding utf8 -Append $status
& C:\Windows\System32\wsl.exe --set-default-version 2 2>&1 | Out-String | Out-File -Encoding utf8 -Append $status

$needReboot = $false
$keys = @(
    'HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\Component Based Servicing\RebootPending',
    'HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\WindowsUpdate\Auto Update\RebootRequired'
)
foreach ($k in $keys) { if (Test-Path $k) { $needReboot = $true } }
("NEED_REBOOT=" + $needReboot) | Out-File -Encoding utf8 -Append $status
("=== admin ended ===") | Out-File -Encoding utf8 -Append $status
