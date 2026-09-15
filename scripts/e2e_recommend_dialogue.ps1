$ErrorActionPreference = 'Continue'
$base = 'http://localhost:8080'
function Post-J($url, $body, $token) {
  $h = @{'Content-Type'='application/json; charset=utf-8'}
  if ($token) { $h['Authorization'] = "Bearer $token" }
  $bytes = [System.Text.Encoding]::UTF8.GetBytes(($body | ConvertTo-Json -Compress -Depth 5))
  try { $r = Invoke-WebRequest -Uri ($base+$url) -Method Post -Headers $h -Body $bytes -UseBasicParsing -TimeoutSec 20; return $r.Content } catch { return "ERR:"+$_.Exception.Message }
}

for ($i=0; $i -lt 30; $i++) {
  try { $hh = Invoke-WebRequest -Uri ($base+'/api/health') -UseBasicParsing -TimeoutSec 3; if ($hh.Content -match '"code":0') { Write-Host "health OK"; break } } catch {}
  Start-Sleep -Seconds 2
}

$login = Post-J '/api/auth/login' @{phone='13800000000';password='123456'}
$token = ($login | ConvertFrom-Json).data.token
Write-Host "TOKEN: $($token.Substring(0,[Math]::Min(16,$token.Length)))..."

# 对话薄弱点 -> 强化练习任务（英文薄弱点，验证技能映射）
$req = @{ grade=3; unit='shopping'; weaknesses=@('grammar and vocabulary usage needs improvement','speaking fluency can be better','listening comprehension not accurate') }
$resp = Post-J '/api/recommend/from-dialogue' $req $token
Write-Host "FROM_DIALOGUE: $resp"
$code = ($resp | ConvertFrom-Json).code
$items = ($resp | ConvertFrom-Json).data
Write-Host ("CODE=$code ITEMS=" + ($items.Count))
if ($code -eq 0 -and $items.Count -gt 0) { Write-Host "RECOMMEND_FROM_DIALOGUE_OK" } else { Write-Host "RECOMMEND_FROM_DIALOGUE_FAIL" }
foreach ($it in $items) { Write-Host ("  - L$($it.level) $($it.title) | $($it.reason)") }
