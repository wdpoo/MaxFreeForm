package com.yifeplayte.maxfreeform.hook.hooks.singlepackage.android

import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import io.github.lingqiqi5211.ezhooktool.core.findAllMethods
import io.github.lingqiqi5211.ezhooktool.core.getFieldOrNull
import io.github.lingqiqi5211.ezhooktool.core.loadClass
import io.github.lingqiqi5211.ezhooktool.core.putField
import io.github.lingqiqi5211.ezhooktool.xposed.dsl.createHook

@Suppress("unused")
object RemoveSmallWindowRestrictions : BaseHook() {
    override val key = "remove_small_window_restrictions"
    override fun hook() {
        runCatching {
            loadClass("android.app.ActivityTaskManager")
                .findAllMethods { notAbstract() }
                .filter { it.name == "supportsSplitScreen" }
                .forEach { it.createHook { returnConstant(true) } }
        }

        runCatching {
            loadClass("com.android.server.wm.ActivityTaskManagerService")
                .findAllMethods { name("retrieveSettings"); notAbstract() }
                .forEach { method ->
                    method.createHook {
                        after { param ->
                            param.thisObject!!.putField(
                                "mDevEnableNonResizableMultiWindow",
                                true,
                            )
                        }
                    }
                }
        }

        runCatching {
            loadClass("com.android.server.wm.WindowManagerService\$SettingsObserver")
                .findAllMethods { notAbstract() }
                .filter {
                    it.name == "updateDevEnableNonResizableMultiWindow" ||
                            it.name == "onChange"
                }.forEach { method ->
                    method.createHook {
                        after { param ->
                            val this0 = param.thisObject!!.getFieldOrNull("this\$0") ?: return@after
                            val mAtmService = this0.getFieldOrNull("mAtmService") ?: return@after
                            mAtmService!!.putField("mDevEnableNonResizableMultiWindow", true)
                        }
                    }
                }
        }

        runCatching {
            loadClass("android.util.MiuiMultiWindowUtils")
                .findAllMethods { name("isForceResizeable"); notAbstract() }
                .forEach { it.createHook { returnConstant(true) } }
        }

        runCatching {
            loadClass("android.util.MiuiMultiWindowAdapter")
                .findAllMethods { notAbstract() }
                .filter {
                    it.name.contains("BlackList", true) &&
                            MutableList::class.java.isAssignableFrom(it.returnType)
                }.forEach { it.createHook { returnConstant(mutableListOf<String>()) } }
        }

        // Author: LittleTurtle2333
        runCatching {
            loadClass("com.android.server.wm.Task")
                .findAllMethods { name("isResizeable"); notAbstract() }
                .forEach { it.createHook { returnConstant(true) } }
        }

        runCatching {
            loadClass("android.util.MiuiMultiWindowUtils")
                .findAllMethods { name("supportFreeform"); notAbstract() }
                .forEach { it.createHook { returnConstant(true) } }
        }
    }
}
