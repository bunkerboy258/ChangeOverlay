package com.bbbwork.changeoverlay.rendering

import com.bbbwork.changeoverlay.settings.ChangeOverlaySettings
import java.awt.Color

//覆盖层主题颜色
object OverlayColors
{
    //创建新增背景色
    fun added(settings: ChangeOverlaySettings.State): Color
    {
        return withOpacity(settings.addedColorRgb, settings.backgroundOpacity)
    }

    //创建删除背景色
    fun deleted(settings: ChangeOverlaySettings.State): Color
    {
        return withOpacity(settings.deletedColorRgb, settings.backgroundOpacity)
    }

    //组合颜色透明度
    private fun withOpacity(
        rgb: Int,
        opacity: Int
    ): Color
    {
        val color = Color(rgb)
        val alpha = (opacity.coerceIn(0, 100) * 255) / 100

        return Color(
            color.red,
            color.green,
            color.blue,
            alpha
        )
    }
}
