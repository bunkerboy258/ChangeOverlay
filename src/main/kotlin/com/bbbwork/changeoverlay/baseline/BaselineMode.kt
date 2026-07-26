package com.bbbwork.changeoverlay.baseline

//基线模式
enum class BaselineMode
{
    GIT_HEAD
    {
        //返回GitHead显示名称
        override fun toString(): String
        {
            return "Git HEAD 基线"
        }
    },
    SESSION_SNAPSHOT
    {
        //返回会话快照显示名称
        override fun toString(): String
        {
            return "会话快照 / Session Snapshot"
        }
    }
}
