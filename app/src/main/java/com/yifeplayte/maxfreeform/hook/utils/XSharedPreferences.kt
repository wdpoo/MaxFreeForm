package com.yifeplayte.maxfreeform.hook.utils

import android.content.SharedPreferences
import io.github.lingqiqi5211.ezhooktool.xposed.EzXposed

/**
 * 读取模块配置。
 *
 * libxposed 用 [EzXposed.base.getRemotePreferences] 替代旧 Xposed API 的 XSharedPreferences。
 */
@Suppress("unused")
object XSharedPreferences {
    const val PREFERENCES_FILE_NAME = "config"

    private fun prefs(): SharedPreferences =
        EzXposed.base.getRemotePreferences(PREFERENCES_FILE_NAME)

    fun getBoolean(key: String, defValue: Boolean): Boolean = prefs().getBoolean(key, defValue)

    fun getInt(key: String, defValue: Int): Int = prefs().getInt(key, defValue)

    fun getFloat(key: String, defValue: Float): Float = prefs().getFloat(key, defValue)

    fun getString(key: String, defValue: String): String =
        prefs().getString(key, defValue) ?: defValue

    fun getStringSet(key: String, defValue: MutableSet<String>): MutableSet<String> =
        prefs().getStringSet(key, defValue)?.toMutableSet() ?: defValue
}
