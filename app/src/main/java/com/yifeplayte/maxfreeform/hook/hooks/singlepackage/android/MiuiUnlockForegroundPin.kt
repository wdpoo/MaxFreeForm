package com.yifeplayte.maxfreeform.hook.hooks.singlepackage.android

import android.os.Handler
import android.os.Looper
import com.yifeplayte.maxfreeform.hook.utils.XSharedPreferences.getStringSet
import io.github.lingqiqi5211.ezhooktool.core.callMethod
import io.github.lingqiqi5211.ezhooktool.core.findMethod
import io.github.lingqiqi5211.ezhooktool.core.findMethodOrNull
import io.github.lingqiqi5211.ezhooktool.core.getFieldOrNull
import io.github.lingqiqi5211.ezhooktool.core.loadClass
import io.github.lingqiqi5211.ezhooktool.core.putField
import io.github.lingqiqi5211.ezhooktool.xposed.common.HookParam
import io.github.lingqiqi5211.ezhooktool.xposed.dsl.createHook
import java.util.WeakHashMap

/**
 * @author 焕晨HChen
 */
class MiuiUnlockForegroundPin {
    companion object {
        private val lastSurfaceVisibility = WeakHashMap<Any, Boolean>()
        @Volatile
        private var sets: MutableSet<String> =
            getStringSet("unlock_foreground_pin_whitelist", mutableSetOf())
    }

    fun initForMIUI() {
        val clazzMiuiFreeFormActivityStack =
            loadClass("com.android.server.wm.MiuiFreeFormActivityStack")
        val clazzMiuiFreeFormGestureController =
            loadClass("com.android.server.wm.MiuiFreeFormGestureController")

        clazzMiuiFreeFormGestureController.findMethod {
            name("moveTaskToBack")
            params(clazzMiuiFreeFormActivityStack)
        }.createHook {
            before { param ->
                refreshSets()
                if (sets.any { it == getPackageName(param) }) {
                    param.result = null
                }
            }
        }

        loadClass("com.android.server.wm.Task").declaredConstructors.forEach { constructor ->
            constructor.createHook {
                after { param ->
                    param.thisObject?.let { lastSurfaceVisibility[it] = false }
                }
            }
        }

        loadClass("com.android.server.wm.Task").findMethod { name("prepareSurfaces") }
            .createHook {
                after { param ->
                    val pkg = param.thisObject.callMethod("getPackageName") as? String
                    if (pkg != null && sets.none { it == pkg }) return@after
                    val transaction = param.thisObject!!.callMethod("getSyncTransaction")
                    val mSurfaceControl =
                        param.thisObject!!.getFieldOrNull("mSurfaceControl") ?: return@after
                    val taskId = param.thisObject!!.callMethod("getRootTaskId") as Int
                    val mWmService =
                        param.thisObject!!.getFieldOrNull("mWmService") ?: return@after
                    val mAtmService =
                        mWmService.getFieldOrNull("mAtmService") ?: return@after
                    val mMiuiFreeFormManagerService =
                        mAtmService.getFieldOrNull("mMiuiFreeFormManagerService")
                            ?: return@after
                    val mffas = mMiuiFreeFormManagerService
                        .callMethod("getMiuiFreeFormActivityStack", taskId)
                        ?: return@after
                    val isAnimating =
                        param.thisObject!!.callMethod("isAnimating", 7) as? Boolean ?: false
                    val inPinMode = mffas.callMethod("inPinMode") as? Boolean ?: false
                    val last = lastSurfaceVisibility[param.thisObject!!] ?: false
                    if (inPinMode && !isAnimating) {
                        transaction!!.callMethod("setVisibility", mSurfaceControl, false)
                        lastSurfaceVisibility[param.thisObject!!] = false
                    } else if (!inPinMode && !last) {
                        transaction!!.callMethod("setVisibility", mSurfaceControl, true)
                        lastSurfaceVisibility[param.thisObject!!] = true
                    }
                }
            }

        clazzMiuiFreeFormGestureController.findMethod {
            name("moveTaskToFront")
            params(clazzMiuiFreeFormActivityStack)
        }.createHook {
            before { param ->
                refreshSets()
                if (sets.any { it == getPackageName(param) }) {
                    param.result = null
                }
            }
        }

        loadClass("com.android.server.wm.MiuiFreeformPinManagerService")
            .findMethodOrNull {
                name("lambda\$unPinFloatingWindow\$0\$com-android-server-wm-MiuiFreeformPinManagerService")
            }?.createHook {
                before { param -> scheduleTopWindowDrawn(param) }
            }

        clazzMiuiFreeFormGestureController.findMethodOrNull {
            name("lambda\$startFullscreenFromFreeform\$2\$com-android-server-wm-MiuiFreeFormGestureController")
        }?.createHook {
            before { param -> scheduleTopWindowDrawn(param) }
        }
    }

    private fun refreshSets() {
        sets = getStringSet("unlock_foreground_pin_whitelist", mutableSetOf())
    }

    private fun getPackageName(param: HookParam): String? {
        val mffas = param.args.getOrNull(0) ?: return null
        return mffas.callMethod("getStackPackageName") as? String
    }

    private fun scheduleTopWindowDrawn(param: HookParam) {
        refreshSets()
        if (sets.none { it == getPackageName(param) }) return
        val mffas = param.args.getOrNull(0) ?: return
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                mffas!!.putField("topWindowHasDrawn", true)
                val mLock = param.thisObject!!.getFieldOrNull("mLock")
                    ?: param.thisObject!!.getFieldOrNull("mMiuiFreeformPinManagerService")
                        ?.getFieldOrNull("mLock")
                if (mLock != null) {
                    synchronized(mLock) {
                        (mLock as Object).notifyAll()
                    }
                }
            } catch (_: Throwable) {
                // ignore, same as the legacy handler
            }
        }, 150)
    }
}
