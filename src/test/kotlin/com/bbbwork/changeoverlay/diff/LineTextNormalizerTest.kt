package com.bbbwork.changeoverlay.diff

import kotlin.test.Test
import kotlin.test.assertEquals

//文本标准化测试
class LineTextNormalizerTest
{
    //测试混合换行符
    @Test
    fun normalizesMixedLineSeparators()
    {
        assertEquals(
            listOf("甲", "乙", "丙"),
            LineTextNormalizer.splitLines("甲\r\n乙\r丙")
        )
    }

    //测试空文本
    @Test
    fun keepsEmptyFileWithoutPhantomLine()
    {
        assertEquals(
            emptyList(),
            LineTextNormalizer.splitLines("")
        )
    }
}
