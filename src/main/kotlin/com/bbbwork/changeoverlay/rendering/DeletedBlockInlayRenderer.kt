package com.bbbwork.changeoverlay.rendering

import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.markup.TextAttributes
import java.awt.Color
import java.awt.Graphics
import java.awt.Rectangle

//删除代码块绘制器
class DeletedBlockInlayRenderer(
    private val lines: List<String>,
    private val backgroundColor: Color,
    private val showMinusPrefix: Boolean
) : EditorCustomElementRenderer
{
    //计算删除块宽度
    override fun calcWidthInPixels(inlay: Inlay<*>): Int
    {
        return inlay.editor.scrollingModel.visibleArea.width
    }

    //计算删除块高度
    override fun calcHeightInPixels(inlay: Inlay<*>): Int
    {
        return lines.size.coerceAtLeast(1) * inlay.editor.lineHeight
    }

    //绘制只读删除内容
    override fun paint(
        inlay: Inlay<*>,
        graphics: Graphics,
        targetRegion: Rectangle,
        textAttributes: TextAttributes
    )
    {
        val editor = inlay.editor
        val visibleArea = editor.scrollingModel.visibleArea
        graphics.color = backgroundColor
        graphics.fillRect(
            visibleArea.x,
            targetRegion.y,
            visibleArea.width,
            targetRegion.height
        )
        graphics.color = editor.colorsScheme.defaultForeground
        graphics.font = editor.colorsScheme.getFont(EditorFontType.PLAIN)
        val fontMetrics = graphics.fontMetrics

        for ((index, line) in lines.withIndex())
        {
            val prefix = if (showMinusPrefix) "- " else ""
            val expandedLine = line.replace("\t", "    ")
            val baseline = targetRegion.y +
                index * editor.lineHeight +
                editor.ascent

            graphics.drawString(
                prefix + expandedLine,
                targetRegion.x,
                baseline
            )
        }
    }
}
