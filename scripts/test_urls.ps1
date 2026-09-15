$urls = @(
  "https://api.adoptium.net/v3/assets/version/21",
  "https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jdk",
  "https://aka.ms/download-jdk/microsoft-jdk-21.0.5-windows-x64.zip",
  "https://aka.ms/download-jdk/microsoft-jdk-21.0.6-windows-x64.zip"
)
foreach ($u in $urls) {
  try {
    $r = Invoke-WebRequest -Uri $u -Method Head -UseBasicParsing -TimeoutSec 25 -MaximumRedirection 8
    Write-Host ("OK   " + $r.StatusCode + "  " + $u)
  } catch {
    Write-Host ("ERR  " + ($_.Exception.Message -split [Environment]::NewLine)[0] + "  " + $u)
  }
}
