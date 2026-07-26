# Change Overlay

Change Overlay is a JetBrains Rider plugin that displays the current file's line changes directly inside an ordinary code editor. Added and revised lines receive a translucent green background. Removed and replaced source lines are rendered as read-only translucent red block inlays near their original positions.

Screenshot placeholder: `docs/change-overlay.png` will be added when a Marketplace screenshot is prepared.

## Compatibility

- JetBrains Rider 2026.1 build 261
- Rider bundled JBR 25 as the local build toolchain
- Java 21 plugin bytecode compatibility
- Gradle 9 through the included Gradle Wrapper
- IntelliJ Platform Gradle Plugin 2.18.1

The plugin is frontend-only. It does not require a ReSharper backend or the IntelliJ Java plugin.

## Development

Set `platformPath` in `gradle.properties` to a Rider 2026.1 installation. On this development machine it is `D:/JetBrains Rider 2026.1`. Set `JAVA_HOME` to that installation's `jbr` directory before invoking the wrapper. The wrapper downloads Gradle 9.0.0 from the Tencent Gradle mirror because the default Gradle service redirects to GitHub and is unreachable on the development network.

Run a development Rider:

```powershell
.\gradlew.bat runIde
```

Run tests:

```powershell
.\gradlew.bat clean test
```

Build the installable ZIP:

```powershell
.\gradlew.bat buildPlugin
```

The ZIP is generated under `build/distributions`.

Run Plugin Verifier:

```powershell
.\gradlew.bat verifyPlugin
```

## Installation

Build the ZIP and open Rider. Select `Settings | Plugins`, use the gear menu, choose `Install Plugin from Disk`, and select the ZIP from `build/distributions`.

## Actions

The `Change Overlay` group is available in the Tools menu and the editor context menu.

- `Toggle Change Overlay` enables or disables rendering.
- `Capture Current State as Baseline` snapshots all currently open project text files in memory.
- `Use Git HEAD as Baseline` switches back to Git HEAD.
- `Refresh Change Overlay` immediately recalculates open editors.
- `Clear Change Overlay` removes the current visual overlays until the next refresh or document change.

No default shortcuts are assigned.

## Baseline Modes

Git HEAD is the default. The plugin runs `git show HEAD:<relative-path>` in a background executor with a five-second timeout. A file absent from HEAD uses an empty baseline. Missing Git, missing repositories, and command errors are logged without uncaught UI exceptions.

Enable `Track Branch Commit History` and select a local branch to keep the most recent local commit visible after committing. When the selected branch is currently checked out and the entire working tree is clean, the plugin automatically uses `HEAD^` as the baseline. A dirty working tree, another checked-out branch, a missing selection, or an initial commit without a parent automatically uses `HEAD`.

Session Snapshot stores the contents of currently open text files in memory. It never modifies files or creates Git commits. Snapshots are discarded when the project service is disposed.

## Settings

Open `Settings | Tools | Change Overlay` to configure enablement, baseline mode, tracked branch commit history, visible change types, added and deleted colors, opacity, debounce duration, maximum file size, maximum line count, and the deleted-line minus prefix. The tracked branch list contains local branches from the first open Git project and is loaded outside the EDT.

## Performance and Limits

Binary files, files larger than 1 MiB, files over 20000 lines, and editors without a valid text document are skipped by default. Git and diff work run outside the EDT. Document changes use a default 300 ms debounce and a monotonically increasing task version so stale results cannot replace newer results.

Deleted blocks use the editor font and expand tabs to four spaces. Very long deleted lines are clipped by the visible editor area rather than wrapped. The first version does not syntax-highlight deleted content.

## IntelliJ Platform APIs

The implementation uses project and application services, `PersistentStateComponent`, `EditorFactoryListener`, `DocumentListener`, `MarkupModel`, `RangeHighlighter`, `InlayModel`, `EditorCustomElementRenderer`, the Action System, Swing settings controls, `Disposable`, and platform executors.

No API intentionally marked `@ApiStatus.Internal` or `@Experimental` is used. Plugin Verifier is the final compatibility authority for the target Rider build.

## Known Limitations

- A project must already have a Git commit for Git HEAD mode to display tracked baselines.
- Branch history tracking applies only when the selected local branch is currently checked out.
- Session snapshots include only files open when capture is invoked.
- Deleted block text has no syntax highlighting.
- Deleted long lines do not wrap.
- Settings changes take effect on the next refresh.
