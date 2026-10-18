package com.yifeplayte.maxfreeform.hook.hooks.singlepackage.systemui

import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS
import io.github.lingqiqi5211.ezhooktool.core.findFieldOrNull
import io.github.lingqiqi5211.ezhooktool.core.findMethodOrNull
import io.github.lingqiqi5211.ezhooktool.core.getAs
import io.github.lingqiqi5211.ezhooktool.core.loadClassOrNull
import io.github.lingqiqi5211.ezhooktool.xposed.dsl.createHook

@Suppress("unused")
object RemoveFreeformBottomBar : BaseHook() {
    override val key = "remove_freeform_bottom_bar"
    override val isEnabled get() = IS_HYPER_OS and super.isEnabled
    override fun hook() {
        loadClassOrNull("com.android.wm.shell.miuimultiwinswitch.miuiwindowdecor.MiuiBaseWindowDecoration")
            ?.findMethodOrNull { name("createBottomCaption") }
            ?.createHook { returnConstant(null) }
        loadClassOrNull("com.android.wm.shell.multitasking.miuimultiwinswitch.miuiwindowdecor.MiuiBottomDecoration")
            ?.findMethodOrNull { name("needBottomCaption") }
            ?.createHook { returnConstant(false) }
        loadClassOrNull("com.android.wm.shell.multitasking.miuimultiwinswitch.miuiwindowdecor.decoration.MiuiDecorationBottom")?.let {
            runCatching {
                it.findMethodOrNull { name("needCaption") }?.createHook {
                    before { param ->
                        val mWindowingMode = param.thisObject.javaClass
                            .findFieldOrNull { name("mWindowingMode"); findAndSuper() }
                            ?.getAs<Int>(param.thisObject)
                        if (mWindowingMode == 5) {
                            param.result = false
                        }
                    }
                }
            }
            runCatching {
                it.findMethodOrNull { name("shouldShowCaptionForWindowingMode") }?.createHook {
                    before { param ->
                        val mWindowingMode = param.thisObject.javaClass
                            .findFieldOrNull { name("mWindowingMode"); findAndSuper() }
                            ?.getAs<Int>(param.thisObject)
                        if (mWindowingMode == 5) {
                            param.result = false
                        }
                    }
                }
            }
        }
    }
}
