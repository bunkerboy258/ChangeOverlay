package com.bbbwork.changeoverlay.rendering

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.editor.markup.TextAttributes
import java.awt.Color

//新增行高亮渲染器
class AddedLineHighlighter
{
    //添加整行背景高亮
    fun add(
        editor: Editor,
        line: Int,
        color: Color
    ): RangeHighlighter?
    {
        val document = editor.document

        if (line !in 0 until document.lineCount)
        {
            return null
        }

        val startOffset = document.getLineStartOffset(line)
        val endOffset = document.getLineEndOffset(line)
        val attributes = TextAttributes()
        attributes.backgroundColor = color

        return editor.markupModel.addRangeHighlighter(
            startOffset,
            endOffset,
            HighlighterLayer.SELECTION - 1,
            attributes,
            HighlighterTargetArea.LINES_IN_RANGE
        )
    }
}
