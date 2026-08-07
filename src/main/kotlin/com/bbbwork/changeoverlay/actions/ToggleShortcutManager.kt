package com.bbbwork.changeoverlay.actions

import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.actionSystem.Shortcut
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.keymap.KeymapManager

//开关快捷键动态注册管理器
object ToggleShortcutManager
{
    /**
     * 快捷键同步操作计划
     *
     * @param shortcutToRemove	需要移除的旧快捷键
     * @param shortcutToAdd	需要补充的新快捷键
     */
    internal data class SynchronizationPlan(
        val shortcutToRemove: KeyboardShortcut?,
        val shortcutToAdd: KeyboardShortcut?
    )

    //Toggle操作在 plugin.xml 中的注册标识
    private const val TOGGLE_ACTION_ID = "com.bbbwork.changeoverlay.actions.Toggle"

    private val logger = Logger.getInstance(ToggleShortcutManager::class.java)

    //替换开关快捷键 空白文本表示清除 快捷键随当前Keymap持久化
    fun synchronize(
        previousText: String?,
        newText: String
    )
    {
        val keymap = KeymapManager.getInstance()?.activeKeymap

        if (keymap == null)
        {
            logger.warn("No active keymap to register the toggle shortcut")

            return
        }

        val plan = planSynchronization(
            previousText,
            newText,
            keymap.getShortcuts(TOGGLE_ACTION_ID)
        )

        if (plan.shortcutToRemove != null)
        {
            keymap.removeShortcut(
                TOGGLE_ACTION_ID,
                plan.shortcutToRemove
            )
        }

        if (plan.shortcutToAdd == null)
        {
            return
        }

        keymap.addShortcut(
            TOGGLE_ACTION_ID,
            plan.shortcutToAdd
        )
    }

    /**
     * 根据配置与当前真实绑定生成幂等同步计划
     *
     * @param previousText	设置变更前的快捷键文本，启动恢复时传入空值
     * @param newText	目标快捷键文本
     * @param currentShortcuts	当前操作的真实快捷键绑定
     * @return 需要执行的最小同步计划
     */
    internal fun planSynchronization(
        previousText: String?,
        newText: String,
        currentShortcuts: Array<out Shortcut>
    ): SynchronizationPlan
    {
        val previousStroke = previousText?.let(ToggleShortcutParser::parse)
        val previousShortcut = previousStroke?.let {
            KeyboardShortcut(it, null)
        }
        val newStroke = ToggleShortcutParser.parse(newText)
        val newShortcut = newStroke?.let {
            KeyboardShortcut(it, null)
        }
        var shortcutToRemove: KeyboardShortcut? = null

        if (
            previousText != null &&
            previousText != newText &&
            previousShortcut != null &&
            currentShortcuts.contains(previousShortcut)
        )
        {
            shortcutToRemove = previousShortcut
        }

        val newShortcutAlreadyBound = newShortcut != null &&
            currentShortcuts.contains(newShortcut) &&
            newShortcut != shortcutToRemove
        var shortcutToAdd: KeyboardShortcut? = null

        if (newShortcut != null && !newShortcutAlreadyBound)
        {
            shortcutToAdd = newShortcut
        }

        return SynchronizationPlan(
            shortcutToRemove,
            shortcutToAdd
        )
    }
}
