package com.bbbwork.changeoverlay.actions

import com.bbbwork.changeoverlay.services.ChangeOverlayToggleService
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
        ChangeOverlayToggleService.getInstance().setEnabled(state)
    }
}
