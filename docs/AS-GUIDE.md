# Android Studio 全套操作手册（助记 ZhujiNote）

> 给"不太懂安卓开发"的同学：照这份从 0 跑通项目、装到模拟器、跑测试、看覆盖率、签名打包。

## 0. 先决条件

- Windows 11 + 已虚拟化（CPU 设置开 VT-x / SVM）
- 项目实际构建目录：`D:\Projects\zhuji_note`（**注意** Gradle 在中文路径下会 `ClassNotFoundException`，请勿在 `C:\Users\l\Desktop\hmwk\移动应用测试` 下直接构建）
- 作业目录 `C:\Users\l\Desktop\hmwk\移动应用测试\zhuji_note` 是 git 镜像，老师可以从这里看代码；如果要构建/调试，**用 D 盘那份**

## 1. 打开项目

1. 双击 `D:\Softwares\AndroidStudio\bin\studio64.exe` 启动 Android Studio
2. 顶栏 `File → Open…`，选 `D:\Projects\zhuji_note`，点 OK
3. 等右下角进度条 "Indexing"、"Gradle sync" 跑完（首次约 1-3 分钟）
4. 如果 sync 失败，菜单 `File → Sync Project with Gradle Files`（圆形大象图标）

## 2. 选 Build Variant 和 SDK

- 左下角 `Build Variants` → app 列保持 `debug`（默认）
- 顶栏 `File → Project Structure…`：
  - `SDK Location` → Android SDK location 填 `C:\Users\l\AppData\Local\Android\Sdk`（项目用 junction 链接到 D:\Android\sdk）
  - JDK location 选 `Embedded JDK` (项目用的就是 AS 自带的 JBR 21)

## 3. 启动模拟器

1. 顶栏小手机图标 `Device Manager`（或 `Tools → Device Manager`）
2. 列表里能看到 `zhuji_avd` (Pixel 7, API 34) — 如果没有，点 `+` → Phone → Pixel 7 → API 34 google_apis x86_64
3. 点 `zhuji_avd` 行右侧的 ▶ 三角，等模拟器开机（首次较慢，2-5 分钟）

> 如果启动报错 "x86_64 emulation requires hardware acceleration"，需要先装 AEHD：以管理员身份运行 `C:\Users\l\AppData\Local\Android\Sdk\extras\google\Android_Emulator_Hypervisor_Driver\silent_install.bat`。装完不用重启。

## 4. 编译 + 装到模拟器

最简单：顶部工具栏选好 `app` + `zhuji_avd`，点绿色 ▶ Run。Android Studio 会自动 `assembleDebug` + `adb install` + 启动 Activity。

或者命令行（项目根目录）：

```powershell
.\gradlew.bat :app:assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n com.zhuji.note/.MainActivity
```

## 5. 用法（验证 AI 功能）

1. App 主页 → 右上角 ⚙️ 进设置
2. "API Key"输入框填 DeepSeek Key（项目作业用 `sk-1ff5842c2fee4ac195995955c5c2cc34`）
3. 点 **保存并验证** → 顶栏会出现进度条 → 几秒后底部 Snackbar 提示 `Key 验证成功：CNY xx.xx`
4. 也可单点 **查询余额** 单测网络
5. 返回主页 → 右下角 + 新笔记 → 标题 + 正文随便写
6. 编辑器右下角 ✨ 浮按 → 弹出"助记 AI · DeepSeek"抽屉
7. 顶部 chip 任选：总结 / 续写 / 润色 / 翻译 / 提取关键词 / 起一个标题 / 生成大纲 / 生成闪卡 / 基于此笔记问答
8. 抽屉先有"思考中…"气泡（DeepSeek `reasoning_content`），再流式出最终答案
9. 答案下方点 "应用到笔记" 自动并入正文

## 6. 跑单元测试 + 覆盖率

在 AS 内：

- Project 视图右键 `app/src/test/java/com/zhuji/note` → `Run 'Tests in com.zhuji.note'` → 全部绿勾（104/104）
- 想看 JaCoCo 累计报告：在 Gradle 工具窗口 `app → Tasks → reporting → jacocoCumulativeReport` 双击
- 报告在 `app/build/reports/coverage/cumulative/html/index.html`，浏览器打开

命令行：

```powershell
.\gradlew.bat :app:testDebugUnitTest                          # 全部 104 个用例
.\gradlew.bat :app:jacocoStage1Report :app:jacocoStage2Report `
              :app:jacocoStage3Report :app:jacocoCumulativeReport
```

## 7. 集成测试（在 AVD 上跑 Espresso）

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

报告在 `app/build/reports/androidTests/connected/index.html`。

## 8. 看应用日志

- 底栏 `Logcat` 工具窗口，过滤器输入 `package:mine` 只看本应用日志
- 想看 OkHttp 请求：过滤 `tag:okhttp.OkHttpClient`

## 9. 截图与录屏

- 模拟器右侧悬浮工具栏 → 相机图标截图，保存到桌面
- 命令行：`adb shell screencap -p /sdcard/x.png && adb pull /sdcard/x.png .`
- 录屏：`adb shell screenrecord /sdcard/r.mp4`（Ctrl-C 停止）→ `adb pull /sdcard/r.mp4 .`

## 10. 签名 APK / 发布

- 顶栏 `Build → Generate Signed Bundle / APK…`
- 选 APK
- "Create new"建 keystore：随便填路径（比如 `D:\Projects\zhuji_note\release.keystore`）+ 密码
- 选 release variant → Finish；输出在 `app/release/app-release.apk`

## 11. 推送到 GitHub

仓库：<https://github.com/bpgigi/zhuji_note>

```powershell
git add -A
git commit -m "feat: 描述你做了啥"
git push
```

push 时若让输用户名 / 密码，浏览器会跳出"Sign in with browser"，登 `Mag1cFall` 账号一次性授权即可。

## 12. 常见坑

| 现象 | 解法 |
|------|------|
| 启动报 `Room cannot verify integrity` | 卸载 App 再装：`adb uninstall com.zhuji.note` |
| Gradle sync 卡住 | 关 AS → `D:\Android\.gradle\caches` 清空 → 重开 |
| 编辑器中文 input 报 `null array` | 是 `adb shell input text` 不支持中文，与本 App 无关；用键盘输入即可 |
| 余额查询失败 NetworkOnMainThreadException | 已修（`DeepSeekClient.balance/listModels/chat` 全部 `withContext(Dispatchers.IO)`） |
| AVD 启动报 hardware accel | 装 AEHD（见 §3） |
| Maven Central 403 | `settings.gradle.kts` 已加阿里云镜像，不用动 |
| Robolectric 拉 android-all jar 403 | `app/build.gradle.kts` 已设 `robolectric.dependency.repo.url`= 阿里云 |

## 13. 项目结构速览

- `app/src/main/java/com/zhuji/note/`
  - `ai/` DeepSeek 流式客户端 + 9 类 prompt 模板
  - `data/` Room + DataStore + EncryptedSharedPreferences + 备份导出
  - `domain/` Note / Tag / Folder + UseCase + Markdown 工具
  - `di/AppModule.kt` Hilt 提供所有单例
  - `reminder/` AlarmManager + 通知通道
  - `ui/`
    - `theme/` Claude+Windsurf ColorScheme + Typography + Shape
    - `common/` 加载条/玻璃面板/AuroraBackground/TypingDot
    - `screens/{notes,edit,settings,stats,trash,ai}` 6 个屏
- `app/src/test/java/...` JUnit / MockK / Robolectric 三阶段
- `app/src/androidTest/java/...` Espresso / Compose UI Test / Hilt Test Runner
- `Jenkinsfile` + `.github/workflows/android-ci.yml` 双 CI

祝你顺利！
