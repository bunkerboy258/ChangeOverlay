package com.bbbwork.changeoverlay.settings

import java.awt.event.KeyEvent
import kotlin.test.Test
import kotlin.test.assertEquals

//按键捕获输入框测试
class ShortcutCaptureFieldTest
{
    //测试捕获组合键
    @Test
    fun capturesModifierCombination()
    {
        val field = ShortcutCaptureField()
        pressKey(
            field,
            KeyEvent.CTRL_DOWN_MASK or KeyEvent.ALT_DOWN_MASK,
            KeyEvent.VK_O
        )

        assertEquals("ctrl alt pressed O", field.keystrokeText())
        assertEquals("Ctrl+Alt+O", field.text)
    }

    //测试单独按下修饰键不记录
    @Test
    fun ignoresModifierOnlyPress()
    {
        val field = ShortcutCaptureField()
        pressKey(
            field,
            KeyEvent.CTRL_DOWN_MASK,
            KeyEvent.VK_CONTROL
        )

        assertEquals("", field.keystrokeText())
        assertEquals("", field.text)
    }

    //测试Backspace清除已捕获快捷键
    @Test
    fun clearsOnBackspace()
    {
        val field = ShortcutCaptureField()
        pressKey(
            field,
            KeyEvent.CTRL_DOWN_MASK,
            KeyEvent.VK_D
        )
        pressKey(field, 0, KeyEvent.VK_BACK_SPACE)

        assertEquals("", field.keystrokeText())
        assertEquals("", field.text)
    }

    //测试Tab不参与捕获
    @Test
    fun ignoresTab()
    {
        val field = ShortcutCaptureField()
        pressKey(field, 0, KeyEvent.VK_TAB)

        assertEquals("", field.keystrokeText())
    }

    //测试回填已持久化快捷键
    @Test
    fun restoresStoredKeyStroke()
    {
        val field = ShortcutCaptureField()
        field.setKeyStroke(
            javax.swing.KeyStroke.getKeyStroke("ctrl alt pressed O")
        )

        assertEquals("ctrl alt pressed O", field.keystrokeText())
        assertEquals("Ctrl+Alt+O", field.text)

        field.setKeyStroke(null)

        assertEquals("", field.keystrokeText())
        assertEquals("", field.text)
    }

    //向全部按键监听分发按下事件
    private fun pressKey(
        field: ShortcutCaptureField,
        modifiers: Int,
        keyCode: Int
    )
    {
        val event = KeyEvent(
            field,
            KeyEvent.KEY_PRESSED,
            System.currentTimeMillis(),
            modifiers,
            keyCode,
            KeyEvent.CHAR_UNDEFINED
        )

        for (listener in field.keyListeners)
        {
            listener.keyPressed(event)
        }
    }
}
