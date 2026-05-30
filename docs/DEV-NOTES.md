# 开发笔记（DEV-NOTES）

> 跨会话持久化的关键工程经验与踩坑记录。新会话务必先读本文件。

## 环境与路径
- 开发构建目录：`D:\Projects\zhuji_note`（ASCII 路径，避免 Robolectric ClassNotFoundException）
- Git 仓库副本：`C:\Users\l\Desktop\hmwk\移动应用测试\zhuji_note`（推 GitHub 用）
- 两处同步：`robocopy D:\...\app\src\main <repo>\app\src\main /MIR`（test/androidTest 同理）
- JDK：`D:\Softwares\AndroidStudio\jbr`（JDK 21）；SDK：`%LOCALAPPDATA%\Android\Sdk`
- Gradle user home：`D:\Android\.gradle`
- AVD：`zhuji_avd`（Pixel, API 34, x86_64）+ AEHD hypervisor
- GitHub：`github.com/bpgigi/zhuji_note`，PAT 在 Windows 凭证管理器（`git credential fill` 取，设 `$env:GH_TOKEN` 给 gh 用）

## DeepSeek API（官方文档核对过）
- 模型 `deepseek-v4-flash`（思考模型，默认 thinking enabled）/ `deepseek-v4-pro`
- 上下文 1M，最大输出 384K
- 端点 `/chat/completions`（不带 /v1）
- thinking 模式下 `temperature`/`top_p`/`penalty` 被忽略（设了不报错但无效），已删
- `thinking` 参数：`{"type":"enabled"}` —— **type 字段不能有 Kotlin 默认值**，否则 kotlinx.serialization(encodeDefaults=false) 会序列化成 `{}` → 400 missing field type
- `reasoning_effort`：low/high；reasoning 走 `reasoning_content`，答案走 `content`（分轨）
- balance 接口 `/user/balance` 返回的是该 key 配额，非账户总额

## 重大踩坑（已修，勿重蹈）
1. **gzip 把 SSE 流式缓冲卡死**：OkHttp 默认发 `Accept-Encoding: gzip`，DeepSeek gzip 压缩 SSE 后 `readUtf8Line()` 要等整个 gzip block 才出一行 → 流式只输出几个字就卡死。修复：请求头强制 `Accept-Encoding: identity`。（curl 默认不发 gzip 所以测着正常，是差异来源）
2. **CI gradlew 权限**：Windows 提交的 gradlew 缺 Linux +x 位 → CI instrumented job `Permission denied` exit 126。修复：`git update-index --chmod=+x gradlew`（100644→100755）+ workflow script 加 `chmod +x gradlew &&` 兜底。
3. **AI 应用结果语义**：`applyAiToContent` 不能一律追加。起标题→替换标题、润色/翻译→替换正文、其余→追加。已抽成纯函数 `ai/AiApplyStrategy.kt` + stage1 测试锁定。
4. **Espresso 断言与真实 UI 脱节**：instrumented-tests 之前被 workflow_dispatch skip 从未真跑，断言文案是凭空写的。真跑后 22 失败。修法：断言匹配真实文案（首页标题"助记 · ZhujiNote"用 substring；编辑器内 AI FAB cd 是"打开 AI 助手"不是"AI 助手"；主题药丸是"暗"不是"暗色模式"）。
5. **a11y 缺陷**：首页"新笔记"ExtendedFAB、Stats/AiChat 返回按钮原本缺 contentDescription，Espresso 找不到节点 + 真实无障碍问题。已补。

## AVD 自动化打法（脚本 scripts/avd_driver.ps1）
- 必须切 3 键导航：`adb shell cmd overlay enable-exclusive com.android.internal.systemui.navbar.threebutton`，否则点底部 FAB 会触发手势导航跳去 Google Lens
- 点底部 UI 前先收键盘（`Hide-Keyboard`），否则 sheet 被键盘顶高、坐标全变
- 按文字 dump 真实坐标再点（`Find-Node`/`Tap-Text`），绝不写死坐标（sheet 半展开/全展开坐标会变）
- 点完截图 + `look_at` 核验，不靠 dump 文本猜
- **禁止长轮询**：不要 `while((Get-Date)<deadline)` 一次跑满 60-120s（对用户像卡死）；用多次短命令查状态
- ModalBottomSheet 用 `skipPartiallyExpanded=true` + 头部固定/中间 weight 可滚/底部按钮 pinned，保证按钮可达

## 构建/测试命令
- 编译：`gradlew :app:assembleDebug`
- 单测：`gradlew :app:testDebugUnitTest`（188 个）
- 覆盖率：`gradlew :app:jacocoCumulativeReport` → `app/build/reports/coverage/cumulative/html/index.html`
- 集成测试：`gradlew :app:connectedDebugAndroidTest`（37 个，需 AVD）
- 装：`adb -s emulator-5554 install -r app\build\outputs\apk\debug\app-debug.apk`

## Git 规范
- commit message 简约一句话，不带版本号/M前缀/feat:前缀
- autocrlf=true，robocopy 后会有纯行尾噪音文件（git diff --numstat 为空），提交真实改动后 `git checkout -- .` 还原噪音
- 推送：`git pull --rebase origin main` 后 `git push`，禁止 force push
