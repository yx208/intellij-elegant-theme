# 2.1.1 设计文档 — Ink Light 语法覆盖补齐（Rust / Python / Go）与三处 UI 微调

## 本版本改动总览

编辑器配色部分是纯补齐：`resources/theme/ink-light.theme.xml` 新增 55 个 attributes 键，**无修改、无删除**。UI 主题 `resources/theme/ink-light.theme.json` 有三处微调：编辑器聚焦时选中 tab 改为选中蓝芯片加中蓝描边，失焦时改为暖沙芯片加淡灰描边而不再几乎消失，工具窗条按钮「已打开但未聚焦」态改用主题选中蓝（见「UI 微调」）。`since-build` 未动；源码只改了一处发版文档的 URL 模板常量（见「发布约定」的文档改名）。

| 分组 | 新增键数 | 补齐前的表现 |
|---|---|---|
| Rust `org.rust.*` | 41 | 此前完全缺失 |
| Python `PY.*` | 9 | 此前完全缺失 |
| Go `GO_*` | 3 | 结构体成员与 `GO_SYNTAX_UPDATE` 三键缺失（其余 16 键 2.1.0 已有） |
| 平台默认 `DEFAULT_REASSIGNED_*` | 2 | 此前缺失，影响所有语言 |

scheme 的 `parent_scheme` 是 `Default`：任何未显式定义的键都落回平台/语言插件为纯白底设计的默认高饱和配色，在宣纸底上既刺眼又与其余语法色不成体系（与 2.1.0 里补 `DEFAULT_BRACES`、`CONSOLE_*`、Inlay hints 是同一类问题）。本版把 Rust / Python / Go 三门语言拉回纸墨体系。

## 取色原则

**编辑器 scheme 不引入任何新色。** 55 个新键用到 14 个 hex，全部是 `ink-light.theme.xml` 中已经存在的值，因此 2.1.0 定下的护眼底线（语法前景色对纸面 `#F6F0DF` 约 4:1–6.5:1，正文约 9:1）无需重新校验。UI JSON 唯一的新值是 tab 描边的中蓝 `#4F74A8`，其余 UI 改动都复用色板里已有的值（见「UI 微调」）。

复用的色值与角色：

| 色值 | 名称 | 该色在本 scheme 中的既有角色 |
|---|---|---|
| `#33568C` | 靛青 | 关键字（`DEFAULT_KEYWORD`） |
| `#2E7093` | 青花 | 函数（`DEFAULT_FUNCTION_DECLARATION`） |
| `#2E7D6E` | 青碧 | 类/接口（`DEFAULT_CLASS_NAME`） |
| `#8E4D82` | 紫棠 | 字段/常量（`DEFAULT_INSTANCE_FIELD` / `DEFAULT_CONSTANT`） |
| `#A85F2E` | 赭石 | 数字 |
| `#96781C` | 秋香 | 注解/元数据 |
| `#C13E2F` | 朱砂 | 错误 |
| `#B3554A` | 陶土 | 错误文案（ANSI red normal） |
| `#886715` | 秋香暗调 | 警告（ANSI yellow normal） |
| `#3D6A78` | 黛蓝 | 链接 |
| `#77882E` | 柳绿 | `GO_SHADOWING_VARIABLE`（「需留意」的标识符） |
| `#6B675E` | 中墨 | 次级文字 |
| `#A79F89` | 淡墨 | 灰化文字（面包屑非激活、ANSI white normal） |
| `#F2DCD3` | 浅陶土底 | `ERROR_HINT`、`DEFAULT_INVALID_STRING_ESCAPE` 背景 |

三条跨语言映射规则（新增键一律按此推导，后续补别的语言沿用）：

1. **语义对齐平台默认键**：类型 → 青碧，函数 → 青花，字段/成员 → 紫棠，关键字与 `self` → 靛青。同一角色在不同语言里是同一个色，换语言不换色感。
2. **「间接 / 受约束」的项加斜体而不换色**：trait 方法、关联函数、`self`、枚举变体、静态项、生命周期。与既有 `DEFAULT_INTERFACE_NAME`、`DEFAULT_CONSTANT` 的斜体约定一致。
3. **只做标记、不抢前景色的项用效果**：重新赋值的变量、`mut` 绑定用中墨下划线，次要标记用青花波浪线——不设 `FOREGROUND`，标识符本身的语义色保持不变。

## Rust（`org.rust.*`，41 键）

| 语义组 | 键 | 取色 |
|---|---|---|
| 类型 | `ENUM`、`STRUCT`、`UNION`、`TYPE_ALIAS`、`TYPE_PARAMETER` | 青碧 `#2E7D6E` |
| trait | `TRAIT` | 青碧 `#2E7D6E` + 斜体 |
| 函数/方法 | `FUNCTION`、`FUNCTION_CALL`、`METHOD`、`METHOD_CALL` | 青花 `#2E7093` |
| 关联/trait 函数 | `ASSOC_FUNCTION`(`_CALL`)、`ASSOC_TRAIT_FUNCTION`(`_CALL`)、`TRAIT_METHOD`(`_CALL`) | 青花 `#2E7093` + 斜体 |
| 值 | `ENUM_VARIANT`、`STATIC` | 紫棠 `#8E4D82` + 斜体 |
| 宏与 crate | `MACRO`、`CRATE` | 秋香 `#96781C` |
| `self` 族 | `SELF_PARAMETER`、`SELF_EXPRESSION` | 靛青 `#33568C` + 斜体 |
| `&mut self` | `MUT_SELF_PARAMETER` | 靛青 + 斜体 + 中墨下划线 |
| `?` 运算符 | `Q_OPERATOR` | 靛青 `#33568C` + 粗体 |
| 生命周期 | `LIFETIME` | 赭石 `#A85F2E` + 斜体 |
| 格式串 | `FORMAT_PARAMETER`、`FORMAT_SPECIFIER` | 青花 `#2E7093` |
| unsafe | `KEYWORD_UNSAFE` | 朱砂 `#C13E2F` |
| unsafe 块 | `UNSAFE_CODE` | 背景 `#F2DCD3`，无前景色 |
| 可变绑定 | `MUT_BINDING` | 中墨 `#6B675E` 下划线，无前景色 |
| 生成项 | `GENERATED_ITEM` | 柳绿 `#77882E` |
| 灰化 | `CFG_DISABLED_CODE`、`DIMMED_TEXT` | 淡墨 `#A79F89` |
| 文档 | `DOC_CODE` | 中墨 `#6B675E` |
| 文档字型 | `DOC_EMPHASIS` / `DOC_STRONG` | 只给斜体 / 粗体，不给色 |
| 内联诊断 | `INLINE_ERROR_DESCRIPTION` / `INLINE_WARNING_DESCRIPTION` / `INLINE_EXPLANATION` | 陶土 `#B3554A` / 秋香暗调 `#886715` / 黛蓝 `#3D6A78`，均斜体 |
| 次要标记 | `SECONDARY_SPOT` | 青花 `#2E7093` 波浪线 |
| 参数 | `PARAMETER` | 显式置空（见「维护须知」） |

`?` 是控制流而非标识符，用关键字色加粗，让它在行尾可见；`'a` 是标注而非标识符，归入赭石（字面量族）而非任何标识符色；`unsafe` 块只给背景不给前景色，否则整块内的语法色会被压掉。

## Python（`PY.*`，9 键）

| 键 | 取色 | 说明 |
|---|---|---|
| `PY.BUILTIN_NAME` | 青花 `#2E7093` | 内置名归函数族 |
| `PY.SELF_PARAMETER` | 靛青 `#33568C` + 斜体 | 与 Rust `self` 同款 |
| `PY.TYPE_PARAMETER` | 青碧 `#2E7D6E` | 类型族 |
| `PY.KEYWORD_ARGUMENT` | 中墨 `#6B675E` | 关键字实参名弱化，不与被调函数抢焦点 |
| `PY.FSTRING_FRAGMENT_BRACES`、`PY.FSTRING_FRAGMENT_COLON`、`PY.FSTRING_FRAGMENT_TYPE_CONVERSION`、`PY.FSTRING_FORMAT_SPEC_NUMBER`、`PY.FSTRING_FORMAT_SPEC_SPECIAL_CHAR` | 青花 `#2E7093` | f-string 的结构符号从竹绿字符串底色里跳出来，与 Rust `FORMAT_*` 同色 |

## Go（`GO_*`，3 键）

| 键 | 取色 | 说明 |
|---|---|---|
| `GO_STRUCT_EXPORTED_MEMBER`、`GO_STRUCT_LOCAL_MEMBER` | 紫棠 `#8E4D82` | 结构体成员 = 字段，与 `DEFAULT_INSTANCE_FIELD` 同色；导出与否不分色（Go 的导出性已由首字母大小写表达） |
| `GO_SYNTAX_UPDATE` | 青花 `#2E7093` 波浪线 | 提示性标记，不占前景色 |

## 平台默认键（2 键，全语言生效）

`DEFAULT_REASSIGNED_LOCAL_VARIABLE`、`DEFAULT_REASSIGNED_PARAMETER`：中墨 `#6B675E` 下划线，**不设前景色**。重新赋值是「关于变量的元信息」，用装饰表达；设前景色会覆盖 Java/Kotlin/JS 等语言各自的局部变量色。Rust 的 `MUT_BINDING`、`MUT_SELF_PARAMETER` 用同一款下划线，语义一致。

## UI 微调（`ink-light.theme.json`）

### 编辑器聚焦时的选中 tab：描边 `#9FB3CC` → `#4F74A8`，芯片改为选中蓝

色板 token `tab-selected-border-active` 从淡黛蓝 `#9FB3CC` 改为中蓝 `#4F74A8`。该 token 的唯一消费者是 `EditorTabs.underlinedBorderColor`，Islands 的 tab 画法（`IslandsTabPainter`）用它描选中 tab 芯片的**整圈轮廓**。旧值对 2.1.0 的暖沙芯片 `#EAE2CC` 对比度仅 1.66，在暖纸上读作发灰的浅蓝；新值 H215° S36% L48%，与强调色黛青 `#44546B` 同色相，对纸面 4.2:1，看得出是蓝，又不至于把 tab 框成深圈。芯片底同步换系：`ui` 键 `EditorTabs.underlinedTabBackground` 从 `layer-0-bg` 暖沙改指 `selection-bg-active-muted` `#D9E2EB`，即树/列表选中蓝再浅一阶的弱化选中色，芯片成为描边的淡化，而不再是蓝圈套沙底；对纸面 1.15:1，描边对芯片 3.7:1，正文墨色对芯片约 8.4:1。`tab-selected-bg-active` 未动（它还喂 SearchEverywhere 与 TabbedPane 焦点色），失焦 tab 的处理见下一小节；`ProgressBar.indeterminateStartColor` 仍是 `#9FB3CC`，2.1.0 刻意与 tab 描边解耦，此处不跟改。

### 编辑器失焦时的选中 tab：暖沙芯片 + 中性灰描边

Islands 的 tab「active / inactive」由**编辑器是否持有键盘焦点**决定（`EditorTabs.checkActive()` 取 `UIUtil.isFocusAncestor`），焦点一到工具窗，选中 tab 就切到 `EditorTabs.inactiveUnderlinedTabBackground` + `inactiveUnderlinedTabBorderColor`。2.1.0 把这一态设成浅纸 `#F1EAD6` + 沙色 `#CFC6AC`，对 tab 栏底 `#F6F0DF` 只有约 1.02:1 与 1.3:1，实际效果是只要在 Project、Terminal 里操作，编辑器的当前 tab 就近乎消失。本版把两个 token 改为：`tab-selected-bg-inactive` `#F1EAD6` → `#EAE2CC`（`layer-0-bg` 的值，与灰描边同为 H44 暖色系，明度 L86% 与聚焦态芯片的 L89% 相近），`tab-selected-border-inactive` `#CFC6AC` → `#B1A88F`（`control-border-raised` 的值，暖灰，对芯片底约 1.8:1）。于是两态遵循同一规则「芯片底是描边的淡化」：蓝底蓝圈表示编辑器持有焦点，沙底淡灰圈表示焦点在别处；失焦态刻意比聚焦态弱一档，靠芯片对纸底的暖沙差与淡灰圈共同定形，不与聚焦态争抢注意力。两个 token 在父主题里各自只有对应的 EditorTabs 键一个消费者，改 token 安全；这取代了 2.1.0「失焦 active tab 浅一纸阶」的设计。

### 工具窗条按钮「已打开但未聚焦」态：改用主题选中蓝

Islands 的条按钮（`SquareStripeButtonLook`）分三态：工具窗**聚焦**时用 `ToolWindow.Button.selectedBackground`（黛青 `#44546B`）配 `selectedForeground` 描边图标；工具窗**已打开但未聚焦**时走的是通用 ActionButton 的 pressed 态，即 `ActionButton.pressedBackground` + `ActionButton.pressedBorderColor`，图标保持常规墨色；悬停走 hover 键。2.1.0 把 pressed 底设为 `toolbar-selected-bg` `#DDE3E4`、描边设为 `accent-brand-border-secondary` `#C7D2D6`，两者都是 S 11–15% 的低饱和灰青，正是 2.1.0 已经指出「没有色相支撑、近乎隐形」的那一档。本版把 `ActionButton.pressedBackground` 改指 `selection-bg-active` `#C8D5E3`（树/列表选中同款），`ActionButton.pressedBorderColor` 改为 `#9FB3CC`（同族略深，与 `ProgressBar.indeterminateStartColor` 同值，按 2.1.0 惯例直接写值、不建共享 token）。

影响范围：`ActionButton.pressedBackground` 是所有工具栏「开启态」切换按钮共用的键（Structure 的排序开关、编辑器 Soft-Wrap 等），它们与条按钮一起变成选中蓝芯片。`toolbar-selected-bg` 与 `toolbar-selected-bg-hovered` 两个 token 本身未动，仍喂 find 栏的 `SearchOption.*` 与 `SegmentedButton.focusedSelectedButtonColor`，那里目前还是 `#DDE3E4`，留作后续统一。

## 硬约束与维护须知

- **`org.rust.PARAMETER` 的空 `<value />` 不要删**。空值表示「显式不着色」，参数名走正文浓墨；删掉该键会落回 `parent_scheme="Default"` 里 Rust 插件自带的参数样式。
- **`org.rust.UNSAFE_CODE` 只能有 `BACKGROUND`**。它整块覆盖 unsafe 区域，加 `FOREGROUND` 会把块内所有语法色压平。底色与 `ERROR_HINT`、`DEFAULT_INVALID_STRING_ESCAPE` 共用 `#F2DCD3`，改一处要同步考虑另两处。
- **效果编码**：`EFFECT_TYPE` 1 = 下划线，2 = 波浪线；`FONT_TYPE` 1 = 粗体，2 = 斜体，3 = 粗斜体。
- **键的排列保持字母序**（IDE 导出即为此序，`org.rust.*` 因小写排在最后）。手工补键时插入到正确位置，避免下次从 IDE 导出时产生大片假 diff。
- **本版只补了 ink-light**。其它 scheme 的覆盖度：`elegant-dark` 有 28 个 Rust / 5 个 Python 键，`islands-light` 有 16 个 Go 键，`islands-dark` 两者皆无。若后续统一，按上面的语义映射表在各自色板内取同角色的色，**不要跨主题直接搬 hex**（深色主题的对比度关系不同）。
- 新增语言覆盖时沿用「取色原则」的三条规则，优先复用既有色值；确需新色，必须先校验对纸面 `#F6F0DF` 的对比度落在 4:1–6.5:1，且饱和度不可低于现有语法色（2.1.0 已验证再降会与墨色正文混淆）。
- **`tab-selected-border-active` 只能在 H≈215° 蓝族内取 L 48%–60%（对芯片底 `#D9E2EB` 约 2.5:1–3.7:1）**。它描的是选中 tab 芯片的整圈轮廓：更浅会退回 2.1.0 发灰的状态，更深（如 `#33568C` 约 5.6:1）会把整个 tab 框成深圈。本条取代 2.1.0 维护须知里「必须取强调色同族的浅晕」的表述。
- **聚焦态芯片 `EditorTabs.underlinedTabBackground` 必须与描边同色系且明显更浅**，当前指 `selection-bg-active-muted`；要调只动该 token 的明度（L 89%±2，与 2.1.0 对选中色「只动明度、不降饱和」的约束一致），不要改回暖沙或换成不带蓝的颜色，否则又成「蓝圈套沙底」。它走 `ui` 显式键而非 `tab-selected-bg-active` token，因为后者还喂 SearchEverywhere 与 TabbedPane 焦点色。
- **失焦 tab 描边 `tab-selected-border-inactive` 保持不带蓝的暖灰，对芯片底约 1.8:1（可接受区间 1.6:1–2.2:1）**。它与蓝圈的分工是「色相表示焦点、分量表示主次」：改成带蓝的颜色两态就分不开；压到 3:1 左右会与聚焦态争抢注意力；改浅回 2.1.0 的 `#CFC6AC` 则失焦时当前 tab 再次消失。`tab-selected-bg-inactive` 保持 `layer-0-bg` 的值：它与灰描边同色系、明度与聚焦态芯片相当，焦点切换时 tab 只换色系、分量不跳。
- **`ActionButton.pressedBackground` 不能设成深色**。它同时服务条按钮的「已打开但未聚焦」态与全部工具栏开启态按钮，这些状态下图标不会换成浅色，深底会把图标吞掉；要改就在选中蓝族 L 84%±2 内动明度（与 `selection-bg-active` 的约束一致）。
- **`#9FB3CC` 现在出现在两个 `ui` 键里**（`ProgressBar.indeterminateStartColor`、`ActionButton.pressedBorderColor`），都是刻意直接写值：两者语义无关，不要为此建共享 token。

## 发布约定

- `plugin.xml`：`<version>` 2.1.0 → 2.1.1；`<change-notes>` 第一个 `<p>` 是更新通知的正文，段内不放 `<a>` 链接。
- 通知的「Full release notes」按钮按版本号拼接指向 `_docs/release-2.1.1.en.md`，发版时该文件必须已在 main 上。
- 文档命名自本版起由 `_docs/design-<version>` 改为 `_docs/release-<version>`（「design」名不副实：这些文件是发版记录，改动清单与硬约束的分量大于设计理念）。2.1.0 的两份文件一并改名，`UpdateNotificationActivity` 的 URL 模板同步更新为 `_docs/release-%s.en.md`。旧路径未保留跳转，已安装 2.1.0 的客户端点「Full release notes」会 404——这是明确接受的取舍。
- 除上述 URL 模板外无源码改动，`since-build` 维持 223。
