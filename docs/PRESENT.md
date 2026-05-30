# ZhujiNote 课堂汇报实操脚本（AS 投影大屏）

> 面向 **MT2026 移动应用测试** 大作业现场汇报。按本脚本顺序操作，全程约 12-15 分钟。
> 建议投影分辨率 1920×1080，Android Studio 字号调大（`Ctrl +` 或 Settings → Editor → Font → Size 18+）。

---

## 0. 开场前准备（汇报前 5 分钟，别在台上做）

1. 插好笔记本、连好投影，确认扩展屏显示正常。
2. 打开 Android Studio，`File → Open` 选 `D:\Projects\zhuji_note`，等 Gradle Sync 完成（右下角进度条消失）。
3. 启动模拟器：`Device Manager`（右侧竖排手机图标）→ `zhuji_avd` → ▶。等开机进桌面。
   - 也可命令行：`.\scripts\start_avd.ps1`
4. 先装一次 APK 保证最新：`.\gradlew.bat :app:assembleDebug` 然后把 `app-debug.apk` 拖进模拟器窗口。
5. 把 GitHub 仓库页 `https://github.com/bpgigi/zhuji_note` 在浏览器开好一个标签页，Actions 页开好另一个标签页。

---

## 1. 项目定位（讲，1 分钟）

> "我们做的是 **ZhujiNote 助记笔记**，一个 production-grade 的 Android Compose 应用，接入 DeepSeek AI agent，并用它完整覆盖课程 L02-L09 的全部测试能力。"

投影打开 `README.md` 顶部的能力对照表（L02-L09 那张），逐行一句话带过。

---

## 2. 代码结构（AS 里看，2 分钟）

在 AS 左侧 `Project` 面板切到 **Android** 视图，展开 `app/java/com.zhuji.note`：

1. 指 `ai/` —— DeepSeek 客户端 + 9 类 AI 动作
2. 指 `data/` —— Room（含 FTS4 全文索引）+ DataStore + 加密 Key 存储
3. 指 `domain/` —— Clean Architecture 的 model / repository / usecase / util
4. 指 `ui/screens/` —— notes / edit / settings / stats / trash / ai / pomodoro / goal / template
5. 打开 `MainActivity.kt`，指 `@AndroidEntryPoint` + `setContent { ZhujiApp() }`

> 一句话："三层 Clean Architecture + Hilt 依赖注入，AI 走 OkHttp SSE 流式。"

---

## 3. 现场跑单元测试 + 三阶段覆盖率（核心，3 分钟）

### 3.1 全部单测
AS 里右键 `app/src/test/java/com/zhuji/note` → `Run 'Tests in 'com.zhuji.note''`。
等 Run 面板出现绿色 **✓ 188 tests passed**。

> "188 个单元测试，100% 通过。按 JUnit → MockK → Robolectric 三阶段组织。"

### 3.2 三阶段 JaCoCo 覆盖率
Terminal（AS 底部 `Terminal` 标签）执行：
```powershell
.\gradlew.bat :app:jacocoCumulativeReport
```
跑完用浏览器打开 `app\build\reports\coverage\cumulative\html\index.html`。

> "累积行覆盖率 65%，方法 58%。UI 层按业界标准交给 Espresso，JaCoCo 聚焦 data/domain/viewmodel/ai 业务层，不刷假数据。"

讲三阶段递进：
- Stage1 纯 JUnit：domain 纯逻辑（格式化、解析、统计）
- Stage2 +MockK：Repository / ViewModel / DeepSeek client（mock 掉 Room/网络）
- Stage3 +Robolectric：真 Room SQL + DataStore + 主题解析

---

## 4. 模拟器实操：核心功能（核心，4 分钟）

把投影切到**模拟器窗口**（或用 `View → Tool Windows → Running Devices` 把模拟器嵌进 AS 投影）。

### 4.1 笔记 CRUD + Markdown
1. 点 `+ 新笔记`，标题输入 `协程学习`，正文输入几行。
2. 点工具栏 `H1`、`加粗`、`列表`，演示 Markdown 标记插入。
3. 点顶部 👁 预览图标，正文渲染成排版后的 Markdown。

### 4.2 AI 助手（最大亮点，重点演示）
1. 在编辑器右下点 ✨ FAB，弹出 AI 抽屉。
2. 点 **总结** —— 现场看 "深度思考中…" 流式输出，几秒后思考自动收起、给出 Markdown 要点。
3. 点 **应用到笔记** —— 答案合并进正文。
4. 横滑功能条，点 **起一个标题** → **应用到笔记** —— 标题被 AI 中文标题替换（不是追加，这是我们修的语义 bug）。
5. 点 **翻译** —— 正文被译文替换。
6. 演示 **问答模式**：在输入框打一个问题，点"基于此笔记问答"，AI 基于笔记内容作答。

> "AI 用的是 DeepSeek 最新 flash 思考模型，reasoning 和 content 分轨流式。我们修过一个 gzip 把 SSE 缓冲卡死的坑，和按动作语义决定结果是替换标题、替换正文还是追加。"

### 4.3 设置页验证 AI 真连通
回首页 → 设置 → 指 API Key 已存（密文）→ 点 **查询余额** → 显示真实 CNY 余额。

> "余额是 DeepSeek 官方 `/user/balance` 接口实时返回的，证明真连通不是 mock。"

### 4.4 其他功能快速过
顶栏 `⋮ 更多` 菜单依次点开：番茄钟（25/5 计时器）、写作目标（进度环+连续天数）、从模板新建（8 种模板）、批量管理（长按多选）。
设置页底部 **数据备份** → 导出 ZIP（弹系统文件保存器）。
编辑器顶部 **分享为图片**（弹系统分享面板）。

---

## 5. CI/CD（讲 + 浏览器看，2 分钟）

切到浏览器 GitHub **Actions** 标签页：

1. 指最新一次 workflow run 的绿色 ✓。
2. 展开 `build-test` job：assembleDebug → 188 单测 → JaCoCo → 上传 APK/报告 artifact。
3. 展开 `instrumented-tests` job：在 API 34 模拟器上跑 Espresso 集成测试。

> "GitHub Actions 每次 push 自动构建 + 单测 + 覆盖率 + 真机模拟器集成测试。另外 `Jenkinsfile` 是等价的 Jenkins 声明式 Pipeline。"

打开 `Jenkinsfile` 扫一眼五个 stage。

---

## 6. 收尾（讲，1 分钟）

> "总结：一个功能完整、测试覆盖扎实、CI/CD 双轨、接入真实 AI 的助记笔记 App，完整覆盖了 L02-L09 的全部测试能力。所有功能都在模拟器上实测验证，截图归档在 `docs/screens`。"

---

## 附：常见翻车点与急救

| 现象 | 急救 |
|------|------|
| 模拟器没开机 | 提前 5 分钟开；备一份 `docs/screens/` 截图兜底 |
| AI 转圈不出 | 检查设置页 Key 是否在、余额是否够；网络不稳就讲已截图的结果 |
| Gradle Sync 卡住 | Terminal 跑 `.\gradlew.bat :app:assembleDebug --offline` |
| 单测面板太小 | Run 面板右上角可放大；或直接看 Terminal 输出 |
| 投影字太小 | AS：`Ctrl 滚轮` 放大编辑器；模拟器：拖大窗口 |
| 找不到某功能入口 | 首页顶栏 `⋮ 更多` 里有番茄钟/写作目标/模板/批量管理 |
