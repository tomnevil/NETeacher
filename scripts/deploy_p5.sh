#!/bin/bash
# NETeacher 部署脚本：组卷 UI + 作业布置统计 + 看板趋势/下钻
# 用法：bash /tmp/deploy_p5.sh   （需 root，在 lhins-8w19mn0t 上执行）
set -uo pipefail

SRC=/opt/neteacher-src
BASE=https://raw.githubusercontent.com/tomnevil/NETeacher/main
LOG=/tmp/deploy_p5.log
FAIL=0

echo "=== NETeacher deploy (P5) $(date '+%F %T') ===" | tee "$LOG"

# ---------- 1. 同步改动文件（按标记校验，失败重试） ----------
# 格式：文件路径|校验标记
FILES="
backend/neteacher-assessment/src/main/java/com/neteacher/assessment/entity/Assessment.java|assignmentId
backend/neteacher-assessment/src/main/java/com/neteacher/assessment/dto/QuizSubmitRequest.java|assignmentId
backend/neteacher-assessment/src/main/java/com/neteacher/assessment/service/AssessmentService.java|setAssignmentId
backend/neteacher-ops/src/main/java/com/neteacher/ops/entity/Assignment.java|class Assignment
backend/neteacher-ops/src/main/java/com/neteacher/ops/repository/AssignmentRepository.java|AssignmentRepository
backend/neteacher-ops/src/main/java/com/neteacher/ops/dto/AssignmentCreateDTO.java|AssignmentCreateDTO
backend/neteacher-ops/src/main/java/com/neteacher/ops/dto/AssignmentStatsDTO.java|AssignmentStatsDTO
backend/neteacher-ops/src/main/java/com/neteacher/ops/service/AssignmentService.java|class AssignmentService
backend/neteacher-ops/src/main/java/com/neteacher/ops/controller/AssignmentController.java|AssignmentController
backend/neteacher-ops/src/main/java/com/neteacher/ops/dto/OpsDashboardDTO.java|classBreakdown
backend/neteacher-ops/src/main/java/com/neteacher/ops/service/OpsDashboardService.java|classBreakdown
web/src/api/types.ts|classBreakdown
web/src/api/ops.ts|assignmentStats
web/src/api/assessment.ts|composePaper
web/src/pages/PaperCompose.tsx|PaperCompose
web/src/pages/Assignments.tsx|Assignments
web/src/pages/OpsDashboard.tsx|TrendChart
web/src/layouts/TeacherLayout.tsx|/teacher/papers
web/src/App.tsx|PaperCompose
"

cd "$SRC" || exit 1
echo "--- syncing files ---" | tee -a "$LOG"
while IFS='|' read -r f marker; do
  [ -z "$f" ] && continue
  ok=0
  for i in $(seq 1 8); do
    curl -sL --max-time 15 -o "$f" "$BASE/$f"
    if [ -s "$f" ] && grep -q -- "$marker" "$f"; then ok=1; break; fi
    sleep 3
  done
  if [ $ok -eq 1 ]; then
    echo "  OK   $f" | tee -a "$LOG"
  else
    echo "  FAIL $f" | tee -a "$LOG"
    FAIL=1
  fi
done <<< "$FILES"

if [ $FAIL -eq 1 ]; then
  echo "!!! 部分文件同步失败，已中止（见 $LOG）。可重跑本脚本重试。" | tee -a "$LOG"
  exit 1
fi

# ---------- 2. 构建后端 ----------
echo "--- building backend ---" | tee -a "$LOG"
cd "$SRC/backend" || exit 1
mvn -pl neteacher-server -am package -DskipTests > /tmp/mvn_p5.log 2>&1
if [ $? -ne 0 ]; then
  echo "!!! 后端构建失败，见 /tmp/mvn_p5.log" | tee -a "$LOG"
  tail -20 /tmp/mvn_p5.log | tee -a "$LOG"
  exit 1
fi
echo "  backend OK" | tee -a "$LOG"

# ---------- 3. 构建前端 ----------
echo "--- building frontend ---" | tee -a "$LOG"
cd "$SRC/web" || exit 1
npm run build > /tmp/npm_p5.log 2>&1
if [ $? -ne 0 ]; then
  echo "!!! 前端构建失败，见 /tmp/npm_p5.log" | tee -a "$LOG"
  tail -20 /tmp/npm_p5.log | tee -a "$LOG"
  exit 1
fi
echo "  frontend OK" | tee -a "$LOG"

# ---------- 4. 部署并重启 ----------
echo "--- deploying ---" | tee -a "$LOG"
systemctl stop neteacher
cp "$SRC/backend/neteacher-server/target/neteacher-server-app.jar" /opt/neteacher/
rm -rf /var/www/neteacher/assets /var/www/neteacher/index.html
cp -r "$SRC/web/dist/." /var/www/neteacher/
systemctl start neteacher
sleep 5
systemctl is-active neteacher | tee -a "$LOG"

echo "=== deploy finished $(date '+%F %T') ===" | tee -a "$LOG"
echo "验证建议："
echo "  1) 教师登录 https://yf-admin.tomneil.asia/teacher/papers 组卷"
echo "  2) /teacher/assignments 下发作业并查看统计"
echo "  3) 管理员 13600000000/123456 访问 /admin 看趋势图与班级下钻"
