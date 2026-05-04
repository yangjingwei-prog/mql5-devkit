## MQL5 DevKit

为 IntelliJ IDEA 提供完整 MQL5/MQL4 开发环境的插件，包含语法支持、代码智能与集成 MetaEditor 编译。

#### 功能特性

**语言与语法**

- 完整的 MQL4 / MQL5 语法解析与高亮 — 覆盖 OOP、预处理器指令、访问修饰符等
- 自定义配色方案 — Settings → Editor → Color Scheme → MQL5
- 代码风格设置 — Settings → Editor → Code Style → MQL5
- 拼写检查 — 仅检查字符串和注释，排除关键词

**代码编辑**

- Live Templates — 18 个代码模板（`ontick`、`oncalc`、`fori`、`while` 等）
- 引号自动配对 — 输入 `"` 或 `'` 自动补全配对
- 智能 Enter — `{` 后自动缩进，块内插入空行
- 代码格式化 — `Ctrl+Alt+L` 基本缩进格式化
- 包围代码 — `Ctrl+Alt+T` 用 `if` / `for` / `while` / `{ }` 包围选中代码
- Import 优化 — `Ctrl+Alt+O` 安全清理 `#include`

**代码智能**

- 参数提示 — `Ctrl+P` 显示函数参数信息
- 查找引用 — `Alt+F7` 查找函数、类、变量的所有使用位置
- 高亮用法 — `Ctrl+Shift+F7` 高亮当前文件中所有匹配项
- 重命名重构 — `Shift+F6` 重命名函数、类、变量
- 代码检查 — 缺少入口函数警告、重复 `#include` 检测

**导航与结构**

- 结构视图 — 类、结构体、枚举、函数大纲
- 快速导航 — 按名称跳转到类、结构体、枚举、函数，支持跨文件索引
- Line Markers — 入口函数（OnTick、OnInit...）显示运行图标，`#include` 显示导航箭头
- 文件图标 — `.mq5` / `.mq4` / `.mqh` 使用 MetaEditor 风格的独立图标
- 括号匹配与代码折叠
- 文档查阅 — `Ctrl+Q` 查看内联文档

**构建与编译**

- 一键编译 — `F7` 调用 MetaEditor 编译当前文件
- 保存编译 — 保存 `.mq5` / `.mq4` 时自动编译
- 智能编译 — 编辑 `.mqh` 头文件时自动编译关联的 `.mq5` 源文件
- 错误标注 — 编译错误/警告以红黄下划线标注到编辑器对应代码行
- 构建日志 — 独立面板展示完整编译输出（含 Clear / Copy 工具栏）
- 通知提醒 — 编译成功或失败时弹出通知

**运行与部署**

- Run on Chart — `F5` 编译 + 部署 `.ex5` + 启动 MetaTrader 5 终端

**项目与模板**

- 项目向导 — 标准 MQL5 目录结构 + 可选初始文件
- 文件模板 — EA / 指标 / 脚本 / 服务 / Include / MQL4 六种模板

**集成**

- 打开 MetaEditor — 右键菜单在 MetaEditor 中打开当前文件
- 路径自动检测 — 自动扫描 MetaEditor 和 MetaTrader 安装路径
- clangd 配置 — 通过 Tools 菜单生成 `.clangd` 配置文件

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
| MetaEditor Path | `metaeditor64.exe` 的路径，点击 **Auto Detect** 可自动检测 |
| Compile on Save | 保存 `.mq5` / `.mq4` 文件时自动编译 |
| Error Analysis | 将编译错误/警告以红黄下划线标注到编辑器 |
| Show Notifications | 编译完成后弹出通知 |

#### 快捷键

| 操作 | 快捷键 |
|------|--------|
| 编译 | `F7` |
| Run on Chart | `F5` |
| 格式化代码 | `Ctrl+Alt+L` |
| 优化 Import | `Ctrl+Alt+O` |
| 包围代码 | `Ctrl+Alt+T` |
| 参数提示 | `Ctrl+P` |
| 查找引用 | `Alt+F7` |
| 高亮用法 | `Ctrl+Shift+F7` |
| 重命名 | `Shift+F6` |
| 快速文档 | `Ctrl+Q` |
| 跳转到错误 | `F2` / `Shift+F2` |

#### 运行环境

- IntelliJ IDEA 2021.1+
- Windows（编译功能需要 MetaEditor `metaeditor64.exe`）
- JDK 17+

#### 来源与致谢

本项目基于开源插件 [Lime MQL Editing](https://github.com/investflow/mqlidea)（原作者 Investflow.Ru）进行**二次开发**。

**相对原版的增强：**
- 扩展词法分析器与解析器，完整支持 MQL5 语法（新关键字、input group、参数化宏、final class 等）
- 新增 MetaEditor 编译集成（构建按钮、保存自动编译、错误解析器、智能编译）
- 新增 ExternalAnnotator，将编译错误/警告标注到编辑器对应位置
- 新增构建日志面板（ActionToolbar）与通知系统
- 新增 Run on Chart（编译 + 部署 + 启动终端）
- 新增代码智能：参数提示、查找引用、高亮用法、重命名重构
- 新增代码编辑：Live Templates、引号配对、包围代码、格式化、拼写检查
- 新增代码检查：缺少入口函数、重复 #include
- 新增项目/文件模板向导
- 新增 MetaEditor 风格文件图标
- 新增设置界面（含路径自动检测）
- 升级构建系统至 Gradle 8.12 + IntelliJ Platform Gradle Plugin 2.15.0
