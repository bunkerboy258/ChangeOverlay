package com.bbbwork.changeoverlay.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

//清除覆盖显示操作
class ClearChangeOverlayAction : AnAction()
{
    //清除全部编辑器覆盖
    override fun actionPerformed(event: AnActionEvent)
    {
        ChangeOverlayActionSupport.service(event)?.clearAll()
    }

    //限制无项目时不可操作
    override fun update(event: AnActionEvent)
    {
        event.presentation.isEnabled = event.project != null
    }
}
