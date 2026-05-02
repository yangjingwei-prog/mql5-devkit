# MQL5 DevKit — 完整实施计划

## 项目概述

基于 Lime MQL Editing 开源插件进行二次开发，添加编译集成、项目模板向导、clangd 集成等功能，打造一个完整的 MQL5 IDEA 开发环境。

**插件名称：** MQL5 DevKit for IntelliJ IDEA
**插件 ID：** `com.mql5.devkit`
**开发语言：** Java 21
**构建工具：** Gradle + IntelliJ Platform Gradle Plugin 2.15.0
**目标 IDE：** IntelliJ IDEA 2025.3+
**基础项目：** [Lime MQL Editing](https://plugins.jetbrains.com/plugin/29941-lime-mql-editing) (GPL3)
**项目路径：** `D:\Code\master\mql5-devkit`

---

## 关于 Maven vs Gradle

**结论：使用 Gradle，不推荐 Maven。**

| 对比项 | Gradle | Maven |
|--------|--------|-------|
| 官方支持 | JetBrains 官方推荐 | 无官方支持 |
| IntelliJ Platform Plugin | 2.x 版本，功能完整 | 无对应 Maven 插件 |
| `runIde` 沙箱测试 | 内置，一键启动 | 无，需手动配置 |
| 插件签名/发布 | 内置 `signPlugin`/`publishPlugin` | 需手动实现 |
| 文档/教程 | 全部基于 Gradle | 几乎没有 |
| Lime MQL Editing | 使用 Gradle | - |

---

## Lime MQL Editing 核心原理分析

### 1. 整体架构（90个 Java 文件）

```
com.limemojito.oss.mql/ (实际包名为 ru.investflow.mqlidea2)
├── MQL4Language.java          # 语言定义（单例）
├── MQL4FileType.java          # .mq4/.mqh 文件类型
├── MQL5FileType.java          # .mq5 文件类型（⚠️ Hack: 复用 MQL4Language）
├── MQL4Lexer.flex             # JFlex 词法规则（226行）
├── MQL4Lexer.java             # JFlex 生成的词法分析器
├── MQL4Icons.java             # 图标
├── parser/                    # 手写递归下降解析器
│   ├── MQL4Parser.java
│   ├── MQL4ParserDefinition.java
│   └── parsing/
│       ├── ClassParsing.java
│       ├── ExpressionParsing.java
│       ├── FunctionsParsing.java
│       ├── statement/EnumParsing.java
│       ├── statement/StatementParsing.java
│       ├── preprocessor/  # 预处理器解析
│       └── util/          # 解析工具类
├── psi/                       # PSI 元素体系
│   ├── MQL4Elements.java
│   ├── MQL4File.java          # 文件 PSI 根节点
│   ├── impl/                  # PSI 元素实现
│   └── stub/                  # Stub 索引（加速符号查找）
├── editor/                    # 编辑器功能
│   ├── MQL4SyntaxHighlighter.java
│   ├── MQL4BraceMatcher.java
│   ├── MQL4Commenter.java
│   ├── MQL4FoldingBuilder.java
│   └── codecompletion/
│       └── MQL4CompletionContributor.java
├── doc/                       # 文档系统
│   ├── MQL4DocumentationProvider.java
│   └── DocEntry.java
├── index/                     # 符号索引 & 导航
├── structure/                 # 结构视图
├── runconfig/                 # 编译运行配置
├── sdk/                       # SDK 类型
├── settings/                  # 设置
├── inspection/                # 代码检查
└── action/                    # 操作
```

### 2. 核心原理详解

#### A. 语言定义 — "MQL5 只是 MQL4 的一个 Hack"

```java
// MQL4Language.java — 唯一的语言定义
public class MQL4Language extends Language {
    public static final MQL4Language INSTANCE = new MQL4Language();
    private MQL4Language() { super("MQL4"); }
}

// MQL5FileType.java — 复用 MQL4Language！
public class MQL5FileType extends LanguageFileType {
    /** A small hack to enable basic support for MQL5 using the MQL4 specs. */
    private MQL5FileType() {
        super(MQL4Language.INSTANCE);  // ⚠️ MQL5 用了 MQL4 的语言定义
    }
}
```

**影响：** MQL4/MQL5 共享同一个语言实例，所有扩展点只注册一次（language="MQL4"），但 .mq4 和 .mq5 都能工作。

#### B. 词法分析器 — JFlex (.flex)

- 定义了 MQL4/MQL5 的所有 Token：关键字、类型、运算符、预处理器指令、颜色常量
- 覆盖了 MQL5 独有类型：`color`, `datetime`, `input`
- 缺少：`sinput` 关键字（MQL5 特有）
- 通过 `FlexAdapter` 适配到 IntelliJ 的 Lexer 接口

#### C. 解析器 — 手写递归下降（非 GrammarKit 生成）

没有 .bnf 文件，解析器完全是手写的 Java 代码：
- **优点：** 完全控制解析逻辑，方便处理 MQL 的特殊语法
- **缺点：** 维护成本高，无法使用 GrammarKit 的自动重构功能

#### D. 代码补全 — 基于 DocEntry 静态数据库

补全来源：
1. `MQL4DocumentationProvider.getEntries()` — 内置的关键字/函数数据库
2. 文件内所有 IDENTIFIER token — 文件级标识符补全
3. 预处理器补全（#property, #include）
4. 注释补全

#### E. 编译集成 — 已有但较基础

- ✅ 调用 `metaeditor64.exe /compile`
- ✅ 解析 .log 输出
- ✅ 支持 Wine（Linux）
- ❌ 错误只在 Console 显示，不在编辑器内标红
- ❌ 没有智能编译目标（编辑 .mqh 时无法找到关联的 .mq5）
- ❌ 没有编译错误在 Problems 面板显示

#### F. 设置 — 极其简陋

```java
// MQL4PluginSettingsImpl.java — 只有 2 个配置项！
public boolean enDocs = true;
public boolean errorAnalysis = true;
```

---

## 我们的改进点

| 功能 | Lime MQL Editing | MQL5 DevKit（我们的目标） |
|------|-----------------|------------------------|
| **MQL5 独立语言** | 共用 MQL4Language（Hack） | 独立的 MQL5Language，支持 MQL5 特有语法 |
| **设置界面** | 只有 2 个选项 | 完整配置面板：自动检测、路径配置、编译选项 |
| **编译错误显示** | 仅在 Console | 编辑器内标红 + Problems 面板 + ExternalAnnotator |
| **智能编译目标** | 无 | 编辑 .mqh 自动找到关联 .mq5 并编译 |
| **编译后操作** | 无 | 通知提示、自动刷新、可选部署 |
| **项目向导** | 无 | New Project Wizard（EA/指标/脚本/服务） |
| **文件模板** | 基础创建 | 带 ${NAME}/${YEAR}/${AUTHOR} 变量的完整模板 |
| **clangd 集成** | 无 | 一键生成 .clangd 配置，提供 C++ 级 IntelliSense |
| **自动检测 MT5** | 无 | 扫描注册表/常见路径自动找到 MetaTrader |
| **Compile on Save** | 无 | 可选的保存后自动编译 |
| **工具窗口** | 无 | MQL5 Build Log 工具窗口 |

### 改进优先级

```
P0 (核心改进):
  1. 完善 Settings UI（自动检测 MT5 路径、编译选项）
  2. 增强 ExternalAnnotator（编辑器内错误标红）
  3. 智能编译目标（.mqh → .mq5 依赖图）

P1 (用户体验):
  4. 项目向导 + 丰富的文件模板
  5. Compile on Save
  6. Build Log 工具窗口

P2 (高级功能):
  7. clangd 集成
  8. 独立 MQL5Language（可选，当前 Hack 方案够用）
```

---

## 当前项目结构（已实现的文件）

```
D:\Code\master\mql5-devkit/
├── build.gradle.kts              ✅ 升级到 IntelliJ Platform Plugin 2.15.0
├── settings.gradle.kts           ✅ rootProject.name = "mql5-devkit"
├── gradle.properties             ✅ Java 21, IC 2025.3, plugin v0.1.0
├── gradle/wrapper/
│   └── gradle-wrapper.properties ✅ Gradle 8.12
│
├── src/main/java/ru/investflow/mqlidea2/
│   │
│   │  ── 从 Lime MQL Editing 继承的核心代码（约90个文件）──
│   ├── MQL4Language.java         # 语言定义
│   ├── MQL4FileType.java         # .mq4/.mqh 文件类型
│   ├── MQL4Lexer.java            # JFlex 词法分析器
│   ├── MQL4Lexer.flex            # JFlex 规则文件
│   ├── MQL4Icons.java            # 图标
│   ├── MQL4PluginResources.java  # 资源管理
│   ├── parser/                   # 手写递归下降解析器
│   ├── psi/                      # PSI 元素体系 + Stub 索引
│   ├── editor/                   # 语法高亮/括号匹配/注释/折叠/代码补全
│   ├── doc/                      # Ctrl+Q 文档系统
│   ├── index/                    # 符号索引 & 导航
│   ├── structure/                # 结构视图
│   ├── runconfig/                # 编译运行配置（原有）
│   ├── sdk/                      # MetaEditor SDK 类型
│   ├── inspection/               # 代码检查
│   ├── action/                   # 文件创建操作
│   ├── annotation/               # 注解
│   └── util/                     # 工具类
│   │
│   │  ── 🆕 Phase 2: 配置管理（已创建骨架）──
│   ├── settings/
│   │   ├── MQL4PluginSettings.java         # 原有接口（保留）
│   │   ├── MQL4PluginSettingsImpl.java     # 原有实现（保留）
│   │   ├── MQL4PluginSettingsPanel.java    # 原有面板（保留）
│   │   ├── Mql5Settings.java               # ✨ 新增：完整持久化配置
│   │   ├── Mql5SettingsConfigurable.java   # ✨ 新增：设置 UI
│   │   └── Mql5PathDetector.java           # ✨ 新增：自动检测 MT5 路径
│   │
│   │  ── 🆕 Phase 3: 编译集成增强（已创建骨架）──
│   ├── compiler/
│   │   ├── Mql5BuildAction.java            # ✨ 一键编译（Ctrl+Shift+F9）
│   │   ├── Mql5ErrorParser.java            # ✨ 编译错误解析
│   │   └── Mql5ExternalAnnotator.java      # ✨ 编辑器内错误标红
│   │
│   │  ── 🆕 Phase 5: clangd 集成（已创建骨架）──
│   ├── clangd/
│   │   ├── ClangdConfigGenerator.java      # ✨ .clangd 配置生成
│   │   └── ClangdSetupAction.java          # ✨ Tools 菜单集成
│   │
│   │  ── 🆕 工具窗口（已创建骨架）──
│   └── ui/
│       └── Mql5ToolWindowFactory.java      # ✨ Build Log 工具窗口
│
├── src/main/resources/
│   ├── META-INF/
│   │   └── plugin.xml             ✅ 已更新：新 ID、新名称、新扩展点
│   ├── doc/                       # MQL 文档（原有）
│   └── icons/                     # 图标（原有）
│
└── src/test/java/                 # 测试代码（原有）
```

---

## 分阶段实施计划

### Phase 1：Fork + 环境搭建（✅ 已完成）

- [x] 复制 mqlidea2 到 mql5-devkit
- [x] 更新 settings.gradle.kts → rootProject.name = "mql5-devkit"
- [x] 更新 gradle.properties → Java 21, IC 2025.3, v0.1.0
- [x] 更新 build.gradle.kts → IntelliJ Platform Plugin 2.15.0, 移除 Kotlin 插件
- [x] 更新 gradle-wrapper.properties → Gradle 8.12
- [x] 更新 plugin.xml → com.mql5.devkit, MQL5 DevKit
- [x] 运行 `./gradlew build` 验证编译通过
- [x] 运行 `./gradlew runIde` 验证沙箱 IDE

### Phase 2：配置管理系统（✅ 已完成）

- [x] `Mql5Settings.java` — 10+ 字段的持久化配置
- [x] `Mql5PathDetector.java` — 自动检测 MetaEditor/MT5 路径
- [x] `Mql5SettingsConfigurable.java` — 设置 UI（Auto Detect 按钮）
- [x] 注册到 plugin.xml（替换原有 MQL4PluginSettingsPanel）

### Phase 3：编译集成增强（✅ 已完成）

- [x] `Mql5BuildAction.java` — 一键编译（Ctrl+Shift+F9）
- [x] `Mql5ErrorParser.java` — 编译错误解析
- [x] `Mql5ExternalAnnotator.java` — 编辑器内错误标注（红/黄下划线）
- [x] `Mql5CompilationResultCache.java` — 项目级错误缓存
- [x] `Mql5CompileOnSaveHandler.java` — 保存时自动编译
- [x] `Mql5RunOnChartAction.java` — 编译+部署+启动终端（Ctrl+Shift+F10）
- [ ] `Mql5SmartTargetResolver.java` — 智能编译目标（.mqh → .mq5）

### Phase 4：项目模板与文件向导（📋 待实现）

- [ ] `Mql5ProjectBuilder.java`
- [ ] `Mql5ModuleBuilder.java`
- [ ] 文件模板：EA / Indicator / Script / Service / Include
- [ ] 模板变量替换
- [ ] 右键菜单增强

### Phase 5：clangd 集成（✅ 基础完成）

- [x] `ClangdConfigGenerator.java` — .clangd 配置生成
- [x] `ClangdSetupAction.java` — Tools 菜单集成
- [ ] clangd LSP 客户端集成（当前仅生成配置文件）

---

## plugin.xml 关键配置说明

```xml
<idea-plugin>
    <id>com.mql5.devkit</id>
    <name>MQL5 DevKit</name>

    <!-- 语言定义使用 "MQL4"（Lime 的 Hack 方案） -->
    <!-- 所有 language="MQL4" 的扩展点同时覆盖 .mq4 和 .mq5 -->

    <extensions>
        <!-- 原有（继承自 Lime） -->
        <fileType language="MQL4" extensions="mq4;mqh;mql4;mq5;mql5"/>
        <lang.parserDefinition language="MQL4"/>
        <lang.syntaxHighlighterFactory language="MQL4"/>
        <completion.contributor language="MQL4"/>
        <!-- ... 等等 -->

        <!-- 新增 -->
        <externalAnnotator language="MQL4"/>        <!-- Phase 3 -->
        <toolWindow id="MQL5 Build" anchor="bottom"/> <!-- Phase 3 -->
    </extensions>

    <actions>
        <action id="MQL5.CompileCurrent"            <!-- Phase 3 -->
                text="Compile MQL5"
                shortcut="ctrl shift F9"/>
        <action id="MQL5.SetupClangd"                <!-- Phase 5 -->
                text="Setup clangd for MQL5"/>
    </actions>
</idea-plugin>
```

---

## MQL5 编译机制说明

MQL5 没有独立编译器，必须使用 MetaTrader 5 自带的 `metaeditor64.exe`：

```bash
# 编译单个文件
metaeditor64.exe /compile:"path\to\file.mq5" /log

# 指定 Include 路径
metaeditor64.exe /compile:"file.mq5" /include:"C:\Users\xxx\MQL5"

# 仅语法检查
metaeditor64.exe /compile:"file.mq5" /s /log

# 编译整个目录
metaeditor64.exe /compile:"path\to\directory"
```

**编译输出格式：**
```
file.mq5(15,5): error 130: undeclared identifier 'xxx'
file.mq5(20,10): warning 501: unused variable 'yyy'
```

**解析正则：** `[^\(]+\((\d+),(\d+)\)\s*:\s*(error|warning)\s+(\d+)\s*:\s*(.*)`

---

## VS Code 插件调研总结（参考）

| 插件 | 架构 | 特点 |
|------|------|------|
| **MQL Clangd** (最强) | clangd 后端 | IntelliSense、编译、回测、调试（代码注入）、智能编译目标 |
| **Buraq MQL5** | TextMate + 正则 | 语法高亮、编译、错误解析 |
| **MQL Tools** | Microsoft C++ 扩展 | 已停维，MQL Clangd 的前身 |
| **MQLens** | 自定义 TypeScript | 500+ 内置函数补全，LSP 计划中 |

**最佳实践借鉴：**
- MQL Clangd 的智能编译目标（编辑 .mqh 自动编译关联 .mq5）
- MQL Clangd 的 clangd 集成方式（.clangd 配置 + 诊断抑制）
- Buraq 的错误解析（正则匹配 MetaEditor 日志）
- 所有插件都通过命令行调用 metaeditor64.exe 编译

---

## 开发环境要求

| 工具 | 版本 | 说明 |
|------|------|------|
| JDK | 21+ | IntelliJ 插件开发要求 |
| IntelliJ IDEA | 2025.3+ | 开发 IDE（Community 版即可） |
| Gradle | 8.12 | 构建工具（通过 wrapper） |
| MetaTrader 5 | 最新版 | 编译测试用 |
| Git | 最新版 | 版本控制 |

---

## 快速开始

```bash
# 1. 在 IntelliJ IDEA 中打开项目
# File > Open > D:\Code\master\mql5-devkit

# 2. 等待 Gradle 同步完成

# 3. 运行沙箱 IDE 测试
./gradlew runIde

# 4. 构建插件
./gradlew build

# 5. 生成的插件在 build/distributions/
```
