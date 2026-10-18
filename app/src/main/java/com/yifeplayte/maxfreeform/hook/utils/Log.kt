package com.yifeplayte.maxfreeform.hook.utils

import android.util.Log as AndroidLog
import com.yifeplayte.maxfreeform.hook.utils.Log.androidLogLevel
import com.yifeplayte.maxfreeform.hook.utils.Log.xposedLogLevel
import io.github.lingqiqi5211.ezhooktool.core.EzLogger
import io.github.lingqiqi5211.ezhooktool.xposed.EzXposed

/**
 * hook 侧日志工具
 *
 * - 同时输出到 logcat 与 libxposed 框架日志
 * - 作为 EzReflect 的日志实现
 */
object Log : EzLogger {
    private const val TAG = "MaxFreeForm"
    private val base get() = runCatching { EzXposed.base }.getOrNull()

    var androidLogLevel: Int = AndroidLog.VERBOSE
    var xposedLogLevel: Int = AndroidLog.INFO

    fun v(msg: String, t: Throwable? = null) {
        if (androidLogLevel <= AndroidLog.VERBOSE) {
            if (t != null) AndroidLog.v(TAG, msg, t) else AndroidLog.v(TAG, msg)
        }
        if (xposedLogLevel <= AndroidLog.VERBOSE) {
            if (t != null) base?.log(AndroidLog.VERBOSE, TAG, msg, t)
            else base?.log(AndroidLog.VERBOSE, TAG, msg)
        }
    }

    fun d(msg: String, t: Throwable? = null) {
        if (androidLogLevel <= AndroidLog.DEBUG) {
            if (t != null) AndroidLog.d(TAG, msg, t) else AndroidLog.d(TAG, msg)
        }
        if (xposedLogLevel <= AndroidLog.DEBUG) {
            if (t != null) base?.log(AndroidLog.DEBUG, TAG, msg, t)
            else base?.log(AndroidLog.DEBUG, TAG, msg)
        }
    }

    fun i(msg: String, t: Throwable? = null) {
        if (androidLogLevel <= AndroidLog.INFO) {
            if (t != null) AndroidLog.i(TAG, msg, t) else AndroidLog.i(TAG, msg)
        }
        if (xposedLogLevel <= AndroidLog.INFO) {
            if (t != null) base?.log(AndroidLog.INFO, TAG, msg, t)
            else base?.log(AndroidLog.INFO, TAG, msg)
        }
    }

    fun w(msg: String, t: Throwable? = null) {
        if (androidLogLevel <= AndroidLog.WARN) {
            if (t != null) AndroidLog.w(TAG, msg, t) else AndroidLog.w(TAG, msg)
        }
        if (xposedLogLevel <= AndroidLog.WARN) {
            if (t != null) base?.log(AndroidLog.WARN, TAG, msg, t)
            else base?.log(AndroidLog.WARN, TAG, msg)
        }
    }

    fun e(msg: String, t: Throwable? = null) {
        if (androidLogLevel <= AndroidLog.ERROR) {
            if (t != null) AndroidLog.e(TAG, msg, t) else AndroidLog.e(TAG, msg)
        }
        if (xposedLogLevel <= AndroidLog.ERROR) {
            if (t != null) base?.log(AndroidLog.ERROR, TAG, msg, t)
            else base?.log(AndroidLog.ERROR, TAG, msg)
        }
    }

    override fun debug(tag: String, msg: String) = d("[$tag] $msg")
    override fun warn(tag: String, msg: String) = w("[$tag] $msg")
    override fun error(tag: String, msg: String, t: Throwable?) = e("[$tag] $msg", t)
}
