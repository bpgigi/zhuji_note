# 参考项目对标分析（Reference Benchmark）

> 本目录收录多个优秀开源笔记类 App 作为 ZhujiNote 的设计参考。源码仅供本地学习借鉴，已在 `.gitignore` 中排除（不入库），此文档记录对标结论。

## 收录项目

| 项目 | 语言 | 架构 | 数据库 | UI | Stars 量级 | 借鉴点 |
|------|------|------|--------|-----|-----------|--------|
| [Notally](https://github.com/OmGodse/Notally) | Kotlin (106 文件) | MVVM | Room | View+部分Compose | 1.5k+ | Room 实体设计、回收站软删除、标签多对多 |
| [Simple-Notes](https://github.com/SimpleMobileTools/Simple-Notes) | Kotlin (52 文件) | MVC/MVVM | Room | View | 3k+ | 多笔记切换、Widget、导入导出 |
| [Markor](https://github.com/gsantner/markor) | Java (159 文件) | MVC | 文件系统 | View | 4k+ | Markdown 编辑器工具栏、实时预览、语法高亮 |
| [Diary](https://github.com/billthefarmer/diary) | Java (11 文件) | 简单 | 文件系统 | View | 200+ | Markdown 日记、轻量结构 |

## 关键对标结论

### 1. Markdown 编辑器（借鉴 Markor）
Markor 的编辑器工具栏提供 H1-H3、加粗、斜体、列表、checkbox、代码块、链接、引用快捷插入。ZhujiNote 的 `MarkdownToolbar.kt` 借鉴此设计，实现 10 项快捷格式化，并在 `TextFieldValue` 上做光标位置感知的文本插入（比 Markor 的纯字符串拼接更精确）。

### 2. Room 实体设计（借鉴 Notally）
Notally 用 `BaseNote` 单实体 + type 字段区分笔记/清单。ZhujiNote 改进为 `NoteEntity` + `TagEntity` + `NoteTagCrossRef` 多对多关系，更符合关系范式，并额外引入 **FTS4 全文索引**（Notally 无此特性）。

### 3. 回收站软删除（借鉴 Notally + Simple-Notes）
两者都用 `folder`/`type` 字段标记 deleted。ZhujiNote 用 `deletedAt: Long?` 时间戳，既能软删除恢复，又能按删除时间排序/自动清理，比布尔标记信息量更大。

### 4. 导入导出（借鉴 Simple-Notes）
Simple-Notes 支持 .txt 导入导出。ZhujiNote 的 `BackupExporter` 升级为 **JSON（结构化全量备份）+ Markdown（单篇导出）+ Zip（打包）** 三种格式。

## ZhujiNote 相比参考项目的超越点

| 维度 | 参考项目普遍情况 | ZhujiNote |
|------|-----------------|-----------|
| UI 框架 | 多为传统 View 体系 | 全量 Jetpack Compose + Material 3 |
| 架构 | MVC/简单 MVVM | 严格 Clean Architecture 三层 |
| AI 能力 | 无 | DeepSeek v4-flash 流式 9 大功能 + 多轮 Chat |
| 主题 | 单一/简单暗色 | Claude 亮色 + Windsurf 暗色双主题，13 槽 Material 3 |
| 全文搜索 | LIKE 模糊匹配 | Room FTS4 倒排索引 |
| 测试 | 少量或无 | 178 单测 + 30 集成测试，逻辑层覆盖 68% |
| CI | 多为单一 build | GitHub Actions（build+test+Espresso）+ Jenkinsfile |
| 安全 | 明文偏好 | EncryptedSharedPreferences（AES256-GCM）存 API Key |
| 高级功能 | — | 番茄钟、写作目标连续天数、双向链接、笔记模板、分享卡片、多选批量 |

## 复现方式

```bash
# 本地拉取参考项目（不入库）
git clone --depth 1 https://github.com/OmGodse/Notally.git refs/Notally-kotlin-room-mvvm
git clone --depth 1 https://github.com/SimpleMobileTools/Simple-Notes.git refs/SimpleNotes-kotlin
git clone --depth 1 https://github.com/gsantner/markor.git refs/Markor-java-android
git clone --depth 1 https://github.com/billthefarmer/diary.git refs/Diary-java
```
