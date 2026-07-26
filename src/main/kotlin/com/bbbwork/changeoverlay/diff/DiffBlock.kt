package com.bbbwork.changeoverlay.diff

//连续行差异块
data class DiffBlock(
    val operation: DiffOperation,
    val originalStartLine: Int,
    val revisedStartLine: Int,
    val originalLines: List<String>,
    val revisedLines: List<String>
)
