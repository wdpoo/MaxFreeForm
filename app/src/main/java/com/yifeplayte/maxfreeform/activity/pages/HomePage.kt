package com.yifeplayte.maxfreeform.activity.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.yifeplayte.maxfreeform.R
import com.yifeplayte.maxfreeform.activity.dialogs.RestartScopeDialog
import com.yifeplayte.maxfreeform.activity.sections.bottomSpacer
import com.yifeplayte.maxfreeform.activity.sections.bubbles
import com.yifeplayte.maxfreeform.activity.sections.home
import com.yifeplayte.maxfreeform.activity.sections.others
import com.yifeplayte.maxfreeform.activity.sections.reboot
import com.yifeplayte.maxfreeform.activity.sections.system
import com.yifeplayte.maxfreeform.activity.sections.systemUI
import com.yifeplayte.maxfreeform.activity.ui.AdaptiveTopAppBar
import com.yifeplayte.maxfreeform.activity.ui.BlurredBar
import com.yifeplayte.maxfreeform.activity.ui.StatusCard
import com.yifeplayte.maxfreeform.utils.Preferences
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.shader.isRenderEffectSupported
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun HomePage() {
    val scrollBehavior = MiuixScrollBehavior()
    val backdrop = rememberBlurBackdrop()
    val blurActive = backdrop != null
    val barColor = if (blurActive) Color.Transparent else MiuixTheme.colorScheme.surface
    val lazyListState = rememberLazyListState()

    Scaffold(
        topBar = {
            BlurredBar(backdrop, blurActive) {
                AdaptiveTopAppBar(
                    title = stringResource(R.string.app_name),
                    color = barColor,
                    scrollBehavior = scrollBehavior,
                )
            }
        },
    ) { innerPadding ->
        // 分区重启：对话框由页面持有一份，分区项只负责发起请求
        var restartTargets by remember { mutableStateOf<List<String>>(emptyList()) }
        val showRestartDialog = remember { mutableStateOf(false) }
        val requestRestart: (List<String>) -> Unit = { targets ->
            restartTargets = targets
            showRestartDialog.value = true
        }

        Box(modifier = if (blurActive) Modifier.layerBackdrop(backdrop) else Modifier) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .scrollEndHaptic()
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .fillMaxHeight(),
                contentPadding = innerPadding,
            ) {
                item { StatusCard(isActive = Preferences.isModuleActivated) }
                system(requestRestart)
                systemUI(requestRestart)
                bubbles(requestRestart)
                home(requestRestart)
                others()
                reboot()
                bottomSpacer()
            }
        }
        RestartScopeDialog(restartTargets, showRestartDialog)
    }
}

@Composable
fun rememberBlurBackdrop(): LayerBackdrop? {
    if (!isRenderEffectSupported()) return null
    val surfaceColor = MiuixTheme.colorScheme.surface
    return rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
}
