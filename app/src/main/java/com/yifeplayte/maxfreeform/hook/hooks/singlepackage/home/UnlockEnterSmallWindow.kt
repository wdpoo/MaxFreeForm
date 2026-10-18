package com.yifeplayte.maxfreeform.hook.hooks.singlepackage.home

import android.util.ArraySet
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS
import io.github.lingqiqi5211.ezhooktool.core.callMethod
import io.github.lingqiqi5211.ezhooktool.core.callStaticMethod
import io.github.lingqiqi5211.ezhooktool.core.findAllMethods
import io.github.lingqiqi5211.ezhooktool.core.getFieldOrNullAs
import io.github.lingqiqi5211.ezhooktool.core.loadClass
import io.github.lingqiqi5211.ezhooktool.xposed.dsl.createHook

@Suppress("unused")
object UnlockEnterSmallWindow : BaseHook() {
    override val key = "unlock_enter_small_window"
    override val isEnabled get() = !IS_HYPER_OS
    override fun hook() {
        val clazzRecentsAndFSGestureUtils =
            loadClass("com.miui.home.launcher.RecentsAndFSGestureUtils")
        clazzRecentsAndFSGestureUtils.findAllMethods { name("canTaskEnterSmallWindow") }
            .forEach { it.createHook { returnConstant(true) } }
        clazzRecentsAndFSGestureUtils.findAllMethods { name("canTaskEnterMiniSmallWindow") }
            .forEach { method ->
                method.createHook {
                    before { param ->
                        param.result = loadClass("com.miui.home.smallwindow.SmallWindowStateHelper")
                            .callStaticMethod("getInstance")!!
                            .callMethod("canEnterMiniSmallWindow") as Boolean
                    }
                }
            }
        loadClass("com.miui.home.smallwindow.SmallWindowStateHelperUseManager")
            .findAllMethods { name("canEnterMiniSmallWindow") }
            .forEach { method ->
                method.createHook {
                    before { param ->
                        param.result = param.thisObject
                            .getFieldOrNullAs<ArraySet<*>>("mMiniSmallWindowInfoSet")!!
                            .isEmpty()
                    }
                }
            }
        loadClass("miui.app.MiuiFreeFormManager")
            .findAllMethods { name("getAllFreeFormStackInfosOnDisplay") }
            .forEach { method ->
                method.createHook {
                    before { param ->
                        if (Throwable().stackTrace.any {
                                it.className == "android.util.MiuiMultiWindowUtils" &&
                                        it.methodName == "startSmallFreeform"
                            }
                        ) {
                            param.result = null
                        }
                    }
                }
            }
        loadClass("android.util.MiuiMultiWindowUtils")
            .findAllMethods { name("hasSmallFreeform") }
            .forEach { method ->
                method.createHook {
                    before { param ->
                        if (Throwable().stackTrace.any {
                                it.className == "android.util.MiuiMultiWindowUtils" &&
                                        it.methodName == "startSmallFreeform"
                            }
                        ) {
                            param.result = false
                        }
                    }
                }
            }
    }
}
