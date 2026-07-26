package com.bbbwork.changeoverlay.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

//使用GitHead基线操作
class UseGitHeadBaselineAction : AnAction()
{
    //切换GitHead并刷新
    override fun actionPerformed(event: AnActionEvent)
    {
        ChangeOverlayActionSupport.service(event)?.useGitHeadBaseline()
    }

    //限制无项目时不可操作
    override fun update(event: AnActionEvent)
    {
        event.presentation.isEnabled = event.project != null
    }
}
