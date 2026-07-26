package com.bbbwork.changeoverlay.baseline

import com.bbbwork.changeoverlay.settings.ChangeOverlaySettings
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

//GitHead基线提供器
class GitHeadBaselineProvider(
    private val project: Project,
    private val repositoryStateReader: GitRepositoryStateReader = GitRepositoryStateReader()
) : BaselineProvider
{
    private val logger = Logger.getInstance(GitHeadBaselineProvider::class.java)

    //读取Git基线
    override fun readBaseline(file: VirtualFile): BaselineResult
    {
        val basePath = project.basePath
            ?: return BaselineResult.Failure("Project has no base directory")
        relativeGitPath(basePath, file)
            ?: return BaselineResult.Skipped("File is outside the project")

        val repositoryRoot = repositoryStateReader.findRepositoryRoot(file.path)
            ?: return BaselineResult.Failure("Project is not inside a Git repository")
        val repositoryRelativePath = relativeGitPath(repositoryRoot, file)
            ?: return BaselineResult.Skipped("File is outside the Git repository")
        val repositoryState = repositoryStateReader.readState(repositoryRoot)
            ?: return BaselineResult.Failure("Unable to read Git repository state")
        val settings = ChangeOverlaySettings.getInstance().state
        val revision = GitBaselineRevisionSelector.select(
            settings.trackBranchCommitHistory,
            settings.trackedBranchName,
            repositoryState
        )
        val request = GitBaselineRequest(
            repositoryRoot,
            repositoryRelativePath,
            revision
        )

        return mapCommandResult(
            request,
            repositoryStateReader.readFile(request)
        )
    }

    //转换Git命令结果
    private fun mapCommandResult(
        request: GitBaselineRequest,
        result: GitRepositoryStateReader.CommandResult
    ): BaselineResult
    {
        if (result is GitRepositoryStateReader.CommandResult.Success)
        {
            return BaselineResult.Success(result.output)
        }

        if (result is GitRepositoryStateReader.CommandResult.Unavailable)
        {
            return BaselineResult.Failure("Git is unavailable")
        }

        if (result is GitRepositoryStateReader.CommandResult.TimedOut)
        {
            logger.warn("Git baseline timed out for ${request.relativePath}")

            return BaselineResult.Failure("Git baseline command timed out")
        }

        if (result is GitRepositoryStateReader.CommandResult.Failure)
        {
            if (isMissingRevisionPath(result.output))
            {
                return BaselineResult.Success("")
            }

            logger.warn(
                "Git baseline failed for ${request.revision} ${request.relativePath} ${result.output.trim()}"
            )
        }

        return BaselineResult.Failure("Unable to read Git baseline")
    }

    //转换Git相对路径
    private fun relativeGitPath(
        rootPath: String,
        file: VirtualFile
    ): String?
    {
        val root = java.nio.file.Path.of(rootPath).toAbsolutePath().normalize()
        val target = java.nio.file.Path.of(file.path).toAbsolutePath().normalize()

        if (!target.startsWith(root))
        {
            return null
        }

        return root.relativize(target).joinToString("/")
    }

    //判断Git版本中文件缺失
    private fun isMissingRevisionPath(error: String): Boolean
    {
        if (error.contains("exists on disk, but not in", ignoreCase = true))
        {
            return true
        }

        if (error.contains("does not exist in", ignoreCase = true))
        {
            return true
        }

        if (error.contains("path", ignoreCase = true) &&
            error.contains("not exist", ignoreCase = true))
        {
            return true
        }

        return false
    }
}
