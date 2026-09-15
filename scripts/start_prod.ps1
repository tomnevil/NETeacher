# start_prod.ps1 - stop dev backend, start prod backend (no wait)
$ErrorActionPreference = 'Stop'
$env:Path = [Environment]::GetEnvironmentVariable('Path', 'Machine') + ';' + [Environment]::GetEnvironmentVariable('Path', 'User')
$java = 'E:\Tom\tools\jdk21\jdk-21.0.6+7\bin\java.exe'
$jar = 'e:\Tom\Projects\NETeacher\backend\neteacher-server\target\neteacher-server-1.0.0-SNAPSHOT.jar'
$log = 'e:\Tom\Projects\NETeacher\scripts\backend_prod.log'

try {
    $p = (Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue).OwningProcess | Select-Object -First 1
    if ($p) { Write-Host ("stopping dev backend pid $p on 8080"); Stop-Process -Id $p -Force }
} catch {
    Write-Host ("netstat 8080 failed ($_), fallback Stop-Process java")
    Get-Process -Name 'java' -ErrorAction SilentlyContinue | Stop-Process -Force
}
Start-Sleep -Seconds 3

$args = @('-jar', $jar,
    '--spring.profiles.active=prod',
    '--spring.datasource.url=jdbc:mysql://localhost:3306/neteacher?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true',
    '--spring.datasource.username=neteacher',
    '--spring.datasource.password=neteacher123',
    '--spring.data.redis.host=localhost',
    '--spring.data.mongodb.uri=mongodb://localhost:27017/neteacher')
Write-Host ("starting prod backend...")
Start-Process -FilePath $java -ArgumentList $args -RedirectStandardOutput $log -RedirectStandardError ($log + '.err') -NoNewWindow
Write-Host "prod backend launch requested (pid may take ~20s to become healthy)"
