# 当前实现状态

**更新时间：** 2026-05-02
**版本：** v0.1.0
**状态：** P0 功能已全部实现，P1 部分完成

## 已完成功能

### 编译集成（Phase 3）✅ 全部完成
- [x] `Mql5BuildAction.java` — 一键编译（Ctrl+Shift+F9），调用 metaeditor64.exe
- [x] `Mql5ErrorParser.java` — 编译日志解析，正则匹配 error/warning
- [x] `Mql5ExternalAnnotator.java` — 编译错误/警告标注到编辑器对应位置（红/黄下划线）
- [x] `Mql5CompilationResultCache.java` — 项目级错误缓存，供 ExternalAnnotator 读取
- [x] `Mql5CompileOnSaveHandler.java` — 保存 .mq5/.mq4 时自动编译
- [x] `Mql5RunOnChartAction.java` — 编译 + 部署 .ex5 + 启动终端（Ctrl+Shift+F10）

### 配置管理（Phase 2）✅ 全部完成
- [x] `Mql5Settings.java` — 10+ 字段持久化配置（PersistentStateComponent）
- [x] `Mql5PathDetector.java` — 自动检测 MetaEditor/终端安装路径
- [x] `Mql5SettingsConfigurable.java` — 设置 UI（Settings > Tools > MQL5 DevKit）

### UI 与工具窗口 ✅ 全部完成
- [x] `Mql5ToolWindowFactory.java` — Build Log 工具窗口（底部面板）
- [x] `Mql5BuildLogService.java` — 编译日志管理服务

### clangd 集成（Phase 5）✅ 基础完成
- [x] `ClangdConfigGenerator.java` — .clangd YAML 配置生成
- [x] `ClangdSetupAction.java` — Tools 菜单集成

### 配置文件更新（Phase 1）✅ 全部完成
- [x] Gradle 8.12 + IntelliJ Platform Plugin 2.15.0
- [x] Java 21, IDEA 2025.3+
- [x] plugin.xml 注册所有新扩展点和 Action

## plugin.xml 注册的扩展点

| 类型 | ID / class | 说明 |
|------|-----------|------|
| applicationService | Mql5Settings | 全局配置持久化 |
| projectService | Mql5BuildLogService | 编译日志服务 |
| projectService | Mql5CompilationResultCache | 错误缓存 |
| externalAnnotator | Mql5ExternalAnnotator | 编辑器错误标注 |
| toolWindow | MQL5 Build | 编译日志面板 |
| projectConfigurable | Mql5SettingsConfigurable | Settings > Tools > MQL5 DevKit |
| action | Compile MQL5 (Ctrl+Shift+F9) | 一键编译 |
| action | Run on Chart (Ctrl+Shift+F10) | 编译+部署+运行 |
| action | Setup clangd for MQL5 | 生成 .clangd |

## 待实现

### P1（用户体验）
- [ ] **智能编译目标** — 编辑 .mqh 时自动找到关联 .mq5 并编译（当前跳过 .mqh）
- [ ] **项目模板向导** — New Project Wizard（EA/指标/脚本/服务）

### P2（高级功能）
- [ ] **clangd LSP 集成** — 当前仅生成 .clangd 配置，未实现 LSP 客户端

### 已调研不可行的方案
- ~~辅助脚本自动挂载指标~~ — MT5 不允许外部程序自动执行脚本
- ~~`metaeditor64.exe /debug` 自动调试~~ — 只打开编辑器，不会自动启动调试
- ~~模板(.tpl)自动渲染~~ — 终端不会自动加载外部写入的 .tpl/.chr 文件
- **结论：** MT5 的安全机制不允许外部程序自动挂载指标到图表，当前 Run on Chart 只能做到编译+部署+启动终端

## MetaEditor 命令行编译格式

```bash
metaeditor64.exe /compile:"file.mq5" /log          # 编译
metaeditor64.exe /compile:"file.mq5" /s /log        # 仅语法检查
metaeditor64.exe /debug:"file.mq5"                  # 打开编辑器调试模式（不自动启动）
```

**日志输出格式：**
```
file.mq5(line,col): error/warning code: message
```

**解析正则：**
```java
Pattern.compile("[^\\(]+\\((\\d+),(\\d+)\\)\\s*:\\s*(error|warning)\\s+(\\d+)\\s*:\\s*(.*)")
```

## Lime 原有代码关键类（不需要修改）

| 类 | 功能 | 说明 |
|----|------|------|
| `MQL4Language` | 语言定义 | 单例，MQL4/MQL5 共用 |
| `MQL4FileType` | 文件类型 | 关联 .mq4/.mqh/.mq5 扩展名 |
| `MQL4Lexer` (JFlex) | 词法分析 | 226 行 .flex 规则 |
| `MQL4Parser` | 语法分析 | 手写递归下降，约 2000 行 |
| `MQL4SyntaxHighlighter` | 语法高亮 | 关键字/字符串/注释/数字颜色 |
| `MQL4CompletionContributor` | 代码补全 | 基于 DocEntry 静态数据库 |
| `MQL4DocumentationProvider` | Ctrl+Q 文档 | 内置 HTML 文档 |
