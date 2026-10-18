package com.yifeplayte.maxfreeform.hook

import com.yifeplayte.maxfreeform.hook.hooks.BaseMultiHook
import com.yifeplayte.maxfreeform.hook.hooks.BasePackage
import com.yifeplayte.maxfreeform.hook.hooks.BaseSubPackage
import com.yifeplayte.maxfreeform.hook.utils.Log
import com.yifeplayte.maxfreeform.utils.ClassScanner.scanObjectOf
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.HotReloadedParam
import io.github.libxposed.api.XposedModuleInterface.HotReloadingParam
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam
import io.github.lingqiqi5211.ezhooktool.core.EzReflect
import io.github.lingqiqi5211.ezhooktool.xposed.EzXposed

private val singlePackagesHooked by lazy {
    scanObjectOf<BasePackage>("com.yifeplayte.maxfreeform.hook.hooks.singlepackage")
}
private val multiPackagesHooked by lazy {
    scanObjectOf<BaseMultiHook>("com.yifeplayte.maxfreeform.hook.hooks.multipackage")
}
private val subPackagesHooked by lazy {
    scanObjectOf<BaseSubPackage>("com.yifeplayte.maxfreeform.hook.hooks.subpackage")
}
val PACKAGE_NAME_HOOKED: Set<String>
    get() {
        val packageNameHooked = mutableSetOf<String>()
        singlePackagesHooked.forEach { packageNameHooked.add(it.packageName) }
        multiPackagesHooked.forEach { packageNameHooked.addAll(it.hooks.keys) }
        subPackagesHooked.forEach { packageNameHooked.add(it.packageName) }
        return packageNameHooked
    }

class MainHook : XposedModule() {
    override fun onModuleLoaded(param: ModuleLoadedParam) {
        EzReflect.logger = Log
        EzXposed.initOnModuleLoaded(this, param)
        EzXposed.onTargetReady { installHooks() }
    }

    override fun onPackageLoaded(param: PackageLoadedParam) {
        if (!param.isFirstPackage || param.packageName !in PACKAGE_NAME_HOOKED) return
        EzXposed.initOnPackageLoaded(param)
    }

    override fun onPackageReady(param: PackageReadyParam) {
        if (!param.isFirstPackage || param.packageName !in PACKAGE_NAME_HOOKED) return
        EzXposed.initOnPackageReady(param)
    }

    override fun onSystemServerStarting(param: SystemServerStartingParam) {
        EzXposed.initOnSystemServerStarting(param)
    }

    override fun onHotReloading(param: HotReloadingParam): Boolean =
        EzXposed.handleHotReloading(param)

    override fun onHotReloaded(param: HotReloadedParam) {
        EzXposed.handleHotReloadedWithTargetReady(this, param, targetReady = { installHooks() })
    }

    private fun installHooks() {
        singlePackagesHooked.forEach { it.init() }
        multiPackagesHooked.forEach { it.init() }
        subPackagesHooked.forEach { it.init() }
    }
}
