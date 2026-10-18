package com.yifeplayte.maxfreeform.hook.hooks.singlepackage.android

import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS
import io.github.lingqiqi5211.ezhooktool.core.findMethod
import io.github.lingqiqi5211.ezhooktool.core.loadClass
import io.github.lingqiqi5211.ezhooktool.xposed.dsl.createHook

@Suppress("unused")
object UnlockSideHideFreeform : BaseHook() {
    override val key = "unlock_side_hide_freeform"
    override val isEnabled get() = !IS_HYPER_OS and super.isEnabled
    override fun hook() {
        loadClass("android.util.MiuiMultiWindowUtils")
            .findMethod { name("multiFreeFormSupported") }
            .createHook { returnConstant(true) }
    }
}
