package com.bbbwork.changeoverlay.diff

//行差异计算器
fun interface LineDiffEngine
{
    //计算原始文本和当前文本差异
    fun diff(
        originalText: String,
        revisedText: String
    ): LineDiffResult
}
