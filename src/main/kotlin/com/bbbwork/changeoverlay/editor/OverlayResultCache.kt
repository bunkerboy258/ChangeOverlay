package com.bbbwork.changeoverlay.editor

import com.bbbwork.changeoverlay.rendering.EditorOverlayMapping

//最近一次覆盖渲染结果缓存
class OverlayResultCache
{
    //缓存的覆盖映射结果
    private var mapping: EditorOverlayMapping? = null

    //缓存结果对应的 Document 修改戳
    private var modificationStamp: Long = 0L

    //存入映射结果和文档修改戳
    fun store(
        mapping: EditorOverlayMapping,
        modificationStamp: Long
    )
    {
        this.mapping = mapping
        this.modificationStamp = modificationStamp
    }

    //读取缓存 文档修改戳不一致时返回null
    fun current(modificationStamp: Long): EditorOverlayMapping?
    {
        if (this.modificationStamp != modificationStamp)
        {
            return null
        }

        return mapping
    }

    //丢弃全部缓存
    fun invalidate()
    {
        mapping = null
        modificationStamp = 0L
    }
}
