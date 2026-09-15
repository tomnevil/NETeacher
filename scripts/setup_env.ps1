$ErrorActionPreference = "Stop"
$tools = "E:\Tom\tools"
$dl = "$tools\downloads"
New-Item -ItemType Directory -Force -Path $dl | Out-Null

# ---- JDK 21 (Microsoft Build of OpenJDK) ----
Write-Host "=== DOWNLOAD JDK 21 (Microsoft Build of OpenJDK) ==="
$jdkUrl = "https://aka.ms/download-jdk/microsoft-jdk-21.0.6-windows-x64.zip"
Write-Host "JDK_URL=$jdkUrl"
Invoke-WebRequest -Uri $jdkUrl -OutFile "$dl\jdk21.zip" -TimeoutSec 600
Write-Host "JDK zip OK"

# ---- Maven (primary + fallback mirrors) ----
Write-Host "=== DOWNLOAD MAVEN ==="
$mvnCandidates = @(
  "https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip",
  "https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip"
)
$mvnOk = $false
foreach ($u in $mvnCandidates) {
  try {
    Invoke-WebRequest -Uri $u -OutFile "$dl\maven.zip" -TimeoutSec 600
    Write-Host "Maven downloaded from $u"
    $mvnOk = $true
    break
  } catch {
    Write-Host "Maven download failed: $u -> $($_.Exception.Message)"
  }
}
if (-not $mvnOk) { throw "Maven download failed from all mirrors" }

# ---- Extract JDK ----
Write-Host "=== EXTRACT JDK ==="
Expand-Archive -Path "$dl\jdk21.zip" -DestinationPath "$tools\jdk21" -Force
$jdkHome = (Get-ChildItem "$tools\jdk21" -Directory | Select-Object -First 1).FullName
Write-Host "JDK_HOME=$jdkHome"

# ---- Extract Maven ----
Write-Host "=== EXTRACT MAVEN ==="
Expand-Archive -Path "$dl\maven.zip" -DestinationPath "$tools\maven-tmp" -Force
$mvnSrc = (Get-ChildItem "$tools\maven-tmp" -Directory | Where-Object { $_.Name -like "apache-maven*" } | Select-Object -First 1).FullName
$mvnHome = "$tools\maven"
if (Test-Path $mvnHome) { Remove-Item $mvnHome -Recurse -Force }
Move-Item $mvnSrc $mvnHome
Write-Host "MVN_HOME=$mvnHome"

# ---- Set User PATH + JAVA_HOME/MAVEN_HOME ----
Write-Host "=== SET USER ENV ==="
$javaBin = "$jdkHome\bin"
$mvnBin = "$mvnHome\bin"
$userPath = [Environment]::GetEnvironmentVariable("Path", "User")
if ($userPath -notlike "*$javaBin*") { $userPath = "$javaBin;$userPath" }
if ($userPath -notlike "*$mvnBin*") { $userPath = "$mvnBin;$userPath" }
[Environment]::SetEnvironmentVariable("Path", $userPath, "User")
[Environment]::SetEnvironmentVariable("JAVA_HOME", $jdkHome, "User")
[Environment]::SetEnvironmentVariable("MAVEN_HOME", $mvnHome, "User")
Write-Host "PATH/JAVA_HOME/MAVEN_HOME updated"

# ---- Verify ----
Write-Host "=== VERIFY ==="
$env:Path = "$javaBin;$mvnBin;$env:Path"
$env:JAVA_HOME = $jdkHome
& "$javaBin\java.exe" -version
cmd /c "$mvnBin\mvn.cmd -version"
Write-Host "=== ALL DONE ==="
