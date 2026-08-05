package com.bbbwork.changeoverlay.services

import com.bbbwork.changeoverlay.settings.ChangeOverlaySettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.ProjectManager

/**
 * 统一管理应用级覆盖开关及全部已打开项目的显示状态
 */
@Service(Service.Level.APP)
class ChangeOverlayToggleService private constructor(
    private val settingsProvider: () -> ChangeOverlaySettings,
    private val synchronizeProjects: (Boolean) -> Unit
)
{
    constructor() : this(
        settingsProvider = {
            ChangeOverlaySettings.getInstance()
        },
        synchronizeProjects = ::synchronizeOpenProjects
    )

    companion object
    {
        /**
         * 创建可注入依赖的测试实例
         *
         * @param settingsProvider	设置服务提供函数
         * @param synchronizeProjects	项目同步函数
         * @return 测试用应用级覆盖开关服务
         */
        internal fun createForTest(
            settingsProvider: () -> ChangeOverlaySettings,
            synchronizeProjects: (Boolean) -> Unit
        ): ChangeOverlayToggleService
        {
            return ChangeOverlayToggleService(
                settingsProvider,
                synchronizeProjects
            )
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

    /**
     * 更新应用级开关并同步全部已打开项目
     *
     * @param enabled 是否启用覆盖显示
     * @return 无
     */
    fun setEnabled(enabled: Boolean)
    {
        settingsProvider().updateState {
            it.copy(enabled = enabled)
        }
        synchronizeProjects(enabled)
    }
}
