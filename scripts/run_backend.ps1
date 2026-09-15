$env:JAVA_HOME = [Environment]::GetEnvironmentVariable('JAVA_HOME', 'User')
$env:Path = "$env:JAVA_HOME\bin;" + [Environment]::GetEnvironmentVariable('Path', 'User') + ';' + [Environment]::GetEnvironmentVariable('Path', 'Machine')

$jar = "e:\Tom\Projects\NETeacher\backend\neteacher-server\target\neteacher-server-1.0.0-SNAPSHOT.jar"
Start-Process -FilePath "$env:JAVA_HOME\bin\java.exe" -ArgumentList "-jar", $jar `
  -RedirectStandardOutput "e:\Tom\Projects\NETeacher\scripts\backend.out.log" `
  -RedirectStandardError  "e:\Tom\Projects\NETeacher\scripts\backend.err.log" -NoNewWindow
Write-Host "backend launching (pid started)..."

$ready = $false
for ($i = 1; $i -le 30; $i++) {
  Start-Sleep -Seconds 2
  try {
    $r = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"a","password":"b"}' -UseBasicParsing -TimeoutSec 3
    Write-Host ("try $i : HTTP " + $r.StatusCode + " (service is UP)")
    $ready = $true
    break
  } catch {
    $sc = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { "DOWN" }
    Write-Host ("try $i : status=$sc (waiting...)")
  }
}

if (-not $ready) {
  Write-Host "=== backend did not become ready; tail of err log ==="
  Get-Content "e:\Tom\Projects\NETeacher\scripts\backend.err.log" -Tail 25
} else {
  Write-Host "=== SWAGGER UI ==="
  try { Write-Host (Invoke-WebRequest -Uri "http://localhost:8080/swagger-ui/index.html" -UseBasicParsing -TimeoutSec 10).StatusCode } catch { "SWAGGER_ERR: $($_.Exception.Message)" }
}
Write-Host "=== DONE (backend still running in background) ==="
