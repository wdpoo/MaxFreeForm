package com.yifeplayte.maxfreeform.hook.hooks.singlepackage.android

import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import io.github.lingqiqi5211.ezhooktool.core.findAllMethods
import io.github.lingqiqi5211.ezhooktool.core.findMethodOrNull
import io.github.lingqiqi5211.ezhooktool.core.loadClass
import io.github.lingqiqi5211.ezhooktool.xposed.dsl.createHook

@Suppress("unused")
object UnlockFreeformQuantityLimit : BaseHook() {
    override val key = "unlock_freeform_quantity_limit"
    override val isEnabled = true
    override fun hook() {
        val clazzMiuiFreeFormStackDisplayStrategy =
            loadClass("com.android.server.wm.MiuiFreeFormStackDisplayStrategy")
        clazzMiuiFreeFormStackDisplayStrategy.findAllMethods { notAbstract() }
            .filter {
                it.name == "getMaxMiuiFreeFormStackCount" ||
                        it.name == "getMaxMiuiFreeFormStackCountForFlashBack"
            }.forEach { method ->
            method.createHook { returnConstant(256) }
        }
        clazzMiuiFreeFormStackDisplayStrategy
            .findMethodOrNull { name("shouldStopStartFreeform") }
            ?.createHook { returnConstant(false) }
    }
}
