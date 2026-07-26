package com.bbbwork.changeoverlay.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

//捕获当前状态操作
class CaptureCurrentStateAction : AnAction()
{
    //捕获打开文件内存快照
    override fun actionPerformed(event: AnActionEvent)
    {
        ChangeOverlayActionSupport.service(event)?.captureCurrentState()
    }

    //限制无项目时不可操作
    override fun update(event: AnActionEvent)
    {
        event.presentation.isEnabled = event.project != null
    }
}
