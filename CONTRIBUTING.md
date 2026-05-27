# Contributing to ZhujiNote

提交 Issue / PR 时请遵循下列约束。

## 分支
- `main` 是受保护的发布分支
- 功能开发开 `feat/<short-name>`，bugfix 开 `fix/<short-name>`

## Commit
- 使用 Conventional Commits：`feat:` `fix:` `docs:` `test:` `refactor:` `chore:`
- 单行简洁，不超过 70 字
- 中文 commit 也接受，但请保留前缀

## 代码风格
- Kotlin 1.9.24；遵循 ktfmt 默认规则
- 不要写注释（除非 KDoc 公共 API）
- Compose 组件命名：`Pascal`，文件按屏幕命名 `XxxScreen.kt`
- 数据流单向：UI → ViewModel(`StateFlow`) → UseCase → Repository → Dao

## 测试
- 任何改动都要补对应的 stage1/stage2/stage3 单测
- UI 改动要在 `androidTest/` 增加 Espresso 或 Compose UI Test
- 提交前本地必须跑过：
  ```powershell
  .\gradlew.bat :app:testDebugUnitTest :app:lintDebug
  ```

## DeepSeek API Key
- 不要把 Key 提交到仓库
- 本地存到设置页（DataStore）；CI 里用 `secrets.DEEPSEEK_KEY`
