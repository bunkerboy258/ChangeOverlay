package com.bbbwork.changeoverlay.actions

import com.bbbwork.changeoverlay.services.ChangeOverlayProjectService
import com.intellij.openapi.actionSystem.AnActionEvent

//操作公共服务读取
object ChangeOverlayActionSupport
{
    //读取当前项目服务
    fun service(event: AnActionEvent): ChangeOverlayProjectService?
    {
        val project = event.project

        if (project == null)
        {
            return null
        }

        return project.getService(ChangeOverlayProjectService::class.java)
    }
}
