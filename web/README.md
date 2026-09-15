# NETeacher Web 端

React 18 + Vite + TypeScript + Tailwind CSS 前端（Web 优先 + 移动端 H5 响应式适配），对应 `docs/` 中《视觉设计规范》《信息架构与页面清单》。

## 技术栈

- React 18 / TypeScript / Vite 5
- Tailwind CSS 3（品牌色 `brand` / `accent`）
- react-router-dom 6（路由）
- zustand（鉴权状态）
- @tanstack/react-query（服务端状态）
- axios（请求，含 JWT 拦截与 `/api` 代理）

## 开发

```bash
npm install
npm run dev      # http://localhost:5173，/api 代理到 http://localhost:8080
```

## 构建

```bash
npm run build    # tsc -b && vite build -> dist/
npm run preview
```

## 目录

- `src/api`：axios 实例与接口（request / user）
- `src/store`：zustand 状态（auth）
- `src/layouts`：学生端布局（桌面侧栏 + 移动端底栏）
- `src/pages`：Login / Home / LearningMap / NotFound
- `src/components/ui`：Button / Card / Input

> 当前为脚手架：页面为占位数据，登录接口对接 `backend` 的 `/api/auth/login`（演示阶段验证码任意）。
