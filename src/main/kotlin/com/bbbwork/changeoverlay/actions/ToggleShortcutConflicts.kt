package com.bbbwork.changeoverlay.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.keymap.KeymapManager
import javax.swing.KeyStroke

//开关快捷键冲突检测
object ToggleShortcutConflicts
{
    //Toggle操作在 plugin.xml 中的注册标识
    private const val TOGGLE_ACTION_ID = "com.bbbwork.changeoverlay.actions.Toggle"

    //返回冲突提示文本 无冲突返回null
    fun describe(keyStroke: KeyStroke): String?
    {
        val keymap = KeymapManager.getInstance()?.activeKeymap
            ?: return null

        //查询占用该快捷键的全部操作 排除Toggle自身
        val occupants = keymap
            .getActionIdList(KeyboardShortcut(keyStroke, null))
            .filter {
                it != TOGGLE_ACTION_ID
            }

        if (occupants.isEmpty())
        {
            return null
        }

        val names = occupants.map {
            actionName(it)
        }

        return "快捷键已被占用: ${names.joinToString("、")} / Shortcut already used by: ${names.joinToString(", ")}"
    }

    //读取操作显示名 取不到时用操作id兜底
    private fun actionName(actionId: String): String
    {
        val action = ActionManager.getInstance().getAction(actionId)
        val text = action?.templatePresentation?.text

        if (text.isNullOrBlank())
        {
            return actionId
        }

        return text
    }
}
