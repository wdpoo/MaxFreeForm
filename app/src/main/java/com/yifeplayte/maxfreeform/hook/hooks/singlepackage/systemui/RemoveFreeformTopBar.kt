package com.yifeplayte.maxfreeform.hook.hooks.singlepackage.systemui

import android.content.ComponentName
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS
import io.github.lingqiqi5211.ezhooktool.core.findMethodOrNull
import io.github.lingqiqi5211.ezhooktool.core.findAllMethods
import io.github.lingqiqi5211.ezhooktool.core.loadClassFirstOrNull
import io.github.lingqiqi5211.ezhooktool.core.loadClassOrNull
import io.github.lingqiqi5211.ezhooktool.xposed.dsl.createHook

@Suppress("unused")
object RemoveFreeformTopBar : BaseHook() {
    override val key = "remove_freeform_top_bar"
    override val isEnabled get() = IS_HYPER_OS and super.isEnabled
    override fun hook() {
        loadClassOrNull("com.android.wm.shell.miuimultiwinswitch.miuiwindowdecor.MiuiBaseWindowDecoration")
            ?.findMethodOrNull {
                name("getBooleanProperty")
                params(String::class.java, ComponentName::class.java)
            }?.createHook {
                before { param ->
                    if (param.args[0] != "miui.window.DOT_ENABLED") return@before
                    param.result = false
                }
            }
        loadClassFirstOrNull(
            "com.android.wm.shell.multitasking.miuimultiwinswitch.miuiwindowdecor.MiuiTopDecoration",
            "com.android.wm.shell.multitasking.miuimultiwinswitch.miuiwindowdecor.decoration.MiuiDecorationDot",
        )?.findAllMethods {
            name("getBooleanProperty")
            notAbstract()
        }?.forEach { method ->
            method.createHook {
                before { param -> param.result = false }
            }
        }
    }
}
