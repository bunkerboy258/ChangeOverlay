package com.bbbwork.changeoverlay.services

import com.bbbwork.changeoverlay.actions.ToggleShortcutManager
import com.bbbwork.changeoverlay.settings.ChangeOverlaySettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

//项目启动初始化活动
class ChangeOverlayStartupActivity : ProjectActivity
{
    //初始化项目覆盖服务
    override suspend fun execute(project: Project)
    {
        project.getService(ChangeOverlayProjectService::class.java)

        //恢复持久化的开关快捷键
        ToggleShortcutManager.apply(
            ChangeOverlaySettings.getInstance().state.toggleShortcutKeystroke
        )
    }
}
