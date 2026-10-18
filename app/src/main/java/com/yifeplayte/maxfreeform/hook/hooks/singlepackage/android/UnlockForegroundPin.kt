package com.yifeplayte.maxfreeform.hook.hooks.singlepackage.android

import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.hook.utils.XSharedPreferences.getStringSet
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS
import io.github.lingqiqi5211.ezhooktool.core.callMethod
import io.github.lingqiqi5211.ezhooktool.core.findMethod
import io.github.lingqiqi5211.ezhooktool.core.loadClass
import io.github.lingqiqi5211.ezhooktool.xposed.dsl.createHook

@Suppress("unused")
object UnlockForegroundPin : BaseHook() {
    override val key = "unlock_foreground_pin"
    override fun hook() =
        if (IS_HYPER_OS) initForHyperOS() else MiuiUnlockForegroundPin().initForMIUI()

    private fun initForHyperOS() {
        loadClass("com.android.server.wm.MiuiFreeFormGestureController")
            .findMethod {
                name("needForegroundPin")
                notAbstract()
            }.createHook {
                before { param ->
                    val packageName = param.args[0]!!.callMethod("getStackPackageName") as String
                    val unlockForegroundPinWhitelist =
                        getStringSet("unlock_foreground_pin_whitelist", mutableSetOf())
                    param.result = unlockForegroundPinWhitelist.contains(packageName)
                }
            }
    }
}
