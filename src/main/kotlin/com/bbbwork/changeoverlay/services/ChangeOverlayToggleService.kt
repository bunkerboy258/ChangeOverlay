package com.bbbwork.changeoverlay.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.ProjectManager

/**
 * 统一管理应用级覆盖开关及全部已打开项目的显示状态
 */
@Service(Service.Level.APP)
class ChangeOverlayToggleService private constructor(
    private val synchronizeProjects: (Boolean) -> Unit
)
{
    @Volatile
    private var enabled = true

    constructor() : this(::synchronizeOpenProjects)

    companion object
    {
        /**
         * 创建可注入依赖的测试实例
         *
         * @param synchronizeProjects	项目同步函数
         * @return 测试用应用级覆盖开关服务
         */
        internal fun createForTest(
            synchronizeProjects: (Boolean) -> Unit
        ): ChangeOverlayToggleService
        {
            return ChangeOverlayToggleService(synchronizeProjects)
        }

        /** @return 应用级覆盖开关服务 */
        fun getInstance(): ChangeOverlayToggleService
        {
            return ApplicationManager
                .getApplication()
                .getService(ChangeOverlayToggleService::class.java)
        }

        /**
         * 将开关状态同步到全部已打开项目
         *
         * @param enabled 是否启用覆盖显示
         * @return 无
         */
        private fun synchronizeOpenProjects(enabled: Boolean)
        {
            for (project in ProjectManager.getInstance().openProjects)
            {
                val service = project.getService(ChangeOverlayProjectService::class.java)

                if (enabled)
                {
                    service.refreshAll()

                    continue
                }

                service.clearAll()
            }
        }
    }

    /** @return 当前 Rider 会话是否启用覆盖显示 */
    fun isEnabled(): Boolean
    {
        return enabled
    }

    /**
     * 更新当前 Rider 会话开关并同步全部已打开项目
     *
     * @param enabled 是否启用覆盖显示
     * @return 无
     */
    fun setEnabled(enabled: Boolean)
    {
        this.enabled = enabled
        synchronizeProjects(enabled)
    }
}
