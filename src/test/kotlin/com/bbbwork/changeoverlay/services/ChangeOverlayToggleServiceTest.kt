package com.bbbwork.changeoverlay.services

import com.bbbwork.changeoverlay.settings.ChangeOverlaySettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 应用级覆盖开关服务测试
 */
class ChangeOverlayToggleServiceTest
{
    /**
     * 验证关闭状态同时写入设置并同步到全部项目入口
     */
    @Test
    fun disablesSettingAndSynchronizesProjects()
    {
        val settings = ChangeOverlaySettings()
        val synchronizedStates = mutableListOf<Boolean>()
        val service = ChangeOverlayToggleService.createForTest(
            settingsProvider = {
                settings
            },
            synchronizeProjects = { enabled ->
                synchronizedStates.add(enabled)
            }
        )

        service.setEnabled(false)

        assertFalse(settings.state.enabled)
        assertEquals(listOf(false), synchronizedStates)
    }

    /**
     * 验证开启状态同时写入设置并同步到全部项目入口
     */
    @Test
    fun enablesSettingAndSynchronizesProjects()
    {
        val settings = ChangeOverlaySettings()
        settings.loadState(
            ChangeOverlaySettings.State(enabled = false)
        )
        val synchronizedStates = mutableListOf<Boolean>()
        val service = ChangeOverlayToggleService.createForTest(
            settingsProvider = {
                settings
            },
            synchronizeProjects = { enabled ->
                synchronizedStates.add(enabled)
            }
        )

        service.setEnabled(true)

        assertTrue(settings.state.enabled)
        assertEquals(listOf(true), synchronizedStates)
    }
}
