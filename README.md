## MQL5 DevKit

<!-- Plugin description -->
An IntelliJ IDEA plugin providing a complete MQL5/MQL4 development environment with syntax support, code intelligence, and integrated MetaEditor compilation.

#### Features

**Language & Syntax**

- Full MQL4 / MQL5 syntax parsing & highlighting — OOP, preprocessor directives, access modifiers, etc.
- Custom color scheme — Settings → Editor → Color Scheme → MQL5
- Code style settings — Settings → Editor → Code Style → MQL5
- Spell checking — strings & comments only, keywords excluded

**Code Editing**

- Live Templates — 18 code templates (`ontick`, `oncalc`, `fori`, `while`, etc.)
- Quote auto-pairing — typing `"` or `'` auto-closes the pair
- Smart Enter — auto-indent after `{`, insert blank line inside blocks
- Code formatting — `Ctrl+Alt+L` basic indentation formatting
- Surround With — `Ctrl+Alt+T` wrap selection with `if` / `for` / `while` / `{ }`
- Import optimization — `Ctrl+Alt+O` safe `#include` cleanup

**Code Intelligence**

- Parameter info — `Ctrl+P` shows function parameter hints
- Find Usages — `Alt+F7` find all references to functions, classes, variables
- Highlight Usages — `Ctrl+Shift+F7` highlight all occurrences in current file
- Rename refactoring — `Shift+F6` rename functions, classes, variables
- Code inspections — missing entry point function, duplicate `#include` warnings

**Navigation & Structure**

- Structure View — outline of classes, structs, enums, functions
- Quick navigation — jump to class, struct, enum, function by name with cross-file index
- Line Markers — gutter icons for entry point functions (OnTick, OnInit...) and `#include` navigation
- File type icons — distinct MetaEditor-style icons for `.mq5` / `.mq4` / `.mqh` files
- Bracket matching & code folding
- Documentation lookup — `Ctrl+Q` inline documentation

**Build & Compilation**

- One-Click Build — `F7` compile current file via MetaEditor
- Compile on Save — auto-compile `.mq5` / `.mq4` when saved
- Smart Compile — edit a `.mqh` header → auto-compile related `.mq5` source
- Error Annotations — compilation errors & warnings as red/yellow underlines at exact locations
- Build Log — dedicated panel with full compiler output (Clear / Copy toolbar)
- Notifications — pop-up on build success or failure

**Run & Deploy**

- Run on Chart — `F5` compile + deploy `.ex5` to terminal + launch MetaTrader 5

**Project & Templates**

- Project wizard — standard MQL5 directory structure with optional initial files
- File templates — EA / Indicator / Script / Service / Include / MQL4

**Integration**

- Open in MetaEditor — right-click → open current file in MetaEditor
- Auto-detect paths — scan for MetaEditor & MetaTrader installations automatically
- clangd config — generate `.clangd` configuration file via Tools menu

#### Build from Source

**Prerequisites:** JDK 17+

```bash
# Build plugin distribution
./gradlew buildPlugin -x test

# Run sandbox IDE for testing
./gradlew runIde

# Run tests
./gradlew test
```

The output zip is at `build/distributions/mql5-devkit-<version>.zip`.

#### Installation

1. Download `mql5-devkit-*.zip` from [Releases](../../releases)
2. Open IntelliJ IDEA → **File → Settings → Plugins → ⚙️ → Install Plugin from Disk...**
3. Select the downloaded `.zip` file and restart IDE

#### Configuration

Open **File → Settings → Tools → MQL5 DevKit**:

| Setting | Description |
|---------|-------------|
| MetaEditor Path | Path to `metaeditor64.exe`. Click **Auto Detect** to find it automatically. |
| Compile on Save | Automatically compile `.mq5` / `.mq4` files when saved |
| Error Analysis | Display compilation errors & warnings as editor annotations |
| Show Notifications | Show pop-up notifications after compilation completes |

#### Keyboard Shortcuts

| Action | Shortcut |
|--------|----------|
| Compile | `F7` |
| Run on Chart | `F5` |
| Format Code | `Ctrl+Alt+L` |
| Optimize Imports | `Ctrl+Alt+O` |
| Surround With | `Ctrl+Alt+T` |
| Parameter Info | `Ctrl+P` |
| Find Usages | `Alt+F7` |
| Highlight Usages | `Ctrl+Shift+F7` |
| Rename | `Shift+F6` |
| Quick Documentation | `Ctrl+Q` |
| Jump to Error | `F2` / `Shift+F2` |

#### Requirements

- IntelliJ IDEA 2021.1+
- Windows (MetaEditor `metaeditor64.exe` required for compilation)
- JDK 17+

#### Source & Credits

This project is a **secondary development** based on the open-source [Lime MQL Editing](https://github.com/investflow/mqlidea) plugin (originally by Investflow.Ru).

**Enhancements over the original:**
- Extended lexer & parser with full MQL5 syntax support (keywords, input groups, parameterized macros, final classes)
- Added MetaEditor compilation integration (build action, compile on save, error parser, smart compile)
- Added ExternalAnnotator to display compilation errors as in-editor annotations
- Added build log panel with ActionToolbar and notification system
- Added Run on Chart (compile + deploy + launch terminal)
- Added code intelligence: parameter info, find usages, highlight usages, rename refactoring
- Added code editing: live templates, quote auto-pairing, surround with, formatting, spell checking
- Added code inspections: missing entry point, duplicate #include
- Added project/file templates with wizard
- Added MetaEditor-style file type icons
- Added settings UI with auto-detection for MetaEditor path
- Upgraded build system to Gradle 8.12 + IntelliJ Platform Gradle Plugin 2.15.0

<!-- Plugin description end -->
