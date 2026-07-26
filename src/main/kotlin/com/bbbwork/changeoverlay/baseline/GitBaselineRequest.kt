package com.bbbwork.changeoverlay.baseline

//Git基线读取请求
data class GitBaselineRequest(
    val repositoryRoot: String,
    val relativePath: String,
    val revision: String
)

//Git仓库当前状态
data class GitRepositoryState(
    val currentBranch: String,
    val worktreeClean: Boolean,
    val hasHeadParent: Boolean
)
