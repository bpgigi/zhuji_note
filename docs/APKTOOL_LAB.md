# APKTool 解包、修改与重打包实验

## 1. 实验目标

以大作业 ZhujiNote 的 CI Debug APK 为被测应用，使用 APKTool 完成：

1. APK 解包；
2. 资源代码修改；
3. APK 重打包；
4. zipalign、重新签名及签名验证；
5. 可选的模拟器安装和启动验证。

本实验将桌面图标和系统任务列表中的应用名从 `助记 ZhujiNote` 修改为
`助记 ZhujiNote（APKTool验证版）`。这个改动不依赖源码重新编译，能够直接证明修改发生在 APKTool
解包后的资源中。

## 2. 一键执行

在仓库根目录打开 PowerShell：

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\apktool_repackage.ps1
```

脚本默认自动下载：

- 本仓库 `main` 分支最新的 `app-debug-apk` CI artifact；
- Eclipse Temurin 21 便携 JRE（仅在系统找不到 Java 时）；
- APKTool 3.0.2；
- uber-apk-signer 1.3.0。

工具会缓存在 `.apktool-tools/`，解包过程位于 `.apktool-work/`，最终结果位于：

```text
dist/apktool/zhuji-note-apktool-signed.apk
dist/apktool/verification.md
```

以上下载缓存和 APK 产物均已加入 `.gitignore`，仓库只提交脚本和过程文档。

## 3. 使用本地 APK

也可以对自己刚编译的 APK 执行：

```powershell
.\scripts\apktool_repackage.ps1 `
  -InputApk .\app\build\outputs\apk\debug\app-debug.apk `
  -NewAppName "助记 ZhujiNote（课程验证版）"
```

首次运行完成后可使用 `-Offline` 禁止联网，直接复用本地缓存。

## 4. 安装验证

启动模拟器或连接测试机，确保 `adb devices` 能看到设备，然后运行：

```powershell
.\scripts\apktool_repackage.ps1 -Install
```

如果设备上已安装由另一证书签名的原版应用，Android 会拒绝覆盖安装。确认可以清除原应用数据后再运行：

```powershell
.\scripts\apktool_repackage.ps1 -Install -ReplaceInstalled
```

安装成功后脚本自动启动 `com.zhuji.note/.MainActivity`。在桌面图标或系统最近任务中检查应用名是否为
“助记 ZhujiNote（APKTool验证版）”。

## 5. 实现过程

脚本执行的核心命令等价于：

```powershell
java -jar apktool_3.0.2.jar decode -f --no-src -o decoded app-debug.apk
# 结构化修改 decoded/res/values/strings.xml 中的 app_name
java -jar apktool_3.0.2.jar build decoded -o unsigned.apk
java -jar uber-apk-signer-1.3.0.jar --apks unsigned.apk --out signed --allowResign
java -jar uber-apk-signer-1.3.0.jar --apks signed.apk --onlyVerify
```

脚本还会校验两个工具 JAR 的 SHA-256，记录输入和输出 APK 的 SHA-256，并在任一步骤返回非零退出码时停止，
避免把失败产物误当作验证成功。

签名完成后，脚本会再次解包最终 APK，并严格检查其中的 `app_name` 是否等于目标值，从而同时验证“资源修改成功”
和“签名产物可正常解析”。

本实验只修改资源，因此解包使用 `--no-src` 保留原始 DEX，不反汇编或重编译业务字节码。这能显著缩短执行时间，
也能减少与目标无关的 APK 变化。

签名前优先使用系统或 Android SDK 中的 `zipalign`；如果本机没有 Android SDK，脚本会从已校验的
uber-apk-signer JAR 中提取其内置 Windows `zipalign`，不需要额外安装 SDK。

## 6. 提交说明示例

```text
完成 ZhujiNote APKTool 逆向验证：
使用脚本自动下载/读取被测 APK，完成解包，修改 app_name 资源，
重新构建并进行 zipalign、Debug 签名与签名校验。
脚本及实验说明：https://github.com/bpgigi/zhuji_note
```
