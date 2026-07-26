package com.bbbwork.changeoverlay.baseline

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.AppExecutorUtil
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

//GitHead基线提供器
class GitHeadBaselineProvider(
    private val project: Project
) : BaselineProvider
{
    companion object
    {
        //Git命令最大等待秒数
        private const val COMMAND_TIMEOUT_SECONDS = 5L
    }

    private val logger = Logger.getInstance(GitHeadBaselineProvider::class.java)

    //读取GitHead基线
    override fun readBaseline(file: VirtualFile): BaselineResult
    {
        val basePath = project.basePath
            ?: return BaselineResult.Failure("Project has no base directory")
        val relativePath = relativeGitPath(basePath, file)
            ?: return BaselineResult.Skipped("File is outside the project")
        val repositoryRoot = findRepositoryRoot(file)
            ?: return BaselineResult.Failure("Project is not inside a Git repository")
        val repositoryRelativePath = relativeGitPath(repositoryRoot.path, file)
            ?: return BaselineResult.Skipped("File is outside the Git repository")

        return runGitShow(repositoryRoot.path, repositoryRelativePath)
    }

    //查找最近Git仓库根目录
    private fun findRepositoryRoot(file: VirtualFile): VirtualFile?
    {
        var current = file.parent

        while (current != null)
        {
            if (current.findChild(".git") != null)
            {
                return current
            }

            current = current.parent
        }

        return null
    }

    //转换Git相对路径
    private fun relativeGitPath(rootPath: String, file: VirtualFile): String?
    {
        val root = java.nio.file.Path.of(rootPath).toAbsolutePath().normalize()
        val target = java.nio.file.Path.of(file.path).toAbsolutePath().normalize()

        if (!target.startsWith(root))
        {
            return null
        }

        return root.relativize(target).joinToString("/")
    }

    //执行GitShow关键调用
    private fun runGitShow(repositoryRoot: String, relativePath: String): BaselineResult
    {
        val process = try
        {
            ProcessBuilder(
                "git",
                "-C",
                repositoryRoot,
                "show",
                "HEAD:$relativePath"
            )
                .redirectErrorStream(true)
                .start()
        }
        catch (exception: Exception)
        {
            logger.warn("Unable to start Git for $relativePath", exception)

            return BaselineResult.Failure("Git is unavailable")
        }

        //并行消费Git输出避免进程管道阻塞
        val outputFuture = CompletableFuture.supplyAsync(
            {
                process.inputStream.readBytes()
            },
            AppExecutorUtil.getAppExecutorService()
        )
        val completed = process.waitFor(
            COMMAND_TIMEOUT_SECONDS,
            TimeUnit.SECONDS
        )

        if (!completed)
        {
            process.destroyForcibly()
            outputFuture.cancel(true)
            logger.warn("Git baseline timed out for $relativePath")

            return BaselineResult.Failure("Git baseline command timed out")
        }

        val output = outputFuture
            .get(1, TimeUnit.SECONDS)
            .toString(StandardCharsets.UTF_8)

        if (process.exitValue() == 0)
        {
            return BaselineResult.Success(output)
        }

        if (isMissingHeadPath(output))
        {
            return BaselineResult.Success("")
        }

        logger.warn("Git baseline failed for $relativePath ${output.trim()}")

        return BaselineResult.Failure("Unable to read Git HEAD baseline")
    }

    //判断Head中文件缺失
    private fun isMissingHeadPath(error: String): Boolean
    {
        if (error.contains("exists on disk, but not in", ignoreCase = true))
        {
            return true
        }

        if (error.contains("does not exist in", ignoreCase = true))
        {
            return true
        }

        if (error.contains("invalid object name", ignoreCase = true))
        {
            return true
        }

        return false
    }
}
