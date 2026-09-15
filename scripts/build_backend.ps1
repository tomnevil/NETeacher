$ErrorActionPreference = "Continue"
# 刷新进程级环境（宿主进程未继承新写入的用户变量）
$env:JAVA_HOME = [Environment]::GetEnvironmentVariable('JAVA_HOME', 'User')
$env:MAVEN_HOME = [Environment]::GetEnvironmentVariable('MAVEN_HOME', 'User')
$env:Path = [Environment]::GetEnvironmentVariable('Path', 'User') + ';' + [Environment]::GetEnvironmentVariable('Path', 'Machine')

$settings = "e:\Tom\Projects\NETeacher\scripts\settings.xml"
Set-Location e:\Tom\Projects\NETeacher\backend

Write-Host "=== MVN VERSION ==="
& "$env:MAVEN_HOME\bin\mvn.cmd" -version

Write-Host "=== BUILD: mvn package -DskipTests ==="
& "$env:MAVEN_HOME\bin\mvn.cmd" -B -s $settings package -DskipTests *>&1 | Select-Object -Last 160

Write-Host "=== BUILD EXIT CODE: $LASTEXITCODE ==="
if ($LASTEXITCODE -eq 0) {
  Write-Host "=== ARTIFACTS ==="
  Get-ChildItem -Path e:\Tom\Projects\NETeacher\backend\neteacher-server\target -Filter *.jar | ForEach-Object { $_.FullName }
}
