package com.bbbwork.changeoverlay.baseline

//Git基线版本选择器
object GitBaselineRevisionSelector
{
    //根据跟踪设置选择Git版本
    fun select(
        trackBranchCommitHistory: Boolean,
        trackedBranchName: String,
        repositoryState: GitRepositoryState
    ): String
    {
        if (!trackBranchCommitHistory)
        {
            return "HEAD"
        }

        if (trackedBranchName.isBlank())
        {
            return "HEAD"
        }

        if (repositoryState.currentBranch != trackedBranchName)
        {
            return "HEAD"
        }

        if (!repositoryState.worktreeClean)
        {
            return "HEAD"
        }

        if (!repositoryState.hasHeadParent)
        {
            return "HEAD"
        }

        return "HEAD^"
    }
}
