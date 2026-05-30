# 项目目标与分工

## 一、最终目标

《移动应用测试》(MT2026) 课程大作业：交付一个 production-grade、功能丰富的 **助记笔记 App（ZhujiNote）**，要求：

- Android Kotlin + Jetpack Compose，Clean Architecture（data / domain / ui 三层）+ Hilt 依赖注入
- 接入 **DeepSeek AI agent**（自动探测最新 flash 模型），提供笔记智能处理能力
- 完整覆盖课程 **L02-L09 全部测试能力**：
  - L02 开发环境（JDK 21 + Android Studio + SDK + AVD）
  - L03 APK 构建与安装
  - L04 GUI 测试 / 截图
  - L05 JUnit 单元测试
  - L06 MockK Mock 测试
  - L07 Robolectric + JaCoCo 三阶段覆盖率（独立 .exec + 累积报告）
  - L08 Espresso 集成测试
  - L09 持续集成（GitHub Actions + Jenkins）
- 暗/亮双主题：Claude 暖橙亮色 + Windsurf 霓虹青暗色
- 在 AVD 模拟器上实测全部功能并截图归档
- 每个里程碑累积推送 GitHub，保留开发痕迹

## 二、核心功能清单

- 笔记 CRUD + Markdown 编辑/预览 + 10 项工具栏 + 双向链接 `[[]]`
- DeepSeek AI：9 个一键功能（总结/续写/润色/翻译/关键词/起标题/大纲/闪卡/问答）+ 多轮对话，SSE 流式
- 8 种笔记模板、番茄钟、写作目标、统计图表、回收站、多选批量、分享为图片、SAF 备份导入导出、提醒
- 全文搜索（FTS4）、标签、置顶/收藏、加密存储 API Key

## 三、测试与质量目标

- 单元测试三阶段（JUnit / MockK / Robolectric）全绿
- JaCoCo 累积覆盖率（聚焦业务层，UI 由集成测试覆盖）
- Espresso 集成测试在模拟器跑通
- CI/CD 双轨（GitHub Actions + Jenkins）全绿

## 四、小组分工（三人）

> 学号、姓名由各成员自行填写。

| 成员 | 学号 | 主要分工 |
|------|------|----------|
| 成员 A | __________ | __________ |
| 成员 B | __________ | __________ |
| 成员 C | __________ | __________ |

建议分工方向（可按实际调整）：
- **成员 A**：应用功能开发（笔记/编辑器/Markdown/主题/各功能屏）
- **成员 B**：AI 集成 + 数据层（DeepSeek 客户端、Room/DataStore、备份）
- **成员 C**：测试与 CI/CD（三阶段单测、Espresso、JaCoCo、GitHub Actions/Jenkins）
