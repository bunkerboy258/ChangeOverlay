package com.bbbwork.changeoverlay.actions

import javax.swing.KeyStroke

//开关快捷键文本解析器
object ToggleShortcutParser
{
    //解析快捷键文本 空白或非法文本返回null
    fun parse(text: String): KeyStroke?
    {
        val trimmed = text.trim()

        if (trimmed.isEmpty())
        {
            return null
        }

        //KeyStroke.getKeyStroke 支持 ctrl alt O 这类空格分隔写法 解析失败返回null
        return KeyStroke.getKeyStroke(trimmed)
    }
}
