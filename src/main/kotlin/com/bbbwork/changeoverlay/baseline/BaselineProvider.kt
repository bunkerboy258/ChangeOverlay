package com.bbbwork.changeoverlay.baseline

import com.intellij.openapi.vfs.VirtualFile

//基线内容提供器
fun interface BaselineProvider
{
    //读取VirtualFile文件基线
    fun readBaseline(file: VirtualFile): BaselineResult
}
