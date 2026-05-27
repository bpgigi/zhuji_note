# 助记 ZhujiNote — 大作业实施记录

> 配合 README.md 的"里程碑"章节，给老师看的过程证据。

## 时间线

| 时间 | 里程碑 | 主要内容 |
|------|--------|----------|
| 2026-05-27 | M1 | 项目脚手架、依赖图、Theme、AI 客户端、主流 UI 屏；首次 `assembleDebug` 通过；推送到 `bpgigi/zhuji_note` |
| 2026-05-27 | M2 | 完成三阶段单元测试 + JaCoCo 累积报告；初版 51 用例全部通过；后扩展到 104 用例 |
| 2026-05-27 | M3 | AEHD hypervisor 装好、`zhuji_avd` 启动；`adb install` + `am start`；亮/暗双截图；新增 Espresso/Compose UI Test、CI YAML、Jenkinsfile、README/Contributing/EditorConfig |
| 2026-05-27 | M4 | （进行中）AVD 上跑 `connectedDebugAndroidTest`；Jenkins 本地启动并跑完整 Pipeline |

## 关键决定

1. **配色** 亮色用 Anthropic Claude 的 coral/cream；暗色用 Windsurf 的 neon cyan；中性 ink/canvas 共享。
2. **AI** 接 DeepSeek `deepseek-v4-flash`，OkHttp 手写 SSE 流式解析；同时拆 `reasoning_content` 与 `content`，让"思考中..."气泡有内容。
3. **测试包结构** 严格按 `stage{1,2,3}` 分包，让 JaCoCo `filter { includeTestsMatching(...) }` 能把每阶段独立 `.exec` 拆出来。
4. **路径** Gradle 在中文路径下 testWorker 会 `ClassNotFoundException`；项目实际编译在 `D:\Projects\zhuji_note`，作业目录 `C:\Users\l\Desktop\hmwk\移动应用测试\zhuji_note` 是 git 镜像，便于老师本地查看。
5. **镜像源** `repo1.maven.org` 在内网 403；切到阿里云 `https://maven.aliyun.com/repository/{public,google,gradle-plugin}` + Robolectric `robolectric.dependency.repo.url` 同步切换。

## 截图清单（docs/screens）

- `01-home-light.png` — 亮色首页（Claude 暖橙）
- `02-home-dark.png`  — 暗色首页（Windsurf 霓虹青）
- `03-edit-empty.png` — 编辑器空白态
- `04-edit-typing.png` — 输入笔记内容
- `05-list-with-note.png` — 笔记列表带数据

后续会补：AI 抽屉打字动画、设置页、统计页、回收站、Espresso 录像。

## 报告产物清单

- `docs/reports/jacoco-cumulative/` — 累计 JaCoCo HTML 报告
- `docs/reports/junit/` — JUnit XML / HTML
- `docs/reports/lint/` — Android Lint
- `app/build/outputs/apk/debug/app-debug.apk` — 可安装产物
