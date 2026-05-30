# 助记 ZhujiNote

> 一款融合 Anthropic Claude 暖色与 Windsurf 霓虹质感的 Android Compose 笔记 App，原生 Material 3，DeepSeek Agent 驱动。

[![Android CI](https://github.com/bpgigi/zhuji_note/actions/workflows/android-ci.yml/badge.svg)](https://github.com/bpgigi/zhuji_note/actions/workflows/android-ci.yml)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-7F52FF?logo=kotlin&logoColor=white)
![AGP](https://img.shields.io/badge/AGP-8.5.2-3DDC84?logo=android&logoColor=white)
![Compose](https://img.shields.io/badge/Compose-BOM%202024.08.00-4285F4?logo=jetpackcompose&logoColor=white)
![minSdk](https://img.shields.io/badge/minSdk-24-lightgrey)
![targetSdk](https://img.shields.io/badge/targetSdk-34-lightgrey)
![Tests](https://img.shields.io/badge/tests-188%2F188%20pass-success)
![Coverage](https://img.shields.io/badge/coverage-LINE%2065.1%25%20%7C%20METHOD%2057.9%25-brightgreen)

## 一、项目定位

ZhujiNote 是为 **MT2026 移动应用测试** 课程大作业设计的 Android 应用，配套实现 L02-L09 全部教学内容：

| 课次 | 主题 | 本仓库交付 |
|------|------|------------|
| L02  | 开发环境 | JDK 21 + AS Panda4 + Android SDK 34 + AVD `zhuji_avd`（D:\Android\sdk junction） |
| L03  | APK 形态 | `assembleDebug` 输出 `app-debug.apk`，本地 `adb install` 验证 |
| L04  | GUI 入门 | `scripts/start_avd.ps1` + `scripts/screenshot.ps1` 自动化截图归档 |
| L05  | 单元测试 JUnit | `src/test/.../stage1/*.kt`（纯 JUnit + Truth） |
| L06  | Mock 单测 | `src/test/.../stage2/*.kt`（MockK + MockWebServer + Turbine） |
| L07  | 测试覆盖 | `src/test/.../stage3/*.kt`（Robolectric） + JaCoCo 三阶段 + 累积报告 |
| L08  | 集成测试 | `src/androidTest/...`（Espresso + Compose UI Test + Hilt 测试 Runner） |
| L09  | 持续集成 | `Jenkinsfile`（Declarative Pipeline）+ `.github/workflows/android-ci.yml` |

## 二、核心功能

### 笔记本身
- 笔记 CRUD（Room + Hilt + Flow，状态实时刷新）
- Markdown 编辑 / 预览（compose-markdown）+ 10 项工具栏（H1/H2/Bold/Italic/Code/Strike/BulletList/NumberedList/Checklist/Quote/Link/CodeBlock）
- 标签多对多（`note_tag_cross`）+ 文件夹分组
- 收藏 / 置顶 / 软删除回收站（SwipeToDismiss 还原/永久删除）/ 30 天自动清理
- 全文搜索（FTS4 虚拟表 + LIKE 降级）+ 多种排序（更新时间 / 创建时间 / 标题 / 字数）
- 统计页（笔记数、字数、标签数、置顶/收藏数 + Canvas 字数趋势折线图）
- 提醒（AlarmManager + 通知通道）
- 番茄钟专注写作模式（经典 25/5、短冲 15/3、深度 50/10）
- 8 种笔记模板（空白/日记/会议/读书/待办/灵感/周报/康奈尔）+ 模板引擎（{{date}} 等变量）
- 双向链接 [[note title]] 解析 + 跳转
- 字数目标 + 写作连续天数追踪
- 多选批量操作（长按进入选择模式 → 全选/批量标签/批量删除）
- PressableScale 触感反馈动效
- 导入/导出（SAF + BackupExporter JSON/Markdown/Zip）

### AI 助手（DeepSeek `deepseek-v4-flash`）
| 入口 | 行为 |
|------|------|
| 编辑器右下 ✨ FAB | 弹出 AI 抽屉，9 类一键操作（总结 / 续写 / 润色 / 翻译 / 关键词 / 自动标题 / 大纲 / 闪卡 / 笔记问答） |
| 顶部 ✨ 图标 | 进入纯对话页 `AiChatScreen`，多轮上下文 + 流式打字 |
| 设置页 | 录入 API Key / 选择模型 / 显示账户余额 |
- 流式 SSE 解析（`reasoning_content` + `content` 分轨）
- 协程取消 + 错误处理（401/429/超时）

### 主题与设计语言
- **亮色** Claude 暖橙主调（`#CC785C` coral + `#FAF9F5` cream）
- **暗色** Windsurf 霓虹青（`#34E8BB` neon + `#0B100F` charcoal） + Claude 暖橙做 tertiary
- Material 3 ColorScheme 全 13 槽位 + 13 级 Typography（Serif Display / Sans Body）
- 自定义 ShapeTokens（`NoteCard 20dp / FAB 18dp / Sheet top 28dp / Chip pill`）
- Aurora 多 blob 渐变背景、TypingDot、GradientText 等装饰组件

## 三、目录结构

```
zhuji_note/
├── app/
│   ├── build.gradle.kts            # AGP 8.5.2 + JaCoCo 三阶段 afterEvaluate
│   └── src/
│       ├── main/java/com/zhuji/note/
│       │   ├── ZhujiNoteApp.kt           # @HiltAndroidApp
│       │   ├── MainActivity.kt           # @AndroidEntryPoint + setContent
│       │   ├── ai/                       # DeepSeek client + AiAction
│       │   ├── data/local/{db,preferences} # Room DAO/Entity + DataStore
│       │   ├── data/repository/          # NoteRepositoryImpl 等
│       │   ├── di/AppModule.kt           # Hilt SingletonComponent
│       │   ├── domain/{model,repository,usecase,util}
│       │   ├── reminder/                 # ReminderReceiver + 通知
│       │   └── ui/{theme,common,screens/{notes,edit,settings,stats,trash,ai}}
│       ├── main/res/                     # Manifest / themes / strings / launcher
│       ├── test/java/com/zhuji/note/
│       │   ├── stage1/                   # 纯 JUnit (56 用例)
│       │   ├── stage2/                   # +MockK +MockWebServer (30 用例)
│       │   └── stage3/                   # +Robolectric (18 用例)
│       └── androidTest/java/com/zhuji/note/  # Espresso + Compose UI Test
├── docs/
│   ├── screens/                          # 实时截图（亮/暗/列表/编辑/AI）
│   └── reports/                          # JaCoCo HTML / 测试报告归档
├── scripts/
│   ├── start_avd.ps1                     # 启 AEHD 加速 AVD
│   └── screenshot.ps1                    # adb screencap → docs/screens/
├── .github/workflows/android-ci.yml       # GitHub Actions
├── Jenkinsfile                            # Jenkins Pipeline
├── gradle/libs.versions.toml              # 集中版本管理
├── settings.gradle.kts                    # 阿里云镜像 + Google + JitPack
└── README.md
```

## 四、技术栈

| 层 | 选型 |
|----|------|
| Build | Gradle 8.9 + AGP 8.5.2 + Kotlin 1.9.24 + KSP 1.9.24-1.0.20 |
| UI | Jetpack Compose BOM 2024.08.00 + Material 3 + Navigation Compose 2.7.7 |
| DI | Hilt 2.51.1（`@HiltAndroidApp` + `@HiltViewModel` + `@Module SingletonComponent`） |
| 持久化 | Room 2.6.1 + DataStore Preferences 1.1.1 + Security-Crypto 1.1.0-alpha06 |
| 网络 | OkHttp 4.12.0（含 SSE / 日志拦截）+ kotlinx-serialization-json 1.6.3 |
| 协程 | kotlinx-coroutines-android 1.8.1 |
| Markdown | dev.jeziellago:compose-markdown 0.5.4 (JitPack) |
| 测试 | JUnit 4.13.2 + Truth 1.4.4 + MockK 1.13.12 + Robolectric 4.13 + Turbine 1.1.0 + MockWebServer 4.12.0 |
| 集测 | Espresso 3.6.1 + Compose UI Test + Hilt Testing |
| 覆盖率 | JaCoCo 0.8.12（三阶段 .exec + 累积报告） |
| CI | GitHub Actions + Jenkins Declarative Pipeline |

## 五、本地构建

### 1. 环境
- JDK 21（项目用 AS 自带 JBR：`D:\Softwares\AndroidStudio\jbr`）
- Android SDK 34（路径：`%LOCALAPPDATA%\Android\Sdk`，已在 `D:\Android\sdk` junction 暴露）
- AVD：`zhuji_avd` (Pixel 7, API 34, x86_64) + AEHD hypervisor

### 2. 一键命令

```powershell
# 编 APK
.\gradlew.bat :app:assembleDebug

# 跑全部单元测试（约 30s）
.\gradlew.bat :app:testDebugUnitTest

# 三阶段 + 累积 JaCoCo 报告
.\gradlew.bat :app:jacocoStage1Report :app:jacocoStage2Report `
              :app:jacocoStage3Report :app:jacocoCumulativeReport

# 集成测试（先 .\scripts\start_avd.ps1）
.\gradlew.bat :app:connectedDebugAndroidTest
```

### 3. 装到模拟器

```powershell
.\scripts\start_avd.ps1
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n com.zhuji.note/.MainActivity
.\scripts\screenshot.ps1   # 落到 docs/screens/<时间戳>.png
```

## 六、覆盖率（最近一次）

| 阶段 | INSTRUCTION | BRANCH | LINE | METHOD | CLASS |
|------|-------------|--------|------|--------|-------|
| **累积（三阶段合并）** | **62.2%** | **53.4%** | **65.1%** | **57.9%** | **49.4%** |

> JaCoCo 按业界标准排除 `ui/screens`、`ui/common`、`ui/theme` 等 Compose UI 包（由 androidTest 的 Espresso/Compose UI Test 覆盖），聚焦 data / domain / viewmodel / ai 业务层。

测试用例总数：**188**（Stage1 117 + Stage2 53 + Stage3 18），通过率 **100%**。

androidTest（Espresso + Compose UI Test）：**37** 用例（EditorE2E 10 + NavigationE2E 10 + NoteFlowE2E 10 + CommonCompose 3 + ThemeRenders 2 + MainActivityIntegration 2）。

## 七、CI/CD

### GitHub Actions

`.github/workflows/android-ci.yml` 在 `push` 与 `pull_request` 时跑：
1. checkout + JDK 21 + Android SDK + Gradle cache
2. `:app:assembleDebug`
3. `:app:testDebugUnitTest :app:jacocoCumulativeReport`
4. 上传 APK / JaCoCo 报告 / Junit 报告 三个 artifact

`instrumented-tests` job 在 `push` 时借 `ReactiveCircus/android-emulator-runner@v2` 在 API 34 模拟器上跑 Espresso 集成测试。

### Jenkins

`Jenkinsfile` 是声明式 Pipeline：

1. Checkout SCM
2. Setup（写 `local.properties`、打印 JDK）
3. Build APK
4. Three-Stage Tests + JaCoCo（同时归档 JUnit / HTML 报告 / cumulative 覆盖率）
5. Archive APK

启动 Jenkins（Windows）：
```powershell
java -Dhudson.plugins.git.GitSCM.ALLOW_LOCAL_CHECKOUT=true `
     -jar jenkins.war --httpPort=8080
```

## 八、开发记录与里程碑

- **M1** scaffold：Compose + Hilt + Room + DataStore + DeepSeek client + theme
- **M2** 三阶段单测 + JaCoCo 覆盖率出报告
- **M3** AVD 跑通 + 装 APK + 实时截图 + 更多测试与功能
- **M4** AI 真功能跑通（NetworkOnMainThreadException 修复）+ Markdown 工具栏 + EncryptedPrefs + FTS
- **M5** 测试 104/104 + AS 操作手册
- **M6** AI Chat 多轮对话验证 + 翻译流式 + 暗色截图
- **M7** Pomodoro + Templates + BiLink + MultiSelect + WritingGoal + Monkey/UIAutomator
- **M8** 178 tests + 37 androidTest + Jenkins pipeline + CI 转绿
- **M9** 按 DeepSeek 官方文档对齐思考参数 + 修复 gzip 流式卡死 + AI 面板交互重做
- **M10** 接通全部断路功能（番茄钟/模板/写作目标/分享图片/SAF 备份/多选/双向链）入导航
- **M11** AI 结果按动作语义应用（标题替换/正文替换/追加）+ 188 tests + CI gradlew 权限修复

## 九、致谢

- Anthropic 公开的品牌色板（Claude coral / cream）
- Windsurf 官网霓虹设计灵感
- Google Android Compose Samples、Philipp Lackner CleanArchitectureNoteApp、`android/cahier`
- DeepSeek 官方 API
