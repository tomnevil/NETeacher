$base = 'http://localhost:8080'
for ($i = 0; $i -lt 40; $i++) {
  try {
    $r = Invoke-WebRequest -Uri ($base + '/api/health') -UseBasicParsing -TimeoutSec 5
    if ($r.Content -match 'code') { Write-Host 'healthy'; break }
  } catch { Start-Sleep -Seconds 2 }
}
$wx = Invoke-WebRequest -Uri ($base + '/api/auth/wechat') -Method Post -ContentType 'application/json' -Body '{"code":"demo"}' -UseBasicParsing -TimeoutSec 15
Write-Host ('WECHAT: ' + $wx.Content)

$login = Invoke-WebRequest -Uri ($base + '/api/auth/login') -Method Post -ContentType 'application/json' -Body '{"phone":"13800000000","password":"123456"}' -UseBasicParsing -TimeoutSec 15
$tok = ($login.Content | ConvertFrom-Json).data.token
$wb = Invoke-WebRequest -Uri ($base + '/api/assessments/wrong') -Headers @{'Authorization' = "Bearer $tok"} -UseBasicParsing -TimeoutSec 15
Write-Host ('WRONGBOOK: ' + $wb.Content)
