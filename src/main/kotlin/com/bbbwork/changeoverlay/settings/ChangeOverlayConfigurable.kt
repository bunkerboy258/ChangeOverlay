package com.bbbwork.changeoverlay.settings

import com.bbbwork.changeoverlay.baseline.BaselineMode
import com.bbbwork.changeoverlay.baseline.GitRepositoryStateReader
import com.bbbwork.changeoverlay.services.ChangeOverlayProjectService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.ui.ColorPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.concurrency.AppExecutorUtil
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JPanel

//插件设置页面
class ChangeOverlayConfigurable : Configurable
{
    private val enabled = JBCheckBox("启用更改覆盖 / Enable Change Overlay")
    private val baselineMode = JComboBox(BaselineMode.entries.toTypedArray())
    private val trackBranchCommitHistory = JBCheckBox("跟踪分支提交历史 / Track Branch Commit History")
    private val trackedBranch = JComboBox<String>()
    private val showAddedLines = JBCheckBox("显示新增行 / Show Added Lines")
    private val showDeletedLines = JBCheckBox("显示删除行 / Show Deleted Lines")
    private val showModifiedLines = JBCheckBox("显示修改行 / Show Modified Lines")
    private val addedColor = ColorPanel()
    private val deletedColor = ColorPanel()
    private val opacity = JBTextField()
    private val debounce = JBTextField()
    private val maximumFileSize = JBTextField()
    private val maximumLineCount = JBTextField()
    private val showMinusPrefix = JBCheckBox("删除行显示减号前缀 / Show Minus Prefix")
    private var panel: JPanel? = null

    //返回设置页面名称
    override fun getDisplayName(): String
    {
        return "更改覆盖 / Change Overlay"
    }

    //创建设置页面组件
    override fun createComponent(): JComponent
    {
        val result = JPanel(GridBagLayout())
        var row = 0

        addRow(result, enabled, row++)
        addRow(result, JBLabel("基线模式 / Baseline Mode"), baselineMode, row++)
        addRow(result, trackBranchCommitHistory, row++)
        addRow(result, JBLabel("跟踪分支 / Tracked Branch"), trackedBranch, row++)
        addRow(result, showAddedLines, row++)
        addRow(result, showDeletedLines, row++)
        addRow(result, showModifiedLines, row++)
        addRow(result, JBLabel("新增背景色 / Added Color"), addedColor, row++)
        addRow(result, JBLabel("删除背景色 / Deleted Color"), deletedColor, row++)
        addRow(result, JBLabel("背景透明度 0 到 100 / Opacity"), opacity, row++)
        addRow(result, JBLabel("刷新防抖毫秒 / Debounce Milliseconds"), debounce, row++)
        addRow(result, JBLabel("最大文件字节数 / Maximum File Size"), maximumFileSize, row++)
        addRow(result, JBLabel("最大行数 / Maximum Line Count"), maximumLineCount, row++)
        addRow(result, showMinusPrefix, row++)

        val filler = GridBagConstraints()
        filler.gridx = 0
        filler.gridy = row
        filler.weighty = 1.0
        filler.fill = GridBagConstraints.VERTICAL
        result.add(JPanel(), filler)
        panel = result
        trackBranchCommitHistory.addActionListener {
            updateTrackedBranchEnabled()
        }
        baselineMode.addActionListener {
            updateTrackedBranchEnabled()
        }
        reset()
        loadLocalBranches()

        return result
    }

    //判断设置是否变化
    override fun isModified(): Boolean
    {
        val state = ChangeOverlaySettings.getInstance().state

        return enabled.isSelected != state.enabled ||
            baselineMode.selectedItem != state.baselineMode ||
            trackBranchCommitHistory.isSelected != state.trackBranchCommitHistory ||
            selectedTrackedBranch() != state.trackedBranchName ||
            showAddedLines.isSelected != state.showAddedLines ||
            showDeletedLines.isSelected != state.showDeletedLines ||
            showModifiedLines.isSelected != state.showModifiedLines ||
            addedColor.selectedColor?.rgb != state.addedColorRgb ||
            deletedColor.selectedColor?.rgb != state.deletedColorRgb ||
            opacity.text.toIntOrNull() != state.backgroundOpacity ||
            debounce.text.toIntOrNull() != state.debounceMilliseconds ||
            maximumFileSize.text.toLongOrNull() != state.maximumFileSizeBytes ||
            maximumLineCount.text.toIntOrNull() != state.maximumLineCount ||
            showMinusPrefix.isSelected != state.showMinusPrefix
    }

    //应用用户设置
    override fun apply()
    {
        val state = ChangeOverlaySettings.getInstance().state
        state.enabled = enabled.isSelected
        state.baselineMode = baselineMode.selectedItem as BaselineMode
        state.trackBranchCommitHistory = trackBranchCommitHistory.isSelected
        state.trackedBranchName = selectedTrackedBranch()
        state.showAddedLines = showAddedLines.isSelected
        state.showDeletedLines = showDeletedLines.isSelected
        state.showModifiedLines = showModifiedLines.isSelected
        state.addedColorRgb = addedColor.selectedColor?.rgb ?: state.addedColorRgb
        state.deletedColorRgb = deletedColor.selectedColor?.rgb ?: state.deletedColorRgb
        state.backgroundOpacity = opacity.text.toIntOrNull()?.coerceIn(0, 100) ?: 55
        state.debounceMilliseconds = debounce.text.toIntOrNull()?.coerceIn(0, 5000) ?: 300
        state.maximumFileSizeBytes = maximumFileSize.text.toLongOrNull()?.coerceAtLeast(1) ?: 1_048_576
        state.maximumLineCount = maximumLineCount.text.toIntOrNull()?.coerceAtLeast(1) ?: 20_000
        state.showMinusPrefix = showMinusPrefix.isSelected

        //刷新全部打开项目应用设置
        for (project in ProjectManager.getInstance().openProjects)
        {
            project
                .getService(ChangeOverlayProjectService::class.java)
                .refreshAll()
        }
    }

    //恢复当前设置值
    override fun reset()
    {
        val state = ChangeOverlaySettings.getInstance().state
        enabled.isSelected = state.enabled
        baselineMode.selectedItem = state.baselineMode
        trackBranchCommitHistory.isSelected = state.trackBranchCommitHistory
        selectTrackedBranch(state.trackedBranchName)
        updateTrackedBranchEnabled()
        showAddedLines.isSelected = state.showAddedLines
        showDeletedLines.isSelected = state.showDeletedLines
        showModifiedLines.isSelected = state.showModifiedLines
        addedColor.selectedColor = java.awt.Color(state.addedColorRgb)
        deletedColor.selectedColor = java.awt.Color(state.deletedColorRgb)
        opacity.text = state.backgroundOpacity.toString()
        debounce.text = state.debounceMilliseconds.toString()
        maximumFileSize.text = state.maximumFileSizeBytes.toString()
        maximumLineCount.text = state.maximumLineCount.toString()
        showMinusPrefix.isSelected = state.showMinusPrefix
    }

    //后台加载当前项目本地分支
    private fun loadLocalBranches()
    {
        val projectPaths = ProjectManager
            .getInstance()
            .openProjects
            .mapNotNull {
                it.basePath
            }

        AppExecutorUtil
            .getAppExecutorService()
            .submit {
                val reader = GitRepositoryStateReader()
                var repositoryRoot: String? = null

                for (projectPath in projectPaths)
                {
                    repositoryRoot = reader.findRepositoryRoot(projectPath)

                    if (repositoryRoot != null)
                    {
                        break
                    }
                }

                if (repositoryRoot == null)
                {
                    return@submit
                }

                val branches = reader.readLocalBranches(repositoryRoot)
                val currentBranch = reader.readState(repositoryRoot)?.currentBranch.orEmpty()

                ApplicationManager.getApplication().invokeLater {
                    if (panel == null)
                    {
                        return@invokeLater
                    }

                    applyLocalBranches(
                        branches,
                        currentBranch
                    )
                }
            }
    }

    //应用本地分支列表
    private fun applyLocalBranches(
        branches: List<String>,
        currentBranch: String
    )
    {
        val storedBranch = ChangeOverlaySettings.getInstance().state.trackedBranchName
        val items = branches.toMutableList()

        if (storedBranch.isNotBlank() && !items.contains(storedBranch))
        {
            items.add(0, storedBranch)
        }

        trackedBranch.model = DefaultComboBoxModel(items.toTypedArray())

        if (storedBranch.isNotBlank())
        {
            trackedBranch.selectedItem = storedBranch

            return
        }

        if (currentBranch.isNotBlank())
        {
            trackedBranch.selectedItem = currentBranch
        }
    }

    //选择已持久化跟踪分支
    private fun selectTrackedBranch(branchName: String)
    {
        if (branchName.isBlank())
        {
            return
        }

        val model = trackedBranch.model

        for (index in 0 until model.size)
        {
            if (model.getElementAt(index) == branchName)
            {
                trackedBranch.selectedItem = branchName

                return
            }
        }

        trackedBranch.addItem(branchName)
        trackedBranch.selectedItem = branchName
    }

    //同步分支选择启用状态
    private fun updateTrackedBranchEnabled()
    {
        val gitHeadSelected = baselineMode.selectedItem == BaselineMode.GIT_HEAD
        trackBranchCommitHistory.isEnabled = gitHeadSelected
        trackedBranch.isEnabled = gitHeadSelected &&
            trackBranchCommitHistory.isSelected
    }

    //读取标准化跟踪分支名称
    private fun selectedTrackedBranch(): String
    {
        return trackedBranch.selectedItem as? String ?: ""
    }

    //释放设置页面
    override fun disposeUIResources()
    {
        panel = null
    }

    //添加跨两列设置行
    private fun addRow(
        panel: JPanel,
        component: JComponent,
        row: Int
    )
    {
        val constraints = createConstraints(row)
        constraints.gridwidth = 2
        panel.add(component, constraints)
    }

    //添加标签和值设置行
    private fun addRow(
        panel: JPanel,
        label: JComponent,
        component: JComponent,
        row: Int
    )
    {
        val labelConstraints = createConstraints(row)
        panel.add(label, labelConstraints)

        val valueConstraints = createConstraints(row)
        valueConstraints.gridx = 1
        valueConstraints.weightx = 1.0
        valueConstraints.fill = GridBagConstraints.HORIZONTAL
        panel.add(component, valueConstraints)
    }

    //创建网格布局约束
    private fun createConstraints(row: Int): GridBagConstraints
    {
        val constraints = GridBagConstraints()
        constraints.gridx = 0
        constraints.gridy = row
        constraints.anchor = GridBagConstraints.WEST
        constraints.insets = Insets(4, 4, 4, 8)

        return constraints
    }
}
