package com.bbbwork.changeoverlay.diff

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

//行差异引擎测试
class MyersLineDiffEngineTest
{
    private val engine = MyersLineDiffEngine()

    //测试单行新增
    @Test
    fun insertsSingleLine()
    {
        assertOperation(
            "a",
            "a\nb",
            DiffOperation.INSERT
        )
    }

    //测试单行删除
    @Test
    fun deletesSingleLine()
    {
        assertOperation(
            "a\nb",
            "a",
            DiffOperation.DELETE
        )
    }

    //测试单行修改
    @Test
    fun changesSingleLine()
    {
        assertOperation(
            "old",
            "new",
            DiffOperation.CHANGE
        )
    }

    //测试多行新增
    @Test
    fun insertsMultipleLines()
    {
        val block = changedBlocks("a", "a\nb\nc").single()
        assertEquals(DiffOperation.INSERT, block.operation)
        assertEquals(listOf("b", "c"), block.revisedLines)
    }

    //测试多行删除
    @Test
    fun deletesMultipleLines()
    {
        val block = changedBlocks("a\nb\nc", "a").single()
        assertEquals(DiffOperation.DELETE, block.operation)
        assertEquals(listOf("b", "c"), block.originalLines)
    }

    //测试多行替换
    @Test
    fun replacesMultipleLines()
    {
        val block = changedBlocks(
            "a\nold1\nold2\nz",
            "a\nnew1\nnew2\nz"
        ).single()
        assertEquals(DiffOperation.CHANGE, block.operation)
        assertEquals(listOf("old1", "old2"), block.originalLines)
        assertEquals(listOf("new1", "new2"), block.revisedLines)
    }

    //测试文件开头变化
    @Test
    fun changesAtFileStart()
    {
        val block = changedBlocks("old\nz", "new\nz").single()
        assertEquals(0, block.originalStartLine)
        assertEquals(0, block.revisedStartLine)
    }

    //测试文件末尾变化
    @Test
    fun changesAtFileEnd()
    {
        val block = changedBlocks("a\nold", "a\nnew").single()
        assertEquals(1, block.originalStartLine)
        assertEquals(1, block.revisedStartLine)
    }

    //测试空文件变化
    @Test
    fun handlesEmptyFile()
    {
        assertOperation(
            "",
            "中文",
            DiffOperation.INSERT
        )
    }

    //测试CRLF统一
    @Test
    fun normalizesCrLf()
    {
        assertTrue(engine.diff("a\r\nb", "a\nb").blocks.all {
            it.operation == DiffOperation.EQUAL
        })
    }

    //测试Unicode和中文
    @Test
    fun handlesUnicodeAndChinese()
    {
        val block = changedBlocks("你好 🌏", "你好 🌍").single()
        assertEquals(DiffOperation.CHANGE, block.operation)
    }

    //测试末尾换行差异
    @Test
    fun detectsMissingFinalNewline()
    {
        assertTrue(changedBlocks("a\n", "a").isNotEmpty())
    }

    //断言包含指定差异类型
    private fun assertOperation(
        original: String,
        revised: String,
        operation: DiffOperation
    )
    {
        assertTrue(changedBlocks(original, revised).any {
            it.operation == operation
        })
    }

    //提取非相同行块
    private fun changedBlocks(
        original: String,
        revised: String
    ): List<DiffBlock>
    {
        return engine.diff(original, revised).blocks.filter {
            it.operation != DiffOperation.EQUAL
        }
    }
}
