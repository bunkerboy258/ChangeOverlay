package com.bbbwork.changeoverlay.baseline

import com.intellij.openapi.vfs.VirtualFile
import java.util.concurrent.ConcurrentHashMap

//会话快照基线提供器
class SnapshotBaselineProvider : BaselineProvider
{
    private val snapshots = ConcurrentHashMap<String, String>()

    //读取内存快照
    override fun readBaseline(file: VirtualFile): BaselineResult
    {
        val snapshot = snapshots[file.url]
            ?: return BaselineResult.Failure("No session snapshot exists for this file")

        return BaselineResult.Success(snapshot)
    }

    //记录文件快照
    fun capture(
        file: VirtualFile,
        text: String
    )
    {
        snapshots[file.url] = text
    }

    //清除所有快照
    fun clear()
    {
        snapshots.clear()
    }
}
