# 测试文档（TESTING）

> 覆盖《移动应用测试》课程 L05-L09 全部能力：JUnit 单元测试、MockK、Robolectric、JaCoCo 三阶段覆盖率、Espresso 集成测试、CI。

## 1. 测试金字塔

```
        ╱╲          E2E / 集成（androidTest）
       ╱  ╲         30 用例：Espresso + Compose UI Test（真机/AVD）
      ╱────╲
     ╱      ╲       Robolectric（Stage3）
    ╱        ╲      18 用例：Room/Dao/DataStore/Theme（JVM 上跑 Android API）
   ╱──────────╲
  ╱            ╲    单元测试（Stage1 + Stage2）
 ╱              ╲   160 用例：纯逻辑 + MockK 替身
╱────────────────╲
```

## 2. 三阶段划分（对应 JaCoCo 三份覆盖率报告）

| 阶段 | 包 | 技术 | 用例数 | 测什么 |
|------|-----|------|--------|--------|
| **Stage1** | `com.zhuji.note.stage1` | 纯 JUnit + Truth | 88 | 纯逻辑：字数统计、Markdown 工具栏、模板引擎、双向链接解析、笔记过滤、番茄钟状态、写作目标、笔记统计 |
| **Stage2** | `com.zhuji.note.stage2` | JUnit + MockK + Turbine + MockWebServer | 49 | ViewModel、Repository（mock DAO）、DeepSeek SSE 客户端、边界用例（401/429/空流） |
| **Stage3** | `com.zhuji.note.stage3` | Robolectric | 18 | Room 数据库真实读写、Dao、DataStore、主题 token |
| **androidTest** | （instrumented） | Espresso + Compose UI Test | 30 | 端到端：编辑器流程、导航、笔记 CRUD 流程 |

## 3. 运行命令

```bash
# 全部单元测试（Stage1+2+3）
./gradlew :app:testDebugUnitTest

# 三阶段独立覆盖率 + 累积报告
./gradlew :app:jacocoStage1Report :app:jacocoStage2Report :app:jacocoStage3Report :app:jacocoCumulativeReport

# 集成测试（需 AVD/真机）
./gradlew :app:connectedDebugAndroidTest

# Monkey 随机压力测试（需设备）
./scripts/monkey_test.ps1

# UIAutomator 端到端 + 自动截图
./scripts/uiautomator_e2e.ps1
```

## 4. 覆盖率结果（逻辑层）

| 指标 | 覆盖率 |
|------|--------|
| LINE | 68.0% |
| INSTRUCTION | 66.0% |
| METHOD | 62.7% |
| BRANCH | 55.7% |
| CLASS | 58.2% |

### 覆盖率范围说明（业界标准做法）
JaCoCo 统计**排除 UI/Compose 层**（`ui/screens/**`、`ui/common/**`、`ui/theme/**`）。理由：
- `@Composable` 函数应由 **androidTest（Espresso/Compose UI Test）** 覆盖，而非单元测试。
- Robolectric 不渲染 Compose，把 UI 行数算进单测分母会失真。
- 这是 Now in Android 等 Google 官方样板项目的通行做法。

排除 Hilt 生成代码、Room `_Impl`、序列化 `$$serializer`、`BuildConfig`、`R` 等机器生成文件（这些不该计入人工编写覆盖率）。

## 5. 提升覆盖率的真实方法（非造假）

1. **ViewModel 测试** ROI 最高——StateFlow 变化 + 事件处理，一个 VM 测 10 case 覆盖数百行。
2. **Repository 集成测**——in-memory Room 或 MockK DAO，覆盖真实 SQL 路径与映射。
3. **AI SSE 分支**——MockWebServer 模拟正常/断流/error/空 content/reasoning_content，覆盖每个解析分支。
4. **纯逻辑全路径**——NoteStatistics/TemplateEngine/BiLinkParser 的边界与异常路径。
5. **排除 UI 层统计**——让数字真实反映逻辑层质量。

## 6. 关键测试技术示例

### MockK（Stage2）
```kotlin
val noteDao = mockk<NoteDao>(relaxUnitFun = true)
coEvery { noteDao.insert(any()) } returns 99L
coVerify(exactly = 1) { noteDao.insert(any()) }
```

### Turbine（Flow 测试）
```kotlin
repo().observeNotes(NoteFilter()).test {
    assertThat(awaitItem()).isEmpty()
    cancelAndIgnoreRemainingEvents()
}
```

### MockWebServer（SSE 流式）
```kotlin
server.enqueue(MockResponse().setResponseCode(200)
    .setHeader("Content-Type", "text/event-stream")
    .setBody("data: {...}\n\ndata: [DONE]\n\n"))
```

### Robolectric（Stage3）
```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class RoomDatabaseTest { /* 真实 Room 读写 */ }
```
