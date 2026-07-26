package com.bbbwork.changeoverlay.rendering

import java.awt.Font
import java.awt.Graphics

//字体回退文本片段
internal data class FontTextRun(
    val text: String,
    val font: Font
)

//编辑器字体回退绘制器
class FontFallbackTextRenderer
{
    //绘制支持中文和Unicode的文本
    fun draw(
        graphics: Graphics,
        text: String,
        baseFont: Font,
        x: Int,
        baseline: Int
    )
    {
        val fallbackFonts = createFallbackFonts(baseFont)
        val runs = createFontRuns(text) {
            codePoint ->
            selectFont(
                codePoint,
                baseFont,
                fallbackFonts
            )
        }
        var currentX = x

        for (run in runs)
        {
            graphics.font = run.font
            graphics.drawString(
                run.text,
                currentX,
                baseline
            )
            currentX += graphics
                .getFontMetrics(run.font)
                .stringWidth(run.text)
        }
    }

    //按字体拆分连续文本片段
    internal fun createFontRuns(
        text: String,
        fontSelector: (Int) -> Font
    ): List<FontTextRun>
    {
        if (text.isEmpty())
        {
            return emptyList()
        }

        val runs = mutableListOf<FontTextRun>()
        val runText = StringBuilder()
        var runFont: Font? = null
        var offset = 0

        while (offset < text.length)
        {
            val codePoint = text.codePointAt(offset)
            val selectedFont = fontSelector(codePoint)

            if (runFont != null && runFont != selectedFont)
            {
                runs += FontTextRun(
                    runText.toString(),
                    runFont
                )
                runText.setLength(0)
            }

            runFont = selectedFont
            runText.appendCodePoint(codePoint)
            offset += Character.charCount(codePoint)
        }

        if (runFont != null)
        {
            runs += FontTextRun(
                runText.toString(),
                runFont
            )
        }

        return runs
    }

    //创建JBR逻辑回退字体
    private fun createFallbackFonts(baseFont: Font): List<Font>
    {
        return listOf(
            Font(
                Font.MONOSPACED,
                baseFont.style,
                baseFont.size
            ),
            Font(
                Font.DIALOG,
                baseFont.style,
                baseFont.size
            ),
            Font(
                Font.SANS_SERIF,
                baseFont.style,
                baseFont.size
            )
        )
    }

    //选择可显示字符的字体
    private fun selectFont(
        codePoint: Int,
        baseFont: Font,
        fallbackFonts: List<Font>
    ): Font
    {
        if (baseFont.canDisplay(codePoint))
        {
            return baseFont
        }

        for (fallbackFont in fallbackFonts)
        {
            if (fallbackFont.canDisplay(codePoint))
            {
                return fallbackFont
            }
        }

        return baseFont
    }
}
