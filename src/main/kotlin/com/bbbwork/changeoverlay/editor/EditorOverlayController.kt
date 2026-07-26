package com.bbbwork.changeoverlay.editor

import com.bbbwork.changeoverlay.baseline.BaselineProvider
import com.bbbwork.changeoverlay.baseline.BaselineResult
import com.bbbwork.changeoverlay.diff.LineDiffEngine
import com.bbbwork.changeoverlay.rendering.AddedLineHighlighter
import com.bbbwork.changeoverlay.rendering.DeletedBlockRenderer
import com.bbbwork.changeoverlay.rendering.DiffToEditorMapper
import com.bbbwork.changeoverlay.rendering.OverlayColors
import com.bbbwork.changeoverlay.settings.ChangeOverlaySettings
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

//单编辑器覆盖控制器
class EditorOverlayController(
    val editor: Editor,
    private val baselineProvider: () -> BaselineProvider,
    private val diffEngine: LineDiffEngine
) : Disposable
{
    private val logger = Logger.getInstance(EditorOverlayController::class.java)
    private val overlayState = EditorOverlayState()
    private val version = AtomicLong()
    private var scheduledTask: Future<*>? = null
    private var lastReportedFailure: String? = null

    init
    {
        //注册Document文档变化监听
        editor.document.addDocumentListener(
            object : DocumentListener
            {
                override fun documentChanged(event: DocumentEvent)
                {
                    scheduleRefresh()
                }
            },
            this
        )
    }

    //按设置延迟刷新
    fun scheduleRefresh()
    {
        val settings = ChangeOverlaySettings.getInstance().state
        val taskVersion = version.incrementAndGet()
        scheduledTask?.cancel(false)

        if (!settings.enabled)
        {
            clearOnEdt()

            return
        }

        scheduledTask = AppExecutorUtil
            .getAppScheduledExecutorService()
            .schedule(
                {
                    computeRefresh(taskVersion)
                },
                settings.debounceMilliseconds.toLong(),
                TimeUnit.MILLISECONDS
            )
    }

    //立即在后台刷新
    fun refreshNow()
    {
        val taskVersion = version.incrementAndGet()
        scheduledTask?.cancel(false)

        scheduledTask = AppExecutorUtil
            .getAppExecutorService()
            .submit {
                computeRefresh(taskVersion)
            }
    }

    //清除当前覆盖显示
    fun clear()
    {
        version.incrementAndGet()
        scheduledTask?.cancel(false)
        clearOnEdt()
    }

    //返回当前文件内容
    fun currentText(): String
    {
        return editor.document.text
    }

    //后台计算关键调用
    private fun computeRefresh(taskVersion: Long)
    {
        val settings = ChangeOverlaySettings.getInstance().state
        val skipReason = EditorEligibilityChecker.check(editor, settings)

        if (skipReason != null)
        {
            logger.debug("Skipping change overlay $skipReason")
            clearIfCurrent(taskVersion)

            return
        }

        val file = FileDocumentManager.getInstance().getFile(editor.document)

        if (file == null)
        {
            clearIfCurrent(taskVersion)

            return
        }

        val baseline = baselineProvider().readBaseline(file)

        if (baseline !is BaselineResult.Success)
        {
            if (baseline is BaselineResult.Failure)
            {
                logger.warn(baseline.message)
                reportFailure(baseline.message)
            }

            clearIfCurrent(taskVersion)

            return
        }

        lastReportedFailure = null
        val currentText = editor.document.text
        val result = diffEngine.diff(
            baseline.text,
            currentText
        )
        val mapping = DiffToEditorMapper.map(result)

        ApplicationManager.getApplication().invokeLater {
            if (taskVersion != version.get())
            {
                return@invokeLater
            }

            if (editor.isDisposed)
            {
                return@invokeLater
            }

            render(mapping)
        }
    }

    //显示去重基线错误提示
    private fun reportFailure(message: String)
    {
        if (lastReportedFailure == message)
        {
            return
        }

        lastReportedFailure = message

        ApplicationManager.getApplication().invokeLater {
            if (editor.isDisposed)
            {
                return@invokeLater
            }

            NotificationGroupManager
                .getInstance()
                .getNotificationGroup("Change Overlay")
                .createNotification(
                    message,
                    NotificationType.WARNING
                )
                .notify(editor.project)
        }
    }

    //在EDT绘制最终结果
    private fun render(mapping: com.bbbwork.changeoverlay.rendering.EditorOverlayMapping)
    {
        val settings = ChangeOverlaySettings.getInstance().state
        overlayState.clear()
        val addedLineHighlighter = AddedLineHighlighter()
        val deletedBlockRenderer = DeletedBlockRenderer()

        if (settings.showAddedLines)
        {
            for (line in mapping.addedLines)
            {
                val highlighter = addedLineHighlighter.add(
                    editor,
                    line,
                    OverlayColors.added(settings)
                )

                if (highlighter != null)
                {
                    overlayState.highlighters += highlighter
                }
            }
        }

        if (settings.showModifiedLines)
        {
            for (line in mapping.modifiedLines)
            {
                val highlighter = addedLineHighlighter.add(
                    editor,
                    line,
                    OverlayColors.added(settings)
                )

                if (highlighter != null)
                {
                    overlayState.highlighters += highlighter
                }
            }
        }

        for (block in mapping.deletedBlocks)
        {
            if (!settings.showDeletedLines && !block.modified)
            {
                continue
            }

            if (!settings.showModifiedLines && block.modified)
            {
                continue
            }

            val inlay = deletedBlockRenderer.add(
                editor,
                block.line,
                block.lines,
                OverlayColors.deleted(settings),
                settings.showMinusPrefix
            )

            if (inlay != null)
            {
                overlayState.inlays += inlay
            }
        }
    }

    //清除仍为当前版本的结果
    private fun clearIfCurrent(taskVersion: Long)
    {
        ApplicationManager.getApplication().invokeLater {
            if (taskVersion == version.get())
            {
                overlayState.clear()
            }
        }
    }

    //切换到EDT清除显示
    private fun clearOnEdt()
    {
        ApplicationManager.getApplication().invokeLater {
            overlayState.clear()
        }
    }

    //释放编辑器全部任务和资源
    override fun dispose()
    {
        version.incrementAndGet()
        scheduledTask?.cancel(true)
        overlayState.clear()
    }
}
