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

# wait for health
for ($i=0; $i -lt 30; $i++) {
  $h = Get-J '/api/health'
  if ($h -match '"code":0' -or $h -notmatch 'ERR') { Write-Host "health OK"; break }
  Start-Sleep -Seconds 2
}
Write-Host "HEALTH: $h"

# login student
$login = Post-J '/api/auth/login' @{phone='13800000000';password='123456'}
Write-Host "LOGIN: $login"
$token = ($login | ConvertFrom-Json).data.token
Write-Host "TOKEN: $($token.Substring(0,[Math]::Min(20,$token.Length)))..."

# quiz
$quiz = Get-J '/api/assessments/quiz?level=1' $token
Write-Host "QUIZ len: $((($quiz|ConvertFrom-Json).data).Count)"

# submit: answer first option for each
$qs = ($quiz|ConvertFrom-Json).data
$answers = $qs | ForEach-Object { @{questionId=$_.id; answer=$_.options[0]} }
$subj = $qs[0].subject; $lvl = $qs[0].level
$submit = Post-J '/api/assessments/quiz/submit' @{subject=$subj; level=$lvl; type='quiz'; answers=$answers} $token
Write-Host "SUBMIT: $submit"

# wrong book
$wrong = Get-J '/api/assessments/wrong' $token
Write-Host "WRONGBOOK: $wrong"

# recommend
$rec = Get-J '/api/recommend/path' $token
Write-Host "RECOMMEND: $rec"

# progress
$prog = Get-J '/api/progress/dashboard' $token
Write-Host "PROGRESS: $prog"

# modules
$mods = Get-J '/api/learning/modules' $token
Write-Host "MODULES: $mods"

# speaking
$spk = Post-J '/api/learning/speaking' @{module='speaking'; targetText='I practice English every morning.'; transcript='I practice English every morning.'; durationSec=5} $token
Write-Host "SPEAKING: $spk"

# ops plans
$plans = Get-J '/api/ops/plans' $token
Write-Host "PLANS: $plans"

# parent bind-status
$bind = Get-J '/api/user/bind-status' $token
Write-Host "BINDSTATUS: $bind"

# wechat login
$wx = Post-J '/api/auth/wechat' @{code='demo'}
Write-Host "WECHAT: $wx"
