package com.bbbwork.changeoverlay.actions

import com.bbbwork.changeoverlay.settings.ChangeOverlaySettings
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction

//切换覆盖显示操作
class ToggleChangeOverlayAction : ToggleAction()
{
    //读取启用状态
    override fun isSelected(event: AnActionEvent): Boolean
    {
        return ChangeOverlaySettings.getInstance().state.enabled
    }

    //更新启用状态
    override fun setSelected(
        event: AnActionEvent,
        state: Boolean
    )
    {
        ChangeOverlaySettings.getInstance().updateState {
            it.copy(enabled = state)
        }
        val service = ChangeOverlayActionSupport.service(event)

        if (state)
        {
            service?.refreshAll()

            return
        }

        service?.clearAll()
    }

    //限制无项目时不可操作
    override fun update(event: AnActionEvent)
    {
        super.update(event)
        event.presentation.isEnabled = event.project != null
    }
}
