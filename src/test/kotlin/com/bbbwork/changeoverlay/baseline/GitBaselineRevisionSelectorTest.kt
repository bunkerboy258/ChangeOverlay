package com.bbbwork.changeoverlay.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

//Git基线版本选择器测试
class GitBaselineRevisionSelectorTest
{
    //测试关闭开关使用Head
    @Test
    fun usesHeadWhenTrackingIsDisabled()
    {
        assertRevision(
            expected = "HEAD",
            enabled = false,
            trackedBranch = "main",
            currentBranch = "main",
            clean = true,
            hasParent = true
        )
    }

    //测试干净跟踪分支使用父提交
    @Test
    fun usesHeadParentForCleanTrackedBranch()
    {
        assertRevision(
            expected = "HEAD^",
            enabled = true,
            trackedBranch = "main",
            currentBranch = "main",
            clean = true,
            hasParent = true
        )
    }

    //测试脏工作区使用Head
    @Test
    fun usesHeadForDirtyWorktree()
    {
        assertRevision(
            expected = "HEAD",
            enabled = true,
            trackedBranch = "main",
            currentBranch = "main",
            clean = false,
            hasParent = true
        )
    }

    //测试非跟踪分支使用Head
    @Test
    fun usesHeadForDifferentBranch()
    {
        assertRevision(
            expected = "HEAD",
            enabled = true,
            trackedBranch = "main",
            currentBranch = "feature",
            clean = true,
            hasParent = true
        )
    }

    //测试首次提交使用Head
    @Test
    fun usesHeadWithoutParentCommit()
    {
        assertRevision(
            expected = "HEAD",
            enabled = true,
            trackedBranch = "main",
            currentBranch = "main",
            clean = true,
            hasParent = false
        )
    }

    //断言Git基线版本
    private fun assertRevision(
        expected: String,
        enabled: Boolean,
        trackedBranch: String,
        currentBranch: String,
        clean: Boolean,
        hasParent: Boolean
    )
    {
        val state = GitRepositoryState(
            currentBranch,
            clean,
            hasParent
        )
        val revision = GitBaselineRevisionSelector.select(
            enabled,
            trackedBranch,
            state
        )

        assertEquals(
            expected,
            revision
        )
    }
}
