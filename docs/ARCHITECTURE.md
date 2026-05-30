# ZhujiNote 架构设计文档（ARCHITECTURE）

> 助记笔记 · Android · Kotlin · Jetpack Compose · Clean Architecture + MVVM

## 1. 总体分层

本项目采用 **Clean Architecture（整洁架构）三层 + MVVM 表现层模式**，依赖方向严格单向：`ui → domain → data`，`domain` 不依赖任何 Android/框架类型。

```
app/src/main/java/com/zhuji/note/
├── ui/            表现层（Jetpack Compose + ViewModel）
│   ├── screens/   各功能屏幕（notes/edit/settings/stats/trash/ai/pomodoro/template/goal）
│   ├── common/    可复用 UI 组件（Loading/Press/Clipboard/MarkdownView）
│   ├── theme/     Material 3 主题（Claude 亮色 + Windsurf 暗色）
│   └── navigation 导航图（Navigation-Compose）
├── domain/        领域层（纯 Kotlin，无 Android 依赖）
│   ├── model/     领域模型（Note/NoteFilter/NoteOrder/Tag/NoteTemplate/Pomodoro/WritingGoal）
│   ├── repository 仓库接口（NoteRepository）
│   └── util/      纯逻辑工具（MarkdownToolbar/BiLinkParser/TemplateEngine/NoteStatistics/ShareCardGenerator）
├── data/          数据层
│   ├── local/db/  Room 数据库（Entities/Daos/ZhujiDatabase + FTS4）
│   ├── local/preferences/  DataStore + EncryptedSharedPreferences（API Key 加密）
│   ├── repository/ 仓库实现（NoteRepositoryImpl）
│   └── backup/    导入导出（BackupExporter：JSON + Markdown + Zip）
├── ai/            DeepSeek AI 客户端（OkHttp SSE 流式 + AiAction 9 功能枚举）
└── di/            Hilt 依赖注入模块（AppModule）
```

## 2. 关键设计决策

### 2.1 为什么 Clean Architecture
- **可测性**：domain 层纯 Kotlin，可用 JUnit 直接跑，无需 Android 运行时 → 单测覆盖率高、跑得快。
- **可替换**：Repository 接口在 domain，实现在 data。测试时用 MockK 替身，生产用 Room 实现。
- **关注点分离**：UI 只管渲染与事件，业务逻辑在 domain，数据访问在 data。

### 2.2 状态管理（MVVM + UDF）
- 每个屏幕一个 `ViewModel`，持有 `StateFlow<UiState>` 作为**单一数据源**。
- UI 通过 `collectAsStateWithLifecycle()` 订阅，单向数据流（UDF）。
- 一次性事件（Snackbar/导航）用 `SharedFlow` 发射，避免配置变更重放。

### 2.3 数据库（Room + FTS4）
- `NoteEntity` / `TagEntity` / `NoteTagCrossRef`（多对多）。
- `NoteFts`（FTS4 虚拟表）做全文搜索，`@Fts4(contentEntity = ...)` 与主表同步。
- 排序/过滤用 **SQL 原生** `RawQuery`（`SupportSQLiteQuery`），而非 Kotlin 内存排序 → 性能更好、覆盖真实 SQL 路径。
- 软删除（`deletedAt`）实现回收站，硬删除清空。

### 2.4 AI 客户端（DeepSeek v4-flash）
- **OkHttp + SSE 流式**：`chatStream()` 返回 `Flow<StreamEvent>`，逐 token 发射。
- **reasoning_content 分离**：v4-flash 是推理模型，思考过程（reasoning_content）与最终答案（content）分别处理，UI 上「思考中…」与正文分区显示。
- **IO 调度**：所有网络调用 `.flowOn(Dispatchers.IO)`，避免 NetworkOnMainThreadException。
- **9 大功能**：总结/续写/润色/翻译/关键词/起标题/大纲/闪卡/问答，每个对应一条 system prompt（`AiAction` 枚举）。
- **多轮对话**：独立 Chat 页维护消息历史，支持上下文连续对话。

### 2.5 安全
- DeepSeek API Key 存于 **EncryptedSharedPreferences**（AES256-GCM），不落明文、不进 DataStore、不进 git。
- `local.properties`、`*.keystore` 均在 `.gitignore`。

## 3. 技术栈

| 层 | 技术 |
|----|------|
| 语言 | Kotlin 1.9.24 |
| UI | Jetpack Compose (BOM) + Material 3 |
| 架构 | Clean Architecture + MVVM + UDF |
| DI | Hilt |
| 数据库 | Room + FTS4 |
| 偏好 | DataStore + EncryptedSharedPreferences |
| 异步 | Coroutines + Flow |
| 网络 | OkHttp（SSE 流式） |
| 序列化 | kotlinx.serialization |
| 导航 | Navigation-Compose |
| 构建 | Gradle 8.9 + AGP 8.5.2 + KSP |

## 4. 模块依赖图

```
        ┌──────────┐
        │   ui     │  Compose + ViewModel
        └────┬─────┘
             │ depends on
        ┌────▼─────┐
        │  domain  │  纯 Kotlin（model + repository接口 + util）
        └────▲─────┘
             │ implements
        ┌────┴─────┐
        │   data   │  Room + DataStore + Repository实现
        └────┬─────┘
             │ uses
        ┌────▼─────┐
        │    ai    │  DeepSeek SSE 客户端
        └──────────┘
  di（Hilt）横向注入所有层
```
