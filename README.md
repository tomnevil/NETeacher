# NETeacher · 中小学英语 AI 学习平台

基于 [`reference/PLAN.md`](reference/PLAN.md) 规划的中小学英语 AI 学习平台（学-练-测-评-用闭环）。完整 PRD 与项目工程计划见 [`docs/`](docs/README.md)。

## 仓库结构

| 路径 | 说明 |
| --- | --- |
| `docs/` | PRD、功能需求（FRD）、UX、工程与交付文档（详见 [docs/README.md](docs/README.md)） |
| `reference/PLAN.md` | 原始系统架构设计纲要 |
| `backend/` | Spring Boot 3 模块化单体后端（详见 [backend/README.md](backend/README.md)） |
| `web/` | React 18 + Vite + TS + Tailwind 前端（详见 [web/README.md](web/README.md)） |

## 技术栈

- 前端：React 18 / Vite / TypeScript / Tailwind / Zustand / React-Query（Web 优先 + H5 响应式）
- 后端：Spring Boot 3 模块化单体（7 业务模块 + 公共模块 + 主应用）
- 数据：MySQL 8 / Redis 7 / MongoDB 7
- AI：腾讯云智聆口语评测 SOE / TTS / 混元大模型（当前为 Mock 适配器占位）

## 快速开始

```bash
# 后端（需 JDK 17+ 与 Maven 3.9+）
cd backend && mvn clean package && java -jar neteacher-server/target/neteacher-server-1.0.0-SNAPSHOT.jar

# 前端（需 Node 18+）
cd web && npm install && npm run dev
```

前端默认 `http://localhost:5173`，开发期 `/api` 代理到后端 `http://localhost:8080`。
