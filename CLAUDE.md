# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

An IntelliJ Platform UI theme plugin ("Elegant Theme") for JetBrains IDEs. The plugin is almost entirely resources (theme JSON + editor color scheme XML) packaged into a jar; the only source code is a single Java class under `src` that shows a one-time release-highlights notification after the plugin is installed or updated.

## Building

There is no Gradle/CLI build. The project is an IntelliJ DevKit plugin module (`elegant-theme.iml`, type `PLUGIN_MODULE`):

- Build in IntelliJ IDEA via **Build → Prepare Plugin Module 'elegant-theme' For Deployment**, which produces `elegant-theme.jar` in the project root.
- Test changes by installing the jar via **Settings → Plugins → Install Plugin from Disk**, or by running the plugin in a DevKit sandbox IDE.
- `out`, `*.jar`, and `test` are gitignored — edit files under `resources` and `src` only; anything under `out` is compiler output.
- Compiling the Java source requires the Project SDK to be an **IntelliJ Platform Plugin SDK** (a plain JDK has no platform classes). The module inherits the project language level (Java 17); `since-build` is **223** because 2022.3+ IDEs run on JBR 17 and can load Java 17 bytecode.

## Architecture

Everything the plugin ships lives in `resources`:

- `resources/META-INF/plugin.xml` — plugin descriptor. Each theme is registered as a **pair** of extensions: a `themeProvider` (UI theme, `.theme.json`) and a matching `colorScheme` (editor scheme, `.theme.xml`). The plugin version lives in the `<version>` tag here (nowhere else).
- `resources/theme/*.theme.json` — UI themes. Each defines a named color palette under `colors` and maps IntelliJ UI keys under `ui` (keys can reference palette names). Each points to its editor scheme via `editorScheme`.
- `resources/theme/*.theme.xml` (and `.icls`) — editor color schemes (`parent_scheme` is `Darcula` or `Light`).
- `src/com/github/yx208/eleganttheme/UpdateNotificationActivity.java` — the plugin's only class, registered in `plugin.xml` as a `notificationGroup` ("Elegant Theme Updates") plus a `postStartupActivity`. After an install/update it shows a sticky notification once per version (last-notified version stored app-wide via `PropertiesComponent`). The notification body is the first `<p>` of `<change-notes>`; its "Full release notes" button opens `_docs/release-<version>.en.md` on GitHub (URL derived from the plugin version). Only APIs present in both 2022.3 and current platforms may be used here — see the compatibility notes in the class javadoc.

Registered themes (in `plugin.xml`): Elegant Islands Light, Elegant Ink Light, Elegant Islands Dark, Elegant Dark. The Islands variants (including Ink Light) set `parentTheme` to JetBrains' Islands themes and layer adjustments on top. Note that `elegant-light.theme.json` exists in `resources/theme` but is **not** registered in `plugin.xml`.

`color-schema.json` at the repo root is a reference palette document, not consumed by the plugin.

## Conventions

- Releases: bump `<version>` in `plugin.xml` and update its `<change-notes>` with a short summary. Keep the release headline in the **first `<p>`** of `<change-notes>` — it is shown verbatim as the update-notification body — and keep `<a>` links out of that paragraph (they are not clickable there). The English release doc must be on `main` at release time because the notification links to it. There is no CHANGELOG (removed after 2.1.0) — the release record for each version is its doc pair under `_docs` (see below). Commit messages before 2.1.0 say "see CHANGELOG.md for details"; that file no longer exists.
- When changing a color, keep the palette-name indirection: change the value in the theme JSON's `colors` block (or the scheme XML) rather than hardcoding hex values into individual `ui` keys.
- Release docs: every release gets a bilingual pair of documents — `_docs/release-<version>.md` (Chinese) plus `_docs/release-<version>.en.md` (English) — serving as that version's release record: what actually changed, design philosophy, final palette, hard constraints, and maintenance notes. Record only final decisions and constraints — no exploration history, rejected alternatives, or thought process. Keep the two in sync when either changes. Write these files with soft wrapping — one paragraph (or list item) per line, no manual line breaks; let the editor wrap. See `_docs/release-2.1.0.md` / `_docs/release-2.1.0.en.md` (Elegant Ink Light) for a full-featured example and `_docs/release-2.1.1.md` for a small colors-only patch. Docs for 2.1.0 and earlier were named `design-<version>`; the files were renamed in 2.1.1, so links from already-installed 2.1.0 clients now 404.
- Theme JSONs that set `parentTheme` may override the parent's semantic palette tokens in their `colors` block (e.g. `layer-2-bg`, `accent-brand-bg`). Such tokens are consumed by the parent theme's `ui` mappings at load time (`UIThemeBean.importFromParentTheme()` merges palettes, child wins), so the DevKit "unused color" inspection flags them falsely — do not delete them based on that inspection. See the maintenance guide in `_docs/release-2.1.0.md`.
