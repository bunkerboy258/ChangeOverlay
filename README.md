# Change Overlay

Change Overlay 是一个 Rider 插件。它把当前文件相对基线的变化直接画在普通代码编辑器里，不需要另外打开 Diff 窗口。

- 新增和修改后的行显示绿色背景
- 删除和修改前的内容显示为红色只读块
- 红色删除块不会写入文件，也不会在复制整份代码时混进去
- 支持 Git HEAD、最近一次本地提交和临时会话快照

## 安装

项目构建后的安装包位于：

```text
build/distributions/ChangeOverlay-<version>.zip
```

在 Rider 中打开：

```text
Settings | Plugins | 齿轮菜单 | Install Plugin from Disk
```

选择 ZIP 后重启 Rider。

## 日常使用

所有配置集中在 Rider 设置左侧顶层：

```text
Settings | 更改覆盖 / Change Overlay
```

常用设置：

- `启用更改覆盖`：总开关
- `基线模式`：选择 Git HEAD 或会话快照
- `跟踪分支提交历史`：工作区干净时显示最近一次本地提交的变化
- `跟踪分支`：选择需要跟踪的本地分支
- `显示新增行`、`显示删除行`、`显示修改行`：分别控制三类差异
- `背景透明度`：建议保持在 40 到 65 之间
- `刷新防抖毫秒`：默认 300 毫秒
- `开关差异预览快捷键 / Toggle Overlay Shortcut`：自定义开关差异预览的快捷键

快捷键设置采用按键捕获方式：点击输入框后直接按下想要的组合键，例如 Ctrl+Alt+O，框内会自动显示；按 Backspace 清除。修改后立即生效，快捷键保存在 Rider 当前键盘映射中，重启后仍然有效。

Tools 菜单和编辑器右键菜单中也有一组快捷操作：

- `启用或关闭更改覆盖`
- `将当前状态设为基线`
- `使用 Git HEAD 作为基线`
- `刷新更改覆盖`
- `清除更改覆盖`

插件默认不占用任何快捷键。除了在设置面板中自定义外，也可以在 Rider 的 Keymap 中为 `启用或关闭更改覆盖` 绑定快捷键。

## 基线怎么选

### Git HEAD

默认模式。当前文件与 `HEAD` 中的版本比较，适合查看尚未提交的修改。

未跟踪的新文件会按空文件处理。项目不是 Git 仓库、Git 不可用或 HEAD 不存在时，插件会给出提示，不会修改仓库。

### 跟踪分支提交历史

先选择一个本地分支，例如 `main`，再打开 `跟踪分支提交历史`。

满足下面三个条件时，插件会使用 `HEAD^` 作为基线：

1. 当前正处于所选分支
2. 整个 Git 工作区干净
3. HEAD 存在父提交

只要有一个条件不满足，就自动回到 HEAD 基线。这样在提交完成后仍能看到刚刚那次本地提交改了什么，同时不会干扰未提交修改的显示。

### 会话快照

执行 `将当前状态设为基线` 后，插件会把当前项目中已经打开的文本文件保存到内存。

后续变化与这份内存快照比较。快照不会修改文件、不会创建提交，关闭项目后自动清除。

## 显示与性能

Git 命令和 diff 计算都在后台线程执行，编辑器绘制才会切回 EDT。文档变化默认经过 300 毫秒防抖，旧任务不能覆盖新结果。重新开启覆盖时，如果文档没有改动，会先立即显示上一次缓存的结果，再在后台静默重算更新。

默认跳过：

- 二进制文件
- 超过 1 MiB 的文件
- 超过 20000 行的文件
- 没有普通文本 Document 的编辑器

删除块使用编辑器字体。字体缺少中文或 Unicode 字形时，会自动使用 JBR 逻辑字体回退。制表符按四个空格显示，超长删除行目前只裁切，不自动换行。

## 本地开发

当前工程目标是 Rider 2026.1 build 261。

- 构建工具链：Rider 自带 JBR 25
- 插件字节码：Java 21
- Gradle：9.0.0 Wrapper
- IntelliJ Platform Gradle Plugin：2.18.1

在 `gradle.properties` 中设置本机 Rider 路径：

```properties
platformPath=D:/JetBrains Rider 2026.1
```

PowerShell 中设置 JBR：

```powershell
$env:JAVA_HOME = "D:\JetBrains Rider 2026.1\jbr"
```

运行测试：

```powershell
.\gradlew.bat clean test
```

构建安装包：

```powershell
.\gradlew.bat buildPlugin
```

运行 Plugin Verifier：

```powershell
.\gradlew.bat verifyPlugin
```

启动开发用 Rider：

```powershell
.\gradlew.bat runIde
```

## 当前限制

- 分支提交历史只跟踪当前检出的本地分支
- 会话快照只包含捕获时已经打开的文本文件
- 删除块暂时没有语法高亮
- 删除块中的长行不会自动换行
- 多项目同时打开时，设置页的分支列表取第一个可用 Git 项目

插件只使用 IntelliJ Platform 前端编辑器 API，不包含 ReSharper 或 .NET 后端模块。当前实现没有主动使用 `@ApiStatus.Internal` 或 `@Experimental` API。
