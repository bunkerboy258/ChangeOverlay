package com.bbbwork.changeoverlay.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

//手动刷新覆盖操作
class RefreshChangeOverlayAction : AnAction()
{
    //立即刷新全部编辑器
    override fun actionPerformed(event: AnActionEvent)
    {
        ChangeOverlayActionSupport.service(event)?.refreshAll()
    }

    //限制无项目时不可操作
    override fun update(event: AnActionEvent)
    {
        event.presentation.isEnabled = event.project != null
    }
}
