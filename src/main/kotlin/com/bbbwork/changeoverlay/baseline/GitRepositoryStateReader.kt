package com.bbbwork.changeoverlay.baseline

import com.intellij.util.concurrency.AppExecutorUtil
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

//Git仓库状态读取器
class GitRepositoryStateReader
{
    companion object
    {
        //Git命令最大等待秒数
        private const val COMMAND_TIMEOUT_SECONDS = 5L
    }

    //Git命令执行结果
    sealed interface CommandResult
    {
        //Git命令成功
        data class Success(
            val output: String
        ) : CommandResult

        //Git命令失败
        data class Failure(
            val output: String
        ) : CommandResult

        //Git命令不可用
        data object Unavailable : CommandResult

        //Git命令超时
        data object TimedOut : CommandResult
    }

    //查找最近Git仓库根目录
    fun findRepositoryRoot(startPath: String): String?
    {
        var current = Path.of(startPath)
            .toAbsolutePath()
            .normalize()

        if (!Files.isDirectory(current))
        {
            current = current.parent
        }

        while (current != null)
        {
            if (Files.exists(current.resolve(".git")))
            {
                return current.toString()
            }

            current = current.parent
        }

        return null
    }

    //读取分支工作区和父提交状态
    fun readState(repositoryRoot: String): GitRepositoryState?
    {
        val branchResult = execute(
            repositoryRoot,
            "branch",
            "--show-current"
        )
        val statusResult = execute(
            repositoryRoot,
            "status",
            "--porcelain"
        )
        val parentResult = execute(
            repositoryRoot,
            "rev-parse",
            "--verify",
            "HEAD^"
        )

        if (branchResult !is CommandResult.Success)
        {
            return null
        }

        if (statusResult !is CommandResult.Success)
        {
            return null
        }

        return GitRepositoryState(
            currentBranch = branchResult.output.trim(),
            worktreeClean = statusResult.output.isBlank(),
            hasHeadParent = parentResult is CommandResult.Success
        )
    }

    //读取全部本地分支
    fun readLocalBranches(repositoryRoot: String): List<String>
    {
        val result = execute(
            repositoryRoot,
            "for-each-ref",
            "--format=%(refname:short)",
            "refs/heads"
        )

        if (result !is CommandResult.Success)
        {
            return emptyList()
        }

        return result.output
            .lineSequence()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinct()
            .sorted()
            .toList()
    }

    //读取指定Git版本文件
    fun readFile(request: GitBaselineRequest): CommandResult
    {
        return execute(
            request.repositoryRoot,
            "show",
            "${request.revision}:${request.relativePath}"
        )
    }

    //执行Git命令关键调用
    private fun execute(
        repositoryRoot: String,
        vararg arguments: String
    ): CommandResult
    {
        val command = mutableListOf(
            "git",
            "-C",
            repositoryRoot
        )
        command.addAll(arguments)

        val process = try
        {
            ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()
        }
        catch (_: Exception)
        {
            return CommandResult.Unavailable
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

            return CommandResult.TimedOut
        }

        val output = outputFuture
            .get(1, TimeUnit.SECONDS)
            .toString(StandardCharsets.UTF_8)

        if (process.exitValue() == 0)
        {
            return CommandResult.Success(output)
        }

        return CommandResult.Failure(output)
    }
}
