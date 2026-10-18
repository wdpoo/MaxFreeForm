package com.yifeplayte.maxfreeform.hook.hooks.singlepackage.systemui

import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.hook.utils.XSharedPreferences.getStringSet
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS
import io.github.lingqiqi5211.ezhooktool.core.findMethod
import io.github.lingqiqi5211.ezhooktool.core.loadClassFirst
import io.github.lingqiqi5211.ezhooktool.xposed.dsl.createHook

@Suppress("unused")
object RemoveFreeformTopBarForList : BaseHook() {
    override val key = "remove_freeform_top_bar"
    override val isEnabled get() = IS_HYPER_OS
    override fun hook() {
        val clazzMiuiMultiWinSwitchConfig =
            loadClassFirst(
                "com.android.wm.shell.multitasking.miuimultiwinswitch.MulWinSwitchConfig",
                "com.android.wm.shell.miuimultiwinswitch.MiuiMultiWinSwitchConfig",
            )
        clazzMiuiMultiWinSwitchConfig.findMethod { name("getDotBlackList") }
            .createHook {
                before { param ->
                    param.result = getStringSet(
                        "remove_freeform_top_bar_list", mutableSetOf()
                    )
                }
            }
    }
}
