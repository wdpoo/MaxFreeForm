package com.yifeplayte.maxfreeform.hook.hooks.multipackage

import android.content.Context
import android.os.UserHandle
import android.provider.Settings
import android.util.ArrayMap
import com.yifeplayte.maxfreeform.hook.hooks.BaseMultiHook
import com.yifeplayte.maxfreeform.hook.utils.hostPackageName
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS
import io.github.lingqiqi5211.ezhooktool.core.callMethod
import io.github.lingqiqi5211.ezhooktool.core.callStaticMethod
import io.github.lingqiqi5211.ezhooktool.core.findMethod
import io.github.lingqiqi5211.ezhooktool.core.getFieldOrNullAs
import io.github.lingqiqi5211.ezhooktool.core.getStaticFieldOrNullAs
import io.github.lingqiqi5211.ezhooktool.core.loadClass
import io.github.lingqiqi5211.ezhooktool.core.loadClassFirst
import io.github.lingqiqi5211.ezhooktool.xposed.dsl.createHook

@Suppress("unused")
object RemoveConversationBubbleSettingsRestriction : BaseMultiHook() {
    override val key = "remove_conversation_bubble_settings_restriction"
    override val hooks = mapOf(
        "com.android.systemui" to { systemUi() },
        "com.miui.securitycenter" to { securityCenter() },
    )
    override val isEnabled
        get() = when (hostPackageName) {
            "com.android.systemui" -> IS_HYPER_OS
            "com.miui.securitycenter" -> !IS_HYPER_OS
            else -> false
        } and super.isEnabled

    private fun systemUi() {
        if (!IS_HYPER_OS) return
        val clazzBubbleApp = loadClass("miui.app.MiuiBubbleApp")
        val constructorBubbleApp =
            clazzBubbleApp.getConstructor(String::class.java, Int::class.java)
        val clazzMiuiMultiWindowUtils = loadClass("android.util.MiuiMultiWindowUtils")
        val clazzMiuiBubbleSettings =
            loadClassFirst(
                "com.android.wm.shell.multitasking.miuifreeform.miuibubbles.settings.MiuiBubbleSettings",
                "com.android.wm.shell.miuifreeform.miuibubbles.settings.MiuiBubbleSettings",
            )
        loadClassFirst(
            "com.android.wm.shell.multitasking.miuifreeform.miuibubbles.MiuiBubbleController",
            "com.android.wm.shell.miuifreeform.miuibubbles.MiuiBubbleController",
        ).declaredConstructors.first()
            .createHook {
                after { param ->
                    val arrayMap = ArrayMap<String, Any>()
                    val mContext = param.thisObject.getFieldOrNullAs<Context>("mContext")
                    val mCurrentUserId =
                        UserHandle::class.java.callStaticMethod("myUserId")
                    val freeformSuggestionList = clazzMiuiMultiWindowUtils.callStaticMethod(
                        "getFreeformSuggestionList", mContext
                    ) as List<*>
                    freeformSuggestionList.associateWith { pkg ->
                        constructorBubbleApp.newInstance(pkg, mCurrentUserId).apply {
                            callMethod("setChecked", true)
                        }
                    }.forEach { (pkg, bubbleApp) ->
                        arrayMap[pkg as String] = bubbleApp
                    }
                    val mBubbleAppMaps = clazzMiuiBubbleSettings.getStaticFieldOrNullAs<ArrayMap<String, Any>>(
                        "mBubbleAppMaps"
                    )
                    mBubbleAppMaps?.putAll(arrayMap)
                    clazzMiuiBubbleSettings.callStaticMethod("updateBubbleAppStates", mContext)
                    val stringMiuiBubbleAppSettings = buildString {
                        mBubbleAppMaps?.values?.forEach {
                            if (isNotEmpty()) append(',')
                            append(it.callMethod("getSpString"))
                        }
                    }
                    Settings.Secure.putString(
                        mContext?.contentResolver,
                        "miui_bubble_app_settings",
                        stringMiuiBubbleAppSettings
                    )
                }
            }
    }

    private fun securityCenter() {
        if (IS_HYPER_OS) return
        val clazzBubbleApp = loadClass("com.miui.bubbles.settings.BubbleApp")
        val constructorBubbleApp =
            clazzBubbleApp.getConstructor(String::class.java, Int::class.java)
        val clazzMiuiMultiWindowUtils = loadClass("android.util.MiuiMultiWindowUtils")
        loadClass("com.miui.bubbles.settings.BubblesSettings")
            .findMethod { name("getDefaultBubbles") }
            .createHook {
                before { param ->
                    val arrayMap = ArrayMap<String, Any>()
                    val mContext = param.thisObject.getFieldOrNullAs<Context>("mContext")
                    val mCurrentUserId = param.thisObject.getFieldOrNullAs<Int>("mCurrentUserId")
                    val freeformSuggestionList = clazzMiuiMultiWindowUtils.callStaticMethod(
                        "getFreeformSuggestionList", mContext
                    ) as List<*>
                    freeformSuggestionList.associateWith { pkg ->
                        constructorBubbleApp.newInstance(pkg, mCurrentUserId).apply {
                            callMethod("setChecked", true)
                        }
                    }.forEach { (pkg, bubbleApp) ->
                        arrayMap[pkg as String] = bubbleApp
                    }
                    param.result = arrayMap
                }
            }
    }
}
