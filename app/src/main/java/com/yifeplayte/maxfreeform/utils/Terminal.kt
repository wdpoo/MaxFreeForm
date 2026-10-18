package com.yifeplayte.maxfreeform.utils

import com.topjohnwu.superuser.Shell
import com.yifeplayte.maxfreeform.BuildConfig

/**
 * 指令工具。
 */
object Terminal {
    init {
        Shell.enableVerboseLogging = BuildConfig.DEBUG
        Shell.setDefaultBuilder(Shell.Builder.create())
    }

    fun exec(command: String) = Shell.cmd(command).exec()
}
