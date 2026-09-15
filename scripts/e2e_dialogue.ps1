$ErrorActionPreference = 'Continue'
$base = 'http://localhost:8080'
function Get-J($url, $token) {
  $h = @{}
  if ($token) { $h['Authorization'] = "Bearer $token" }
  try { $r = Invoke-WebRequest -Uri ($base+$url) -Headers $h -UseBasicParsing -TimeoutSec 15; return $r.Content } catch { return "ERR:"+$_.Exception.Message }
}
function Post-J($url, $body, $token) {
  $h = @{'Content-Type'='application/json'}
  if ($token) { $h['Authorization'] = "Bearer $token" }
  try { $r = Invoke-WebRequest -Uri ($base+$url) -Method Post -Headers $h -Body ($body | ConvertTo-Json -Compress) -UseBasicParsing -TimeoutSec 20; return $r.Content } catch { return "ERR:"+$_.Exception.Message }
}

# wait health
for ($i=0; $i -lt 30; $i++) {
  $h = Get-J '/api/health'
  if ($h -match '"code":0') { Write-Host "health OK"; break }
  Start-Sleep -Seconds 2
}

# login
$login = Post-J '/api/auth/login' @{phone='13800000000';password='123456'}
$token = ($login | ConvertFrom-Json).data.token
Write-Host "TOKEN: $($token.Substring(0,[Math]::Min(20,$token.Length)))..."

# start dialogue
$start = Post-J '/api/learning/dialogue/start' @{grade=3; unit='购物'} $token
Write-Host "START: $start"
$sid = ($start | ConvertFrom-Json).data.sessionId
Write-Host "SESSION: $sid"

# turn 1
$t1 = Post-J '/api/learning/dialogue/turn' @{sessionId=$sid; transcript='I went to the supermarket and bought some apples.'} $token
Write-Host "TURN1: $t1"

# turn 2
$t2 = Post-J '/api/learning/dialogue/turn' @{sessionId=$sid; transcript='My favorite fruit is watermelon because it is sweet.'} $token
Write-Host "TURN2: $t2"

# end
$end = Post-J '/api/learning/dialogue/end' @{sessionId=$sid} $token
Write-Host "END: $end"

# verify code==0 on each
$all = @($start,$t1,$t2,$end) | ForEach-Object { ($_ | ConvertFrom-Json).code }
Write-Host ("ALL_CODES: " + ($all -join ','))
if ($all -contains 0) { Write-Host "DIALOGUE_FLOW_OK" } else { Write-Host "DIALOGUE_FLOW_FAIL" }
