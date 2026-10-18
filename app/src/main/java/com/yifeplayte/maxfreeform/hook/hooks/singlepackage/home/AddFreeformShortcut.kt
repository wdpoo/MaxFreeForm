package com.yifeplayte.maxfreeform.hook.hooks.singlepackage.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Intent
import android.os.UserHandle
import android.view.View.OnClickListener
import com.yifeplayte.maxfreeform.R
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.hook.utils.hostPackageName
import com.yifeplayte.maxfreeform.utils.Build.IS_TABLET
import io.github.lingqiqi5211.ezhooktool.core.callMethod
import io.github.lingqiqi5211.ezhooktool.core.callStaticMethod
import io.github.lingqiqi5211.ezhooktool.core.findAllMethods
import io.github.lingqiqi5211.ezhooktool.core.findMethod
import io.github.lingqiqi5211.ezhooktool.core.getStaticFieldOrNullAs
import io.github.lingqiqi5211.ezhooktool.core.loadClass
import io.github.lingqiqi5211.ezhooktool.core.putStaticField
import io.github.lingqiqi5211.ezhooktool.xposed.EzXposed
import io.github.lingqiqi5211.ezhooktool.xposed.dsl.createHook

@Suppress("unused")
object AddFreeformShortcut : BaseHook() {
    override val key = "add_freeform_shortcut"
    private val clazzLauncherUtils by lazy { loadClass("com.miui.launcher.utils.LauncherUtils") }
    override fun hook() {
        if (IS_TABLET) initForPad() else initForPhone()
        loadClass("com.miui.home.launcher.shortcuts.SystemShortcutMenu")
            .findAllMethods { name("getMaxShortcutItemCount") }
            .forEach { method ->
                method.createHook { after { param -> param.result = 6 } }
            }
        loadClass("com.miui.home.launcher.shortcuts.AppShortcutMenu")
            .findAllMethods { name("getMaxShortcutItemCount") }
            .forEach { method ->
                method.createHook { after { param -> param.result = 6 } }
            }
        loadClass("com.miui.home.launcher.shortcuts.ShortcutMenuItem")
            .findAllMethods { name("getShortTitle") }
            .forEach { method ->
                method.createHook {
                    after { param ->
                        param.result = when (param.result) {
                            "应用信息" -> "信息"
                            "新建窗口" -> "多开"
                            else -> param.result
                        }
                    }
                }
            }
    }

    private fun initForPad() {
        val clazzShortcutInfo = loadClass("com.miui.home.launcher.ShortcutInfo")
        loadClass("com.miui.home.launcher.shortcuts.SystemShortcutMenuItem\$MultipleSmallWindowShortcutMenuItem")
            .findMethod { name("isValid") }
            .createHook {
                before { param ->
                    param.result =
                        param.args[0]!!::class.java.isAssignableFrom(clazzShortcutInfo)
                }
            }
        loadClass("com.miui.home.launcher.shortcuts.SystemShortcutMenuItem\$SmallWindowShortcutMenuItem")
            .findMethod { name("isValid") }
            .createHook {
                before { param ->
                    param.result =
                        param.args[0]!!::class.java.isAssignableFrom(clazzShortcutInfo)
                }
            }
    }

    @SuppressLint("DiscouragedApi")
    private fun initForPhone() {
        val clazzSystemShortcutMenuItem =
            loadClass("com.miui.home.launcher.shortcuts.SystemShortcutMenuItem")

        Activity::class.java.findAllMethods { name("onCreate") }.forEach { method ->
            method.createHook {
                after { param ->
                    EzXposed.initAppContext(param.thisObject as Activity)
                }
            }
        }
        loadClass("com.miui.home.launcher.util.ViewDarkModeHelper")
            .findAllMethods { name("onConfigurationChanged") }
            .forEach { method ->
                method.createHook {
                    after {
                        clazzSystemShortcutMenuItem.callStaticMethod(
                            "createAllSystemShortcutMenuItems",
                        )
                    }
                }
            }
        loadClass("com.miui.home.launcher.shortcuts.SystemShortcutMenuItem\$AppDetailsShortcutMenuItem")
            .findAllMethods { name("getOnClickListener") }
            .forEach { method ->
                method.createHook {
                    before { param ->
                        when (val shortTitle =
                            param.thisObject.callMethod("getShortTitle")) {
                            in setOf(
                                EzXposed.moduleRes.getString(R.string.freeform),
                                EzXposed.moduleRes.getString(R.string.new_task)
                            ) -> {
                                param.result = OnClickListener { view ->
                                    val context = view.context
                                    val componentName = param.thisObject
                                        .callMethod("getComponentName") as ComponentName
                                    val userHandle = param.thisObject
                                        .callMethod("getUserHandle") as UserHandle
                                    val intent = Intent().apply {
                                        action = "android.intent.action.MAIN"
                                        component = componentName
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        if (shortTitle == EzXposed.moduleRes
                                                .getString(R.string.new_task)
                                        ) {
                                            addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                                        }
                                    }
                                    loadClass("com.miui.launcher.utils.ActivityUtilsCompat")
                                        .callStaticMethod(
                                            "makeFreeformActivityOptions",
                                            context,
                                            componentName.packageName,
                                        )?.let {
                                            clazzLauncherUtils.callStaticMethod(
                                                "startActivityAsUser",
                                                context,
                                                intent,
                                                (it as ActivityOptions).toBundle(),
                                                userHandle,
                                            )
                                        }
                                }
                            }
                        }
                    }
                }
            }
        clazzSystemShortcutMenuItem
            .findAllMethods { name("createAllSystemShortcutMenuItems") }
            .forEach { method ->
                method.createHook {
                    after {
                        val mAllSystemShortcutMenuItems =
                            clazzSystemShortcutMenuItem.getStaticFieldOrNullAs<List<Any>>(
                                "sAllSystemShortcutMenuItems"
                            )!!
                        val appDetailsClass = loadClass(
                            "com.miui.home.launcher.shortcuts.SystemShortcutMenuItem\$AppDetailsShortcutMenuItem"
                        )
                        val mSmallWindowInstance = appDetailsClass.getConstructor()
                            .newInstance().apply {
                                callMethod(
                                    "setShortTitle",
                                    EzXposed.moduleRes.getString(R.string.freeform),
                                )
                                callMethod(
                                    "setIconDrawable",
                                    EzXposed.appContext.let {
                                        it.getDrawable(
                                            it.resources.getIdentifier(
                                                "shortcut_menu_size_adapt",
                                                "drawable",
                                                hostPackageName,
                                            )
                                        )
                                    },
                                )
                            }
                        val mNewTaskInstance = appDetailsClass.getConstructor()
                            .newInstance().apply {
                                callMethod(
                                    "setShortTitle",
                                    EzXposed.moduleRes.getString(R.string.new_task),
                                )
                                callMethod(
                                    "setIconDrawable",
                                    EzXposed.appContext.let {
                                        it.getDrawable(
                                            it.resources.getIdentifier(
                                                "add_pair_to_workspace",
                                                "drawable",
                                                hostPackageName,
                                            )
                                        )
                                    },
                                )
                            }
                        val sAllSystemShortcutMenuItems = ArrayList<Any>().apply {
                            add(mSmallWindowInstance)
                            add(mNewTaskInstance)
                            addAll(mAllSystemShortcutMenuItems)
                        }
                        clazzSystemShortcutMenuItem.putStaticField(
                            "sAllSystemShortcutMenuItems",
                            sAllSystemShortcutMenuItems,
                        )
                    }
                }
            }
    }
}
