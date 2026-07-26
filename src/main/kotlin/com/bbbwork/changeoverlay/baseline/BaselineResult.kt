package com.bbbwork.changeoverlay.baseline

//基线读取结果
sealed interface BaselineResult
{
    //成功读取基线
    data class Success(
        val text: String
    ) : BaselineResult

    //跳过当前文件
    data class Skipped(
        val reason: String
    ) : BaselineResult

    //读取基线失败
    data class Failure(
        val message: String
    ) : BaselineResult
}
