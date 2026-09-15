# API 接口规范

> 前后端交互契约。统一通过 API 网关（鉴权/限流/路由）。编号 `API-<域>-<序号>`，与功能域一致。
> 存储与字段见 `数据字典.md`；性能见 `非功能需求与性能指标.md`。

## 1. 通用约定

- **协议**：HTTPS + JSON；实时评测分用 WebSocket/SSE（路径 `/ws/eval/:taskId`）。
- **版本**：URL 前缀 `/api/v1`；破坏性变更升 v2。
- **鉴权**：`Authorization: Bearer <JWT>`；网关校验并注入 `userId`/`userType`。
- **幂等**：写操作带 `Idempotency-Key`，服务端去重（积分/进度一致见 `系统架构设计.md` §3.3）。
- **分页**：`?page=1&size=20`，响应 `total/page/list`。
- **错误码**：
  | code | 含义 |
  | --- | --- |
  | 0 | 成功 |
  | 401 | 未认证 |
  | 403 | 无权限（如未绑定家长/免费版超限） |
  | 429 | 限流 |
  | 500 | 服务错误 |
  | 503 | AI 能力降级 |

## 2. 核心接口清单

### API-ACC-001 登录
`POST /api/v1/auth/login` — 手机号/微信登录，返回 JWT + 用户态（含 `status` 是否游客/已绑定）。

### API-ACC-002 绑定家长
`POST /api/v1/account/bind-parent` — 学生绑定家长手机号并短信确认。403 若未确认。

### API-ACC-003 游客计数
`GET /api/v1/account/guest/quota` — 返回剩余免费课次数（3→0 引导注册）。

### API-CRS-001 入学测评定级
`POST /api/v1/placement` — 提交听力/口语(SOE)/词汇子项 → 返回 `init_level`。降级：无麦克风仅词汇+听力。

### API-CRS-002 课程/学习地图
`GET /api/v1/courses?level=L3` — 课程列表；`GET /api/v1/map` — 学习地图节点状态。

### API-LRN-001 口语评测提交
`POST /api/v1/speaking/evaluate` — 上传音频分片 → 返回实时分；`WS /ws/eval/:taskId` 推送详细报告。

### API-LRN-002 语法/单词判分
`POST /api/v1/exercise/grade` — 提交作答 → 即时反馈（含知识点讲解引用）。

### API-TST-001 单元测提交判分
`POST /api/v1/quiz/unit/submit` — 客观题自动判分，主观题入队 AI+人工复核。

### API-TST-002 报告查询
`GET /api/v1/report/:id` — 测评报告（成绩、音素详情、雷达图数据）。

### API-TRK-001 学生仪表盘
`GET /api/v1/student/dashboard` — 今日时长/掌握词数/口语均分/完成率。

### API-TRK-002 家长报告
`GET /api/v1/parent/report?childId=&range=week|month` — 仅见绑定孩子数据（403 越权）。

### API-PTH-001 推荐
`GET /api/v1/recommend` — 返回夯实包/路径/下一关卡（实时+离线融合）。

### API-OPS-001 会员开通
`POST /api/v1/membership/activate` — 开通会员，影响配额开关（见 `FR-OPS-003/004`）。

## 3. 限流
- 口语评测：免费版每日 3 次（`FR-OPS-003`）；会员无限。网关按 `userId` 配额。
- 通用：单用户 100 QPS，突发 200。

## 4. 安全
- 传输加密 TLS1.2+；敏感字段（手机号）落库加密。
- 越权防护：所有 `childId`/`classId` 资源校验归属（家长绑定/教师班级）。
- 防沉迷：学习时长由后端校验（`FR-OPS-009/010`）。
