## MQL5 DevKit

<!-- Plugin description -->
An IntelliJ IDEA plugin providing a complete MQL5/MQL4 development environment with syntax support, code intelligence, and integrated MetaEditor compilation.

#### Features

- **Language Support** — Full MQL4 / MQL5 syntax parsing & highlighting, covering OOP, preprocessor directives, modifiers, etc.
- **Code Navigation** — Quick navigation by class, struct, enum, function names with cross-file symbol index
- **Code View** — Structure outline, bracket matching, code folding
- **Documentation** — Inline documentation lookup on hover / shortcut (Ctrl+Q)
- **One-Click Build** — Integrated MetaEditor compilation via toolbar / menu
- **Compile on Save** — Auto-compile `.mq5` / `.mq4` files on save with instant feedback
- **Error Annotations** — Compilation errors & warnings shown as red/yellow underlines at exact code locations
- **Build Log** — Dedicated Build Log panel with full compiler output and error summary
- **Notifications** — Pop-up notifications on build success or failure
- **Flexible Settings** — Auto-detect MetaEditor path, toggle compile-on-save, error analysis, notification preferences

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
| MetaEditor Path | Path to `metaeditor64.exe`. Click **Auto Detect** to find it automatically from installed MetaTrader 5. |
| Compile on Save | Automatically compile `.mq5` / `.mq4` files when saved |
| Error Analysis | Display compilation errors & warnings as editor annotations (red/yellow underlines) |
| Show Notifications | Show pop-up notifications after compilation completes |

#### Usage

- **Build** — Click the build button on toolbar, or **Build → Build Project** (Ctrl+F9) to compile the current MQL5 file
- **Compile on Save** — When enabled, saving a `.mq5` / `.mq4` file triggers automatic compilation
- **Build Log** — Open **Tool Windows → Build Log** to view compiler output and error summary
- **Error Navigation** — Compilation errors are underlined in the editor; press F2 / Shift+F2 to jump between errors

#### Requirements

- IntelliJ IDEA 2021.1+
- Windows (MetaEditor `metaeditor64.exe` required for compilation)
- JDK 17+

#### Source & Credits

This project is a **secondary development** based on the open-source [Lime MQL Editing](https://github.com/investflow/mqlidea) plugin (originally by Investflow.Ru).

**Enhancements over the original:**
- Extended lexer & parser with full MQL5 syntax support (keywords, input groups, parameterized macros, final classes)
- Added MetaEditor compilation integration (build action, compile on save, error parser)
- Added ExternalAnnotator to display compilation errors as in-editor annotations
- Added build log panel and notification system
- Added settings UI for MetaEditor path, compile options, and analysis toggles
- Upgraded build system to Gradle 8.12 + IntelliJ Platform Gradle Plugin 2.15.0
- Targeting modern IntelliJ IDEA versions (2021.1+)

<!-- Plugin description end -->
