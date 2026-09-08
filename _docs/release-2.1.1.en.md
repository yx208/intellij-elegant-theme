# 2.1.1 Design Notes — Ink Light Syntax Coverage (Rust / Python / Go) and Three UI Touches

## What changed in this release

On the editor-scheme side 2.1.1 is pure gap-filling: `resources/theme/ink-light.theme.xml` gains 55 attribute keys — **nothing modified, nothing removed**. The UI theme `resources/theme/ink-light.theme.json` gets three touches: the focused selected tab becomes a selection-blue chip with a mid-blue outline, the unfocused selected tab becomes a sand chip with a light gray outline instead of all but vanishing, and the side-bar button of an open-but-unfocused tool window switches to the theme's selection blue (see "UI touches"). `since-build` is untouched; the only source change is one URL-template constant, from the docs rename described under "Release conventions".

| Group | Keys added | State before |
|---|---|---|
| Rust `org.rust.*` | 41 | entirely missing |
| Python `PY.*` | 9 | entirely missing |
| Go `GO_*` | 3 | struct members and `GO_SYNTAX_UPDATE` missing (the other 16 shipped in 2.1.0) |
| Platform `DEFAULT_REASSIGNED_*` | 2 | missing; affects every language |

The scheme's `parent_scheme` is `Default`, so any key left undefined falls back to the platform's (or the language plugin's) saturated defaults, which are designed for a pure-white background. On rice paper they are both harsh and out of system with the rest of the syntax colors — the same class of gap 2.1.0 closed for `DEFAULT_BRACES`, `CONSOLE_*` and inlay hints. This release pulls Rust, Python and Go back into the ink-on-paper system.

## Color selection principles

**No new colors in the editor scheme.** The 55 new keys use 14 hex values, all of which already existed in `ink-light.theme.xml`. The eye-comfort floor set in 2.1.0 — syntax foregrounds at roughly 4:1–6.5:1 against the paper `#F6F0DF`, body text at about 9:1 — therefore needs no re-verification. The only new value in the UI JSON is the tab outline's mid blue `#4F74A8`; the other UI touches reuse values already in the palette (see "UI touches").

Reused values and their roles:

| Value | Name | Existing role in this scheme |
|---|---|---|
| `#33568C` | indigo | keywords (`DEFAULT_KEYWORD`) |
| `#2E7093` | porcelain blue | functions (`DEFAULT_FUNCTION_DECLARATION`) |
| `#2E7D6E` | celadon | classes/interfaces (`DEFAULT_CLASS_NAME`) |
| `#8E4D82` | mulberry | fields/constants (`DEFAULT_INSTANCE_FIELD` / `DEFAULT_CONSTANT`) |
| `#A85F2E` | ochre | numbers |
| `#96781C` | autumn gold | annotations/metadata |
| `#C13E2F` | cinnabar | errors |
| `#B3554A` | terracotta | error text (ANSI red normal) |
| `#886715` | deep autumn gold | warnings (ANSI yellow normal) |
| `#3D6A78` | slate blue | links |
| `#77882E` | willow green | `GO_SHADOWING_VARIABLE` ("watch out" identifiers) |
| `#6B675E` | mid ink | secondary text |
| `#A79F89` | pale ink | dimmed text (inactive breadcrumbs, ANSI white normal) |
| `#F2DCD3` | pale terracotta wash | background of `ERROR_HINT` and `DEFAULT_INVALID_STRING_ESCAPE` |

Three cross-language mapping rules — every new key follows them, and so should any future language:

1. **Align with the platform default keys by role**: types → celadon, functions → porcelain blue, fields/members → mulberry, keywords and `self` → indigo. A role keeps the same color across languages, so switching languages never changes the feel.
2. **Indirect or constrained items get italics, not a different color**: trait methods, associated functions, `self`, enum variants, statics, lifetimes. This matches the existing italic convention of `DEFAULT_INTERFACE_NAME` and `DEFAULT_CONSTANT`.
3. **Markers that must not steal the foreground use effects**: reassigned variables and `mut` bindings get a mid-ink underline, secondary marks a porcelain-blue wave. No `FOREGROUND` is set, so the identifier keeps its own semantic color.

## Rust (`org.rust.*`, 41 keys)

| Group | Keys | Color |
|---|---|---|
| Types | `ENUM`, `STRUCT`, `UNION`, `TYPE_ALIAS`, `TYPE_PARAMETER` | celadon `#2E7D6E` |
| Traits | `TRAIT` | celadon `#2E7D6E` + italic |
| Functions/methods | `FUNCTION`, `FUNCTION_CALL`, `METHOD`, `METHOD_CALL` | porcelain blue `#2E7093` |
| Associated/trait functions | `ASSOC_FUNCTION`(`_CALL`), `ASSOC_TRAIT_FUNCTION`(`_CALL`), `TRAIT_METHOD`(`_CALL`) | porcelain blue `#2E7093` + italic |
| Values | `ENUM_VARIANT`, `STATIC` | mulberry `#8E4D82` + italic |
| Macros and crates | `MACRO`, `CRATE` | autumn gold `#96781C` |
| `self` family | `SELF_PARAMETER`, `SELF_EXPRESSION` | indigo `#33568C` + italic |
| `&mut self` | `MUT_SELF_PARAMETER` | indigo + italic + mid-ink underline |
| `?` operator | `Q_OPERATOR` | indigo `#33568C` + bold |
| Lifetimes | `LIFETIME` | ochre `#A85F2E` + italic |
| Format strings | `FORMAT_PARAMETER`, `FORMAT_SPECIFIER` | porcelain blue `#2E7093` |
| unsafe | `KEYWORD_UNSAFE` | cinnabar `#C13E2F` |
| unsafe blocks | `UNSAFE_CODE` | background `#F2DCD3`, no foreground |
| Mutable bindings | `MUT_BINDING` | mid-ink `#6B675E` underline, no foreground |
| Generated items | `GENERATED_ITEM` | willow green `#77882E` |
| Dimmed | `CFG_DISABLED_CODE`, `DIMMED_TEXT` | pale ink `#A79F89` |
| Docs | `DOC_CODE` | mid ink `#6B675E` |
| Doc emphasis | `DOC_EMPHASIS` / `DOC_STRONG` | italic / bold only, no color |
| Inline diagnostics | `INLINE_ERROR_DESCRIPTION` / `INLINE_WARNING_DESCRIPTION` / `INLINE_EXPLANATION` | terracotta `#B3554A` / deep autumn gold `#886715` / slate blue `#3D6A78`, all italic |
| Secondary marks | `SECONDARY_SPOT` | porcelain blue `#2E7093` wave |
| Parameters | `PARAMETER` | explicitly empty (see maintenance notes) |

`?` is control flow rather than an identifier, so it takes the keyword color in bold to stay visible at end of line; `'a` is an annotation rather than an identifier, so it joins ochre (the literal family) instead of any identifier color; an `unsafe` block gets background only, because a foreground would flatten every syntax color inside it.

## Python (`PY.*`, 9 keys)

| Key | Color | Rationale |
|---|---|---|
| `PY.BUILTIN_NAME` | porcelain blue `#2E7093` | builtins belong to the function family |
| `PY.SELF_PARAMETER` | indigo `#33568C` + italic | same treatment as Rust's `self` |
| `PY.TYPE_PARAMETER` | celadon `#2E7D6E` | type family |
| `PY.KEYWORD_ARGUMENT` | mid ink `#6B675E` | keyword argument names step back so they do not compete with the callee |
| `PY.FSTRING_FRAGMENT_BRACES`, `PY.FSTRING_FRAGMENT_COLON`, `PY.FSTRING_FRAGMENT_TYPE_CONVERSION`, `PY.FSTRING_FORMAT_SPEC_NUMBER`, `PY.FSTRING_FORMAT_SPEC_SPECIAL_CHAR` | porcelain blue `#2E7093` | f-string structure lifts out of the bamboo-green string body; same color as Rust's `FORMAT_*` |

## Go (`GO_*`, 3 keys)

| Key | Color | Rationale |
|---|---|---|
| `GO_STRUCT_EXPORTED_MEMBER`, `GO_STRUCT_LOCAL_MEMBER` | mulberry `#8E4D82` | struct members are fields, matching `DEFAULT_INSTANCE_FIELD`; exported and local share one color, since Go already expresses export through capitalization |
| `GO_SYNTAX_UPDATE` | porcelain blue `#2E7093` wave | a hint marker, so it takes no foreground |

## Platform default keys (2 keys, all languages)

`DEFAULT_REASSIGNED_LOCAL_VARIABLE` and `DEFAULT_REASSIGNED_PARAMETER`: mid ink `#6B675E` underline, **no foreground**. Reassignment is metadata about a variable, so it is expressed as decoration; a foreground would override each language's own local-variable color in Java, Kotlin, JS and the rest. Rust's `MUT_BINDING` and `MUT_SELF_PARAMETER` use the same underline for consistency.

## UI touches (`ink-light.theme.json`)

### Focused selected tab: outline `#9FB3CC` → `#4F74A8`, chip switched to the selection blue

The palette token `tab-selected-border-active` moves from the pale slate blue `#9FB3CC` to the mid blue `#4F74A8`. Its sole consumer is `EditorTabs.underlinedBorderColor`, which the Islands tab painter (`IslandsTabPainter`) uses to stroke the **full outline** of the selected tab chip. The old value had only 1.66:1 contrast against 2.1.0's warm sand chip `#EAE2CC` and read as a grayish pale blue on warm paper. The new value (H215° S36% L48%) shares its hue with the brand slate `#44546B` and reaches 4.2:1 against the paper — unmistakably blue, yet not dark enough to frame the tab in a heavy ring. The chip changes family with it: the `ui` key `EditorTabs.underlinedTabBackground` moves from the warm sand `layer-0-bg` to `selection-bg-active-muted` `#D9E2EB`, the muted selection blue one step lighter than tree/list selection, so the chip is now a pale tint of its own outline rather than a blue ring around a sand tile — 1.15:1 against the paper, 3.7:1 outline-to-chip, about 8.4:1 for body ink on the chip. `tab-selected-bg-active` is unchanged (it still feeds the SearchEverywhere tab and the TabbedPane focus color) and the unfocused tab is handled in the next section; `ProgressBar.indeterminateStartColor` stays at `#9FB3CC`, since 2.1.0 deliberately decoupled it from the tab outline.

### Selected tab while the editor is unfocused: sand chip + neutral gray outline

Islands decides a tab's active/inactive state by **whether the editor holds keyboard focus** (`EditorTabs.checkActive()` reads `UIUtil.isFocusAncestor`), so the moment focus moves to a tool window the selected tab switches to `EditorTabs.inactiveUnderlinedTabBackground` + `inactiveUnderlinedTabBorderColor`. 2.1.0 set that state to pale paper `#F1EAD6` with a sand outline `#CFC6AC` — about 1.02:1 and 1.3:1 against the tab bar `#F6F0DF` — which in practice meant the current editor tab all but vanished whenever you worked in Project or Terminal. This release changes both tokens: `tab-selected-bg-inactive` `#F1EAD6` → `#EAE2CC` (the value of `layer-0-bg`; the same H44 warm family as the gray outline, and at L86% close to the focused chip's L89%), and `tab-selected-border-inactive` `#CFC6AC` → `#B1A88F` (the value of `control-border-raised`, a warm gray at about 1.8:1 against the chip). Both states now follow the same rule — the chip is a pale tint of its outline: blue-on-blue means the editor has focus, sand with a light gray outline means focus is elsewhere. The unfocused state is deliberately one step weaker: the tab is defined by the sand chip against the paper plus the light outline, and does not compete with the focused state. Each token has exactly one consumer in the parent theme (its EditorTabs key), so changing the token is safe; this supersedes 2.1.0's "one paper step lighter when unfocused" design.

### Side-bar button of an open, unfocused tool window: the theme's selection blue

The Islands stripe button (`SquareStripeButtonLook`) has three states. When the tool window is **focused** it paints `ToolWindow.Button.selectedBackground` (brand slate `#44546B`) with the icon stroked in `selectedForeground`. When the tool window is **open but unfocused** it falls through to the generic ActionButton pressed state — `ActionButton.pressedBackground` plus `ActionButton.pressedBorderColor` — with the icon in its regular ink color. Hover uses the hover keys. 2.1.0 pointed the pressed background at `toolbar-selected-bg` `#DDE3E4` and the border at `accent-brand-border-secondary` `#C7D2D6`, both low-saturation gray-blues (S 11–15%) — exactly the band 2.1.0 itself described as hue-less and near-invisible. This release re-points `ActionButton.pressedBackground` to `selection-bg-active` `#C8D5E3` (the same blue as tree/list selection) and sets `ActionButton.pressedBorderColor` to `#9FB3CC` (a slightly deeper tone of the same family, the same value as `ProgressBar.indeterminateStartColor`, written inline per the 2.1.0 convention rather than through a shared token).

Blast radius: `ActionButton.pressedBackground` is shared by every toggled-on toolbar action button (the Structure view's sort toggles, the editor's Soft-Wrap toggle, and so on), so they turn into the same selection-blue chip together with the stripe button. The `toolbar-selected-bg` and `toolbar-selected-bg-hovered` tokens themselves are untouched; they still feed the find bar's `SearchOption.*` keys and `SegmentedButton.focusedSelectedButtonColor`, which therefore remain `#DDE3E4` and are left for a later pass.

## Hard constraints and maintenance notes

- **Do not delete the empty `<value />` on `org.rust.PARAMETER`.** The empty value means "explicitly unstyled", so parameter names render in body ink; removing the key falls back to the Rust plugin's own parameter style from `parent_scheme="Default"`.
- **`org.rust.UNSAFE_CODE` may only carry `BACKGROUND`.** It covers the whole unsafe region; adding a `FOREGROUND` flattens every syntax color inside it. Its wash `#F2DCD3` is shared with `ERROR_HINT` and `DEFAULT_INVALID_STRING_ESCAPE` — changing one means considering all three.
- **Effect encoding**: `EFFECT_TYPE` 1 = underline, 2 = wave; `FONT_TYPE` 1 = bold, 2 = italic, 3 = bold italic.
- **Keep keys in alphabetical order** (that is the IDE export order; `org.rust.*` sorts last because it is lowercase). Insert hand-written keys at the right position so the next IDE export does not produce a large spurious diff.
- **Only Ink Light was updated in this release.** Coverage elsewhere: `elegant-dark` has 28 Rust and 5 Python keys, `islands-light` has 16 Go keys, `islands-dark` has neither. If these are unified later, derive colors from each scheme's own palette using the role mapping above — **do not copy hex values across themes** (dark schemes have different contrast relationships).
- When covering a new language, follow the three mapping rules and reuse existing values first. If a genuinely new color is needed, verify its contrast against the paper `#F6F0DF` lands in 4:1–6.5:1, and do not drop below the saturation of the current syntax colors — 2.1.0 established that going lower blends them into the body ink.
- **`tab-selected-border-active` must stay in the H≈215° blue family at L 48%–60% (roughly 2.5:1–3.7:1 against the chip `#D9E2EB`).** It strokes the full outline of the selected tab chip: lighter slides back into the grayish 2.1.0 look, darker (e.g. `#33568C` at about 5.6:1) frames the whole tab in a heavy ring. This supersedes the 2.1.0 maintenance note that called for a pale tint of the accent family.
- **The focused chip `EditorTabs.underlinedTabBackground` must share the outline's family and be clearly lighter**; it currently points at `selection-bg-active-muted`. Tune it only through that token's lightness (L 89%±2, in line with the 2.1.0 selection-color rule of moving lightness only, never saturation); do not move it back to warm sand or to anything without blue, or the tab reverts to a blue ring around a sand tile. It is an explicit `ui` key rather than the `tab-selected-bg-active` token because that token also feeds the SearchEverywhere tab and the TabbedPane focus color.
- **Keep the unfocused-tab outline `tab-selected-border-inactive` a blue-free warm gray at about 1.8:1 against the chip (acceptable band 1.6:1–2.2:1).** Its job, opposite the blue ring, is "hue signals focus, weight signals priority": a bluish tint blurs the two states, pushing it to around 3:1 makes it compete with the focused state, and lightening it back to 2.1.0's `#CFC6AC` makes the current tab vanish again when unfocused. `tab-selected-bg-inactive` keeps the value of `layer-0-bg`: it shares the gray outline's family and matches the focused chip's weight, so a focus change only swaps the family, never the tab's weight.
- **Never make `ActionButton.pressedBackground` dark.** It serves both the stripe button's open-but-unfocused state and every toggled-on toolbar button; the icons are not recolored in those states, so a dark fill swallows them. Adjust lightness only, within L 84%±2 of the selection-blue family (the same constraint as `selection-bg-active`).
- **`#9FB3CC` now appears in two `ui` keys** (`ProgressBar.indeterminateStartColor`, `ActionButton.pressedBorderColor`), both written inline on purpose: the two roles are semantically unrelated, so do not introduce a shared token for them.

## Release conventions

- `plugin.xml`: `<version>` 2.1.0 → 2.1.1; the first `<p>` of `<change-notes>` is the update notification body and must contain no `<a>` links.
- The notification's "Full release notes" button is built from the version number and points at `_docs/release-2.1.1.en.md`, so that file must be on `main` at release time.
- Starting with this release, version docs are named `_docs/release-<version>` instead of `_docs/design-<version>` — "design" undersold what these files are: release records, where the change list and the hard constraints outweigh the design rationale. The 2.1.0 pair was renamed as well, and the URL template in `UpdateNotificationActivity` now reads `_docs/release-%s.en.md`. No redirect was left at the old paths, so "Full release notes" 404s for clients still on 2.1.0 — a deliberate trade-off.
- Apart from that URL template there are no source changes; `since-build` stays at 223.
