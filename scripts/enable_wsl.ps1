# enable_wsl.ps1 - Enable WSL2 backend for Docker Desktop (self-elevating)
$ErrorActionPreference = 'Stop'
$log = Join-Path $PSScriptRoot 'enable_wsl.log'
$status = Join-Path $PSScriptRoot 'wsl_status.txt'

function Test-Admin {
    $id = [Security.Principal.WindowsIdentity]::GetCurrent()
    $p = New-Object Security.Principal.WindowsPrincipal($id)
    return $p.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
}

if (-not (Test-Admin)) {
    Write-Host "=== non-admin, relaunching elevated (approve the UAC prompt) ==="
    $proc = Start-Process -FilePath 'powershell.exe' `
        -ArgumentList "-NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`"" `
        -Verb RunAs -PassThru
    while (-not $proc.HasExited) { Write-Host "." -NoNewline; Start-Sleep -Seconds 3 }
    Write-Host ""
    Write-Host "=== elevated process ended, status file below ==="
    if (Test-Path $status) { Get-Content -Encoding UTF8 $status | Write-Host } else { Write-Host "status file not found" }
    exit 0
}

# --- admin branch ---
function Out-Status($msg) {
    $msg | Out-File -Encoding utf8 -Append $status
    Write-Host $msg
}

"" | Set-Content -Encoding utf8 $status
Out-Status ("[" + (Get-Date -Format 'yyyy-MM-dd HH:mm:ss') + "] admin branch start")

$wslFeat = Get-WindowsOptionalFeature -Online -FeatureName Microsoft-Windows-Subsystem-Linux
$vmpFeat = Get-WindowsOptionalFeature -Online -FeatureName VirtualMachinePlatform
Out-Status ("WSL_State=" + $wslFeat.State)
Out-Status ("VMP_State=" + $vmpFeat.State)

if ($wslFeat.State -ne 'Enabled') {
    Out-Status "enabling WSL feature..."
    dism /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart | Out-String | Out-File -Encoding utf8 -Append $status
}
if ($vmpFeat.State -ne 'Enabled') {
    Out-Status "enabling VirtualMachinePlatform feature..."
    dism /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart | Out-String | Out-File -Encoding utf8 -Append $status
}

Out-Status "--- wsl --update ---"
& C:\Windows\System32\wsl.exe --update 2>&1 | Out-String | Out-File -Encoding utf8 -Append $status
Out-Status "--- wsl --set-default-version 2 ---"
& C:\Windows\System32\wsl.exe --set-default-version 2 2>&1 | Out-String | Out-File -Encoding utf8 -Append $status
Out-Status "--- wsl --version ---"
& C:\Windows\System32\wsl.exe --version 2>&1 | Out-String | Out-File -Encoding utf8 -Append $status

$needReboot = $false
$keys = @(
    'HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\Component Based Servicing\RebootPending',
    'HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\WindowsUpdate\Auto Update\RebootRequired'
)
foreach ($k in $keys) { if (Test-Path $k) { $needReboot = $true } }
Out-Status ("NEED_REBOOT=" + $needReboot)
if ($needReboot) {
    Out-Status "=> Reboot required for WSL2 kernel to take effect. After reboot, run docker compose up -d."
} else {
    Out-Status "=> No reboot required."
}
Out-Status "=== admin branch ended ==="
