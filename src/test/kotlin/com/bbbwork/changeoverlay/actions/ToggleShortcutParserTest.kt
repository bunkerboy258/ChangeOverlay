package com.bbbwork.changeoverlay.actions

import java.awt.event.KeyEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

//开关快捷键解析测试
class ToggleShortcutParserTest
{
    //测试空白输入返回null
    @Test
    fun returnsNullForBlankText()
    {
        assertNull(ToggleShortcutParser.parse(""))
        assertNull(ToggleShortcutParser.parse("   "))
    }

    //测试单个按键
    @Test
    fun parsesSingleKey()
    {
        val keyStroke = ToggleShortcutParser.parse("F5")

        assertNotNull(keyStroke)
        assertEquals(KeyEvent.VK_F5, keyStroke.keyCode)
        assertEquals(0, keyStroke.modifiers)
    }

    //测试组合按键
    @Test
    fun parsesModifierCombination()
    {
        val keyStroke = ToggleShortcutParser.parse("ctrl alt O")

        assertNotNull(keyStroke)
        assertEquals(KeyEvent.VK_O, keyStroke.keyCode)
        assertEquals(
            KeyEvent.CTRL_DOWN_MASK or KeyEvent.ALT_DOWN_MASK,
            keyStroke.modifiers
        )
    }

    //测试忽略首尾空白
    @Test
    fun trimsSurroundingWhitespace()
    {
        val keyStroke = ToggleShortcutParser.parse("  shift F10  ")

        assertNotNull(keyStroke)
        assertEquals(KeyEvent.VK_F10, keyStroke.keyCode)
        assertEquals(KeyEvent.SHIFT_DOWN_MASK, keyStroke.modifiers)
    }

    //测试非法文本返回null
    @Test
    fun returnsNullForInvalidText()
    {
        assertNull(ToggleShortcutParser.parse("not a real keystroke !!!"))
    }
}
