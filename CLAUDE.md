# MQL5 DevKit — IntelliJ IDEA Plugin

## 项目简介

基于 Lime MQL Editing 开源插件的二次开发项目，为 IntelliJ IDEA 提供完整的 MQL5 开发环境。

## 技术栈

- **语言：** Java 21（不用 Kotlin）
- **构建：** Gradle 8.12 + IntelliJ Platform Gradle Plugin 2.15.0
- **目标 IDE：** IntelliJ IDEA 2025.3+
- **包名：** `ru.investflow.mqlidea2`

## 关键约束

1. **MQL5 没有独立编译器**，必须通过 `metaeditor64.exe /compile` 命令行编译
2. **MQL5 复用 MQL4Language**（Lime 的 Hack），所有 plugin.xml 中 `language="MQL4"` 同时覆盖 .mq4 和 .mq5
3. **词法分析器**用 JFlex（.flex 文件），解析器是手写递归下降（非 GrammarKit BNF）
4. **不要改包名**，保持 `ru.investflow.mqlidea2` 以兼容 Lime 原有代码
5. **MT5 不支持外部程序自动挂载指标**——终端无命令行参数挂载 EA/指标，MetaEditor `/debug` 不会自动启动调试

## 项目结构

- `docs/IMPLEMENTATION_PLAN.md` — 完整实施计划和架构分析（**必读**）
- `docs/CURRENT_STATUS.md` — 当前实现状态
- `src/main/java/ru/investflow/mqlidea2/` — 源码根目录
  - 从 Lime 继承的核心代码（parser, psi, editor, doc, index 等）
  - `compiler/` — 编译集成：BuildAction、ExternalAnnotator、ErrorParser、CompileOnSave、RunOnChart、SmartCompileTarget
  - `settings/` — 配置管理：Mql5Settings、PathDetector、SettingsConfigurable（含 Test 按钮）
  - `ui/` — Build Log 工具窗口（含 Clear/Copy 工具栏）、BuildLogService
  - `clangd/` — clangd 配置生成
  - `refactor/` — 重命名重构（RenameProcessor）
- `src/main/resources/META-INF/plugin.xml` — 插件描述符

## 构建命令

```bash
JAVA_HOME="/c/Program Files/java/jdk-17.0.2" ./gradlew buildPlugin -x test   # 构建插件
./gradlew runIde                                                               # 启动沙箱 IDE 测试
./gradlew test                                                                 # 运行测试
```

## 已实现功能

| 功能 | 快捷键 | 说明 |
|------|--------|------|
| 一键编译 | Ctrl+Shift+F9 | 调用 metaeditor64.exe 编译当前文件 |
| 保存编译 | 自动 | 保存 .mq5/.mq4 文件时自动编译 |
| 智能编译 | 自动 | 编辑 .mqh 时自动找到关联 .mq5 并编译 |
| 错误标红 | 自动 | 编译错误/警告标注到编辑器对应位置 |
| Run on Chart | Ctrl+Shift+F10 | 编译 + 部署 .ex5 + 启动终端 |
| 设置界面 | — | Settings > Tools > MQL5 DevKit（含 Test/Auto Detect） |
| 自动检测 | — | 自动扫描 MetaEditor/终端安装路径 |
| clangd 配置 | Tools 菜单 | 生成 .clangd 配置文件 |
| Build Log | 工具窗口 | 底部面板显示编译输出（含 Clear/Copy） |
| 项目模板 | New Project | 标准目录结构 + 可选初始文件（EA/指标/脚本） |
| 文件模板 | New → MQL5 | EA/指标/脚本/服务/Include/MQL4 六种模板 |
| Live Templates | — | 18 个代码模板（ontick、oncalc、fori 等） |
| 打开 MetaEditor | 右键菜单 | 在 MetaEditor 中打开当前文件 |
| 自动配对 | 自动 | 输入引号自动补全配对（含 QuoteHandler） |
| 颜色设置 | Settings | Editor > Color Scheme > MQL5 |
| 参数提示 | Ctrl+P | 函数参数信息提示 |
| 查找引用 | Alt+F7 | 查找函数/类的使用位置 |
| 重命名 | Shift+F6 | 重命名函数/类 |
| Line Markers | 自动 | 入口函数显示运行图标，#include 显示导航箭头 |
| Enter 缩进 | Enter | { 后自动缩进 |
| 高亮用法 | Ctrl+Shift+F7 | 高亮当前标识符所有出现 |
| 文件图标 | 自动 | .mqh/.mq5 不同图标 |
| 代码检查 | 自动 | 缺少入口函数、重复 #include |
| 代码格式化 | Ctrl+Alt+L | 基本缩进格式化 |
| Import 优化 | Ctrl+Alt+O | #include 优化（安全模式） |
| 拼写检查 | 自动 | 仅检查字符串和注释，排除关键词 |
| 包围代码 | Ctrl+Alt+T | if/for/while/braces 包围选中代码 |
| 代码风格 | Settings | Editor > Code Style > MQL5 |

## 待实现

- P2：clangd LSP 集成（当前仅配置生成）
