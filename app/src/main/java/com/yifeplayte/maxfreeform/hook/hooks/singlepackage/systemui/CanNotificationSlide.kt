package com.yifeplayte.maxfreeform.hook.hooks.singlepackage.systemui

import android.app.PendingIntent
import android.text.TextUtils
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS
import io.github.lingqiqi5211.ezhooktool.core.callMethod
import io.github.lingqiqi5211.ezhooktool.core.findMethodOrNull
import io.github.lingqiqi5211.ezhooktool.core.getFieldOrNull
import io.github.lingqiqi5211.ezhooktool.core.getFieldOrNullAs
import io.github.lingqiqi5211.ezhooktool.core.loadClassOrNull
import io.github.lingqiqi5211.ezhooktool.core.putField
import io.github.lingqiqi5211.ezhooktool.xposed.dsl.createHook

@Suppress("unused")
object CanNotificationSlide : BaseHook() {
    override val key = "can_notification_slide"
    override fun hook() {
        loadClassOrNull("com.android.systemui.statusbar.notification.NotificationSettingsManager")
            ?.findMethodOrNull { name("canSlide") }
            ?.createHook { returnConstant(true) }
        if (!IS_HYPER_OS) return
        loadClassOrNull("com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow")
            ?.findMethodOrNull { name("updateMiniWindowBar") }
            ?.createHook {
                before { param ->
                    val miniWindowTargetPkg =
                        param.thisObject.callMethod("getMiniWindowTargetPkg") as? String
                            ?: return@before
                    val mAppMiniWindowManager =
                        param.thisObject.callMethod("getMAppMiniWindowManager")
                            ?: return@before
                    val notificationSettingsManager =
                        mAppMiniWindowManager.getFieldOrNull("notificationSettingsManager")
                            ?: return@before
                    val mAllowNotificationSlide = notificationSettingsManager
                        .getFieldOrNullAs<MutableList<String>>("mAllowNotificationSlide")
                        ?: return@before
                    if (miniWindowTargetPkg !in mAllowNotificationSlide) {
                        mAllowNotificationSlide.add(miniWindowTargetPkg)
                    }
                }
            }
        loadClassOrNull("com.android.systemui.statusbar.notification.row.ExpandableNotificationRowInjector")
            ?.findMethodOrNull { name("updateMiniWindowBar") }
            ?.createHook {
                before { param ->
                    val miniWindowTargetPkg = run {
                        val pendingIntent =
                            param.thisObject.callMethod("getPendingIntent") as? PendingIntent
                        val view = param.thisObject.getFieldOrNull("view") ?: return@run null
                        val entry = view.callMethod("getEntry") ?: return@run null
                        val notification = entry.getFieldOrNull("mSbn") ?: return@run null
                        val mPkgName = notification.getFieldOrNullAs<String>("mPkgName")
                        val opPkg = notification.callMethod("getOpPkg") as? String
                        if (!TextUtils.equals(mPkgName, opPkg)) {
                            mPkgName
                        } else {
                            pendingIntent?.creatorPackage
                        }
                    } ?: return@before
                    val appMiniWindowManager =
                        param.thisObject.getFieldOrNull("appMiniWindowManager") ?: return@before
                    val notificationSettingsManager =
                        appMiniWindowManager.getFieldOrNull("notificationSettingsManager")
                            ?: return@before
                    val mAllowNotificationSlide = notificationSettingsManager
                        .getFieldOrNullAs<List<String>>("mAllowNotificationSlide")
                        ?: return@before
                    if (miniWindowTargetPkg in mAllowNotificationSlide) return@before
                    notificationSettingsManager.putField(
                        "mAllowNotificationSlide",
                        mAllowNotificationSlide + miniWindowTargetPkg,
                    )
                }
            }
    }
}
