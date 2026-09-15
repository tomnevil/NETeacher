$jar = 'E:\Tom\Projects\NETeacher\backend\neteacher-server\target\neteacher-server-1.0.0-SNAPSHOT.jar'
$jcmd = 'E:\Tom\tools\jdk21\jdk-21.0.6+7\bin\jar.exe'
Write-Host '--- neteacher libs in BOOT-INF/lib ---'
& $jcmd tf $jar | Select-String 'BOOT-INF/lib/neteacher'
Write-Host '--- classes inside neteacher-learning (executable layout check) ---'
$tmp = 'E:\Tom\Projects\NETeacher\scripts\_learn.jar'
& $jcmd xf $jar 'BOOT-INF/lib/neteacher-learning-1.0.0-SNAPSHOT.jar'
Move-Item 'BOOT-INF/lib/neteacher-learning-1.0.0-SNAPSHOT.jar' $tmp -Force
& $jcmd tf $tmp | Select-String 'LearningRecordRepository|BOOT-INF/classes'
Remove-Item $tmp -Force
