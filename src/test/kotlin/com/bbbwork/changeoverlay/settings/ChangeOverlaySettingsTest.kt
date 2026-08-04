package com.bbbwork.changeoverlay.settings

import com.bbbwork.changeoverlay.baseline.BaselineMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame

/**
 * 插件设置持久化状态测试
 */
class ChangeOverlaySettingsTest
{
    /**
     * 验证总开关更新会替换状态对象
     */
    @Test
    fun replacesStateWhenEnabledChanges()
    {
        val settings = ChangeOverlaySettings()
        val previousState = settings.state

        val updatedState = settings.updateState {
            it.copy(enabled = false)
        }

        assertNotSame(previousState, updatedState)
        assertFalse(updatedState.enabled)
    }

    /**
     * 验证更新总开关时保留其他设置
     */
    @Test
    fun preservesOtherSettingsWhenEnabledChanges()
    {
        val settings = ChangeOverlaySettings()
        settings.loadState(
            ChangeOverlaySettings.State(
                baselineMode = BaselineMode.SESSION_SNAPSHOT,
                trackedBranchName = "feature/settings",
                backgroundOpacity = 72
            )
        )

        val updatedState = settings.updateState {
            it.copy(enabled = false)
        }

        assertEquals(BaselineMode.SESSION_SNAPSHOT, updatedState.baselineMode)
        assertEquals("feature/settings", updatedState.trackedBranchName)
        assertEquals(72, updatedState.backgroundOpacity)
    }
}
