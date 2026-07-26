package com.bbbwork.changeoverlay.rendering

import com.bbbwork.changeoverlay.diff.DiffOperation
import com.bbbwork.changeoverlay.diff.LineDiffResult

//编辑器覆盖映射结果
data class EditorOverlayMapping(
    val addedLines: Set<Int>,
    val modifiedLines: Set<Int>,
    val deletedBlocks: List<DeletedBlockMapping>
)

//删除块编辑器位置
data class DeletedBlockMapping(
    val line: Int,
    val lines: List<String>,
    val modified: Boolean
)

//差异到编辑器位置映射器
object DiffToEditorMapper
{
    //映射新增行和删除块
    fun map(result: LineDiffResult): EditorOverlayMapping
    {
        val addedLines = linkedSetOf<Int>()
        val modifiedLines = linkedSetOf<Int>()
        val deletedBlocks = mutableListOf<DeletedBlockMapping>()

        for (block in result.blocks)
        {
            if (block.operation == DiffOperation.INSERT)
            {
                addRevisedLines(
                    addedLines,
                    block.revisedStartLine,
                    block.revisedLines.size
                )
            }

            if (block.operation == DiffOperation.DELETE)
            {
                deletedBlocks += DeletedBlockMapping(
                    block.revisedStartLine,
                    block.originalLines,
                    false
                )
            }

            if (block.operation == DiffOperation.CHANGE)
            {
                deletedBlocks += DeletedBlockMapping(
                    block.revisedStartLine,
                    block.originalLines,
                    true
                )

                addRevisedLines(
                    modifiedLines,
                    block.revisedStartLine,
                    block.revisedLines.size
                )
            }
        }

        return EditorOverlayMapping(
            addedLines,
            modifiedLines,
            deletedBlocks
        )
    }

    //记录连续当前文本行
    private fun addRevisedLines(
        lines: MutableSet<Int>,
        startLine: Int,
        count: Int
    )
    {
        for (line in startLine until startLine + count)
        {
            lines += line
        }
    }
}
