package com.bbbwork.changeoverlay.settings

import com.bbbwork.changeoverlay.baseline.BaselineMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

/**
 * 持久化配置状态测试
 */
class PersistentSettingsStateTest
{
    /**
     * 验证持久化配置变化时替换状态对象
     */
    @Test
    fun replacesStateWhenPersistentSettingChanges()
    {
        val settings = ChangeOverlaySettings()
        val previousState = settings.state

        val updatedState = settings.updateState {
            it.copy(backgroundOpacity = 72)
        }

        assertNotSame(previousState, updatedState)
        assertEquals(72, updatedState.backgroundOpacity)
    }

    /**
     * 验证更新单项持久化配置时保留其他配置
     */
    @Test
    fun preservesOtherPersistentSettingsWhenOneChanges()
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
            it.copy(showMinusPrefix = false)
        }

        assertEquals(BaselineMode.SESSION_SNAPSHOT, updatedState.baselineMode)
        assertEquals("feature/settings", updatedState.trackedBranchName)
        assertEquals(72, updatedState.backgroundOpacity)
    }
}
