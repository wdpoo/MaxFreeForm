package com.yifeplayte.maxfreeform.utils

import com.yifeplayte.maxfreeform.App

/**
 * 触发所有运行中作用域进程的热重载。
 */
fun reloadAllTargets() {
    val service = App.mService ?: return
    val targets = service.runningTargets ?: return
    for (target in targets) {
        service.hotReloadModule(target, null) { _, _ -> }
    }
}
