package com.yifeplayte.maxfreeform.hook.hooks.singlepackage.systemui

import android.view.View
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.utils.Build.HYPER_OS_VERSION_CODE
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS
import io.github.lingqiqi5211.ezhooktool.core.findMethod
import io.github.lingqiqi5211.ezhooktool.core.loadClassFirst
import io.github.lingqiqi5211.ezhooktool.xposed.dsl.createHook

@Suppress("unused")
object HideFreeformTopBar : BaseHook() {
    override val key = "hide_freeform_top_bar"
    override val isEnabled get() = IS_HYPER_OS and (HYPER_OS_VERSION_CODE < 3) and super.isEnabled
    override fun hook() {
        val clazzMiuiBaseWindowDecoration =
            loadClassFirst(
                "com.android.wm.shell.multitasking.miuimultiwinswitch.miuiwindowdecor.MiuiTopDecoration",
                "com.android.wm.shell.miuimultiwinswitch.miuiwindowdecor.MiuiBaseWindowDecoration",
            )
        clazzMiuiBaseWindowDecoration.findMethod { name("createTopCaption") }
            .createHook {
                after { param ->
                    val view = param.result as View
                    view.alpha = 0f
                }
            }
    }
}
