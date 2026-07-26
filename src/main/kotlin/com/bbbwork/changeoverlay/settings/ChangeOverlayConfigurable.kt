package com.bbbwork.changeoverlay.settings

import com.bbbwork.changeoverlay.baseline.BaselineMode
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import com.bbbwork.changeoverlay.services.ChangeOverlayProjectService
import com.intellij.ui.ColorPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JPanel

//插件设置页面
class ChangeOverlayConfigurable : Configurable
{
    private val enabled = JBCheckBox("Enable Change Overlay")
    private val baselineMode = JComboBox(BaselineMode.entries.toTypedArray())
    private val showAddedLines = JBCheckBox("Show Added Lines")
    private val showDeletedLines = JBCheckBox("Show Deleted Lines")
    private val showModifiedLines = JBCheckBox("Show Modified Lines")
    private val addedColor = ColorPanel()
    private val deletedColor = ColorPanel()
    private val opacity = JBTextField()
    private val debounce = JBTextField()
    private val maximumFileSize = JBTextField()
    private val maximumLineCount = JBTextField()
    private val showMinusPrefix = JBCheckBox("Show Minus Prefix for Deleted Lines")
    private var panel: JPanel? = null

    //返回设置页面名称
    override fun getDisplayName(): String
    {
        return "Change Overlay"
    }

    //创建设置页面组件
    override fun createComponent(): JComponent
    {
        val result = JPanel(GridBagLayout())
        var row = 0

        addRow(result, enabled, row++)
        addRow(result, JBLabel("Baseline Mode"), baselineMode, row++)
        addRow(result, showAddedLines, row++)
        addRow(result, showDeletedLines, row++)
        addRow(result, showModifiedLines, row++)
        addRow(result, JBLabel("Added Background Color"), addedColor, row++)
        addRow(result, JBLabel("Deleted Background Color"), deletedColor, row++)
        addRow(result, JBLabel("Background Opacity 0 to 100"), opacity, row++)
        addRow(result, JBLabel("Debounce Milliseconds"), debounce, row++)
        addRow(result, JBLabel("Maximum File Size Bytes"), maximumFileSize, row++)
        addRow(result, JBLabel("Maximum Line Count"), maximumLineCount, row++)
        addRow(result, showMinusPrefix, row++)

        val filler = GridBagConstraints()
        filler.gridx = 0
        filler.gridy = row
        filler.weighty = 1.0
        filler.fill = GridBagConstraints.VERTICAL
        result.add(JPanel(), filler)
        panel = result
        reset()

        return result
    }

    //判断设置是否变化
    override fun isModified(): Boolean
    {
        val state = ChangeOverlaySettings.getInstance().state

        return enabled.isSelected != state.enabled ||
            baselineMode.selectedItem != state.baselineMode ||
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
