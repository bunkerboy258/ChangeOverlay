package com.bbbwork.changeoverlay.settings

import com.bbbwork.changeoverlay.baseline.BaselineMode
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

//持久化插件设置
@Service(Service.Level.APP)
@State(
    name = "ChangeOverlaySettings",
    storages = [Storage("changeOverlay.xml")]
)
class ChangeOverlaySettings : PersistentStateComponent<ChangeOverlaySettings.State>
{
    //插件设置数据
    data class State(
        var enabled: Boolean = true,
        var baselineMode: BaselineMode = BaselineMode.GIT_HEAD,
        var trackBranchCommitHistory: Boolean = false,
        var trackedBranchName: String = "",
        var showAddedLines: Boolean = true,
        var showDeletedLines: Boolean = true,
        var showModifiedLines: Boolean = true,
        var addedColorRgb: Int = 0x3A8F5A,
        var deletedColorRgb: Int = 0xB84C4C,
        var backgroundOpacity: Int = 55,
        var debounceMilliseconds: Int = 300,
        var maximumFileSizeBytes: Long = 1_048_576,
        var maximumLineCount: Int = 20_000,
        var showMinusPrefix: Boolean = true,
        var toggleShortcutKeystroke: String = ""
    )

    private var state = State()

    companion object
    {
        //读取应用级设置
        fun getInstance(): ChangeOverlaySettings
        {
            return ApplicationManager.getApplication().getService(ChangeOverlaySettings::class.java)
        }
    }

    //返回持久化数据
    override fun getState(): State
    {
        return state
    }

    //加载持久化数据
    override fun loadState(state: State)
    {
        this.state = state
    }
}
