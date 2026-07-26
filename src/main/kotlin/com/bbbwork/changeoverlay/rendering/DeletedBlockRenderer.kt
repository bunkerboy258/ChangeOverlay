package com.bbbwork.changeoverlay.rendering

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Inlay
import java.awt.Color

//删除代码块渲染器
class DeletedBlockRenderer
{
    //在映射位置添加块级Inlay
    fun add(
        editor: Editor,
        line: Int,
        lines: List<String>,
        color: Color,
        showMinusPrefix: Boolean
    ): Inlay<*>?
    {
        if (lines.isEmpty())
        {
            return null
        }

        val document = editor.document
        val offset = if (line >= document.lineCount)
        {
            document.textLength
        }
        else
        {
            document.getLineStartOffset(line.coerceAtLeast(0))
        }
        val renderer = DeletedBlockInlayRenderer(
            lines,
            color,
            showMinusPrefix
        )

        return editor.inlayModel.addBlockElement(
            offset,
            true,
            true,
            0,
            renderer
        )
    }
}
