package com.bbbwork.changeoverlay.diff

import com.github.difflib.DiffUtils
import com.github.difflib.patch.DeltaType

//Myers行差异计算器
class MyersLineDiffEngine : LineDiffEngine
{
    //计算行级差异
    override fun diff(
        originalText: String,
        revisedText: String
    ): LineDiffResult
    {
        val originalLines = LineTextNormalizer.splitLines(originalText)
        val revisedLines = LineTextNormalizer.splitLines(revisedText)
        val patch = DiffUtils.diff(originalLines, revisedLines)
        val blocks = mutableListOf<DiffBlock>()
        var originalCursor = 0
        var revisedCursor = 0

        for (delta in patch.deltas.sortedBy { it.source.position })
        {
            val equalCount = delta.source.position - originalCursor

            if (equalCount > 0)
            {
                blocks += DiffBlock(
                    operation = DiffOperation.EQUAL,
                    originalStartLine = originalCursor,
                    revisedStartLine = revisedCursor,
                    originalLines = originalLines.subList(
                        originalCursor,
                        originalCursor + equalCount
                    ),
                    revisedLines = revisedLines.subList(
                        revisedCursor,
                        revisedCursor + equalCount
                    )
                )
            }

            val operation = when (delta.type)
            {
                DeltaType.INSERT -> DiffOperation.INSERT
                DeltaType.DELETE -> DiffOperation.DELETE
                DeltaType.CHANGE -> DiffOperation.CHANGE
                DeltaType.EQUAL -> DiffOperation.EQUAL
            }

            blocks += DiffBlock(
                operation = operation,
                originalStartLine = delta.source.position,
                revisedStartLine = delta.target.position,
                originalLines = delta.source.lines.toList(),
                revisedLines = delta.target.lines.toList()
            )

            originalCursor = delta.source.position + delta.source.size()
            revisedCursor = delta.target.position + delta.target.size()
        }

        val originalRemaining = originalLines.size - originalCursor

        if (originalRemaining > 0)
        {
            blocks += DiffBlock(
                operation = DiffOperation.EQUAL,
                originalStartLine = originalCursor,
                revisedStartLine = revisedCursor,
                originalLines = originalLines.subList(
                    originalCursor,
                    originalLines.size
                ),
                revisedLines = revisedLines.subList(
                    revisedCursor,
                    revisedLines.size
                )
            )
        }

        return LineDiffResult(blocks)
    }
}
