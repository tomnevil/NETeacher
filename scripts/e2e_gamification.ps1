$base = "http://localhost:8080"
$h = @{ "Content-Type" = "application/json" }

# 1) 登录
$login = @{ phone = "13800000000"; password = "123456" } | ConvertTo-Json
$r = Invoke-WebRequest -Uri "$base/api/auth/login" -Method Post -Headers $h -Body $login -UseBasicParsing
$token = (($r.Content | ConvertFrom-Json).data.token)
Write-Host ("LOGIN_OK token_len=" + $token.Length)

$auth = @{ "Content-Type" = "application/json"; "Authorization" = "Bearer $token" }

# 2) 首页聚合
$r = Invoke-WebRequest -Uri "$base/api/home/today" -Headers $auth -UseBasicParsing
$d = ($r.Content | ConvertFrom-Json).data
Write-Host ("HOME_OK tasks=" + $d.tasks.Count + " totalStars=" + $d.totalStars + " speakingAvg=" + $d.stats.speakingAvg + " streak=" + $d.stats.streakDays)

# 3) 学习地图
$r = Invoke-WebRequest -Uri "$base/api/home/map" -Headers $auth -UseBasicParsing
$levels = (($r.Content | ConvertFrom-Json).data)
Write-Host ("MAP_OK levels=" + $levels.Count)
foreach ($lv in $levels) { Write-Host ("  " + $lv.lv + " " + $lv.name + " unlocked=" + $lv.unlocked + " stars=" + $lv.stars + " current=" + $lv.current) }

# 4) 打卡状态
$r = Invoke-WebRequest -Uri "$base/api/home/checkin" -Headers $auth -UseBasicParsing
$ci = ($r.Content | ConvertFrom-Json).data
Write-Host ("CHECKIN_GET checkedToday=" + $ci.checkedToday + " streak=" + $ci.streakDays)

# 5) 打卡
$r = Invoke-WebRequest -Uri "$base/api/home/checkin" -Method Post -Headers $auth -UseBasicParsing
$ci = ($r.Content | ConvertFrom-Json).data
Write-Host ("CHECKIN_POST checkedToday=" + $ci.checkedToday + " streak=" + $ci.streakDays + " total=" + $ci.totalDays)

# 6) 跟读任务
$r = Invoke-WebRequest -Uri "$base/api/assessments/speaking/task?level=3" -Headers $auth -UseBasicParsing
$task = ($r.Content | ConvertFrom-Json).data
Write-Host ("SPEAK_TASK ref=" + $task.refText + " words=" + $task.words.Count)

# 7) 跟读评测
$ev = @{ taskId = $task.id; refText = $task.refText; transcript = "We read books in the library" } | ConvertTo-Json
$r = Invoke-WebRequest -Uri "$base/api/assessments/speaking/evaluate" -Method Post -Headers $auth -Body $ev -UseBasicParsing
$res = ($r.Content | ConvertFrom-Json).data
Write-Host ("SPEAK_EVAL score=" + $res.score + " accuracy=" + $res.accuracy + " phonemes=" + $res.phonemes.Count + " waveRef=" + $res.waveformRef.Count + " waveUser=" + $res.waveformUser.Count)

Write-Host "ALL_OK"
