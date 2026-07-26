package com.bbbwork.changeoverlay.services

import com.bbbwork.changeoverlay.baseline.BaselineMode
import com.bbbwork.changeoverlay.baseline.BaselineProvider
import com.bbbwork.changeoverlay.baseline.GitHeadBaselineProvider
import com.bbbwork.changeoverlay.baseline.SnapshotBaselineProvider
import com.bbbwork.changeoverlay.diff.MyersLineDiffEngine
import com.bbbwork.changeoverlay.editor.EditorOverlayController
import com.bbbwork.changeoverlay.settings.ChangeOverlaySettings
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ide.ui.LafManagerListener
import java.util.concurrent.ConcurrentHashMap

//项目级覆盖管理服务
@Service(Service.Level.PROJECT)
class ChangeOverlayProjectService(
    private val project: Project
) : Disposable
{
    private val snapshotProvider = SnapshotBaselineProvider()
    private val gitHeadProvider = GitHeadBaselineProvider(project)
    private val controllers = ConcurrentHashMap<Editor, EditorOverlayController>()
    private val editorFactoryListener = object : EditorFactoryListener
    {
        //注册新打开编辑器
        override fun editorCreated(event: EditorFactoryEvent)
        {
            registerEditor(event.editor)
        }

        //释放已关闭编辑器
        override fun editorReleased(event: EditorFactoryEvent)
        {
            unregisterEditor(event.editor)
        }
    }

    init
    {
        val editorFactory = EditorFactory.getInstance()

        //注册Editor编辑器生命周期监听
        editorFactory.addEditorFactoryListener(
            editorFactoryListener,
            this
        )

        //监听IDE主题变化并重绘颜色
        project.messageBus
            .connect(this)
            .subscribe(
                LafManagerListener.TOPIC,
                LafManagerListener {
                    refreshAll()
                }
            )

        for (editor in editorFactory.allEditors)
        {
            registerEditor(editor)
        }
    }

    //读取当前基线提供器
    fun baselineProvider(): BaselineProvider
    {
        val mode = ChangeOverlaySettings.getInstance().state.baselineMode

        if (mode == BaselineMode.SESSION_SNAPSHOT)
        {
            return snapshotProvider
        }

        return gitHeadProvider
    }

    //刷新全部项目编辑器
    fun refreshAll()
    {
        for (controller in controllers.values)
        {
            controller.refreshNow()
        }
    }

    //清除全部项目覆盖
    fun clearAll()
    {
        for (controller in controllers.values)
        {
            controller.clear()
        }
    }

    //捕获全部打开文本文件快照
    fun captureCurrentState()
    {
        for (controller in controllers.values)
        {
            val file = FileDocumentManager.getInstance().getFile(controller.editor.document)

            if (file != null)
            {
                snapshotProvider.capture(
                    file,
                    controller.currentText()
                )
            }
        }

        ChangeOverlaySettings.getInstance().state.baselineMode = BaselineMode.SESSION_SNAPSHOT
        refreshAll()
    }

    //切回GitHead基线
    fun useGitHeadBaseline()
    {
        ChangeOverlaySettings.getInstance().state.baselineMode = BaselineMode.GIT_HEAD
        refreshAll()
    }

    //注册单个项目编辑器
    private fun registerEditor(editor: Editor)
    {
        if (editor.project != project)
        {
            return
        }

        if (controllers.containsKey(editor))
        {
            return
        }

        val controller = EditorOverlayController(
            editor,
            ::baselineProvider,
            MyersLineDiffEngine()
        )
        controllers[editor] = controller
        Disposer.register(this, controller)
        controller.refreshNow()
    }

    //注销单个项目编辑器
    private fun unregisterEditor(editor: Editor)
    {
        val controller = controllers.remove(editor)

        if (controller != null)
        {
            Disposer.dispose(controller)
        }
    }

    //释放项目资源
    override fun dispose()
    {
        snapshotProvider.clear()
        controllers.clear()
    }
}
