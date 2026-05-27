# 助记 ZhujiNote

> 一款融合 Anthropic Claude 暖色与 Windsurf 霓虹质感的 Android Compose 笔记 App，原生 Material 3，DeepSeek Agent 驱动。

[![Android CI](https://github.com/bpgigi/zhuji_note/actions/workflows/android-ci.yml/badge.svg)](https://github.com/bpgigi/zhuji_note/actions/workflows/android-ci.yml)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-7F52FF?logo=kotlin&logoColor=white)
![AGP](https://img.shields.io/badge/AGP-8.5.2-3DDC84?logo=android&logoColor=white)
![Compose](https://img.shields.io/badge/Compose-BOM%202024.08.00-4285F4?logo=jetpackcompose&logoColor=white)
![minSdk](https://img.shields.io/badge/minSdk-24-lightgrey)
![targetSdk](https://img.shields.io/badge/targetSdk-34-lightgrey)
![Tests](https://img.shields.io/badge/tests-104%2F104%20pass-success)
![Coverage](https://img.shields.io/badge/coverage-LINE%2030%25%20%7C%20METHOD%2035%25-yellow)

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
- Markdown 编辑 / 预览（compose-markdown）
- 标签多对多（`note_tag_cross`）+ 文件夹分组
- 收藏 / 置顶 / 软删除回收站 / 30 天自动清理
- 全文搜索（标题 + 正文 LIKE）+ 多种排序（更新时间 / 创建时间 / 标题 / 字数）
- 统计页（笔记数、字数、标签数、置顶 / 收藏数）
- 提醒（AlarmManager + 通知通道）

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
| Stage 1 (JUnit)        | 8.4%  |  3.1% | 12.4% | 14.6% |  9.7% |
| Stage 2 (+MockK)       | 12.0% |  6.5% | 16.6% | 21.7% | 17.7% |
| Stage 3 (+Robolectric) | 5.7%  |  0.0% |  6.9% |  9.5% |  3.4% |
| **累积**                | **21.6%** | **9.2%** | **30.0%** | **34.8%** | **26.6%** |

测试用例总数：**104**（Stage1 56 + Stage2 30 + Stage3 18），通过率 **100%**。

## 七、CI/CD

### GitHub Actions

`.github/workflows/android-ci.yml` 在 `push` 与 `pull_request` 时跑：
1. checkout + JDK 17 + Android SDK + Gradle cache
2. `:app:assembleDebug`
3. `:app:testDebugUnitTest :app:jacocoCumulativeReport`
4. 上传 APK / JaCoCo 报告 / Junit 报告 三个 artifact

`workflow_dispatch` 触发 `instrumented-tests` job，借 `ReactiveCircus/android-emulator-runner@v2` 跑 Espresso。

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
- **M4**（计划）Espresso 在 AVD 上跑通 + Jenkins 本地构建一次

## 九、致谢

- Anthropic 公开的品牌色板（Claude coral / cream）
- Windsurf 官网霓虹设计灵感
- Google Android Compose Samples、Philipp Lackner CleanArchitectureNoteApp、`android/cahier`
- DeepSeek 官方 API
