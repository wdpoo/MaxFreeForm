package com.yifeplayte.maxfreeform.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

/**
 * 模块配置读写。
 *
 * 旧版 Xposed API 下，hook 进程通过 XSharedPreferences 读取模块私有目录外的
 * 世界可读配置文件；LSPosed 会把 MODE_WORLD_READABLE 请求重定向到该文件。
 * 模块未激活时该请求会抛 SecurityException，此时退回私有配置，让 UI 仍可打开
 * 并显示未激活状态。
 */
object Preferences {
    private var _sp: SharedPreferences? = null

    val sp: SharedPreferences
        get() = checkNotNull(_sp) { "Preferences is not initialized" }

    var isModuleActivated: Boolean = false
        private set

    fun ensureInit(context: Context) {
        if (_sp == null) init(context)
    }

    @SuppressLint("WorldReadableFiles")
    @Suppress("DEPRECATION")
    fun init(context: Context) {
        val worldReadable = runCatching {
            context.getSharedPreferences("config", Context.MODE_WORLD_READABLE)
        }
        isModuleActivated = worldReadable.isSuccess
        _sp = worldReadable.getOrElse {
            context.getSharedPreferences("config", Context.MODE_PRIVATE)
        }
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        sp.getBoolean(key, defaultValue)

    fun putBoolean(key: String, value: Boolean) {
        sp.edit().putBoolean(key, value).apply()
    }

    fun getStringSet(key: String, defaultValue: Set<String>): Set<String> =
        sp.getStringSet(key, defaultValue) ?: defaultValue

    fun putStringSet(key: String, value: Set<String>) {
        sp.edit().putStringSet(key, HashSet(value)).apply()
    }

    fun clear() {
        sp.edit().clear().apply()
    }
}
