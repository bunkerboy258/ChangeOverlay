package com.bbbwork.changeoverlay.settings

import com.intellij.ui.components.JBTextField
import java.awt.event.InputEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.KeyStroke

//按键捕获输入框 聚焦后按下组合键自动记录
class ShortcutCaptureField : JBTextField()
{
    //当前捕获的 KeyStroke 组合键
    private var capturedStroke: KeyStroke? = null

    //冲突检测委托 返回冲突提示文本表示拒绝本次按键
    var conflictChecker: ((KeyStroke) -> String?)? = null

    init
    {
        //输入框只读 内容完全由按键捕获产生
        isEditable = false

        addKeyListener(
            object : KeyAdapter()
            {
                override fun keyPressed(event: KeyEvent)
                {
                    onKeyPressed(event)
                }
            }
        )
    }

    //返回持久化用快捷键文本
    fun keystrokeText(): String
    {
        return capturedStroke?.toString() ?: ""
    }

    //设置已有快捷键并刷新显示
    fun setKeyStroke(keyStroke: KeyStroke?)
    {
        capturedStroke = keyStroke
        text = displayText(keyStroke)
    }

    //处理按键捕获
    private fun onKeyPressed(event: KeyEvent)
    {
        //Tab保留给焦点导航不参与捕获
        if (event.keyCode == KeyEvent.VK_TAB)
        {
            return
        }

        //Backspace或Delete清除已捕获快捷键
        if (event.keyCode == KeyEvent.VK_BACK_SPACE || event.keyCode == KeyEvent.VK_DELETE)
        {
            setKeyStroke(null)
            event.consume()

            return
        }

        //单独按下修饰键时用户可能还在按住 不记录
        if (isModifierKey(event.keyCode))
        {
            return
        }

        //getKeyStrokeForEvent 直接生成 pressed 类型的 KeyStroke
        val candidate = KeyStroke.getKeyStrokeForEvent(event)
        val conflict = conflictChecker?.invoke(candidate)

        //检测到冲突时拒绝本次按键 保留原快捷键
        if (conflict != null)
        {
            text = displayText(capturedStroke)
            event.consume()

            return
        }

        setKeyStroke(candidate)
        event.consume()
    }

    //判断修饰键
    private fun isModifierKey(keyCode: Int): Boolean
    {
        if (keyCode == KeyEvent.VK_SHIFT)
        {
            return true
        }

        if (keyCode == KeyEvent.VK_CONTROL)
        {
            return true
        }

        if (keyCode == KeyEvent.VK_ALT)
        {
            return true
        }

        if (keyCode == KeyEvent.VK_ALT_GRAPH)
        {
            return true
        }

        if (keyCode == KeyEvent.VK_META)
        {
            return true
        }

        return false
    }

    //生成友好显示文本
    private fun displayText(keyStroke: KeyStroke?): String
    {
        if (keyStroke == null)
        {
            return ""
        }

        val modifiers = InputEvent.getModifiersExText(keyStroke.modifiers)
        val keyText = KeyEvent.getKeyText(keyStroke.keyCode)

        if (modifiers.isEmpty())
        {
            return keyText
        }

        return "$modifiers+$keyText"
    }
}
