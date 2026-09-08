# 2.1.0 Design Doc — Elegant Ink Light (Ink-Wash Rice-Paper Theme)

## Design philosophy

This release adds **Elegant Ink Light (Beta)**, an eye-friendly ink-wash style light theme. The guiding image is "mineral pigments painted on rice paper (宣纸)":

- **Rice paper as the ground**: the backgrounds simulate pale-yellow rice paper. In the Islands layout every island (editor, tool windows, terminal) is a sheet of paper `#F6F0DF`, with the deeper warm-sand window base `#EAE2CC` showing between islands; dialogs and input controls take the paper steps in between — four rice-paper layers in total.
- **Ink as the text**: no pure black or pure white anywhere. Body text is dense ink `#3B3A36` (≈9:1 against the paper), secondary text medium ink `#6B675E`, comments pale-ink italic `#8F8873`.
- **Traditional colors as the pigments**: syntax highlighting and the terminal ANSI palette are drawn entirely from low-saturation traditional Chinese colors — 靛青 indigo, 竹绿 bamboo green, 赭石 ochre, 青花 porcelain blue, 紫棠 plum, 青碧 teal jade, 秋香 golden khaki, 朱砂 vermilion — held to 4:1–6.5:1 contrast against the paper: clearly softer than the default Light theme's saturated primaries while keeping syntax distinguishable.
- **Interaction as brushwork**: hover = pale ink wash (the brush tip grazing the paper), selection = pale 黛蓝 wash (`#C8D5E3`, the committed stroke); a single 黛青 indigo-slate UI accent `#44546B` (buttons, tab underline, focus ring, checkboxes).
- **Colors only, shape untouched**: based on JetBrains' official Islands Light — only colors are overridden, never layout or component behavior (the single metric exception is menu and settings-tree row spacing, see "Menu & settings-tree row spacing"). Implementation prefers overriding the parent theme's semantic tokens over per-key hardcoding, so everything not overridden keeps tracking upstream Islands updates; hardcoded keys that bypass the palette and lower-level inheritance leaks are patched point by point.

## What this release actually changed

The changes that landed in 2.1.0 (details in the sections below):

1. **New theme pair**: `ink-light.theme.json` (UI theme) + `ink-light.theme.xml` (editor scheme), registered in `plugin.xml` as Elegant Ink Light.
2. **Rice-paper backgrounds + ink text system**: 70 semantic palette tokens of Islands Light overridden — four paper layers, three ink levels.
3. **黛青 UI accent system**: buttons / tab underline / active toolbar in 黛青, tree/list selection as a pale 黛蓝 wash, links in 黛蓝, and error/warning/success desaturated to vermilion / golden khaki / moss.
4. **Editor syntax colors**: eight traditional-color syntax families; punctuation and identifiers unified to dense ink, error squiggle softened to vermilion, diff colors filled in to harmonize with the paper, caret in dense ink.
5. **Full terminal / console recolor**: paper background + an ink-derived ANSI 16-color ramp (the scheme previously defined no `CONSOLE_*` keys, so the terminal inherited Default's pure-white base).
6. **Warm-ink inlay hint chips**: parameter/type hints moved from cool gray to paper-family chips, with the current parameter in the 黛青 family.
7. **Editor file tab selected state**: warm-sand fill with a pale 黛蓝 halo border; the active tab of an unfocused split goes one paper step lighter.
8. **Checkbox / Radio fully recolored**: the official bright blue overridden to 黛青 + paper via `icons.ColorPalette`.
9. **Ink-wash hover system**: 4 translucent-black alpha tokens re-tinted to ink and strengthened one notch.
10. **Plugin manager page follows the paper palette**: fixed the patchwork of pure-white rows against cream section headers.
11. **Inheritance-leak fixes**: 13 explicit ui key groups suppressing the pure white / cold gray leaking up the five-level inheritance chain (non-editable combo, button fills, completion popup, separators, table zebra rows, …).
12. **Slider / ProgressBar warmed**: cold-gray tracks/thumbs and gradient endpoints re-tinted warm.
13. **Menu & settings-tree row spacing relaxed**: the top/bottom values of `PopupMenu.Selection.outerInsets` / `Menu.Selection.outerInsets` go 1px → 2px (each popup menu item gains 2px); the Settings navigation tree gets an explicit `SettingsTree.rowHeight` = 27 (undefined by default — renderer-sized and cramped). The theme's only metric changes.
14. **Update notification mechanism**: adds the plugin's single source class; the first project open after an install/update shows a one-time release-highlights notification, once per version (see "Update notification mechanism"). The minimum supported IDE accordingly moves from 2021.1 (211) to 2022.3 (223).

## Structure

| File | Purpose |
|---|---|
| `resources/theme/ink-light.theme.json` | UI theme, `parentTheme: "Islands Light"` |
| `resources/theme/ink-light.theme.xml` | Editor scheme, `parent_scheme="Default"`, recolored key-for-key against `islands-light.theme.xml` |
| `plugin.xml` | Registers the `elegant-ink-light` themeProvider + `ElegantInkLight` colorScheme, plus the update notification's `notificationGroup` + `postStartupActivity` |
| `src/com/github/yx208/eleganttheme/UpdateNotificationActivity.java` | Update-notification startup activity, the plugin's only source file (see "Update notification mechanism") |

## Core mechanism: overriding the parent theme's semantic palette

The official Islands Light theme (`themes/islands/ManyIslandsLight.theme.json` inside `intellij.platform.ide.impl.jar`) routes all of its UI mappings through semantic token reference chains, e.g.:

```
ui:     MainWindow.background → main-window-bg → layer-0-bg → gray-150
```

When the platform loads a child theme (`importFromParentTheme()` in `UIThemeBean.kt`), it merges both `colors` maps — **child entries win on name collisions** — and the `ui` mappings inherited from the parent resolve against the merged palette. So this theme's JSON only needs to override semantic tokens (`layer-2-bg`, `accent-brand-bg`, …): hundreds of inherited ui mappings turn warm automatically, while everything not overridden (gradients, badges, component behavior) keeps the official defaults and tracks upstream Islands updates for free.

**Important (do not delete)**: the tokens in `colors` are never referenced by this file's own `ui` block — their consumers live in the parent theme. The DevKit "unused color" inspection only scans this file and flags all of them as unused; do not delete based on that inspection. The 70 tokens currently kept (including the 4 ink-wash hover tokens, see "Controls & hover system") were each verified to have a real consumer chain (direct parent-ui reference, or transitive reference through other palette tokens). The following 9 tokens were confirmed to have **no** consumer in Islands Light and were removed — do not re-add them: `layer-0-bg-inline`, `layer-0-border-inline`, `layer-1-border`, `control-bg-small-disabled`, `control-border-small`, `toolbar-run-bg-hovered`, `toolbar-stop-bg-hovered`, `icon-green-stroke`, `transparent`.

A few keys that bypass the palette as hardcoded hex in the parent (`FileColor.*`, `Tag.background`, `Editor.ToolTip.selectionBackground`) are overridden explicitly in the `ui` block. The `ToolWindow`/`Tree`/`Island` key groups reuse the settings shared by this repo's Elegant Islands family.

The plugin manager page (Settings → Plugins) is another spot that bypasses the semantic palette: the parent maps `Plugins.background` to the literal color name `white` (#FFFFFF), while `Plugins.SectionHeader.background` is undefined and falls through the wildcard `*.background` → `dialog-bg` → `layer-1-bg` (cream in this theme), producing a patchwork of pure-white rows against cream section headers. The `ui` block therefore overrides the `Plugins` key group explicitly: `background` → `layer-2-bg` (paper), `SectionHeader.background` → `layer-1-bg` (pinned explicitly instead of relying on the wildcard), `SectionHeader.foreground` → `text-secondary`, `lightSelectionBackground` → `selection-bg-active-muted`, and `tagBackground` → `#E4DBC2` (matching `Tag.background`). Hover (`transparent-black-10`) and the search field (`control-bg` → `layer-1-bg-inline`) inherit correctly from the parent and are left untouched.

Editor file tabs, selected state: the focused window's active tab fills with warm sand `layer-0-bg` (#EAE2CC, the window-base tone, set via the explicit `EditorTabs.underlinedTabBackground` override in the `ui` block) inside a pale 黛蓝 halo border `#9FB3CC`; the active tab in an unfocused split goes one paper step lighter — the `tab-selected-bg-inactive` token becomes `#F1EAD6` (safe to change at token level: EditorTabs is its only consumer) — with the `#CFC6AC` border kept.

Two hard constraints: (1) the only consumer of `tab-selected-border-active` is `EditorTabs.underlinedBorderColor`, which draws the **full border around the selected tab chip**, not a single underline — it must stay a pale halo of the accent family (the official Islands value there is the brand tint `blue-120` #A7C5FF, ~1.7 contrast against its chip base); a solid dark value rings the whole tab in a near-black frame. (2) The `tab-selected-bg-active` token stays pale 黛青 `#E2E7ED` — **never make it paper-colored** — it still feeds `SearchEverywhere.Tab.selectedBackground` and `TabbedPane.focusColor`, where a paper tone would make the focus tint invisible; that is exactly why the active tab goes through an explicit `ui` key instead of the token.

## Color system

### Rice-paper background layers (UI)

| Layer | Value | Used for |
|---|---|---|
| `layer-2-bg` paper surface | `#F6F0DF` | Editor island, tool-window islands, popups |
| `layer-1-bg` | `#F1EAD6` | Dialogs |
| `layer-1-bg-inline` | `#F9F4E6` | Text fields, controls |
| `layer-0-bg` window base | `#EAE2CC` | Main-window background between islands, toolbar, status bar |

### Ink text

| Role | Value |
|---|---|
| Body, dense ink (浓墨) | `#3B3A36` (no pure black anywhere) |
| Secondary, medium ink (中墨) | `#6B675E` |
| Muted / disabled | `#5A5548` / `#9B937B` |

### UI accent (黛青)

| Role | Value |
|---|---|
| Accent, 黛青 (indigo-slate) | `#44546B` (buttons, settings tab underline, active toolbar) |
| Selection, pale 黛蓝 wash | `#C8D5E3` (tree/list/menu selection; muted step `#D9E2EB`, unfocused `#EAE4D2`) |
| Links, 黛蓝 | `#3D6A78` |
| Error / warning / success | 朱砂 `#B3554A` / 秋香 `#886715` / 苔绿 `#557441` (all desaturated) |

### Editor syntax colors

| Element | Value | Traditional color |
|---|---|---|
| Keyword | `#33568C` | 靛青 indigo |
| String | `#4A7A3D` | 竹绿 bamboo green |
| Number | `#A85F2E` | 赭石 ochre |
| Function | `#2E7093` | 青花 porcelain blue |
| Constant/field | `#8E4D82` | 紫棠 plum purple |
| Class/interface | `#2E7D6E` | 青碧 teal jade |
| Annotation/metadata | `#96781C` | 秋香 golden khaki |
| Error | `#C13E2F` | 朱砂 vermilion |
| Comment (italic) | `#8F8873` | 淡墨 pale ink |

Eye-comfort baseline: all syntax foregrounds sit at roughly 4:1–6.5:1 contrast against the paper surface (body text ≈ 9:1) — clearly softer than the default Light theme's harsh primaries, while keeping syntax distinguishable. Constraint: do not lower the syntax saturation any further — values too close to the ink body text lose syntax distinguishability (the current values are the finalized one-notch-raised set under this constraint). Syntax colors are tuned only in `ink-light.theme.xml`; the UI-level 黛青 accent is independent.

### Terminal / console colors

When a scheme defines no `CONSOLE_*` keys, the console/terminal background does not follow the `TEXT` background but inherits `parent_scheme="Default"`'s pure white, and the ANSI 16 colors are the classic primaries designed for a white base (islands-light is missing the whole block too). The full console block is therefore defined explicitly (mirroring the approach in `islands-dark.theme.xml`).

Background `CONSOLE_BACKGROUND_KEY` = `#F6F0DF`, the same paper tone as the editor — in the Islands layout the terminal is an island too, sharing one sheet of "paper". Base keys: normal/system output `#3B3A36` / `#6B675E`, error output vermilion `#C13E2F`, user input bamboo green `#4A7A3D` italic (JetBrains convention), `CONSOLE_RANGE_TO_EXECUTE` effect `#698851`.

All 16 ANSI colors derive from the theme's existing ink palette: the normal set reuses syntax/semantic tokens directly, the bright set lightens within the same hue (WCAG contrast against the paper `#F6F0DF` in parentheses):

| ANSI | normal | bright |
|---|---|---|
| black | ink `#3B3A36` (10.0) | gray ink `#6B675E` (5.0) |
| red | terracotta `#B3554A` (4.3) | vermilion `#C13E2F` (4.6) |
| green | moss `#557441` (4.7) | bamboo `#4A7A3D` (4.5) |
| yellow | golden khaki `#886715` (4.6) | gold `#96781C` (3.7) |
| blue | indigo `#33568C` (6.5) | bright indigo `#4268A6` (4.9) |
| magenta | plum `#8E4D82` (5.2) | bright plum `#A34E9B` (4.5) |
| cyan | teal jade `#2E7D6E` (4.3) | bright jade `#2B8170` (4.1) |
| white | pale ink `#A79F89` (2.3) | paper white `#F9F4E6` (1.0) |

Constraint: on a light terminal, ANSI white/bright-white must stay near the base color — TUI programs mostly use them as reverse-video/background colors (ANSI background colors share the same 16-color table), and a dark value would turn those cases muddy. Apart from the three purpose-tuned hex values `#4268A6`, `#A34E9B`, and `#2B8170`, everything reuses existing palette tokens.

### Editor scheme additions relative to islands-light.theme.xml

- `DEFAULT_BRACES/BRACKETS/PARENTHS/COMMA/SEMICOLON/DOT/OPERATION_SIGN/IDENTIFIER` unified to dense ink (otherwise they inherit pure black from Default and clash with body text)
- `ERRORS_ATTRIBUTES` error squiggle softened to vermilion
- `DIFF_INSERTED/DELETED/CONFLICT` given paper-harmonious backgrounds (the original file only defined MODIFIED)
- `CARET_COLOR` dense ink
- The full `CONSOLE_*` block (background + base outputs + ANSI 16 colors, see "Terminal / console colors" above; islands-light is missing it too — the parent scheme Default's console is a pure-white base)
- Inlay hints (parameter/type hint chips — neither the official Light nor Islands scheme covers these, so they otherwise inherit Default's cool-gray chips `#EDEDED`/`#7A7A7A` plus the light-blue current parameter `#BCDAF7`): `INLINE_PARAMETER_HINT`/`INLAY_DEFAULT` bg `#EAE2CC` fg `#6B675E` (paper→chip luminance step mirrors the official white→#EDEDED, ~4.4:1), `INLINE_PARAMETER_HINT_HIGHLIGHTED` bg `#DDD4BB` fg `#5A5548`, `INLINE_PARAMETER_HINT_CURRENT` bg `#C7D2D6` fg `#44546B` (the 黛青 accent family, echoing the UI accent), `INLAY_TEXT_WITHOUT_BACKGROUND` fg `#7E7867` (slightly darker than the italic comment `#8F8873`, upright, so the two stay distinguishable)

## Controls & hover system

### Checkbox / Radio (icons.ColorPalette)

The parent hardcodes the official bright blue `#3574F0` in `icons.ColorPalette`, bypassing the semantic palette entirely, so the only fix is per-key overrides in this theme's ColorPalette. The new-UI Radio SVGs reuse the same `Checkbox.*` palette keys, so one set covers both controls. The merge mechanism matches `colors` (Islands itself overrides expUI's ColorPalette per key), so child keys always win.

| Key | Official | This theme | Rationale |
|---|---|---|---|
| Background.Selected / Border.Selected | `#3574F0` | `#44546B` | 黛青, matches default buttons |
| Foreground.Selected (checkmark/dot) | `#FFFFFF` | `#F6F0DF` | text-over-accent paper |
| Focus.Wide (focus ring) | `#3574F0` | `#44546B` | see below |
| Background.Default | `#FFFFFF` | `#F9F4E6` | same paper base as inputs |
| Border.Default | `#A8ADBD` | `#B1A88F` | control-border-raised warm gray |
| Background.Disabled | `#F7F8FA` | `#F1EAD6` | layer-1-bg |
| Border.Disabled | `#DFE1E5` | `#DDD4BB` | control-border-disabled |
| Foreground.Disabled | `#C9CCD6` | `#C0B79E` | toggle-button-bg warm gray |

The `Focus.Wide` ring uses solid 黛青 rather than the pale halo used for tab borders: a focus ring is transient keyboard feedback where visibility wins, the official theme likewise uses the full-strength brand color, and it must match `*.focusColor` (→ `accent-brand-border`, which already resolves to 黛青).

### Ink-wash hover system

Semantics: **hover = pale ink wash, selection = pale 黛蓝 wash** — the brush tip grazing the paper leaves an ink shadow; only committing the stroke shows the blue. The official hovers are all pure-black alphas, with row hover at a barely visible 3%; they are re-tinted to ink `#3B3A36` alpha with the whole hover system unified at 8% strength (4 token overrides in `colors`):

| Token | Official | This theme | Coverage |
|---|---|---|---|
| `selection-bg-hovered` | `#00000008` (3%) | `#3B3A3614` (8%) | tree/list/table/plugin-list row hover |
| `core-bg-transparent-hovered` | `#00000012` (7%) | `#3B3A3614` (8%) | toolbar/title-bar icons, editor tabs, status bar, main menu bar — dozens of keys |
| `core-bg-transparent-pressed` | `#00000020` (12.5%) | `#3B3A3622` (13%) | icon button pressed state |
| `core-border-transparent` | `#00000020` | `#3B3A3622` | toolbar separators on hover |

The selection color gets its distinguishability from **hue, not lightness**: a low-saturation gray-cyan (S≈10–15%) has no hue support on warm paper — taken light (`#DDE3E4`) it is nearly invisible, darkened (`#C7D2D6`) it turns gray and dirty; neither direction works. The official blue-140 `#D0DFFE` stays crisp at 91% lightness precisely because its blue hue is unambiguous (S≈96%). The selection is therefore a pale wash of the 黛蓝 family, sharing the tab-border halo `#9FB3CC` and the 黛青 accent's hue (H≈211°, S≈33%): `selection-bg-active` official `#D0DFFE` → `#C8D5E3` (L 84%, tree/list/menu selection, applied through the `*.selectionBackground` wildcard), `selection-bg-active-muted` official `#E3EBFE` → `#D9E2EB` (L 89%, muted selection, e.g. plugin list), `selection-bg-inactive` unfocused selection stays warm sand `#EAE4D2`. Hierarchy: row hover 8% ink < muted selection < selection. Constraint: when tuning later, move lightness only (within L 84%±2) and never drop the saturation — a gray tone reads dirty or invisible at any lightness.

Constraint: popup menu items highlight via `MenuItem.selectionBackground` (menus have no separate hover key; the official theme also uses the selection color as the menu highlight), which follows the selection's pale 黛蓝 `#C8D5E3` — a menu is a "choose now" control, so selection semantics are correct and echo tree selection. The tool-window stripe buttons' `ToolWindow.Button.hoverBackground` is an opaque cold gray `#EBECF0` at the expUI level and is explicitly re-pointed to `core-bg-transparent-hovered` to follow this system.

### Menu & settings-tree row spacing

In the new UI a popup menu item's height = `List.rowHeight` (24 in Islands) plus the top/bottom of `*.Selection.outerInsets` (compile-time platform defaults: `1,7,1,7` for PopupMenu, `1,4,1,4` for Menu; nothing anywhere in the theme chain overrides them), which reads cramped. Relaxed to 2px top/bottom — `PopupMenu.Selection.outerInsets` = `2,7,2,7`, `Menu.Selection.outerInsets` = `2,4,2,4` (official left/right kept): each item gains 2px and the rounded selection blocks get breathing room between them.

The Settings dialog's left navigation tree goes through neither the Menu nor the List system: `SettingsTreeView` reads the dedicated key `SettingsTree.rowHeight` (the official metadata notes "zero or undefined = each node sizes itself from the renderer"; nothing in the theme chain defines it), set explicitly to 27 to relax the spacing.

These are the theme's only metric (non-color) changes. Constraint: do not tune menu or settings-tree spacing via `List.rowHeight` / `Tree.rowHeight` — those keys feed every list/tree row height and are far too broad; the top/bottom of `*.Selection.innerInsets` are ignored in the new UI (row height comes from rowHeight), only outerInsets contribute to the preferred height. The insets string format is `top,left,bottom,right`.

### Inheritance-leak fixes (explicit ui keys, do not delete)

The full inheritance chain has **five levels**: Ink Light → Islands Light → ExperimentalLightWithLightHeader → Light (expUI) → IntelliJ (intellijlaf). Islands' `*` wildcards (`*.background`, `*.selectionBackground`, … 23 entries) cover most components, but two classes of keys leak through:

1. **Keys whose suffix is not in the wildcard list** — intellijlaf's pure white / cold gray leaks straight into this theme. A non-editable combo box paints its whole pill with `ComboBox.nonEditableBackground` (not `ComboBox.background`), and plain button fills use `Button.startBackground/endBackground` — both `white` in intellijlaf.
2. **Keys defined explicitly at a lower level and not overridden by Islands** — whether an inherited explicit key beats a child theme's wildcard is unconfirmed against platform sources, so every visible case gets an explicit key, which is correct under either mechanism. Do not delete one of these even if it tests identical to the wildcard result — it is defensive insurance.

| ui key | Leaked value (source level) | This theme |
|---|---|---|
| `ComboBox.nonEditableBackground` | `#FFFFFF` (intellijlaf) | `layer-1-bg-inline` |
| `Button.startBackground` / `endBackground` | `#FFFFFF` (intellijlaf) | `layer-1-bg-inline` (flat, no gradient) |
| `Button.disabledBorderColor`, `Component.disabledBorderColor` | `#D1D1D1` (intellijlaf) | `control-border-disabled` |
| `TextField.disabledBackground` | `#F2F2F2` (intellijlaf) | `layer-1-bg` |
| `CompletionPopup.background` | pale blue `#EBF4FE` (intellijlaf) | `layer-2-bg` (official = completion matches the editor base) |
| `CompletionPopup.selectionBackground` / `selectionInactiveBackground` | `#C5DFFC` / `#DFDFDF` | `selection-bg-active` / `selection-bg-inactive` |
| `Menu.separatorColor`, `Popup.separatorColor`, `SearchEverywhere.List.separatorColor` | `#D9D9D9` (intellijlaf) | `layer-2-border` |
| `EditorTabs.borderColor`, `DefaultTabs.borderColor` | `#D1D1D1` (intellijlaf) | `layer-2-border` |
| `Table.alternativeRowBackground` | `#FFFFFF` (intellijlaf) | `layer-1-bg` (zebra rows) |
| `StatusBar.Breadcrumbs.floatingBackground` | `#FFFFFF` (expUI light) | `layer-2-bg` |
| `Popup.inactiveBorderColor` | `#ABABAB` (intellijlaf) | `control-border` |
| `SearchEverywhere.SearchField.borderColor` | `#C4C4C4` (intellijlaf) | `control-border` |
| `ToolWindow.Button.hoverBackground` | `#EBECF0` opaque (expUI light) | `core-bg-transparent-hovered` |

### Slider / ProgressBar

All three Slider keys are hardcoded cold grays: track `#C9CCD6` → `control-bg-small` (same as the ProgressBar track), ticks and thumb border `#818594` → `accent-neutral-bg` `#867E68`, and the thumb `Slider.buttonColor` is another intellijlaf `white` leak → `layer-1-bg-inline`.

The ProgressBar gradient endpoints are hardcoded to ramp colors in the parent (`blue-120`, …); ramp tokens have too many consumers to override wholesale, so explicit ui keys patch them precisely: indeterminate start `#A7C5FF` → pale 黛蓝 `#9FB3CC` (same value as the tab border for family consistency — deliberately not extracted into a shared token to avoid semantic coupling), passed end `#A3CFAE` → `accent-success-border-secondary`, failed end `#FFB0B2` → `accent-error-border-secondary`.

## Update notification mechanism

Starting with 2.1.0 the plugin carries its single source class, `src/com/github/yx208/eleganttheme/UpdateNotificationActivity.java` (registered in `plugin.xml` as the "Elegant Theme Updates" `notificationGroup` plus a `postStartupActivity`): on the first project open after an install or update it shows one sticky notification (STICKY_BALLOON) with the release highlights; the last-notified version is stored application-wide in `PropertiesComponent`, so each version notifies exactly once, with different titles for fresh installs and updates.

Release conventions and hard constraints:

- **The notification body is the first `<p>` of the change-notes.** Put the release headline in that first `<p>` on every release and keep `<a>` links out of it (they are not clickable there); later paragraphs are unrestricted.
- The "Full release notes" button builds its URL from the plugin version: `https://github.com/yx208/elegant-theme/blob/main/_docs/release-<version>.en.md` must exist on `main` when the version ships.
- `since-build` was raised from 211 to **223** together with this feature: the source compiles at the project language level (**Java 17**), and 2022.3+ IDEs run on JBR 17 and can load Java 17 bytecode (older IDEs run on JBR 11 and fail with `UnsupportedClassVersionError`). Compiling requires the Project SDK to be an IntelliJ Platform Plugin SDK (a plain JDK has no platform classes).
- Only APIs that exist in **both** 2022.3 and current platforms may be used.
