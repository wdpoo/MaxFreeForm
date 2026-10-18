package com.yifeplayte.maxfreeform.hook.hooks.singlepackage.systemui

import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS
import io.github.lingqiqi5211.ezhooktool.core.findAllMethods
import io.github.lingqiqi5211.ezhooktool.core.loadClass
import io.github.lingqiqi5211.ezhooktool.xposed.dsl.createHook

@Suppress("unused")
object UnlockMultipleTask : BaseHook() {
    override val key = "unlock_multiple_task"
    override val isEnabled get() = IS_HYPER_OS and super.isEnabled
    override fun hook() {
        runCatching {
            loadClass("android.app.ActivityTaskManager")
                .findAllMethods { name("supportMultipleTask") }
                .forEach { it.createHook { returnConstant(true) } }
        }
    }
}
