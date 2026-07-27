package com.bbbwork.changeoverlay.actions

import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.keymap.KeymapManager

//开关快捷键动态注册管理器
object ToggleShortcutManager
{
    //Toggle操作在 plugin.xml 中的注册标识
    private const val TOGGLE_ACTION_ID = "com.bbbwork.changeoverlay.actions.Toggle"

    private val logger = Logger.getInstance(ToggleShortcutManager::class.java)

    //替换开关快捷键 空白文本表示清除 快捷键随当前Keymap持久化
    fun apply(
        previousText: String,
        newText: String
    )
    {
        if (previousText == newText)
        {
            return
        }

        val keymap = KeymapManager.getInstance()?.activeKeymap

        if (keymap == null)
        {
            logger.warn("No active keymap to register the toggle shortcut")

            return
        }

        //先移除上一次写入的快捷键
        val previousStroke = ToggleShortcutParser.parse(previousText)

        if (previousStroke != null)
        {
            keymap.removeShortcut(
                TOGGLE_ACTION_ID,
                KeyboardShortcut(previousStroke, null)
            )
        }

        val newStroke = ToggleShortcutParser.parse(newText)

        if (newStroke == null)
        {
            return
        }

        keymap.addShortcut(
            TOGGLE_ACTION_ID,
            KeyboardShortcut(newStroke, null)
        )
    }
}
