package com.bbbwork.changeoverlay.services

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 当前 Rider 会话覆盖开关服务测试
 */
class ChangeOverlayRuntimeServiceTest
{
    /**
     * 验证每个 Rider 会话默认启用覆盖显示
     */
    @Test
    fun startsEnabledForEachRiderSession()
    {
        val synchronizedStates = mutableListOf<Boolean>()
        val service = ChangeOverlayToggleService.createForTest(
            synchronizeProjects = { enabled ->
                synchronizedStates.add(enabled)
            }
        )

        assertTrue(service.isEnabled())
        assertTrue(synchronizedStates.isEmpty())
    }

    /**
     * 验证关闭当前会话时同步全部项目入口
     */
    @Test
    fun disablesCurrentSessionAndSynchronizesProjects()
    {
        val synchronizedStates = mutableListOf<Boolean>()
        val service = ChangeOverlayToggleService.createForTest(
            synchronizeProjects = { enabled ->
                synchronizedStates.add(enabled)
            }
        )

        service.setEnabled(false)

        assertFalse(service.isEnabled())
        assertEquals(listOf(false), synchronizedStates)
    }

    /**
     * 验证新会话不继承上一会话的关闭状态
     */
    @Test
    fun newSessionStartsEnabledAfterPreviousSessionWasDisabled()
    {
        val previousSession = ChangeOverlayToggleService.createForTest { }
        previousSession.setEnabled(false)

        val newSession = ChangeOverlayToggleService.createForTest { }

        assertFalse(previousSession.isEnabled())
        assertTrue(newSession.isEnabled())
    }
}
