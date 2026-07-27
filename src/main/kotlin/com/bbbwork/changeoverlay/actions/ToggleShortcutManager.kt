package com.bbbwork.changeoverlay.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.CustomShortcutSet
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.actionSystem.ShortcutSet
import com.intellij.openapi.diagnostic.Logger

//开关快捷键动态注册管理器
object ToggleShortcutManager
{
    //Toggle操作在 plugin.xml 中的注册标识
    private const val TOGGLE_ACTION_ID = "com.bbbwork.changeoverlay.actions.Toggle"

    private val logger = Logger.getInstance(ToggleShortcutManager::class.java)

    //记录当前已注册的 ShortcutSet 便于下次注销
    private var registeredShortcut: ShortcutSet? = null

    //应用快捷键文本 空白表示清除快捷键
    fun apply(keystrokeText: String)
    {
        val action = ActionManager.getInstance().getAction(TOGGLE_ACTION_ID)

        if (action == null)
        {
            logger.warn("Toggle action $TOGGLE_ACTION_ID is not registered")

            return
        }

        //先注销上一次注册的快捷键
        val previous = registeredShortcut

        if (previous != null)
        {
            action.unregisterCustomShortcutSet(previous)
            registeredShortcut = null
        }

        val keyStroke = ToggleShortcutParser.parse(keystrokeText)

        if (keyStroke == null)
        {
            return
        }

        //组件参数传null表示全局注册
        val shortcutSet = CustomShortcutSet(KeyboardShortcut(keyStroke, null))
        action.registerCustomShortcutSet(shortcutSet, null)
        registeredShortcut = shortcutSet
    }
}
