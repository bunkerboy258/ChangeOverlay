package com.bbbwork.changeoverlay.diff

//文本行标准化器
object LineTextNormalizer
{
    //拆分文本并统一换行符
    fun splitLines(text: String): List<String>
    {
        if (text.isEmpty())
        {
            return emptyList()
        }

        val normalized = text
            .replace("\r\n", "\n")
            .replace('\r', '\n')

        return normalized.split('\n')
    }
}
