package com.bbbwork.changeoverlay.rendering

import com.bbbwork.changeoverlay.diff.MyersLineDiffEngine
import kotlin.test.Test
import kotlin.test.assertEquals

//编辑器位置映射测试
class DiffToEditorMapperTest
{
    private val engine = MyersLineDiffEngine()

    //测试修改块红绿映射
    @Test
    fun mapsReplacementToDeletedAndAddedContent()
    {
        val mapping = DiffToEditorMapper.map(
            engine.diff(
                "a\nold1\nold2\nz",
                "a\nnew1\nnew2\nz"
            )
        )

        assertEquals(emptySet(), mapping.addedLines)
        assertEquals(setOf(1, 2), mapping.modifiedLines)
        assertEquals(1, mapping.deletedBlocks.single().line)
        assertEquals(
            listOf("old1", "old2"),
            mapping.deletedBlocks.single().lines
        )
    }

    //测试末尾删除映射
    @Test
    fun mapsEndDeletionAfterLastCurrentLine()
    {
        val mapping = DiffToEditorMapper.map(
            engine.diff(
                "a\nremoved",
                "a"
            )
        )

        assertEquals(1, mapping.deletedBlocks.single().line)
    }
}
