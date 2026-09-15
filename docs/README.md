# NETeacher 文档中心

> 面向 6-15 岁中小学生的 AI 英语学习平台 · 产品需求文档（PRD）与项目工程计划
> 阅读对象：产品、设计、研发、测试、运营、项目管理、B 端合作方

本文档体系基于 `reference/PLAN.md`（系统架构专家视角的原始设计纲要）**扩写并结构化**，形成可评审、可追溯、可执行的完整 PRD 与工程计划。本次交付为**纯文档**，不含任何业务代码与工程脚手架。

---

## 一、阅读路径

| 角色 | 建议阅读顺序 |
| --- | --- |
| 决策/PM | README → `00-product/PRD-主文档.md` → `00-product/范围边界与里程碑概览.md` → `04-delivery/里程碑与迭代计划.md` |
| 产品 | README → `00-product/*` → `01-functional/*` → `02-ux/*` |
| 前端 | README → `03-engineering/技术选型对比与决策.md` → `03-engineering/系统架构设计.md` → `03-engineering/API接口规范.md` → `02-ux/*` |
| 后端 | README → `03-engineering/系统架构设计.md` → `03-engineering/数据字典.md` → `03-engineering/API接口规范.md` → `03-engineering/非功能需求与性能指标.md` |
| 测试 | `01-functional/*` → `03-engineering/API接口规范.md` → `04-delivery/质量与测试策略.md` |
| 合规/法务 | `03-engineering/合规安全与未成年人保护.md` → `01-functional/FRD-账号与多角色.md` |

---

## 二、目录结构

```
NETeacher/
├── reference/
│   └── PLAN.md                      # [只读基线] 原始设计纲要，本套文档的输入源
└── docs/
    ├── README.md                    # 本文件：导航、版本、术语表、编号规范、PLAN 映射
    ├── 00-product/
    │   ├── PRD-主文档.md            # 定位、用户分层、价值主张、北极星指标、版本范围、需求总表
    │   └── 范围边界与里程碑概览.md   # 本期非目标、周期版本、发布节奏
    ├── 01-functional/
    │   ├── FRD-账号与多角色.md
    │   ├── FRD-分级课程体系.md
    │   ├── FRD-互动学习模块.md
    │   ├── FRD-评测体系.md
    │   ├── FRD-学习进度追踪.md
    │   ├── FRD-个性化学习路径.md
    │   └── FRD-运营激励与商业化.md
    ├── 02-ux/
    │   ├── 信息架构与页面清单.md
    │   ├── 核心交互流程说明.md
    │   └── 视觉设计规范.md
    ├── 03-engineering/
    │   ├── 技术选型对比与决策.md
    │   ├── 系统架构设计.md
    │   ├── 数据字典.md
    │   ├── API接口规范.md
    │   ├── 非功能需求与性能指标.md
    │   └── 合规安全与未成年人保护.md
    └── 04-delivery/
        ├── 里程碑与迭代计划.md
        ├── 任务拆解与工作量估算.md
        ├── 质量与测试策略.md
        ├── 风险登记册.md
        └── 协作与变更管理.md
```

---

## 三、需求编号规范

所有需求项必须具有全局唯一编号，主文档 `PRD-主文档.md` 的「需求总表」为唯一登记处，子文档不得重复编号。

| 类型 | 前缀 | 示例 | 说明 |
| --- | --- | --- | --- |
| 功能需求 | `FR-<域>-<三位序号>` | `FR-ACC-001` | `<域>` 取自下方功能域缩写 |
| 非功能需求 | `NFR-<类>-<序号>` | `NFR-PERF-001` | `<类>`：PERF 性能 / CAP 容量 / AVAIL 可用性 / COMP 兼容性 / OBS 可观测 / SEC 安全 / REL 可靠 |
| API 接口 | `API-<域>-<三位序号>` | `API-ACC-001` | 与功能域缩写一致 |
| 数据实体 | `DE-<序号>` | `DE-001` | 数据字典中的核心实体 |

**功能域缩写**：`ACC` 账号与多角色 · `CRS` 分级课程 · `LRN` 互动学习 · `TST` 评测体系 · `TRK` 进度追踪 · `PTH` 学习路径 · `OPS` 运营激励 · `UX` 体验/通用。

**优先级定义**：

- **P0**：MVP 必备，缺失则产品不可用，纳入里程碑 M1。
- **P1**：重要能力，纳入里程碑 M2。
- **P2**：增强/增值能力，纳入 M3 及以后。

---

## 四、术语表（Glossary）

| 术语 | 英文 | 解释 |
| --- | --- | --- |
| 分级课程体系 | Graded Curriculum | 按 CEFR 与新课标分 L1-L6 的体系化课程 |
| 主修课 | Core Course | 体系化动画课，每课 5-8 分钟 |
| 拓展课 | Elective Course | 主题式场景微课 |
| 专项课 | Focus Course | 针对薄弱项的强化训练包 |
| 单元测 | Unit Quiz | 每学完约 5 课时进行综合测评 |
| 阶段测 | Stage Exam | 学期/级别末模拟考，产出能力雷达图 |
| 能力雷达图 | Competency Radar | 词汇/语法/听力/口语/阅读/写作六维能力视图 |
| 学习地图 | Learning Map | 以关卡形式可视化 L1-L6 学习路径 |
| 知识图谱 | Knowledge Graph | 知识点及其依赖关系的图结构 |
| 发音评测 | SOE (Spoken English Evaluation) | 腾讯云智聆口语评测，音素级打分（**非「慧眼」**，见勘误） |
| ASR | Automatic Speech Recognition | 自动语音识别 |
| TTS | Text To Speech | 语音合成 |
| LLM | Large Language Model | 大语言模型，用于对话/纠错/批改 |
| 艾宾浩斯曲线 | Ebbinghaus Forgetting Curve | 记忆遗忘曲线，用于复习间隔调度 |
| 模块化单体 | Modular Monolith | 单进程内多模块、高内聚，预留微服务拆分缝 |
| 事件表/Outbox | Outbox Pattern | 本地事务+事件表实现最终一致性的可靠消息模式 |
| 北极星指标 | North Star Metric | 代表产品核心价值的核心度量 |
| 防沉迷 | Anti-Addiction | 未成年人使用时长与时段控制机制 |
| CEFR | Common European Framework | 欧洲语言共同参考框架 |
| 新课标 | 2022 版义务教育英语课标 | 中国大陆义务教育英语课程标准（2022 版） |

> 新增术语须在各自文档中首次出现时补充登记到此表。

---

## 五、PLAN 映射与勘误

原始 `reference/PLAN.md` 六大维度与本文档的对应关系、以及需要修正的内容，详见 [`范围边界与里程碑概览.md`](./00-product/范围边界与里程碑概览.md) 

> PLAN 映射总表另见 `PRD-主文档.md` §8 与 `00-product/范围边界与里程碑概览.md` §3。关键勘误：

| 项 | PLAN 原文 | 修正结论 | 影响文档 |
| --- | --- | --- | --- |
| 口语评测产品 | 腾讯云「慧眼」 | 「慧眼」为人脸核身产品；口语评测对应 **腾讯云智聆口语评测 SOE** | `03-engineering/技术选型对比与决策.md`、`03-engineering/系统架构设计.md`、`01-functional/FRD-互动学习模块.md` |
| 客户端形态 | React Native 跨端 | 改为 **Web 优先 + 移动端 H5 响应式适配**（用户确认） | `03-engineering/技术选型对比与决策.md`、`02-ux/视觉设计规范.md` |
| 后端形态 | Spring Cloud 微服务 | MVP 采用 **Spring Boot 模块化单体**，预留微服务拆分缝 | `03-engineering/技术选型对比与决策.md`、`03-engineering/系统架构设计.md` |
| 分布式事务 | Seata | P0/P1 用 **本地事务 + Outbox 事件表** 替代，避免重型组件 | `03-engineering/系统架构设计.md` |

---

## 六、版本与变更记录

| 版本 | 日期 | 作者 | 说明 |
| --- | --- | --- | --- |
| v0.1-DRAFT | 2026-09-13 | 系统架构/产品 | 基于 PLAN.md 首版扩写，文档体系建立 |

> 本仓库文档采用 Git 版本管理；重大变更需在 `04-delivery/协作与变更管理.md` 中走评审流程后合入 `main`。

---

## 七、文档编写模板（各 FRD 子文档统一遵守）

每个功能点（需求条目）建议包含以下字段，按需省略不适用项：

```markdown
### FR-XXX-001 需求标题
- 优先级：P0 / P1 / P2
- 所属里程碑：M1
- 关联 PLAN：PLAN 2.2 单词记忆
- 功能描述：……
- 前置条件：……
- 主流程：
  1. 用户……
  2. 系统……
- 异常与边界：
  - E1：网络中断时……
  - E2：未成年人无家长绑定……
- 状态机（涉及状态流转时）：用 Mermaid stateDiagram
- 验收标准：
  - AC1：……
  - AC2：……
- 关联需求：FR-XXX-002、NFR-PERF-001
```

---

## 八、图表规范

- 所有流程图、时序图、状态图使用 **Mermaid** 代码块（显式声明 `mermaid` 语言），不使用截图，保证可维护与可评审。
- 架构图、对比矩阵使用 Markdown 表格或 Mermaid。
