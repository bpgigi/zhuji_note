# ZhujiNote 操作与汇报教程

> 一份文档搞定：在 Android Studio 里打开项目、装到模拟器、跑测试、看覆盖率，以及课堂投影大屏的汇报流程。

---

## 一、环境与打开项目

- Windows 11，CPU 虚拟化已开（BIOS 的 VT-x / SVM）
- **构建目录用 `D:\Projects\zhuji_note`**（中文路径下 Gradle/Robolectric 会 ClassNotFoundException）
- git 仓库副本 `C:\Users\l\Desktop\hmwk\移动应用测试\zhuji_note` 仅供看代码/推送

打开步骤：
1. 启动 `D:\Softwares\AndroidStudio\bin\studio64.exe`
2. `File → Open` 选 `D:\Projects\zhuji_note`，等右下角 Gradle Sync + Indexing 跑完（首次 1-3 分钟）
3. Sync 失败就点大象图标 `Sync Project with Gradle Files`
4. `File → Project Structure`：SDK 指 `C:\Users\l\AppData\Local\Android\Sdk`，JDK 选 Embedded JDK（AS 自带 JBR 21）

---

## 二、启动模拟器 + 装 App

1. 顶栏手机图标 `Device Manager` → 列表里 `zhuji_avd`(Pixel, API 34) 点 ▶ 开机（首次 2-5 分钟）
   - 若报 hardware acceleration，管理员运行 `…\Sdk\extras\google\Android_Emulator_Hypervisor_Driver\silent_install.bat`
2. 最简单：顶部选 `app` + `zhuji_avd`，点绿色 ▶ Run（自动编译+装+启动）
3. 或命令行：
```powershell
.\gradlew.bat :app:assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n com.zhuji.note/.MainActivity
```

---

## 三、跑测试与覆盖率

AS 内：右键 `app/src/test/java/com/zhuji/note` → `Run Tests` → 看 188 全绿。
命令行：
```powershell
.\gradlew.bat :app:testDebugUnitTest                 # 188 单元测试
.\gradlew.bat :app:jacocoCumulativeReport            # 三阶段累积覆盖率
.\gradlew.bat :app:connectedDebugAndroidTest         # 37 集成测试(需 AVD)
```
覆盖率报告：浏览器开 `app\build\reports\coverage\cumulative\html\index.html`。

---

## 四、课堂汇报流程（投影大屏，12-15 分钟）

### 汇报前 5 分钟准备（别在台上做）
- 连好投影确认扩展屏正常；AS 字号调大（Settings → Editor → Font → Size 18+）
- 启动 `zhuji_avd`，装好最新 APK
- 浏览器开两个标签：仓库主页 + Actions 页

### 1. 项目定位（1 分钟）
打开 `README.md` 顶部 L02-L09 能力对照表，一句话："助记笔记 App，接入 DeepSeek AI，完整覆盖课程全部测试能力。"

### 2. 代码结构（2 分钟）
AS 左侧切 Android 视图，展开 `com.zhuji.note`：
- `ai/` DeepSeek 客户端 + 9 类 AI 动作
- `data/` Room(FTS4) + DataStore + 加密 Key
- `domain/` Clean Architecture model/usecase/util
- `ui/screens/` 各功能屏
- 打开 `MainActivity.kt` 指 `@AndroidEntryPoint`

### 3. 现场跑测试 + 覆盖率（3 分钟）
- 右键 test 目录 Run → 绿色 **188 tests passed**
- Terminal 跑 `jacocoCumulativeReport`，浏览器开 HTML → 讲三阶段递进（JUnit→MockK→Robolectric），LINE 65%

### 4. 模拟器演示（4 分钟，最大亮点）
投影切模拟器窗口：
1. `+ 新笔记`，写标题正文，点 H1/加粗/列表演示 Markdown，点 👁 预览看渲染
2. 编辑器右下 ✨ FAB → AI 抽屉 → 点 **总结**：看"深度思考中"流式 → 思考自动收起 → Markdown 要点 → **应用到笔记**
3. 横滑功能条点 **起一个标题** → 应用 → 标题被 AI 中文标题**替换**（非追加）
4. 点 **翻译** → 正文被译文替换
5. **问答模式**：输入框打问题 → 基于笔记作答
6. 回设置 → **查询余额** → 显示真实 CNY（证明真连通非 mock）
7. 顶栏其他图标 + `⋮ 更多`：番茄钟、写作目标、模板新建、批量管理、分享为图片、数据备份导出 ZIP

### 5. CI/CD（2 分钟）
浏览器 Actions 页：指最新绿 ✓ → 展开 `build-test`(assembleDebug+188单测+JaCoCo+APK) 和 `instrumented-tests`(模拟器跑 37 Espresso)。打开 `Jenkinsfile` 扫一眼 5 个 stage。

### 6. 收尾（1 分钟）
"功能完整、测试扎实、CI/CD 双轨、接入真实 AI，完整覆盖 L02-L09，全部功能模拟器实测，截图归档 docs/screens。"

---

## 五、常见翻车急救

| 现象 | 解法 |
|------|------|
| 模拟器没开机 | 提前 5 分钟开；备 docs/screens 截图兜底 |
| AI 转圈不出 | 检查设置页 Key + 余额；网络不稳就讲已截图结果 |
| Gradle sync 卡住 | 关 AS，清 `D:\Android\.gradle\caches`，重开 |
| 启动报 Room integrity | `adb uninstall com.zhuji.note` 再装 |
| AVD 报 hardware accel | 装 AEHD（见第二节） |
| 投影字太小 | AS Ctrl+滚轮放大；模拟器拖大窗口 |
| 找不到某功能 | 首页顶栏 `⋮ 更多` 里有番茄钟/写作目标/模板/批量管理 |

---

## 六、签名打包 / 推送（备用）

签名 APK：`Build → Generate Signed Bundle / APK → APK → 新建 keystore → release → Finish`，输出 `app/release/app-release.apk`。

推送：
```powershell
git add -A
git commit -m "描述改动"
git push
```
