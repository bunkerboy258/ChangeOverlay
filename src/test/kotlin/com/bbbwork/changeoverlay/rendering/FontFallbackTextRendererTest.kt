package com.bbbwork.changeoverlay.rendering

import java.awt.Font
import kotlin.test.Test
import kotlin.test.assertEquals

//字体回退文本绘制器测试
class FontFallbackTextRendererTest
{
    private val renderer = FontFallbackTextRenderer()
    private val editorFont = Font(
        "EditorTest",
        Font.PLAIN,
        12
    )
    private val chineseFont = Font(
        Font.DIALOG,
        Font.PLAIN,
        12
    )

    //测试中文字符切换回退字体
    @Test
    fun separatesChineseFallbackRun()
    {
        val runs = renderer.createFontRuns("code中文end") {
            codePoint ->
            if (codePoint > 127)
            {
                chineseFont
            }
            else
            {
                editorFont
            }
        }

        assertEquals(
            listOf("code", "中文", "end"),
            runs.map(FontTextRun::text)
        )
        assertEquals(
            listOf(editorFont, chineseFont, editorFont),
            runs.map(FontTextRun::font)
        )
    }

    //测试Unicode代理对保持完整
    @Test
    fun keepsUnicodeSurrogatePairTogether()
    {
        val emojiFont = Font(
            Font.SANS_SERIF,
            Font.PLAIN,
            12
        )
        val runs = renderer.createFontRuns("A🌏B") {
            codePoint ->
            if (codePoint > 0xFFFF)
            {
                emojiFont
            }
            else
            {
                editorFont
            }
        }

        assertEquals(
            listOf("A", "🌏", "B"),
            runs.map(FontTextRun::text)
        )
    }
}
