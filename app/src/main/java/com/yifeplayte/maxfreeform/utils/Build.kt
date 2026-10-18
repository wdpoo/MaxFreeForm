package com.yifeplayte.maxfreeform.utils

import io.github.lingqiqi5211.ezhooktool.core.callStaticMethod
import io.github.lingqiqi5211.ezhooktool.core.getStaticFieldOrNullAs
import io.github.lingqiqi5211.ezhooktool.core.loadClass

/**
 * 获取系统信息。
 *
 * 在模块 UI 进程与 hook 进程中都会使用；miui.os.Build / SystemProperties 均为框架类。
 */
@Suppress("unused")
object Build {
    private val clazzMiuiBuild by lazy {
        loadClass("miui.os.Build")
    }

    private val clazzSystemProperties by lazy {
        loadClass("android.os.SystemProperties")
    }

    val IS_TABLET by lazy {
        runCatching { clazzMiuiBuild.getStaticFieldOrNullAs<Boolean>("IS_TABLET") }
            .getOrNull() ?: false
    }

    val IS_INTERNATIONAL_BUILD by lazy {
        runCatching {
            clazzMiuiBuild.getStaticFieldOrNullAs<Boolean>("IS_INTERNATIONAL_BUILD")
        }.getOrNull() ?: false
    }

    private val hyperOsVersionCode: Int by lazy {
        runCatching {
            clazzSystemProperties.callStaticMethod("getInt", "ro.mi.os.version.code", -1) as? Int
        }.getOrNull() ?: -1
    }

    val IS_HYPER_OS by lazy {
        hyperOsVersionCode != -1
    }

    val HYPER_OS_VERSION_CODE by lazy {
        hyperOsVersionCode
    }
}
