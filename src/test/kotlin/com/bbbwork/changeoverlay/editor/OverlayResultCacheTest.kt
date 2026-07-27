package com.bbbwork.changeoverlay.editor

import com.bbbwork.changeoverlay.rendering.EditorOverlayMapping
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

//覆盖结果缓存测试
class OverlayResultCacheTest
{
    //测试存入后按相同修改戳取回
    @Test
    fun returnsStoredMappingForSameStamp()
    {
        val cache = OverlayResultCache()
        val mapping = createMapping()
        cache.store(mapping, 42L)

        assertSame(mapping, cache.current(42L))
    }

    //测试修改戳不一致返回null
    @Test
    fun returnsNullForDifferentStamp()
    {
        val cache = OverlayResultCache()
        cache.store(createMapping(), 42L)

        assertNull(cache.current(43L))
    }

    //测试失效后返回null
    @Test
    fun returnsNullAfterInvalidate()
    {
        val cache = OverlayResultCache()
        cache.store(createMapping(), 42L)
        cache.invalidate()

        assertNull(cache.current(42L))
    }

    //测试新缓存默认返回null
    @Test
    fun returnsNullWhenEmpty()
    {
        val cache = OverlayResultCache()

        assertNull(cache.current(0L))
    }

    //创建空映射结果
    private fun createMapping(): EditorOverlayMapping
    {
        return EditorOverlayMapping(
            emptySet(),
            emptySet(),
            emptyList()
        )
    }
}
