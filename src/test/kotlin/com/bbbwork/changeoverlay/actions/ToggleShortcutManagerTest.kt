package com.bbbwork.changeoverlay.actions

import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.actionSystem.Shortcut
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 开关快捷键同步管理器测试
 */
class ToggleShortcutManagerTest
{
    /**
     * 验证启动时配置存在但真实绑定缺失会补充快捷键
     */
    @Test
    fun addsConfiguredShortcutWhenBindingIsMissing()
    {
        val expectedShortcut = shortcut("alt pressed X")

        val plan = ToggleShortcutManager.planSynchronization(
            null,
            "alt pressed X",
            emptyArray()
        )

        assertNull(plan.shortcutToRemove)
        assertEquals(expectedShortcut, plan.shortcutToAdd)
    }

    /**
     * 验证真实绑定已存在时不会重复添加快捷键
     */
    @Test
    fun doesNothingWhenConfiguredShortcutIsAlreadyBound()
    {
        val existingShortcut = shortcut("alt pressed X")

        val plan = ToggleShortcutManager.planSynchronization(
            null,
            "alt pressed X",
            arrayOf<Shortcut>(existingShortcut)
        )

        assertNull(plan.shortcutToRemove)
        assertNull(plan.shortcutToAdd)
    }

    /**
     * 验证更换配置时移除旧绑定并添加新绑定
     */
    @Test
    fun replacesPreviousShortcut()
    {
        val previousShortcut = shortcut("alt pressed X")
        val expectedShortcut = shortcut("ctrl alt pressed X")

        val plan = ToggleShortcutManager.planSynchronization(
            "alt pressed X",
            "ctrl alt pressed X",
            arrayOf<Shortcut>(previousShortcut)
        )

        assertEquals(previousShortcut, plan.shortcutToRemove)
        assertEquals(expectedShortcut, plan.shortcutToAdd)
    }

    /**
     * 验证清空配置时只移除旧绑定
     */
    @Test
    fun removesPreviousShortcutWhenConfigurationIsCleared()
    {
        val previousShortcut = shortcut("alt pressed X")

        val plan = ToggleShortcutManager.planSynchronization(
            "alt pressed X",
            "",
            arrayOf<Shortcut>(previousShortcut)
        )

        assertEquals(previousShortcut, plan.shortcutToRemove)
        assertNull(plan.shortcutToAdd)
    }

    /**
     * 创建测试快捷键
     *
     * @param text	快捷键文本
     * @return 键盘快捷键
     */
    private fun shortcut(text: String): KeyboardShortcut
    {
        val keyStroke = requireNotNull(ToggleShortcutParser.parse(text))

        return KeyboardShortcut(keyStroke, null)
    }
}
