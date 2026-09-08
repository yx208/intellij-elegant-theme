# 2.1.0 设计文档 — Elegant Ink Light（水墨宣纸主题）

## 设计理念

新增一套护眼的水墨风浅色主题 **Elegant Ink Light（Beta）**，整体意象是「矿物颜料画在宣纸上」：

- **宣纸为底**：背景模拟淡黄宣纸。Islands 布局中每个岛（编辑器、工具窗口、终端）是一张纸面 `#F6F0DF`，岛与岛之间露出更深的暖沙窗底 `#EAE2CC`，对话框与输入控件在两者之间再分纸阶，共四层宣纸层次。
- **墨色为字**：全局无纯黑、无纯白。正文浓墨 `#3B3A36`（对纸面约 9:1），次级中墨 `#6B675E`，注释淡墨斜体 `#8F8873`。
- **传统色为彩**：语法高亮与终端 ANSI 色全部取低饱和中国传统色——靛青、竹绿、赭石、青花、紫棠、青碧、秋香、朱砂——对纸面对比度控制在 4:1–6.5:1，显著柔和于默认 Light 主题的高饱和原色，同时保证语法区分度。
- **交互如运笔**：悬停 = 淡墨晕（笔尖掠纸），选中 = 淡黛蓝晕（落笔见青 `#C8D5E3`）；UI 强调色统一为黛青 `#44546B`（按钮、Tab 下划线、焦点环、复选框）。
- **只改色，不改形**：基于 JetBrains 官方 Islands Light，仅覆盖颜色，不改任何布局与组件行为（唯一的度量例外是菜单与设置树行距，见「菜单与设置树行距」一节）。实现上优先覆盖父主题语义 token（而非逐键硬编码），官方升级 Islands 时未覆盖项自动跟进；对绕过色板的硬编码键与底层继承泄漏做定点修补。

## 本版本改动总览

2.1.0 实际落地的改动（明细见后文各节）：

1. **新增主题对**：`ink-light.theme.json`（UI 主题）+ `ink-light.theme.xml`（编辑器 scheme），在 `plugin.xml` 注册为 Elegant Ink Light。
2. **宣纸背景 + 墨色文字体系**：覆盖 Islands Light 语义色板 70 个 token，四层纸阶 + 三级墨色。
3. **黛青 UI 强调系统**：按钮/Tab 下划线/工具栏激活用黛青，树/列表选中淡黛蓝晕，链接黛蓝，错误/警告/成功降饱和为朱砂/秋香/苔绿。
4. **编辑器语法配色**：八系传统色语法高亮；标点与标识符统一浓墨、错误波浪线降为朱砂、diff 补齐纸面和谐色、光标浓墨。
5. **终端 / Console 整块配色**：纸色背景 + 墨系 ANSI 16 色（此前 scheme 无任何 `CONSOLE_*` 键，终端继承 Default 的纯白底）。
6. **Inlay hints 暖墨芯片**：参数/类型提示从冷灰改为纸系芯片，当前参数用黛青系。
7. **编辑器文件 Tab 选中态**：暖沙底 + 淡黛蓝晕描边，分屏失焦浅一纸阶。
8. **Checkbox / Radio 全套改色**：经 `icons.ColorPalette` 把官方亮蓝覆盖为黛青 + 纸色。
9. **淡墨 hover 体系**：4 个透明黑 alpha token 改为墨色并提一档强度。
10. **插件管理页跟随纸色**：修复纯白列表行与米色分组头的拼接花斑。
11. **继承泄漏修补**：13 组显式 ui 键压制五层继承链漏上来的纯白/冷灰（非编辑下拉、按钮填充、补全弹窗、分隔线、表格斑马纹等）。
12. **Slider / ProgressBar 暖化**：冷灰轨道/滑钮与渐变端点改暖。
13. **菜单与设置树行距放宽**：`PopupMenu.Selection.outerInsets` / `Menu.Selection.outerInsets` 上下值 1px → 2px（弹出菜单每项增高 2px）；Settings 左侧导航树显式定行高 `SettingsTree.rowHeight` = 27（默认未定义、按渲染器自适应，偏紧）。本主题仅有的度量改动。
14. **更新通知机制**：新增本插件唯一的源码类，安装/更新后首次打开项目时弹一条本版要点通知，每个版本只弹一次（见「更新通知机制」一节）。插件最低兼容版本随之由 2021.1（211）提升至 2022.3（223）。

## 构成

| 文件 | 作用 |
|---|---|
| `resources/theme/ink-light.theme.json` | UI 主题，`parentTheme: "Islands Light"` |
| `resources/theme/ink-light.theme.xml` | 编辑器配色，`parent_scheme="Default"`，键位对照 `islands-light.theme.xml` 全量改色 |
| `plugin.xml` | 注册 `elegant-ink-light` themeProvider + `ElegantInkLight` colorScheme，以及更新通知的 `notificationGroup` + `postStartupActivity` |
| `src/com/github/yx208/eleganttheme/UpdateNotificationActivity.java` | 更新通知启动活动，本插件唯一源码（见「更新通知机制」） |

## 核心机制：覆盖父主题语义色板

官方 Islands Light（`intellij.platform.ide.impl.jar` 内的 `themes/islands/ManyIslandsLight.theme.json`）的所有 UI 映射都走语义 token 引用链，例如：

```
ui:     MainWindow.background → main-window-bg → layer-0-bg → gray-150
```

平台加载子主题时（源码 `UIThemeBean.kt` 的 `importFromParentTheme()`）会把父子两边的 `colors` 合并、**同名键以子主题为准**，父主题继承下来的 ui 映射按合并后的色板解析。因此本主题的 JSON 只需覆盖语义 token（`layer-2-bg`、`accent-brand-bg` 等），父主题几百条 ui 映射自动变暖，渐变、徽章、组件行为等未覆盖项原样继承官方默认，官方升级 Islands 时可自动跟进。

**重要提示（防误删）**：`colors` 里的 token 不会被本文件的 `ui` 块引用，消费者在父主题里。DevKit 的 unused 检查只扫本文件，会把它们全部误标为未使用——不要据此删除。当前保留的 70 项（含 4 个淡墨 hover token，见「控件与 hover 体系」）全部经过引用链验证（父主题 ui 直接引用 + 色板内传递引用 > 0）。以下 9 个 token 在 Islands Light 中确认无消费者，已删除，勿加回：`layer-0-bg-inline`、`layer-0-border-inline`、`layer-1-border`、`control-bg-small-disabled`、`control-border-small`、`toolbar-run-bg-hovered`、`toolbar-stop-bg-hovered`、`icon-green-stroke`、`transparent`。

个别绕过色板的硬编码键（`FileColor.*`、`Tag.background`、`Editor.ToolTip.selectionBackground`）在 `ui` 块中显式覆盖；`ToolWindow`/`Tree`/`Island` 三组键沿用本仓库 Elegant Islands 家族的既有设置。

插件管理页（Settings → Plugins）是另一处绕过语义色板的例外：父主题把 `Plugins.background` 直接映射到字面色名 `white`（#FFFFFF），而分组头 `Plugins.SectionHeader.background` 未定义、走通配符 `*.background` → `dialog-bg` → `layer-1-bg`（本主题为米色），导致纯白列表行与米色分组头拼接花斑。因此在 `ui` 块显式覆盖 `Plugins` 一组键：`background` → `layer-2-bg`（纸面）、`SectionHeader.background` → `layer-1-bg`（显式固定，不再依赖通配符）、`SectionHeader.foreground` → `text-secondary`、`lightSelectionBackground` → `selection-bg-active-muted`、`tagBackground` → `#E4DBC2`（与 `Tag.background` 一致）。悬停（`transparent-black-10`）与搜索框（`control-bg` → `layer-1-bg-inline`）继承父主题即已正确，未覆盖。

编辑器文件 tab 的选中态：聚焦窗口的 active tab 底色用暖沙 `layer-0-bg`（#EAE2CC，同窗口底色，经 `ui` 块的 `EditorTabs.underlinedTabBackground` 显式覆盖）+ 淡黛蓝晕 `#9FB3CC` 描边；分屏失焦的 active tab 在此基础上浅一纸阶——`tab-selected-bg-inactive` token 改为 `#F1EAD6`（该 token 只有 EditorTabs 一个消费者，改 token 安全），线维持 `#CFC6AC`。

两条硬约束：① `tab-selected-border-active` 唯一的消费者 `EditorTabs.underlinedBorderColor` 画的是选中 tab 芯片的**整圈描边** 而非一条下划线，必须取强调色同族的浅晕（官方 Islands 此处即品牌色浅调 `blue-120` #A7C5FF，对芯片底对比度约 1.7），实心深色会把整个 tab 框成黑圈；② `tab-selected-bg-active` token 保持淡黛青 `#E2E7ED` **不要改成纸色**——它还喂 `SearchEverywhere.Tab.selectedBackground` 和 `TabbedPane.focusColor`，纸色会让 focus 色隐形，这正是 active tab 走 `ui` 显式键而非 token 的原因。

## 色彩系统

### 宣纸背景层次（UI）

| 层 | 色值 | 用途 |
|---|---|---|
| `layer-2-bg` 纸面 | `#F6F0DF` | 编辑器岛、工具窗口岛、弹窗 |
| `layer-1-bg` | `#F1EAD6` | 对话框 |
| `layer-1-bg-inline` | `#F9F4E6` | 输入框、控件 |
| `layer-0-bg` 窗口底 | `#EAE2CC` | 岛屿之间的主窗口底色、工具栏、状态栏 |

### 墨色文字

| 角色 | 色值 |
|---|---|
| 正文 浓墨 | `#3B3A36`（全局无纯黑） |
| 次级 中墨 | `#6B675E` |
| 弱化 | `#5A5548` / `#9B937B` |

### UI 强调色（黛青）

| 角色 | 色值 |
|---|---|
| 强调 黛青 | `#44546B`（按钮、设置页 Tab 下划线、工具栏激活） |
| 选中 淡黛蓝晕 | `#C8D5E3`（树/列表/菜单选中；弱化档 `#D9E2EB`，失焦 `#EAE4D2`） |
| 链接 黛蓝 | `#3D6A78` |
| 错误/警告/成功 | 朱砂 `#B3554A` / 秋香 `#886715` / 苔绿 `#557441`（均降饱和） |

### 编辑器语法色

| 元素 | 色值 | 传统色名 |
|---|---|---|
| 关键字 | `#33568C` | 靛青 |
| 字符串 | `#4A7A3D` | 竹绿 |
| 数字 | `#A85F2E` | 赭石 |
| 函数 | `#2E7093` | 青花 |
| 常量/字段 | `#8E4D82` | 紫棠 |
| 类/接口 | `#2E7D6E` | 青碧 |
| 注解/元数据 | `#96781C` | 秋香 |
| 错误 | `#C13E2F` | 朱砂 |
| 注释（斜体） | `#8F8873` | 淡墨 |

护眼底线：所有语法前景色在纸面上的对比度约 4:1–6.5:1（正文约 9:1），显著低于默认 Light 主题的刺眼原色，但保证语法区分度。约束：语法色饱和度不可再降——与正文墨色明度过近、饱和过低会导致语法区分度不足（当前值已是在此约束下提过一档饱和度的定稿）；语法色只在 `ink-light.theme.xml` 调整，UI 层黛青独立不动。

### 终端 / Console 配色

scheme 不显式定义 `CONSOLE_*` 键时，console/terminal 背景不跟随 `TEXT` 背景，而是继承 `parent_scheme="Default"` 的纯白，ANSI 16 色也是为纯白底设计的经典原色（islands-light 同样缺失整块）。因此显式定义整块 console 配色（对照 `islands-dark.theme.xml` 的做法）。

背景 `CONSOLE_BACKGROUND_KEY` = `#F6F0DF`，与编辑器同纸色——Islands 布局里终端也是一个 island，共用一张「纸」。基础键：普通/系统输出 `#3B3A36` / `#6B675E`，错误输出朱砂 `#C13E2F`，用户输入竹绿 `#4A7A3D` 斜体（JetBrains 惯例），`CONSOLE_RANGE_TO_EXECUTE` 效果色 `#698851`。

ANSI 16 色全部从主题既有墨水色系派生，normal 直接复用语法/语义 token，bright 同色相提亮（括号内为对纸面 `#F6F0DF` 的 WCAG 对比度）：

| ANSI | normal | bright |
|---|---|---|
| black | 墨 `#3B3A36` (10.0) | 灰墨 `#6B675E` (5.0) |
| red | 陶土 `#B3554A` (4.3) | 朱砂 `#C13E2F` (4.6) |
| green | 苔绿 `#557441` (4.7) | 竹绿 `#4A7A3D` (4.5) |
| yellow | 秋香 `#886715` (4.6) | 金黄 `#96781C` (3.7) |
| blue | 靛青 `#33568C` (6.5) | 亮靛 `#4268A6` (4.9) |
| magenta | 紫棠 `#8E4D82` (5.2) | 亮紫 `#A34E9B` (4.5) |
| cyan | 青碧 `#2E7D6E` (4.3) | 亮青 `#2B8170` (4.1) |
| white | 淡墨 `#A79F89` (2.3) | 纸白 `#F9F4E6` (1.0) |

约束：浅色终端里 ANSI white/bright-white 必须取「近底色」——它们更多被 TUI 程序用作反白/背景色（ANSI 背景色共用同一张 16 色表），取深色会让反白场景变泥。除 `#4268A6`、`#A34E9B`、`#2B8170` 三个 bright 值为专调 hex 外，其余全部复用色板既有 token。

### 编辑器 scheme 相比 islands-light.theme.xml 的补充键

- `DEFAULT_BRACES/BRACKETS/PARENTHS/COMMA/SEMICOLON/DOT/OPERATION_SIGN/IDENTIFIER` 统一浓墨（否则继承 Default 的纯黑，与正文冲突）
- `ERRORS_ATTRIBUTES` 波浪线降为朱砂
- `DIFF_INSERTED/DELETED/CONFLICT` 补充纸面和谐色（原文件只有 MODIFIED）
- `CARET_COLOR` 浓墨
- `CONSOLE_*` 整块（背景 + 基础输出 + ANSI 16 色，见上节「终端 / Console 配色」；islands-light 同样缺失，父 scheme Default 的 console 是纯白底）
- Inlay hints（参数名/类型提示芯片，官方 Light/Islands scheme 均未覆盖，否则继承 Default 的冷灰芯片 `#EDEDED`/`#7A7A7A` 加浅蓝当前参数 `#BCDAF7`）：`INLINE_PARAMETER_HINT`/`INLAY_DEFAULT` 底 `#EAE2CC` 字 `#6B675E`（纸→芯片明度差复刻官方 白→#EDEDED，约 4.4:1）、`INLINE_PARAMETER_HINT_HIGHLIGHTED` 底 `#DDD4BB` 字 `#5A5548`、`INLINE_PARAMETER_HINT_CURRENT` 底 `#C7D2D6` 字 `#44546B`（黛青系，与 UI 强调色呼应）、`INLAY_TEXT_WITHOUT_BACKGROUND` 字 `#7E7867`（比斜体注释 `#8F8873` 略深，直立体，两者可区分）

## 控件与 hover 体系

### Checkbox / Radio（icons.ColorPalette）

父主题在 `icons.ColorPalette` 里硬编码官方亮蓝 `#3574F0`，不走语义色板，只能在本主题 ColorPalette 按键覆盖；新 UI 的 Radio SVG 复用同组 `Checkbox.*` 键，一次覆盖两种控件。合并机制与 `colors` 相同（Islands 自己的 ColorPalette 就是按键覆盖 expUI 的 ColorPalette），子主题键必胜。

| 键 | 官方 | 本主题 | 依据 |
|---|---|---|---|
| Background.Selected / Border.Selected | `#3574F0` | `#44546B` | 黛青，与默认按钮一致 |
| Foreground.Selected（勾/圆点） | `#FFFFFF` | `#F6F0DF` | text-over-accent 纸色 |
| Focus.Wide（焦点环） | `#3574F0` | `#44546B` | 见下 |
| Background.Default | `#FFFFFF` | `#F9F4E6` | 与输入框同纸底 |
| Border.Default | `#A8ADBD` | `#B1A88F` | control-border-raised 暖灰 |
| Background.Disabled | `#F7F8FA` | `#F1EAD6` | layer-1-bg |
| Border.Disabled | `#DFE1E5` | `#DDD4BB` | control-border-disabled |
| Foreground.Disabled | `#C9CCD6` | `#C0B79E` | toggle-button-bg 暖灰 |

焦点环 `Focus.Wide` 用实心黛青而非 tab 描边那种淡晕：焦点环是瞬态键盘反馈、可见性优先，官方同样用全强度品牌色，且需与 `*.focusColor`（→ `accent-brand-border`，已解析为黛青）一致。

### 淡墨 hover 体系

语义：**悬停 = 淡墨晕，选中 = 淡黛蓝晕**——笔尖掠过是墨晕，落笔选中才见青。官方 hover 全是纯黑 alpha，行悬停仅 3% 几乎不可见；改为墨色 `#3B3A36` 的 alpha，整套 hover 统一 8% 强度（`colors` 覆盖 4 个 token）：

| token | 官方 | 本主题 | 覆盖面 |
|---|---|---|---|
| `selection-bg-hovered` | `#00000008` (3%) | `#3B3A3614` (8%) | 树/列表/表格/插件列表行悬停 |
| `core-bg-transparent-hovered` | `#00000012` (7%) | `#3B3A3614` (8%) | 工具栏/标题栏图标、编辑器 tab、状态栏、主菜单条等几十个键 |
| `core-bg-transparent-pressed` | `#00000020` (12.5%) | `#3B3A3622` (13%) | 图标按钮按下态 |
| `core-border-transparent` | `#00000020` | `#3B3A3622` | hover 时的工具栏分隔线 |

选中色的辨识度靠**色相**而非明度：低饱和灰青（S≈10–15%）在暖纸上没有色相支撑，取浅（`#DDE3E4`）近乎隐形、压暗（`#C7D2D6`）发灰发脏，两个方向都不成立；官方 blue-140 `#D0DFFE` 明度高达 91% 仍然清爽，正是因为蓝色相明确（S≈96%）。因此选中色定为黛蓝族淡晕，与 tab 描边 `#9FB3CC`、黛青强调同族（H≈211°、S≈33%）：`selection-bg-active` 官方 `#D0DFFE` → `#C8D5E3`（L 84%，树/列表/菜单选中，经 `*.selectionBackground` 通配符生效）、`selection-bg-active-muted` 官方 `#E3EBFE` → `#D9E2EB`（L 89%，插件列表等弱化选中）、`selection-bg-inactive` 失焦选中维持暖沙 `#EAE4D2`。层级：行悬停 8% 墨 < 弱化选中 < 选中。约束：后续调整只动明度（L 84%±2 区间），不要降饱和——灰调无论深浅都会发脏或隐形。

约束：弹出菜单项的高亮键是 `MenuItem.selectionBackground`（菜单无独立 hover 键，官方即以 selection 色作菜单高亮），跟随选中色淡黛蓝晕 `#C8D5E3`——菜单是「即选即用」控件，选中语义正确，也与树选中行呼应。左/下侧工具窗条按钮 `ToolWindow.Button.hoverBackground` 在 expUI 层是不透明冷灰 `#EBECF0`，显式改回引用 `core-bg-transparent-hovered` 跟随本体系。

### 菜单与设置树行距

新 UI 下弹出菜单每项的高度 = `List.rowHeight`（Islands 定为 24）+ `*.Selection.outerInsets` 的上下值（平台编译期默认：PopupMenu 为 `1,7,1,7`，Menu 为 `1,4,1,4`，主题链全程未覆盖），默认行距偏紧。放宽为上下各 2px——`PopupMenu.Selection.outerInsets` = `2,7,2,7`、`Menu.Selection.outerInsets` = `2,4,2,4`（左右维持官方值），每项增高 2px，圆角选中块之间留出空隙。

Settings 左侧导航树不走 Menu/List 体系：`SettingsTreeView` 读专用键 `SettingsTree.rowHeight`（官方元数据注明「0 或未定义 = 节点按渲染器自适应高度」，全主题链未定义），显式定为 27 放宽行距。

以上是本主题仅有的度量（非颜色）改动。约束：不要用 `List.rowHeight` / `Tree.rowHeight` 调菜单或设置树行距——它们同时喂所有列表/树的行高，影响面过大；`*.Selection.innerInsets` 的上下值在新 UI 下被忽略（行高由 rowHeight 决定），只有 outerInsets 计入首选高度。insets 字符串格式为 `上,左,下,右`。

### 继承泄漏修补（ui 显式键，勿删）

完整继承链有**五层**：Ink Light → Islands Light → ExperimentalLightWithLightHeader → Light (expUI) → IntelliJ (intellijlaf)。Islands 的 `*` 通配符（`*.background`、`*.selectionBackground` 等 23 条）兜住多数组件，但两类键会漏：

1. **后缀不在通配符列表内的键**——intellijlaf 的纯白/冷灰直接漏进本主题。非编辑下拉的整个药丸走 `ComboBox.nonEditableBackground`（不走 `ComboBox.background`），普通按钮填充走 `Button.startBackground/ endBackground`，都是 intellijlaf 的 `white`。
2. **底层显式定义、Islands 未重写的键**——继承显式键与子主题通配符谁赢未经源码确认，对可见项一律加显式键，两种机制下都正确。即使某键实测「恰与通配符结果相同」也不要删，它是防御性保险。

| ui 键 | 泄漏值（来源层） | 本主题 |
|---|---|---|
| `ComboBox.nonEditableBackground` | `#FFFFFF`（intellijlaf） | `layer-1-bg-inline` |
| `Button.startBackground` / `endBackground` | `#FFFFFF`（intellijlaf） | `layer-1-bg-inline`（平涂无渐变） |
| `Button.disabledBorderColor`、`Component.disabledBorderColor` | `#D1D1D1`（intellijlaf） | `control-border-disabled` |
| `TextField.disabledBackground` | `#F2F2F2`（intellijlaf） | `layer-1-bg` |
| `CompletionPopup.background` | 淡蓝 `#EBF4FE`（intellijlaf） | `layer-2-bg`（官方=补全与编辑器同底） |
| `CompletionPopup.selectionBackground` / `selectionInactiveBackground` | `#C5DFFC` / `#DFDFDF` | `selection-bg-active` / `selection-bg-inactive` |
| `Menu.separatorColor`、`Popup.separatorColor`、`SearchEverywhere.List.separatorColor` | `#D9D9D9`（intellijlaf） | `layer-2-border` |
| `EditorTabs.borderColor`、`DefaultTabs.borderColor` | `#D1D1D1`（intellijlaf） | `layer-2-border` |
| `Table.alternativeRowBackground` | `#FFFFFF`（intellijlaf） | `layer-1-bg`（斑马纹） |
| `StatusBar.Breadcrumbs.floatingBackground` | `#FFFFFF`（expUI light） | `layer-2-bg` |
| `Popup.inactiveBorderColor` | `#ABABAB`（intellijlaf） | `control-border` |
| `SearchEverywhere.SearchField.borderColor` | `#C4C4C4`（intellijlaf） | `control-border` |
| `ToolWindow.Button.hoverBackground` | `#EBECF0` 不透明（expUI light） | `core-bg-transparent-hovered` |

### Slider / ProgressBar

Slider 三键全部硬编码冷灰：轨道 `#C9CCD6` → `control-bg-small`（与 ProgressBar 轨道同款）、刻度与滑钮边 `#818594` → `accent-neutral-bg` `#867E68`、滑钮 `Slider.buttonColor` 同为 intellijlaf `white` 泄漏 → `layer-1-bg-inline`。

ProgressBar 渐变端点父主题硬编码 ramp 色（`blue-120` 等），ramp token 消费者众多不宜整体覆盖，ui 显式键精准修：不定进度起点 `#A7C5FF` → 淡黛蓝 `#9FB3CC`（与 tab 描边同值取家族一致，刻意不建共享 token 避免语义耦合）、成功端 `#A3CFAE` → `accent-success-border-secondary`、失败端 `#FFB0B2` → `accent-error-border-secondary`。

## 更新通知机制

2.1.0 起插件带有唯一一个源码类 `src/com/github/yx208/eleganttheme/UpdateNotificationActivity.java`（`plugin.xml` 注册 `notificationGroup`「Elegant Theme Updates」+ `postStartupActivity`）：安装或更新后首次打开项目时弹一条驻留通知（STICKY_BALLOON）展示本版要点；应用级 `PropertiesComponent` 记录「上次已通知版本」，每个版本只弹一次，首装与更新的标题措辞不同。

发版约定与硬约束：

- **通知正文 = change-notes 的第一个 `<p>`**。每次发版把版本亮点写在第一个 `<p>`，且其中不放 `<a>` 链接（通知内不可点击）；后续段落不受限。
- 「Full release notes」按钮按版本号拼接 `https://github.com/yx208/elegant-theme/blob/main/_docs/release-<version>.en.md`，发版时英文设计文档必须已在 main 上。
- `since-build` 随本功能由 211 提升为 **223**：源码按项目语言级别 **Java 17** 编译，2022.3 起的 IDE 运行于 JBR 17，可加载 Java 17 字节码（更老的 IDE 运行于 JBR 11，会 `UnsupportedClassVersionError`）。编译需把 Project SDK 设为 IntelliJ Platform Plugin SDK（普通 JDK 没有平台类）。
- 只可使用 2022.3 与最新平台**同时存在**的 API。
