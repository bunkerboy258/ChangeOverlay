package com.bbbwork.changeoverlay.editor

import com.bbbwork.changeoverlay.settings.ChangeOverlaySettings
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager

//编辑器处理资格检查器
object EditorEligibilityChecker
{
    //检查普通文本编辑器限制
    fun check(
        editor: Editor,
        settings: ChangeOverlaySettings.State
    ): String?
    {
        val file = FileDocumentManager.getInstance().getFile(editor.document)
            ?: return "Editor has no text file"

        if (file.fileType.isBinary)
        {
            return "Binary file"
        }

        if (file.length > settings.maximumFileSizeBytes)
        {
            return "File exceeds maximum size"
        }

        if (editor.document.lineCount > settings.maximumLineCount)
        {
            return "File exceeds maximum line count"
        }

        if (editor.isDisposed)
        {
            return "Editor is disposed"
        }

        return null
    }
}
