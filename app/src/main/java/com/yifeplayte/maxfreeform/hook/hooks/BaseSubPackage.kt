package com.yifeplayte.maxfreeform.hook.hooks

import com.yifeplayte.maxfreeform.hook.utils.Log
import com.yifeplayte.maxfreeform.hook.utils.isHostPackage
import com.yifeplayte.maxfreeform.utils.ClassScanner.scanObjectOf

abstract class BaseSubPackage(val packageName: String) {
    private var isInit: Boolean = false
    open val hooks: List<BaseSubHook> by lazy {
        scanObjectOf<BaseSubHook>(javaClass.packageName + "." + javaClass.simpleName.lowercase())
    }

    fun init() {
        if (!isHostPackage(packageName)) return
        if (isInit) return
        runCatching {
            hooks.forEach {
                it.init(javaClass.classLoader ?: ClassLoader.getSystemClassLoader())
            }
            isInit = true
            Log.i("Inited package: ${this.javaClass.simpleName}")
        }.onFailure {
            Log.e("Failed init package: ${this.javaClass.simpleName}", it)
        }
    }
}
