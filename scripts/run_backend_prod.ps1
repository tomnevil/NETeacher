# run_backend_prod.ps1 - stop dev backend, start prod backend against localhost containers
$ErrorActionPreference = 'Stop'
$env:Path = [Environment]::GetEnvironmentVariable('Path', 'Machine') + ';' + [Environment]::GetEnvironmentVariable('Path', 'User')
$java = 'E:\Tom\tools\jdk21\jdk-21.0.6+7\bin\java.exe'
$jar = 'e:\Tom\Projects\NETeacher\backend\neteacher-server\target\neteacher-server-1.0.0-SNAPSHOT.jar'
$log = 'e:\Tom\Projects\NETeacher\scripts\backend_prod.log'

# 1) free port 8080
try {
    $p = (Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue).OwningProcess | Select-Object -First 1
    if ($p) { Write-Host ("stopping dev backend pid $p on 8080"); Stop-Process -Id $p -Force }
} catch {
    Write-Host ("could not resolve 8080 pid via netstat ($_), trying Stop-Process java")
    Get-Process -Name 'java' -ErrorAction SilentlyContinue | Stop-Process -Force
}
Start-Sleep -Seconds 3

# 2) start prod backend with localhost overrides (containers publish to host ports)
$args = @('-jar', $jar,
    '--spring.profiles.active=prod',
    '--spring.datasource.url=jdbc:mysql://localhost:3306/neteacher?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true',
    '--spring.datasource.username=neteacher',
    '--spring.datasource.password=neteacher123',
    '--spring.data.redis.host=localhost',
    '--spring.data.mongodb.uri=mongodb://localhost:27017/neteacher')
Write-Host ("starting prod backend: $java $args")
Start-Process -FilePath $java -ArgumentList $args -RedirectStandardOutput $log -RedirectStandardError ($log + '.err') -NoNewWindow

# 3) poll health
$up = $false
for ($i = 1; $i -le 60; $i++) {
    Start-Sleep -Seconds 3
    try {
        $r = Invoke-WebRequest -Uri 'http://localhost:8080/actuator/health' -UseBasicParsing -TimeoutSec 5
        $body = $r.Content
        Write-Host ("try $i : HTTP $($r.StatusCode) : $body")
        if ($r.StatusCode -eq 200) { $up = $true; break }
    } catch {
        $msg = $_.Exception.Message.Split([Environment]::NewLine)[0]
        Write-Host ("try $i : not ready ($msg)")
    }
}
if (-not $up) {
    Write-Host "=== backend not healthy; tail log ==="
    Get-Content -Encoding UTF8 $log -Tail 40 | Write-Host
    Get-Content -Encoding UTF8 ($log + '.err') -Tail 20 | Write-Host
    exit 1
}
Write-Host "=== prod backend healthy ==="
