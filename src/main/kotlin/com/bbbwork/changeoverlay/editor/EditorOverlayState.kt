package com.bbbwork.changeoverlay.editor

import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.markup.RangeHighlighter

//单编辑器覆盖资源状态
class EditorOverlayState
{
    val highlighters = mutableListOf<RangeHighlighter>()
    val inlays = mutableListOf<Inlay<*>>()

    //释放全部覆盖资源
    fun clear()
    {
        for (highlighter in highlighters)
        {
            highlighter.dispose()
        }

        for (inlay in inlays)
        {
            inlay.dispose()
        }

        highlighters.clear()
        inlays.clear()
    }
}
