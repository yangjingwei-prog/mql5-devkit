## MQL5 DevKit

为 IntelliJ IDEA 提供完整 MQL5/MQL4 开发环境的插件，包含语法支持、代码智能与集成 MetaEditor 编译。

#### 功能特性

- **语言支持** — 完整的 MQL4 / MQL5 语法解析与高亮，覆盖 OOP、预处理器指令、修饰符等
- **代码导航** — 按类、结构体、枚举、函数名快速跳转，支持跨文件符号索引
- **代码视图** — 结构大纲（Structure View）、括号匹配、代码折叠
- **文档查阅** — 悬停/快捷键查看内联文档（Ctrl+Q）
- **一键编译** — 集成 MetaEditor 编译器，工具栏/菜单直接触发构建
- **保存编译** — 保存 `.mq5` / `.mq4` 文件时自动编译，即时反馈结果
- **错误标注** — 编译错误/警告以红黄下划线实时标注到编辑器对应代码行
- **构建日志** — 独立的 Build Log 面板，展示完整编译输出与错误摘要
- **通知提醒** — 编译完成后弹出错误/成功通知
- **灵活配置** — MetaEditor 路径自动检测、编译选项开关、通知偏好设置

#### 源码编译

**前提条件：** JDK 17+

```bash
# 构建插件安装包
./gradlew buildPlugin -x test

# 启动沙箱 IDE 进行测试
./gradlew runIde

# 运行测试
./gradlew test
```

构建产物位于 `build/distributions/mql5-devkit-<version>.zip`。

#### 安装

1. 从 [Releases](../../releases) 下载 `mql5-devkit-*.zip`
2. 打开 IntelliJ IDEA → **File → Settings → Plugins → ⚙️ → Install Plugin from Disk...**
3. 选择下载的 `.zip` 文件，重启 IDE

#### 配置

打开 **File → Settings → Tools → MQL5 DevKit**：

| 配置项 | 说明 |
|--------|------|
| MetaEditor Path | `metaeditor64.exe` 的路径，点击 **Auto Detect** 可自动从已安装的 MetaTrader 5 中检测 |
| Compile on Save | 保存 `.mq5` / `.mq4` 文件时自动编译 |
| Error Analysis | 将编译错误/警告以红黄下划线标注到编辑器 |
| Show Notifications | 编译完成后弹出通知 |

#### 使用说明

- **手动编译** — 点击工具栏构建按钮，或 **Build → Build Project**（Ctrl+F9）编译当前 MQL5 文件
- **保存编译** — 开启后，保存 `.mq5` / `.mq4` 文件时自动触发编译
- **构建日志** — 打开 **Tool Windows → Build Log** 查看编译输出和错误摘要
- **错误跳转** — 编译错误会在编辑器中以下划线标出，按 F2 / Shift+F2 在错误间跳转

#### 运行环境

- IntelliJ IDEA 2021.1+
- Windows（编译功能需要 MetaEditor `metaeditor64.exe`）
- JDK 17+

#### 来源与致谢

本项目基于开源插件 [Lime MQL Editing](https://github.com/investflow/mqlidea)（原作者 Investflow.Ru）进行**二次开发**。

**相对原版的增强：**
- 扩展词法分析器与解析器，完整支持 MQL5 语法（新关键字、input group、参数化宏、final class 等）
- 新增 MetaEditor 编译集成（构建按钮、保存自动编译、错误解析器）
- 新增 ExternalAnnotator，将编译错误/警告标注到编辑器对应位置
- 新增构建日志面板与通知系统
- 新增设置界面：MetaEditor 路径配置、编译选项开关、分析偏好
- 升级构建系统至 Gradle 8.12 + IntelliJ Platform Gradle Plugin 2.15.0
- 兼容 IntelliJ IDEA 2021.1+ 版本
